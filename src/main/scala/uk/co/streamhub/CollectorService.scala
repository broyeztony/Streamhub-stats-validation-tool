package uk.co.streamhub

import akka.actor.Actor
import net.sf.uadetector.{ReadableUserAgent, UserAgentStringParser}
import net.sf.uadetector.service.UADetectorServiceFactory
import spray.httpx.SprayJsonSupport._
import spray.routing._
import CollectorJsonProtocol._
import scala.util.control.Breaks._


class CollectorServiceActor extends Actor with CollectorService {

  def actorRefFactory = context

  def receive = runRoute(myRoute)
}

trait CollectorService extends HttpService {

  val tickStartTimes: List[(Double, Double)]  = List( (0.083, 0.084), (0.16, 0.17), (0.25, 0.25), (0.33, 0.34), (0.41, 0.42),
    (0.5, 0.5), (0.58, 0.59), (0.66, 0.67), (0.75, 0.75), (0.83, 0.84), (0.91, 0.92) )

  val eventNamesAndParameters                 = List( "player_error",
                                                      "player_start",
                                                      "player_play_completed",
                                                      "completion",
                                                      "click",
                                                      "player_play_seek",
                                                      "click_pause",
                                                      "click_pause_off",
                                                      "click_player_fullscreen",
                                                      "completionRate" )

  val completionRates: List[Int]               = List( 1, 25, 50, 75, 95 )

  val myRoute =
    path("api" / "player" / ) {

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

            val ua: UA                = checkUserAGent( agent )

            val errors: List[String]  = checkUrlParams(publicId, partnerId, analyticsId, playerId, isLive,
              parentPublicId, startTime, sessionId, randomSessionKey)

            new ApiValidationResponse(new Response( if(errors.length > 0) 0 else 1 ), ua, errors)
          }
      }
    } ~
      path("api" / "playerevent" / ) {

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

              val ua: UA                = checkUserAGent( agent )

              val urlParamsErrors: List[String]  = checkUrlParams(publicId, partnerId, analyticsId, playerId, isLive,
                parentPublicId, startTime, sessionId, randomSessionKey)

              val eventNameError        = checkEventName(eventName)
              val completionRateError   = checkCompletionRate(eventName, completionRate)

              val errors = urlParamsErrors ::: eventNameError ::: completionRateError

              new ApiValidationResponse(new Response( if(errors.length > 0) 0 else 1 ), ua, errors)

            }
        }
      }

  def checkUrlParams(publicId: String,
                     partnerId: String,
                     analyticsId: String,
                     playerId: String,
                     isLive: Boolean,
                     parentPublicId: Option[String],
                     startTime: Double,
                     sessionId: String,
                     randomSessionKey: String): List[String] = {

    val publicIdError                 = checkIfEmpty("publicId", publicId)
    val partnerIdError                = checkIfEmpty("partnerId", partnerId)
    val analyticsIdError              = checkIfEmpty("analyticsId", analyticsId)
    val playerIdError                 = checkIfEmpty("playerId", playerId)
    val isLiveError                   = checkIfLive(isLive, parentPublicId)
    val startTimeError                = checkStartTime(startTime)
    val sessionIdError                = checkIfEmpty("sessionId", sessionId)
    val randomSessionKeyError         = checkIfEmpty("randomSessionKey", randomSessionKey)

    publicIdError ::: partnerIdError ::: analyticsIdError ::: playerIdError ::: isLiveError ::: startTimeError :::
      sessionIdError ::: randomSessionKeyError
  }

  def checkIfEmpty(fieldName: String, fieldValue: String): List[String] = {

    var error: List[String] = List()
    if (fieldValue.isEmpty){
      error = error ::: List( fieldName + " can not be empty." )
    }

    error
  }

  def checkUserAGent(agent: String): UA = {

    val parser: UserAgentStringParser = UADetectorServiceFactory.getResourceModuleParser();
    val rua: ReadableUserAgent = parser.parse(agent);

    val userAgent: UA = UA(rua.getName, rua.getOperatingSystem.getName,
      rua.getType.getName, rua.getDeviceCategory.getCategory.getName)

    userAgent
  }

  def checkIfLive(isLive: Boolean, parentPublicId: Option[String]): List[String] = {

    if (isLive)
    {
      val hasChannelId = parentPublicId match {
        case Some(parentPublicId) => true
        case None => false
      }

      if (hasChannelId && parentPublicId.get.length > 0) List()
      else
        List( "The parameter parentPublicId is either missing or has an empty value." )
    }
    else
      List()
  }

  def checkStartTime(startTime: Double): List[String] = {

    var stIn0to1minuteRange = false

    breakable {
      for( (low, high) <- tickStartTimes ) {
        if ((startTime >= low && startTime <= high)) {
          stIn0to1minuteRange = true
          break
        }
      }
    }

    val errors: List[String] = if (stIn0to1minuteRange || startTime % 1 == 0) List()
    else {
      List( startTime + " is not a valid value. Ticks must be sent every 5 seconds during the first " +
        "minute of video, then every minute after the first minute. The startTime value has " +
        "to be provided minutely." )
    }

    errors
  }

  def checkEventName(eventName: String): List[String] = {

    val error: List[String] = if( eventNamesAndParameters.contains( eventName ) )
      List()
    else
      List( eventName + " is not a valid event name" )

    error
  }

  def checkCompletionRate(eventName: String, completionRate: Option[Int]): List[String] = {

    var completionRateValidation:List[String]  = List()

    if( eventName == "completion" ){

      completionRate match {
        case Some(completionRate) => {

          completionRateValidation = if( completionRates.contains( completionRate ) )
            List()
          else {
            List("Completion rate is either missing or invalid. Found " + completionRate +
              ", expected: one of " + completionRates )
          }
        }
        case None => {

          completionRateValidation = List( "a completionRate value needs to be provided " +
            "along with a completion event." )
        }
      }
    }

    completionRateValidation
  }
}





