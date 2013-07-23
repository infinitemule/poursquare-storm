package com.infinitemule.hopperhack.foursq.storm

import com.infinitemule.hopperhack.foursq._
import FoursquareJsonProtocol._

import com.infinitemule.hopperhack.storm.StormBolt
import com.infinitemule.hopperhack.finagle.{FinagleHttpClientService,
                                            FinagleHttpRequestBuilder,
                                            FinagleHttpsClientService}


import com.twitter.finagle.Service
import com.twitter.util.{Await, Future}

import org.jboss.netty.buffer.ChannelBuffers
import org.jboss.netty.handler.codec.http.{HttpRequest, HttpResponse}

import backtype.storm.tuple.Tuple

import java.net.URL
import spray.json._

/**
 * Since I was using Foursquare's "public" API (i.e. their web site) I wanted to pretend
 * I was a browser.
 */
object FoursquareStorm {  
  val ua  = "Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:22.0) Gecko/20100101 Firefox/22.0"
}


/**
 * Bolt that takes a Foursqaure tiny URL (i.e. 4sq.com/djeIjX3k) and
 * parses the HTTP response to find the redirect link, which is the "real" link
 * to the Foursquare checkin. 
 * 
 * Notes:
 *   - I noticed that if I made the Finagle Service a member of the class,
 *     I would get a NotSerializable exception.  My guess is that Storm
 *     needs these all members to be serializable so that it can distribute
 *     them on the cluster.  I solved this by just creating the service on
 *     every new tuple and hoping that it wasn't too expensive to do so. 
 *     
 */
class FoursquareCheckinUrlResolverBolt(val host: String) extends StormBolt {

  override val fields = List("checkinUrl") 
	      
  var client:  Service[HttpRequest, HttpResponse] = _
    
  
  /**
   * 
   */
  override def execute(tuple: Tuple) = {
    
    client  = FinagleHttpClientService(host)
    
    val url = new URL(tuple.getStringByField("link")).getPath()
    
    val request = new FinagleHttpRequestBuilder(host, url).userAgent(FoursquareStorm.ua).get()    
    val response: Future[HttpResponse] = client(request)

    
    response.onSuccess { resp: HttpResponse =>
    
      // Our URL is in the Location header.
      
      val location = resp.getHeader("Location")
      
      // I noiced tht some tiny URLs point to things other than checkins,
      // so we need to capture only the ones that contain "checkin"
      
      if(location.contains("checkin")) {
        emitAndAck(tuple, location)  
      }
            
    }

    response.onFailure { cause: Throwable =>
      println("FAILED with " + cause)
    } 

    Await.ready(response) 
  }
  
  override def cleanup() = {    
    client.close()
  }
  
}


/**
 * Bolt that takes the resolved URL and fetches the checkin web page
 * and emits the embedded JSON document found in the script tag of the page.
 */
class FoursquareCheckinFetcherBolt extends StormBolt {

  override val fields = List("checkinUrl", "checkinHtml") 
	
  val host = "foursquare.com"
            
  var client:  Service[HttpRequest, HttpResponse] = _
    
  
  /**
   * 
   */
  override def execute(tuple: Tuple) = {
    
    client  = FinagleHttpsClientService(host)
    
    val url = "https://" + host + tuple.getStringByField("checkinUrl").split(host)(1)
        
    val request = new FinagleHttpRequestBuilder(host, url).userAgent(FoursquareStorm.ua).get()    
    val response: Future[HttpResponse] = client(request)

    
    response.onSuccess { resp: HttpResponse =>
      emitAndAck(tuple, url, processHtml(resp.getContent().toString("UTF-8")))      
    }

    response.onFailure { cause: Throwable =>
      println("FAILED with " + cause)
    } 
    
    Await.ready(response)  

  }
  
  override def cleanup() = {    
    client.close()    
  }
  
  
  /**
   * This should be extracted into a separate class so that
   * it can be tested properly.  
   */
  private def processHtml(html: String) = {
    
    // First we chop out our JSON from the page by splitting
    // where the JSON starts and ends.  Then we need to 
    // put quotes around checkin and fullVenue so that is becomes
    // a valid JSON object.
    
    "{" + html    
        .split("'\\#checkinDetailPage'\\)\\.get\\(0\\), ")(1)
        .split(",canZoomMap")(0)
        .replaceFirst("checkin", "\"checkin\"")
        .replaceFirst("fullVenue", "\"fullVenue\"") + "}"
  }
  
}


/**
 * Bolt that takes a checkin JSON object and emits Foursquare API objects. 
 */
class FoursquareCheckinParserBolt extends StormBolt {

  override val fields = List("checkin") 
  
  override def execute(tuple: Tuple) = {
    
    val url = tuple.getStringByField("checkinUrl")
    
    val resp = tuple.getStringByField("checkinHtml")
                  .asJson.convertTo[FoursquareCheckinResponse]
    
    emitAndAck(tuple, FoursquareCheckinRecord(url, resp.checkin, resp.fullVenue))      
        
  } 
  
}


/**
 * Bolt that takes a Foursquare checkin and submits it to the client app using  
 * a web service. 
 */
class FoursquareCheckinRepositoryBolt(host: String, port: Int, path: String) extends StormBolt {
  
  override val fields = List("saveResult")

  var client:  Service[HttpRequest, HttpResponse] = _

  
  override def execute(tuple: Tuple) = {
    
    val record = tuple.getValue(0).asInstanceOf[FoursquareCheckinRecord]
    
    client  = FinagleHttpClientService(host, port)
            
    val request = new FinagleHttpRequestBuilder(host, path)
                      .userAgent(FoursquareStorm.ua)
                      .content(record.toJson.toString)
                      .contentType("application/json")
                      .post()
        
    val response: Future[HttpResponse] = client(request)

    response.onSuccess { resp: HttpResponse =>
      
      // The result here should really be parsed to see if there 
      // were an error response.
      
      val result = resp.getContent().toString("UTF-8")
      
      emitAndAck(tuple, result)        
            
    }

    // Don't do this:  Handle the exception properly.
    response.onFailure { cause: Throwable =>
      println("FAILED with " + cause)
    } 

    Await.ready(response)    
  }
}
