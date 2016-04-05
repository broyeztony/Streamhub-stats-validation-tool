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

// this trait defines our service behavior independently from the service actor
trait MyService extends HttpService {

  val myRoute =
    path( "api" / "player" ){

      parameters( 'publicId.as[ String ],
                  'partnerId.as[ String ],
                  'agent.as[ String ] ) {
        ( publicId, partnerId, agent ) =>

          complete {

            val userAgentValidation = checkUserAGent( agent )
            val publicIdValidation  = checkIfEmpty( "publicId", publicId )
            val partnerIdValidation = checkIfEmpty( "partnerId", partnerId )

            new ApiPlayerResponse( userAgentValidation, publicIdValidation, partnerIdValidation )
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

}





