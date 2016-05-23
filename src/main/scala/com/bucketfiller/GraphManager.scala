package com.bucketfiller

import akka.actor.{Actor, ActorRef}
import scala.collection._
import Simulation._

class GraphManager(bucketFiller: ActorRef) extends Actor {
    import GraphManager._
    
    var visited = mutable.Map[State, State]()
    
    def receive = {
        case Visit(source, targets) => handleVisit(source, targets)
        case FindPath(source, target) => findPath(source, target)
    }
    
    def handleVisit(source: State, targets: Set[State]) = {
        val frontier = targets.filter(target => !visited.contains(target))
        
        markVisited(source, frontier)
        bucketFiller ! Frontier(source, frontier)
    }
    
    def markVisited(source: State, targets: Set[State]) = {
        targets.foreach(target => visited += (target -> source))
    }
    
    def findPath(source: State, target: State): Unit = {
        var path = List[State]()
        var currState = target
        
        while (currState != source) {
            visited.get(currState) match {
                case Some(state) => 
                    path = currState :: path
                    currState = state
                case _ => 
                    sender ! BadPath(source, target, currState)
                    return ()
            }
        }
        
        sender ! Path(currState :: path)
        return ()
    }
    
}

object GraphManager {
    case class Visit(source: State, targets: Set[State])
    case class FindPath(source: State, target: State)
    case class Path(states: List[State])
    case class BadPath(source: State, target: State, brokenLink: State)
}