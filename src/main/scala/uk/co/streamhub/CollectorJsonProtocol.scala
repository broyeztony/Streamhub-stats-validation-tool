package uk.co.streamhub

import spray.json.DefaultJsonProtocol


case class UA(name: String, operatingSystem: String, device: String, category:String )

case class Response(status: Int)

case class ApiValidationResponse(response: Response, userAgent: UA, errors: List[String])


object CollectorJsonProtocol extends DefaultJsonProtocol {

  implicit val responseFormat               = jsonFormat1( Response )
  implicit val uaFormat                     = jsonFormat4( UA )
  implicit val apiValidationResponseFormat  = jsonFormat3( ApiValidationResponse )

}


