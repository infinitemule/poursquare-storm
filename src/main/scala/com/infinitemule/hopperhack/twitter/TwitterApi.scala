package com.infinitemule.hopperhack.twitter

/*
 * Set of case classes that model the Twitter API.  
 * See: https://dev.twitter.com/docs/api/1.1
 */

case class TwitterStatusPlace(
  id:          String,
  url:         String,
  placeType:   String,
  name:        String,
  fullName:    String,
  countryCode: String,
  country:     String
)

case class TwitterStatusUrl(
  url:         String,
  expandedUrl: String,
  displayUrl:  String,
  indicies:    List[Int]
)

case class TwitterStatusEntities(
  urls: List[TwitterStatusUrl]    
)

case class TwitterStatusCoordinates(
  coordinates: Tuple2[Double,Double],
  coordType: String
)

case class TwitterStatus(
  id:    Long,
  idStr: String,
  
  text: String,
  lang: String,
  
  created: String,
  
  entities: TwitterStatusEntities,
  
  coordinates: Option[TwitterStatusCoordinates]
)


case class TwitterStatusResponse(        
  statuses:List[TwitterStatus]    
)

