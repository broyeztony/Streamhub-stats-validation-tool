package com.example


import spray.json.DefaultJsonProtocol


case class UA(name: String, operatingSystem: String, device: String, category:String )
case class ApiPlayerResponse( userAgent: UA,
                              publicId: String,
                              partnerIdValidation: String )


object MyJsonProtocol extends DefaultJsonProtocol {
  implicit val uaFormat                   = jsonFormat4( UA )
  implicit val apiPlayerResponseFormat    = jsonFormat3( ApiPlayerResponse )
}


