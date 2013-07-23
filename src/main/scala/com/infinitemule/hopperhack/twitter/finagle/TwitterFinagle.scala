package com.infinitemule.hopperhack.twitter.finagle

import com.infinitemule.hopperhack.finagle._

/*
 *  Classes that abstract out the Twitter API (or at least just the 
 *  search functionality)  
 */


object FinagleTwitterApi {
  
  val host    = "api.twitter.com"
  val version = "1.1"
    
  val search = s"/${version}/search/tweets.json"
      
}

class FinagleTwitterApi(val auth: String) {
  
  def search() = {
    TwitterFinagleSearchRequest(auth)
  }
  
}


object TwitterFinagleClientService {
  
  def apply() = {    
    FinagleHttpsClientService(FinagleTwitterApi.host)
  }
  
}


object TwitterFinagleSearchRequest {
  
  def apply(auth: String) = {
    new TwitterFinagleSearchRequestBuilder(auth)                      
  }
  
}


/**
 * Class that simplifies the building of a Twitter search request.  This
 * may not be the best example of how to make a builder in Scala so I would
 * encourage to find other examples if you are really interested.
 */
class TwitterFinagleSearchRequestBuilder(val auth: String) {
  
  var _query      = ""  
  var _geocode    = ""
  var _resultType = ""
  var _count      = ""
    
  var _sinceId = ""
  var _maxId   = ""
  
  var _includeEntities = ""
    
  def build() = {

    val params  = List(_query, _geocode, _resultType, _count, _sinceId, _maxId, _includeEntities)    
    val options = params filter { _.nonEmpty } mkString("&") 
    
    
    FinagleHttpRequest(FinagleTwitterApi.host, "%s?%s".format(FinagleTwitterApi.search, options))
                      .auth("Bearer " + auth)
                      .get()

  }  
    
  def query(p: String) = {
    _query = "q=" + p
    this
  }
  
  def geocode(lat: Double, lon: Double) = {
	_geocode = "geocode=%s,%s".format(lat, lon)
	this
  }
  
  def geocode(lat: Double, lon: Double, within: Int) = {
	_geocode = "geocode=%s,%s,%smi".format(lat, lon, within)
	this
  }
  
  def resultType(rt: String) = {
    _resultType = "result_type=" + rt 
    this
  }

  def count(c: Int) = {
    _count = "count=" + c 
    this
  }
  
  def sinceId(p: String) = {
    _sinceId = "since_id=" + p
    this
  }

  def maxId(p: String) = {
    _maxId= "max_id=" + p
    this
  }
  
  def includeEntities(p: Boolean) = {
    _includeEntities = "include_entities=" + p.toString.toLowerCase
    this
  }
  
}
