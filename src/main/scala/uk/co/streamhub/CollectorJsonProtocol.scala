package uk.co.streamhub

import spray.json.DefaultJsonProtocol


case class UA(name: String, operatingSystem: String, device: String, category:String )

case class ApiPlayerResponse( publicId: String,
                              partnerId: String,
                              analyticsId: String,
                              playerId: String,
                              isLive: String,
                              startTime: String,
                              userAgent: UA )

case class ApiPlayerEventResponse(publicId: String,
                                  partnerId: String,
                                  analyticsId: String,
                                  playerId: String,
                                  isLive: String,
                                  startTime: String,
                                  userAgent: UA,
                                  event: String,
                                  completionRate: String)


object CollectorJsonProtocol extends DefaultJsonProtocol {

  implicit val uaFormat                     = jsonFormat4( UA )
  implicit val apiPlayerResponseFormat      = jsonFormat7( ApiPlayerResponse )
  implicit val apiPlayerEventResponseFormat = jsonFormat9( ApiPlayerEventResponse )

}


