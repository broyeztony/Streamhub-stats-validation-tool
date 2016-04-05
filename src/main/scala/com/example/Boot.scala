package com.example

import akka.actor.{ActorSystem, Props}
import akka.event.LoggingAdapter
import akka.io.IO
import spray.can.Http
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.duration._

object Boot extends App {

  // we need an ActorSystem to host our application in
  implicit val system = ActorSystem("on-spray-can")

  // create and start our service actor
  val service = system.actorOf(Props[MyServiceActor], "demo-service")

  implicit val timeout = Timeout(5.seconds)

  val logger: LoggingAdapter = system.log

  Thread.setDefaultUncaughtExceptionHandler( InsightUncaughtExceptionHandler )

  IO(Http) ? Http.Bind(service, interface = "localhost", port = 8080)
}
