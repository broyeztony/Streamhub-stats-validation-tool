package uk.co.streamhub

import spray.json.DefaultJsonProtocol


case class UA(name: String, operatingSystem: String, device: String, category:String )

case class Response(status: Int)

case class ApiPlayerResponse( response: Response,
                              publicId: String,
                              partnerId: String,
                              analyticsId: String,
                              playerId: String,
                              isLive: String,
                              startTime: String,
                              userAgent: UA,
                              sessionId: String,
                              randomSessionKey: String)

case class ApiPlayerEventResponse(response: Response,
                                  publicId: String,
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

  implicit val responseFormat               = jsonFormat1( Response )
  implicit val uaFormat                     = jsonFormat4( UA )
  implicit val apiPlayerResponseFormat      = jsonFormat10( ApiPlayerResponse )
  implicit val apiPlayerEventResponseFormat = jsonFormat12( ApiPlayerEventResponse )

}


