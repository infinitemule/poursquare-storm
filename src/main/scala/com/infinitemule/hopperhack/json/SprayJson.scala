package com.infinitemule.hopperhack.json

import spray.json._
import DefaultJsonProtocol._


/**
 * This is a simple set of implicits to which simplifies
 * writing protocols.  See the Twitter and Foursquare
 * JSON protocols for examples.
 */
object SprayJson {
    
  implicit def enrichJsValue(value: JsValue) = new {

    def apply(field: String): Option[JsValue] = {
      value.asJsObject.fields.get(field)     
    }
    
  }
  
  
  implicit def enrichOptionJsValue(value: Option[JsValue]) = new {

    def as[T : JsonReader](): T = {
      value.get.convertTo[T]     
    }
    
    def asOption[T : JsonReader](): Option[T] = {
      value match {
        case Some(JsNull) => None
        case Some(x)      => Some(x.convertTo[T])
        case None         => None
      }
    }
    
    def asList[T : JsonReader](): List[T] = {
      value.get.convertTo[JsArray].elements.map { _.convertTo[T] }        
    }
    
  }  
}