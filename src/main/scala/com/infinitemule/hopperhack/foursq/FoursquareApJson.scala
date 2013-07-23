package com.infinitemule.hopperhack.foursq

import com.infinitemule.hopperhack.json.SprayJson._
import spray.json._
import DefaultJsonProtocol._


/**
 * Spray JSON protocols for the Foursquare API.  All these do is take JSON 
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
 
 */
object FoursquareJsonProtocol extends DefaultJsonProtocol {
  

  implicit object FoursquareVenueLocationJsonFormat extends RootJsonFormat[FoursquareVenueLocation] {
    
    def write(obj: FoursquareVenueLocation) = JsObject()
            
    def read(value: JsValue) = {
                  
      FoursquareVenueLocation(
        value("address")   .asOption[String],
        value("city")      .asOption[String],
        value("state")     .asOption[String],
        value("postalCode").asOption[String],
        value("country")   .asOption[String],
        value("cc")        .asOption[String],
        value("lat")       .asOption[Double],
        value("lng")       .asOption[Double],
        value("distance")  .asOption[Int]
      )
                        
    }
   
  }
  
  
  implicit object FoursquareIconJsonFormat extends RootJsonFormat[FoursquareIcon] {
    
    def write(obj: FoursquareIcon) = JsObject() 
            
    def read(value: JsValue) = {
      
      FoursquareIcon(
        value("mapPrefix").as[String], 
        value("prefix")   .as[String],
        value("suffix")   .as[String])
    }
        
  }

  
  implicit object FoursquarePhotoJsonFormat extends RootJsonFormat[FoursquarePhoto] {
    
    def write(obj: FoursquarePhoto) = JsObject() 
            
    def read(value: JsValue) = {
      
      FoursquarePhoto(
        value("prefix")   .as[String],
        value("suffix")   .as[String])
    }
        
  }  
  
  
  implicit object FoursquareUserJsonFormat extends RootJsonFormat[FoursquareUser] {
    
    def write(obj: FoursquareUser) = JsObject() 
            
    def read(value: JsValue) = {
      
      FoursquareUser(
        value("id")          .as[String], 
        value("firstName")   .as[String],
        value("lastName")    .asOption[String].getOrElse(""),
        value("gender")      .as[String],
        value("canonicalUrl").as[String],
        value("photo")       .as[FoursquarePhoto])
    }
        
  }
  
  
  implicit object FoursquareCategoryJsonFormat extends RootJsonFormat[FoursquareCategory] {
    
    def write(obj: FoursquareCategory) = JsArray() 
            
    def read(value: JsValue) = {
      
      FoursquareCategory(
        value("id")        .as[String], 
        value("name")      .as[String],
        value("pluralName").as[String],
        value("shortName") .as[String],
        value("icon")      .as[FoursquareIcon],
        value("primary")   .asOption[Boolean])
    }
        
  }
  
  
  implicit object FoursquareVenueStatsJsonFormat extends RootJsonFormat[FoursquareVenueStats] {
    
    def write(obj: FoursquareVenueStats) = JsObject() 
            
    def read(value: JsValue) = {
      
      FoursquareVenueStats(
        value("checkinsCount").as[Int], 
        value("usersCount")   .as[Int],
        value("tipCount")     .as[Int])      
    }
    
  }
  
  
  implicit object FoursquareVenueJsonFormat extends RootJsonFormat[FoursquareVenue] {
    
    def write(obj: FoursquareVenue) = JsArray() 
            
    def read(value: JsValue) = {
      
      FoursquareVenue(
        value("id")         .as[String], 
        value("name")       .as[String],
        value("description").asOption[String],
        value("url")        .asOption[String],
        value("location")   .as[FoursquareVenueLocation], 
        value("tags")       .asList[String],
        value("categories") .asList[FoursquareCategory],
        value("stats")      .as[FoursquareVenueStats])
      
    }
    
  }

  
  implicit object FoursquareCheckinSourceJsonFormat extends RootJsonFormat[FoursquareCheckinSource] {
    
    def write(obj: FoursquareCheckinSource) = JsObject() 
            
    def read(value: JsValue) = {
                  
      FoursquareCheckinSource(
        value("name").as[String],
        value("url") .as[String]
      )
      
    }
    
  }


