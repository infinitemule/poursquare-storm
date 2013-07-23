package com.infinitemule.hopperhack.twitter

import spray.json._
import DefaultJsonProtocol._

import com.infinitemule.hopperhack.json.SprayJson._

/**
 * Spray JSON protocols for the Twitter API.  All these do is take JSON 
 * and translate them to Scala objects and vice versa.
 * 
 * See: https://github.com/spray/spray-json
 * 
 * Notes: 
 *   - There should really be some exception handling here in case the
 *     JSON comes back as invalid.
 *   - Calling write should probably throw an exception instead of an
 *     empty object.  That way, if someone else is using your code
 *     and trying to convert back to JSON, they won't waste their time 
 *     trying to find out why it doesn't work.
 *      
 */
object TwitterJsonProtocol extends DefaultJsonProtocol {

  
  implicit object TwitterStatusCoordinatesJsonFormat extends RootJsonFormat[TwitterStatusCoordinates] {
    
    def write(resp: TwitterStatusCoordinates) = JsObject() 
            
    def read(value: JsValue) = {
      
      val coords = value("coordinates").asList[Double]
      
      TwitterStatusCoordinates(
        Tuple2[Double, Double](coords(0), coords(1)), 
        value("type").as[String])
                        
    }
    
  }
  
  
  implicit object TwitterStatusUrlJsonFormat extends RootJsonFormat[TwitterStatusUrl] {
    
    def write(resp: TwitterStatusUrl) = JsObject()     
    
    def read(value: JsValue) = {
      
      TwitterStatusUrl(
        value("url")         .as[String], 
        value("display_url") .as[String], 
        value("expanded_url").as[String], 
        value("indices")     .asList[Int])           
                  
    }
    
  }
  

  implicit object TwitterStatusEntitiesJsonFormat extends RootJsonFormat[TwitterStatusEntities] {
    
    def write(resp: TwitterStatusEntities) = JsArray() 
            
    def read(value: JsValue) = {
      
      TwitterStatusEntities(
        value("urls").asList[TwitterStatusUrl])
                        
    }
    
  }
  
  
  implicit object TwitterStatusJsonFormat extends RootJsonFormat[TwitterStatus] {
    
    def write(resp: TwitterStatus) = JsArray() 
            
    def read(value: JsValue) = {

      TwitterStatus(
        value("id").as[Long],
        value("id_str").as[String],
        value("text").as[String],
        value("lang").as[String],
        value("created_at").as[String],
        value("entities").as[TwitterStatusEntities],
        value("coordinates").asOption[TwitterStatusCoordinates])
                          
    }
    
  }
  
  
  implicit object TwitterStatusResponseJsonFormat extends RootJsonFormat[TwitterStatusResponse] {
    
    def write(resp: TwitterStatusResponse) = JsArray() 
      
    def read(value: JsValue) = {
      
      TwitterStatusResponse(
        value("statuses").asList[TwitterStatus]
      )
      
    } 
    
  }
  
}
