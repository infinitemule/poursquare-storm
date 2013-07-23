package com.infinitemule.hopperhack.finagle

import com.twitter.finagle.Service
import com.twitter.finagle.builder.ClientBuilder
import com.twitter.finagle.http.Http

import com.twitter.util.Duration

import java.net.InetSocketAddress

import java.util.concurrent.TimeUnit._

import org.jboss.netty.buffer.ChannelBuffers
import org.jboss.netty.handler.codec.http.{HttpRequest, 
                                           HttpResponse,
                                           DefaultHttpRequest, 
                                           HttpVersion, 
                                           HttpMethod}

/**
 * A simple wrapper around the client builder so that you can easily
 * create a simple HTTP service (i.e. FinagleHttpClientService("api.twitter.com"))  
 * This is all I needed for the hackathon, if you wanted more options, you would
 * need to expose the options that you wanted through method arguments or
 * a builder class. 
 */
object FinagleHttpClientService {

  def apply(host: String): Service[HttpRequest, HttpResponse] = {    
    this(host, 80)
  }
  
  def apply(host: String, port: Int): Service[HttpRequest, HttpResponse] = {
    
    // I'm not sure if the connection limit and timeout are the most
    // optimal, see the Finagle documentation for more information.
    
    ClientBuilder()
      .codec(Http())
      .hosts(new InetSocketAddress(host, port))      
      .hostConnectionLimit(1)
      .tcpConnectTimeout(Duration(5, SECONDS))
      .build()
  }
  
}


/**
 * Same as above just for HTTPS.  The default port is different and you 
 * need to specify the host in the tls() method.
 */
object FinagleHttpsClientService {

  def apply(host: String): Service[HttpRequest, HttpResponse] = {
    
    ClientBuilder()
      .codec(Http())
      .hosts(new InetSocketAddress(host, 443))
      .tls(host)
      .hostConnectionLimit(1)
      .tcpConnectTimeout(Duration(5, SECONDS))
      .build()
  }
}


/**
 * 
 */
object FinagleHttpRequest {
    
  def apply(host: String, path: String): FinagleHttpRequestBuilder = {
    new FinagleHttpRequestBuilder(host, path)
  }
  
}


/**
 * Builder that simplifies building Finagle HttpRequests.  See
 * the Twitter and Finagle bolts for examples. 
 */
class FinagleHttpRequestBuilder(var host: String, var path: String) {
  
  var userAgent:     String = "Finagle 6.5.1"
  var authorization: String = ""
  
  var content     = ""
  var contentType = ""
    
  def get(): DefaultHttpRequest = {
    createRequest(HttpMethod.GET)     
  }
  
  def post(): DefaultHttpRequest = {
    createRequest(HttpMethod.POST)     
  }

  
  def auth(auth: String): FinagleHttpRequestBuilder = {
    authorization = auth
    this
  }  
  
  def userAgent(ua: String): FinagleHttpRequestBuilder = {
    userAgent = ua
    this
  }
  
  def content(c: String): FinagleHttpRequestBuilder = {
    content = c
    this
  }
  
  def contentType(ct: String): FinagleHttpRequestBuilder = {
    contentType = ct
    this
  }

  
  /**
   * 
   */
  private def createRequest(method: HttpMethod): DefaultHttpRequest = {
    
    val req = new DefaultHttpRequest(HttpVersion.HTTP_1_1, method, path)

    req.setHeader("Host", host)
    req.setHeader("User-Agent", userAgent)

    if(authorization.nonEmpty) {
      req.setHeader("Authorization", authorization)
    }

    if(contentType.nonEmpty) {
      req.setHeader("Content-Type", contentType)
    }
    
    if(content.nonEmpty) {
      req.setContent(ChannelBuffers.copiedBuffer(content, "UTF-8"))
      req.setHeader("Content-Length", content.length.toString)
    }
        
    req
    
  }
  
}
