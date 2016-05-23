package com.bucketfiller

import akka.actor.{Actor, ActorRef, Props}
import scala.collection._
import scala.concurrent.duration._
import scala.concurrent.Await
import akka.pattern.ask
import akka.util.Timeout

class Simulation(player: ActorRef) extends Actor {
    import Simulation._
    
    val graphManager = context.actorOf(Props(new GraphManager(self)))
    val moveMaker = context.actorOf(Props(new MoveMaker(graphManager)))
    val levelManager = context.actorOf(Props(new LevelManager(moveMaker)))
    var isStarted = false
    var endValue = -1
    var initState = State(Bucket(-1, -1), Bucket(-1, -1))
    
    def receive = {
        case Start(capacityA, capacityB, goal) => start(capacityA, capacityB, goal)
        case Frontier(source, states) => process(source, states)
    }
    
    def start(capacityA: Int, capacityB: Int, goal: Int) = {
        if (!isStarted) {
            initState = State(Bucket(0, capacityA), Bucket(0, capacityB))
            isStarted = true
            endValue = goal
            levelManager ! LevelManager.Init(Set(initState))
        }
    }
    
    def process(source: State, states: Set[State]): Unit = {
        
        if (!isStarted) {
            return ()
        }
        
        states.find({
            case State(Bucket(valueA, _), Bucket(valueB, _)) => 
                valueA == endValue || valueB == endValue
        }) match {
            case Some(endState) =>
                implicit val timeout = Timeout(30.seconds)
                val future = graphManager ? GraphManager.FindPath(initState, endState)
                val GraphManager.Path(path) = Await.result(future, 30.seconds).asInstanceOf[GraphManager.Path]
                player ! Result(path)
            case _ => levelManager ! LevelManager.Next(source, states)
        }
        
        return ()
    }
}

object Simulation {
    case class Bucket(value: Int, capacity: Int)
    case class State(a: Bucket, b: Bucket)
    case class Frontier(source: State, states: Set[State])
    case class Start(capacityA: Int, capacityB: Int, goal: Int)
    case class Result(path: List[State])
}