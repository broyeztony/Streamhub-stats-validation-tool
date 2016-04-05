package com.example


import spray.json.DefaultJsonProtocol


case class UA(name: String, operatingSystem: String, device: String, category:String )

case class ApiPlayerResponse( publicIdValidation: String,
                              partnerIdValidation: String,
                              analyticsIdValidation: String,
                              playerIdValidation: String,
                              isLiveValidation: String,
                              userAgentValidation: UA )


object MyJsonProtocol extends DefaultJsonProtocol {
  implicit val uaFormat                   = jsonFormat4( UA )
  implicit val apiPlayerResponseFormat    = jsonFormat6( ApiPlayerResponse )
}


