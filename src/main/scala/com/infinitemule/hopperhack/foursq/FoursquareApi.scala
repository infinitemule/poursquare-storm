package com.infinitemule.hopperhack.foursq

/*
 * Case classes that model the Foursquare API
 * 
 * See: https://developer.foursquare.com/docs/
 */


/*
 * Venue
 */

case class FoursquareVenueLocation(
  address:    Option[String],
  city:       Option[String],
  state:      Option[String],
  postalCode: Option[String],
  country:    Option[String],
  cc:         Option[String],
  
  lat: Option[Double],
  lng: Option[Double],
  
  distance: Option[Int]    
)


case class FoursquareVenueStats(
  checkinsCount: Int,
  usersCount:    Int,
  tipCount:      Int
)


case class FoursquareVenue(
  id:   String,
  name: String,
  
  description: Option[String],
  url:         Option[String],
  
  location: FoursquareVenueLocation,
  
  tags:       List[String],
  categories: List[FoursquareCategory], 
  stats:      FoursquareVenueStats    
)



/*
 * Photos and Categories  
 */

case class FoursquareIcon(
  mapPrefix: String,
  prefix:    String,        
  suffix:    String    
)


case class FoursquareCategory(
  id:         String,
  name:       String, 
  pluralName: String,
  shortName:  String,
  icon:       FoursquareIcon,
  primary:    Option[Boolean]  
)

case class FoursquarePhoto(
  prefix: String,
  suffix: String    
)


/*
 * User
 */

case class FoursquareUser(
  id:        String,  
  firstName: String,
  lastName:  String,
  gender:    String,
  
  canonicalUrl: String,
  photo:        FoursquarePhoto
)



/*
 * Checkin
 */

case class FoursquareCheckinSource(
  name: String,
  url:  String
)

case class FoursquareCheckin(
  id:           String,
  createdAt:    Long,
  user:         Option[FoursquareUser],
  source:       FoursquareCheckinSource,
  canoncialUrl: String
)


case class FoursquareCheckinResponse(   
  checkin:   FoursquareCheckin,
  fullVenue: FoursquareVenue
)



/*
 * Persistence
 */

case class FoursquareCheckinRecord(  
  url:     String,
  checkin: FoursquareCheckin,
  venue:   FoursquareVenue
)


