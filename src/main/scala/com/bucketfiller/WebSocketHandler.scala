package com.bucketfiller

import akka.actor.Actor
import akka.actor.Props
import akka.actor.Status.Success
import org.mashupbots.socko.events.WebSocketFrameEvent
import org.mashupbots.socko.webserver.WebServer

class WebSocketHandler(webServer: WebServer) extends Actor {
  var connectionId = ""
  
  def receive = {
    case event: WebSocketFrameEvent => handleEvent(event)
    case Simulation.Result(path) => 
      webServer.webSocketConnections.writeText(toString(path), connectionId)
      context.stop(self)
  }
  
  def handleEvent(event: WebSocketFrameEvent) = {
    val message = event.readText
    
    connectionId = event.webSocketId
    if (message == "start") {
      startSimulation(connectionId)
    } else {
      context.stop(self)
    }
  }
  
  def startSimulation(id: String) = {
    val simulation = context.actorOf(Props(new Simulation(self)))
    simulation ! Simulation.Start(3, 5, 4)
  }
  
  def toString(path: List[Simulation.State]): String = {
    val states =  path.map({ case Simulation.State(Simulation.Bucket(a, _), Simulation.Bucket(b, _)) => s"[$a, $b]"  })
            .reduce((acc, str) => acc + "," + str)
            
   return s"[$states]"
  }
}