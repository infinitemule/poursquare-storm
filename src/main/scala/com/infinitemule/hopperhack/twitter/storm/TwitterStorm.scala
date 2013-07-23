package com.infinitemule.hopperhack.twitter.storm

import com.infinitemule.hopperhack.storm.StormSpout
import com.infinitemule.hopperhack.storm.StormBolt

import com.infinitemule.hopperhack.finagle.FinagleHttpsClientService

import com.infinitemule.hopperhack.twitter.finagle.{FinagleTwitterApi,
                                                    TwitterFinagleClientService}

import com.infinitemule.hopperhack.twitter.TwitterJsonProtocol._

import com.infinitemule.hopperhack.twitter.{TwitterStatusResponse,
                                            TwitterStatus}

import backtype.storm.spout.SpoutOutputCollector
import backtype.storm.task.{TopologyContext, OutputCollector}
import backtype.storm.tuple.{Fields, Tuple, Values}
import backtype.storm.topology.OutputFieldsDeclarer

import com.twitter.finagle.Service
import com.twitter.util.{Future, Await}

import org.jboss.netty.handler.codec.http.{HttpResponse,
                                           HttpRequest,
                                           DefaultHttpRequest}

import spray.json._

import scala.collection.mutable.SynchronizedQueue

import org.apache.log4j.Logger
import java.util.{Map => JavaMap}


/**
 * Spout that gathers tweets about Foursquare checkins and
 * emits a one field tuple with the JSON result.  It 
 * requests tweets every five seconds to avoid rate limiting.
 * 
 * Notes:
 *   - I really didn't know to properly feed a spout.  I saw
 *     an example using a Kestral queue that fed into a collection
 *     so I used as synchronized queue.  It seemed to work fine at
 *     the hackathon, but my guess is that there may be a better 
 *     way to do this.
 * 
 */
class TwitterApiSpout(val bearerToken: String) extends StormSpout {

  override val fields = List("json")
    
  var log: Logger =  _
  
  // This is used to limit the next search request to only the
  // tweets that haven't been requested already.
  
  val sinceIdMatcher = """"max_id"\:(\d+),""".r
  var sinceId: String = "0"
      
  val tweets = new SynchronizedQueue[String]()
    
  var twitter: FinagleTwitterApi = _  
  var client:  Service[HttpRequest, HttpResponse] = _


  override def open(conf: JavaMap[_,_], context: TopologyContext, collector: SpoutOutputCollector) = {
    
    super.open(conf, context, collector)

    log = Logger.getLogger(this.getClass())
    
    twitter = new FinagleTwitterApi(bearerToken)    
    client  = TwitterFinagleClientService()
    
  }
    

  override def close() = {    
    client.close()
  }
  
  override def nextTuple() = {
  
    if(tweets.isEmpty) { 
      requestMoreTweets()
      Thread.sleep(5000)
    }
    else {
      collector.emit(new Values(tweets.dequeue))  
    }    
      
    
  }
  
  
  private def requestMoreTweets() {

    // Originally, I was just getting checkins for the Boston area for 
    // testing, so that's why I was using the geocoding options.
    
    val request = twitter.search()
                  .query("4sq.com")
                  //.geocode(42.346223, -71.100669, within=250)
                  .resultType("recent")
                  .includeEntities(true)
                  .sinceId(sinceId)
                  //.count(10)
                  .count(100)
                  .build()
        
    val response: Future[HttpResponse] = client(request)

    response.onSuccess { resp =>
      
      val json = resp.getContent().toString("UTF-8")
      tweets += json
      
      // Not sure if regex was a good idea her, but I didn't
      // want to write a whole protocol just to get one field 
      // (that max_id is found in the search meta section of the
      // JSON response.
      
      sinceId = (
        for { 
          sinceIdMatcher(id) <- sinceIdMatcher findFirstIn json 
        } yield id).getOrElse("0")
      
    }
    
    
    // Don't do this.  Properly handle the exception.
    //
    // Although this does bring up an interesting point about
    // how to handle a web service failure in a Storm spout or bolt.  
    // I think you can configure Finagle to retry a few times before giving
    // up, but still, what happens when a tuple ultimately can't be processed?
    response.onFailure { cause: Throwable =>
      println("Failed With " + cause)
    }
    
    Await.ready(response)    
    
  }

}


/**
 * Bolt that parses the Twitter API Search response, and emits each
 * tweet as a tuple.
 */
class TwitterApiParserBolt extends StormBolt {

  override val fields = List("tweet") 
	
  override def execute(tuple: Tuple) = {
    
    val apiResp = tuple.getString(0).asJson.convertTo[TwitterStatusResponse]
    
    apiResp.statuses foreach { tweet =>
      collector.emit(tuple, new Values(tweet))
      collector.ack(tuple)
    }
        
  }  
  
}

/**
 * Bolt that takes a tweet and finds the specified URL, in our
 * case this will be the 4sq.com tiny URL which we will need to 
 * resolve to get the Foursquare checkin and venue information.
 */
class TwitterApiLinkBolt(val tinyUrl: String) extends StormBolt {

  override val fields = List("statusId", "link") 
	
  override def execute(tuple: Tuple) = {
    
    val tweet = tuple.getValue(0).asInstanceOf[TwitterStatus]
    
    tweet.entities.urls foreach { url =>
      if(url.displayUrl.contains(tinyUrl)) {
        collector.emit(tuple, new Values(tweet.idStr, url.displayUrl))
        collector.ack(tuple)
      }
    }
        
  }  
  
}