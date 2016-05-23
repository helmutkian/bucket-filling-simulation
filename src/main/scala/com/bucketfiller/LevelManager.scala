package com.bucketfiller

import akka.actor.{Actor, ActorRef}
import Simulation._
import scala.collection._

class LevelManager(moveMaker: ActorRef) extends Actor {
    import LevelManager._
    
    var currentLevel: Set[State] = Set()
    var nextLevel: Set[State] = Set()

    def receive = {
        case Init(states) => moveToLevel(states)
        case Next(prevState, nextStates) => addNext(prevState, nextStates)
    }
    
    def moveToLevel(states: Set[State]) = {
        nextLevel = Set()
        currentLevel = states
        currentLevel.foreach(state => makeMove(state))
    }
    
    def addNext(prevState: State, nextStates: Set[State]) = {
        currentLevel = currentLevel - prevState
        nextStates.foreach(nextState => nextLevel = nextLevel + nextState)
        
        if (currentLevel.isEmpty) {
            moveToLevel(nextLevel)
        }
    }
    
    def makeMove(state: State) = {
        moveMaker ! MoveMaker.Move(state)
    }
}

object LevelManager {
    case class Init(states: Set[State])
    case class Next(prevState: State, nextStates: Set[State])
}