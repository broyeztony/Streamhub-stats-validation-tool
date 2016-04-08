package uk.co.streamhub

import akka.actor.Actor
import net.sf.uadetector.{ReadableUserAgent, UserAgentStringParser}
import net.sf.uadetector.service.UADetectorServiceFactory
import spray.httpx.SprayJsonSupport._
import spray.routing._
import CollectorJsonProtocol._


// we don't implement our route structure directly in the service actor because
// we want to be able to test it independently, without having to spin up an actor
class CollectorServiceActor extends Actor with CollectorService {

  def actorRefFactory = context

  def receive = runRoute(myRoute)
}

trait CollectorService extends HttpService {

  val tickStartTimes          = List(5 / 60, 10 / 60, 15 / 60, 20 / 60, 25 / 60, 30 / 60, 35 / 60, 40 / 60, 45 / 60, 50 / 60, 55 / 60);

  val eventNamesAndParameters = List("player_error", "player_start", "player_play_completed", "completion",
    "click", "player_play_seek", "click_pause", "click_pause_off", "click_player_fullscreen", "completionRate")

  val completionRates         = List( 0.01, 0.25, 0.50, 0.75, 0.95 )

  val myRoute =
    path("api" / "player") {

      parameters('publicId.as[String],
        'partnerId.as[String],
        'analyticsId.as[String],
        'playerId.as[String],
        'isLive.as[Boolean],
        'startTime.as[Float],
        'agent.as[String],
        'parentPublicId.as[String].?
      ) {

        (publicId, partnerId, analyticsId, playerId, isLive, startTime, agent, parentPublicId) =>

          complete {

            val checkedUrlParams = checkUrlParams(agent, publicId, partnerId,
              analyticsId, playerId, isLive, parentPublicId, startTime)

            new ApiPlayerResponse(checkedUrlParams._1, checkedUrlParams._2, checkedUrlParams._3,
              checkedUrlParams._4, checkedUrlParams._5, checkedUrlParams._6,
              checkedUrlParams._7)
          }
      }
    } ~
      path("api" / "playerevent") {

        parameters('publicId.as[String],
          'partnerId.as[String],
          'analyticsId.as[String],
          'playerId.as[String],
          'isLive.as[Boolean],
          'startTime.as[Float],
          'agent.as[String],
          'parentPublicId.as[String].?,
          'event.as[String],
          'completionRate.as[Float].?
        ) {

          (publicId, partnerId, analyticsId,
           playerId, isLive, startTime, agent, parentPublicId, eventName, completionRate) =>

            complete {

              val checkedUrlParams = checkUrlParams(agent, publicId, partnerId,
                analyticsId, playerId, isLive, parentPublicId, startTime)

              val eventNameValidation = if( eventNamesAndParameters.contains( eventName ) ) eventName + " is a valid event name"
              else eventName + " is not a valid event name"

              val completionRateValidation = if( completionRates.contains( completionRate ) )
                completionRate + " is a valid completion rate value"
              else
                "Completion rate is either missing or invalid. Ignore this warning if you did not send a completion event."


              new ApiPlayerEventResponse(checkedUrlParams._1, checkedUrlParams._2, checkedUrlParams._3,
                checkedUrlParams._4, checkedUrlParams._5, checkedUrlParams._6, checkedUrlParams._7,
                eventNameValidation, completionRateValidation )

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
                     startTime: Float): (String, String, String, String, String, String, UA) = {

    val publicIdValidation = checkIfEmpty("publicId", publicId)
    val partnerIdValidation = checkIfEmpty("partnerId", partnerId)
    val analyticsIdValidation = checkIfEmpty("analyticsId", analyticsId)
    val playerIdValidation = checkIfEmpty("playerId", playerId)
    val isLiveValidation = checkIfLive(isLive, parentPublicId)
    val startTimeValidation = checkStartTime(startTime)
    val userAgentValidation = checkUserAGent(agent)

    (publicIdValidation, partnerIdValidation, analyticsIdValidation, playerIdValidation,
      isLiveValidation, startTimeValidation, userAgentValidation)
  }

  def checkIfEmpty(fieldName: String, fieldValue: String): String = {

    val checkResponse = if (fieldValue.isEmpty)
      "The " + fieldName + " field can not be empty."
    else
      fieldValue + " is a valid value for the field."

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

    if (isLive) {
      val hasChannelId = parentPublicId match {
        case Some(parentPublicId) => true
        case None => false
      }

      if (hasChannelId && parentPublicId.get.length > 0)
        "The fields isLive and parentPublicId have consistent values."
      else
        "The parameter parentPublicId is either missing or has an empty value."
    }
    else
      "The field isLive has an acceptable value."

  }

  def checkStartTime(startTime: Float): String = {

    val checkResponse: String = if (tickStartTimes.contains(startTime) ||
      startTime % 1 != 0)
      startTime + " is an acceptable value"
    else
      startTime + " is not acceptable. Ticks must be sent every 5 seconds during the first " +
        "minute of video, then every minute after the first minute. The startTime value has " +
        "to be provided minutely."

    checkResponse
  }



}




