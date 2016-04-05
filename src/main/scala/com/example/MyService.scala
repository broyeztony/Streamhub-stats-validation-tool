package com.example
import akka.actor.Actor
import net.sf.uadetector.{ReadableUserAgent, UserAgentStringParser}
import net.sf.uadetector.service.UADetectorServiceFactory
import spray.httpx.SprayJsonSupport._
import spray.routing._
import com.example.MyJsonProtocol._


// we don't implement our route structure directly in the service actor because
// we want to be able to test it independently, without having to spin up an actor
class MyServiceActor extends Actor with MyService {

  def actorRefFactory = context
  def receive         = runRoute( myRoute )
}

trait MyService extends HttpService {

  val myRoute =
    path( "api" / "player" ){

      parameters( 'publicId.as[ String ],
                  'partnerId.as[ String ],
                  'analyticsId.as[ String ],
                  'playerId.as[ String ],
                  'isLive.as[ Boolean ],
                  'agent.as[ String ],
                  'channelId.as[ String ].?
                  ) {

        ( publicId, partnerId, analyticsId, playerId, isLive, agent, channelId ) =>

          complete {

            val userAgentValidation   = checkUserAGent( agent )
            val publicIdValidation    = checkIfEmpty( "publicId", publicId )
            val partnerIdValidation   = checkIfEmpty( "partnerId", partnerId )
            val analyticsIdValidation = checkIfEmpty( "analyticsId", analyticsId )
            val playerIdValidation    = checkIfEmpty( "playerId", playerId )
            val isLiveValidation      = checkIfLive( isLive, channelId );

            new ApiPlayerResponse(publicIdValidation,
                                  partnerIdValidation,
                                  analyticsIdValidation,
                                  playerIdValidation,
                                  isLiveValidation,
                                  userAgentValidation)
          }
        }
      }

  def checkIfEmpty( fieldName: String, fieldValue: String ): String = {

    val checkResponse = if(fieldValue.isEmpty)
      "The " + fieldName + " field can not be empty."
    else
      fieldValue + " is an acceptable value."

    checkResponse
  }

  def checkUserAGent( agent:String ): UA = {

    val parser: UserAgentStringParser   = UADetectorServiceFactory.getResourceModuleParser();
    val rua : ReadableUserAgent         = parser.parse( agent );

    val userAgent: UA = UA( rua.getName, rua.getOperatingSystem.getName,
      rua.getType.getName, rua.getDeviceCategory.getCategory.getName )

    userAgent
  }

  def checkIfLive( isLive:Boolean, channelId:Option[String] ): String  = {

    if(isLive){
      val hasChannelId = channelId match {
        case Some(channelId) => true
        case None => false
      }

      if(hasChannelId && channelId.get.length > 0)
        "The fields isLive and channelId have consistent values."
      else
        "The parameter channelId is either missing or has an empty value."
    }
    else
      "The field isLive has an acceptable value."

  }



}





