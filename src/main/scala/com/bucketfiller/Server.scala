package com.bucketfiller

import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Flow
import akka.actor._
import scala.io.StdIn
import akka.http.scaladsl.model.ws.{Message, TextMessage, UpgradeToWebSocket}

object Server extends App {

  implicit val system = ActorSystem("SYSTEM")
  implicit val flowMaterializer = ActorMaterializer()

  val interface = "localhost"
  val port = 8080
  
  val echoService: Flow[Message, Message, _] = Flow[Message].map {
    case TextMessage.Strict(txt) => TextMessage("ECHO: " + txt)
    case _ => TextMessage("Message type unsupported")
}
  
  val route = get {
    pathEndOrSingleSlash {
      complete("Hello world")
    }
  } ~
  path("ws-echo") {
    get {
      handleWebSocketMessages(echoService)
    }
}
  
  val binding = Http().bindAndHandle(route, interface, port)
  println(s"Server is now online at http://$interface:$port\nPress RETURN to stop...")
  StdIn.readLine()

  import system.dispatcher

  binding.flatMap(_.unbind()).onComplete(_ => system.shutdown())
  println("Server is down...")

}