  implicit object FoursquareCheckinJsonFormat extends RootJsonFormat[FoursquareCheckin] {
    
    def write(obj: FoursquareCheckin) = JsObject() 
            
    def read(value: JsValue) = {
                  
      FoursquareCheckin(
        value("id")          .as[String],
        value("createdAt")   .as[Long],
        value("user")        .asOption[FoursquareUser],
        value("source")      .as[FoursquareCheckinSource],
        value("canonicalUrl").as[String]
      )
      
    }
    
  }
  
  
  implicit object FoursquareCheckinResponseProtocol extends RootJsonFormat[FoursquareCheckinResponse] {
    
    def write(obj: FoursquareCheckinResponse) = JsObject() 
            
    def read(value: JsValue) = {
                        
      FoursquareCheckinResponse(
        value("checkin")  .as[FoursquareCheckin],
        value("fullVenue").as[FoursquareVenue]
      )
 
    }
    
  }
  
  implicit object FoursquareCheckinRecordProtocol extends RootJsonFormat[FoursquareCheckinRecord] {
    
    def write(obj: FoursquareCheckinRecord) = {
                  
      JsObject(          
        
        "_id"        -> JsString(obj.checkin.id),
        "url"        -> JsString(obj.url),
        "created_at" -> JsNumber(obj.checkin.createdAt),
        
        "venue" -> JsObject(
          "id"   -> JsString(obj.venue.id),
          "name" -> JsString(obj.venue.name),
          "location" -> JsObject(
            "address" -> JsStringOpt(obj.venue.location.address),
            "city"    -> JsStringOpt(obj.venue.location.city),
            "state"   -> JsStringOpt(obj.venue.location.state),
            "country" -> JsStringOpt(obj.venue.location.country),
            "cc"      -> JsStringOpt(obj.venue.location.cc),
            "lat"     -> JsDoubleOpt(obj.venue.location.lat),
            "lng"     -> JsDoubleOpt(obj.venue.location.lng)
          ),
          "tags" -> JsArray(obj.venue.tags map { JsString(_) }),
          "categories" -> JsArray(obj.venue.categories map { CategoryJsonObj(_) } ),
          "stats" -> JsObject(
            "checkins_count" -> JsNumber(obj.venue.stats.checkinsCount),
            "tip_count"      -> JsNumber(obj.venue.stats.tipCount),
            "users count"    -> JsNumber(obj.venue.stats.usersCount)
          )
        ),
        
        "source" -> JsObject(
          "name" -> JsString(obj.checkin.source.name),
          "url"  -> JsString(obj.checkin.source.url)
        ),
        
        "user" -> UserJsObject(obj.checkin.user)
      )      
    }

    private def CategoryJsonObj(obj: FoursquareCategory): JsValue = {
      
      JsObject(
        "id"   -> JsString(obj.id) ,    
        "name" -> JsString(obj.name),
        "plaural_name" -> JsString(obj.pluralName),
        "short_name"   -> JsString(obj.shortName),
        "icon" -> JsObject(
          "map_prefix" -> JsString(obj.icon.mapPrefix),
          "prefix"     -> JsString(obj.icon.prefix),
          "suffix"     -> JsString(obj.icon.suffix)
        ),
        "primary" -> JsBooleanOpt(obj.primary)
      )      
      
    }
    
    private def UserJsObject(obj: Option[FoursquareUser]): JsValue = obj match {
      
      case Some(user) => {
        JsObject(
          "id"            -> JsString(user.id),
          "first_name"    -> JsString(user.firstName),
          "last_name"     -> JsString(user.lastName),
          "gender"        -> JsString(user.gender),
          "canonical_url" -> JsString(user.canonicalUrl),
          "photo"         -> JsObject(
            "prefix" -> JsString(user.photo.prefix),
            "suffix" -> JsString(user.photo.suffix)
          )       
        )
      }
      case None => JsNull
      
    }
    
    def read(value: JsValue) = null
    
  }
    
  // TODO Refactor and move to SprayJson
  
  // - By refactoring, I meant that I could write these as implicits so
  //   they can be expresses much better, for example user.id.asJsString
  //   Instead of JString(user.id)
  // - Also, Opt should probably be something like "Nullable" instead since.
  //   the idea was that if a member was an Option and was None, you would want
  //   to write 'null' out to the JSON document
  
  def JsStringOpt(os: Option[String]): JsValue = {
    
    os match {
      case Some(x) => JsString(x)
      case None => JsNull
    }
    
  }
  
  def JsDoubleOpt(os: Option[Double]): JsValue = {
    os match {
      case Some(x) => JsNumber(x)
      case None => JsNull
    }
  }
    
  def JsBooleanOpt(os: Option[Boolean]): JsValue = {
    os match {
      case Some(x) => JsBoolean(x)
      case None => JsNull
    }
  }

}