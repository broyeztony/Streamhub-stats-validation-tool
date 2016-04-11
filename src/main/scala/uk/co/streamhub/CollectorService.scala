package uk.co.streamhub

import akka.actor.Actor
import net.sf.uadetector.{ReadableUserAgent, UserAgentStringParser}
import net.sf.uadetector.service.UADetectorServiceFactory
import spray.httpx.SprayJsonSupport._
import spray.routing._
import CollectorJsonProtocol._

class CollectorServiceActor extends Actor with CollectorService {

  def actorRefFactory = context

  def receive = runRoute(myRoute)
}

trait CollectorService extends HttpService {

  var requestIsValid:Boolean         = true
  val tickStartTimes: List[Double] = List(0.08333333333,
                                          0.1666666667,
                                          0.25,
                                          0.3333333333,
                                          0.4166666667,
                                          0.5,
                                          0.5833333333,
                                          0.6666666667,
                                          0.75,
                                          0.8333333333,
                                          0.9166666667)

  val eventNamesAndParameters       = List("player_error", "player_start", "player_play_completed", "completion",
    "click", "player_play_seek", "click_pause", "click_pause_off", "click_player_fullscreen", "completionRate")

  val completionRates: List[Int]    = List( 1, 25, 50, 75, 95 )

  val myRoute =
    path("api" / "player") {

      parameters(
        'publicId.as[String],
        'partnerId.as[String],
        'analyticsId.as[String],
        'playerId.as[String],
        'isLive.as[Boolean],
        'startTime.as[Double],
        'agent.as[String],
        'parentPublicId.as[String].?,
        'sessionId.as[String],
        'randomSessionKey.as[String]
      ) {

        (publicId, partnerId, analyticsId, playerId, isLive, startTime,
         agent, parentPublicId, sessionId, randomSessionKey) =>

          complete {

            // First assume the request is valid
            requestIsValid = true

            val checkedUrlParams = checkUrlParams(agent, publicId, partnerId,
              analyticsId, playerId, isLive, parentPublicId, startTime, sessionId, randomSessionKey)

            new ApiPlayerResponse(requestIsValid, checkedUrlParams._1, checkedUrlParams._2, checkedUrlParams._3,
              checkedUrlParams._4, checkedUrlParams._5, checkedUrlParams._6, checkedUrlParams._7,
              checkedUrlParams._8, checkedUrlParams._9)
          }
      }
    } ~
      path("api" / "playerevent") {

        parameters(
          'publicId.as[String],
          'partnerId.as[String],
          'analyticsId.as[String],
          'playerId.as[String],
          'isLive.as[Boolean],
          'startTime.as[Double],
          'agent.as[String],
          'parentPublicId.as[String].?,
          'event.as[String],
          'completionRate.as[Int].?,
          'sessionId.as[String],
          'randomSessionKey.as[String]
        ) {

          (publicId, partnerId, analyticsId, playerId, isLive, startTime, agent,
           parentPublicId, eventName, completionRate, sessionId, randomSessionKey) =>

            complete {

              // First assume the request is valid
              requestIsValid = true

              val checkedUrlParams          = checkUrlParams(agent, publicId, partnerId, analyticsId, playerId, isLive, parentPublicId, startTime, sessionId, randomSessionKey)
              val eventNameValidation       = checkEventName(eventName)
              val completionRateValidation  = checkCompletionRate(eventName, completionRate)


              new ApiPlayerEventResponse(requestIsValid, checkedUrlParams._1, checkedUrlParams._2, checkedUrlParams._3,
                checkedUrlParams._4, checkedUrlParams._5, checkedUrlParams._6, checkedUrlParams._7,
                checkedUrlParams._8, checkedUrlParams._9, eventNameValidation, completionRateValidation)

            }
        }
      }

  def checkUrlParams(agent: String,
                     publicId: String,
                     partnerId: String,
                     analyticsId: String,
                     playerId: String,
                     isLive: Boolean,
                     parentPublicId: Option[String],
                     startTime: Double,
                     sessionId: String,
                     randomSessionKey: String): (String, String, String, String, String, String, UA, String, String) = {

    val publicIdValidation            = checkIfEmpty("publicId", publicId)
    val partnerIdValidation           = checkIfEmpty("partnerId", partnerId)
    val analyticsIdValidation         = checkIfEmpty("analyticsId", analyticsId)
    val playerIdValidation            = checkIfEmpty("playerId", playerId)
    val isLiveValidation              = checkIfLive(isLive, parentPublicId)
    val startTimeValidation           = checkStartTime(startTime)
    val userAgentValidation           = checkUserAGent(agent)
    val sessionIdValidation           = checkIfEmpty("sessionId", sessionId)
    val randomSessionKeyValidation    = checkIfEmpty("randomSessionKey", randomSessionKey)

    (publicIdValidation, partnerIdValidation, analyticsIdValidation, playerIdValidation,
      isLiveValidation, startTimeValidation, userAgentValidation, sessionIdValidation, randomSessionKeyValidation)
  }

  def checkIfEmpty(fieldName: String, fieldValue: String): String = {

    val checkResponse = if (fieldValue.isEmpty){
      requestIsValid = false
      "KO => can not be empty."
    }
    else
      fieldValue + " => OK"

    checkResponse
  }

  def checkUserAGent(agent: String): UA = {

    val parser: UserAgentStringParser = UADetectorServiceFactory.getResourceModuleParser();
    val rua: ReadableUserAgent = parser.parse(agent);

    val userAgent: UA = UA(rua.getName, rua.getOperatingSystem.getName,
      rua.getType.getName, rua.getDeviceCategory.getCategory.getName)

    userAgent
  }

  def checkIfLive(isLive: Boolean, parentPublicId: Option[String]): String = {

    if (isLive)
    {
      val hasChannelId = parentPublicId match {
        case Some(parentPublicId) => true
        case None => false
      }

      if (hasChannelId && parentPublicId.get.length > 0)
        "=> OK"
      else {
        requestIsValid = false
        "KO => The parameter parentPublicId is either missing or has an empty value."
      }
    }
    else
     isLive +  " => OK"
  }

  def checkStartTime(startTime: Double): String = {

    val checkResponse: String = if (tickStartTimes.contains(startTime) ||
      startTime % 1 == 0)
      startTime + " => OK"
    else {

      requestIsValid = false

      startTime + " KO => is not an acceptable value. Ticks must be sent every 5 seconds during the first " +
        "minute of video, then every minute after the first minute. The startTime value has " +
        "to be provided minutely."
    }


    checkResponse
  }

  def checkEventName(eventName: String): String = {

    val eventNameValidation = if( eventNamesAndParameters.contains( eventName ) )
      eventName + " => OK"
    else {

      requestIsValid = false
      "KO => " + eventName + " is not a valid event name"
    }


    eventNameValidation
  }

  def checkCompletionRate(eventName: String, completionRate: Option[Int]): String = {

    var completionRateValidation = "N/A"

    if( eventName == "completion" ){

      completionRate match {
        case Some(completionRate) => {

          completionRateValidation = if( completionRates.contains( completionRate ) )
            completionRate + " => OK"
          else {

            requestIsValid = false
            "KO => Completion rate is either missing or invalid. Found " + completionRate +
              ", expected: one of " + completionRates toString
          }

        }
        case None => {

          requestIsValid = false
          completionRateValidation = "KO => a completionRate value needs to be provided " +
            "along with a completion event."
        }
      }
    }

    completionRateValidation
  }



}





