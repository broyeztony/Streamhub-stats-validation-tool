package uk.co.streamhub

import spray.json.DefaultJsonProtocol


case class UA(name: String, operatingSystem: String, device: String, category:String )

case class ApiPlayerResponse( publicId: String,
                              partnerId: String,
                              analyticsId: String,
                              playerId: String,
                              isLive: String,
                              startTime: String,
                              userAgent: UA,
                              sessionId: String,
                              randomSessionKey: String)

case class ApiPlayerEventResponse(publicId: String,
                                  partnerId: String,
                                  analyticsId: String,
                                  playerId: String,
                                  isLive: String,
                                  startTime: String,
                                  userAgent: UA,
                                  sessionId: String,
                                  randomSesssionKey: String,
                                  event: String,
                                  completionRate: String)


object CollectorJsonProtocol extends DefaultJsonProtocol {

  implicit val uaFormat                     = jsonFormat4( UA )
  implicit val apiPlayerResponseFormat      = jsonFormat9( ApiPlayerResponse )
  implicit val apiPlayerEventResponseFormat = jsonFormat11( ApiPlayerEventResponse )

}


