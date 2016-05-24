package com.bucketfiller

import akka.actor.Actor
import akka.actor.Props
import akka.actor.Status.Success
import org.mashupbots.socko.events.WebSocketFrameEvent
import org.mashupbots.socko.webserver.WebServer
import scala.util.parsing.json._
import scala.collection._

class WebSocketHandler(webServer: WebServer) extends Actor {
  var connectionId = ""
  
  def receive = {
    case event: WebSocketFrameEvent => handleEvent(event)
    case LevelManager.Next(state, childStates) => handleNext(state, childStates)
    case Simulation.Result(path) => handleResult(path)
    case Simulation.BeginLevel(level) => handleLevel(level)
  }
  
  def handleEvent(event: WebSocketFrameEvent) = {
    JSON.parseFull(event.readText) match {
      case Some(msg) => 
        val obj = msg.asInstanceOf[Map[String, Double]]
        if (obj.contains("a") && obj.contains("b") && obj.contains("goal")) {
          val Some(capacityA) = obj.get("a")
          val Some(capacityB) = obj.get("b")
          val Some(goal) = obj.get("goal")
          
          connectionId = event.webSocketId
          startSimulation(capacityA.toInt, capacityB.toInt, goal.toInt)
        }
      case _ => 
        context.stop(self)
    }
  }
    
  def startSimulation(capacityA: Int, capacityB: Int, goal: Int) = {
    val simulation = context.actorOf(Props(new Simulation(self)))
    
    simulation ! Simulation.Start(capacityA, capacityB, goal)
  }
  
  def handleResult(path: List[Simulation.State]) = {
    sendMsg("result", toString(path))
    context.stop(self)
  }
  
  def handleNext(state: Simulation.State, childStates: Set[Simulation.State])  = {
    sendMsg("next", toString(state :: childStates.toList))
  }
  
  def handleLevel(level: Set[Simulation.State]) = {
    sendMsg("level", toString(level.toList))
  }
  
  def sendMsg(topic: String, msg: String) = {
    val payload = "{\"topic\": \"" + topic + "\", \"data\": " + msg + "}"
        webServer.webSocketConnections.writeText(payload, connectionId)
  }
  
  def toString(path: Seq[Simulation.State]): String = {
    val states =  path.map({ case Simulation.State(Simulation.Bucket(a, _), Simulation.Bucket(b, _)) => s"[$a, $b]"  })
            .reduce((acc, str) => acc + "," + str)
            
   return s"[$states]"
  }
}