package com.bucketfiller

import akka.actor.{Actor, ActorRef}
import Simulation._


class MoveMaker(graphManager: ActorRef) extends Actor {
    import MoveMaker._
    
    def receive = {
        case Move(state) => getLegalMoves(state)
    }
    
    def getLegalMoves(state: State) {
        val State(a, b) = state
        val Bucket(valueA, capacityA) = a
        val Bucket(valueB, capacityB) = b
        var states = Set[State]() 
        
        // Fill A
        if (valueA < capacityA) {
            states = states + State(Bucket(capacityA, capacityA), b) 
        }
        
        // Fill B
        if (valueB < capacityB) {
            states =  states + State(a, Bucket(capacityB, capacityB))
        }
        
        // Dump A
        if (valueA > 0) {
            states = states + State(Bucket(0, capacityA), b)
        }
        
        // Dump B
        if (valueB > 0) {
            states = states + State(a, Bucket(0, capacityB))
        }
        
        // Transfer A
        if (valueA > 0 && valueB < capacityB) {
            val (newA, newB) = transfer(a, b)

            states = states + State(newA, newB) 
        }
        
        // Transfer B
        if (valueB > 0 && valueA < capacityA) {
            val (newB, newA) = transfer(b, a)
            
            states = states + State(newA, newB)
        }
        
        graphManager ! GraphManager.Visit(state, states)
    }
    
    def transfer(source: Bucket, dest: Bucket): (Bucket, Bucket) = {
        val Bucket(srcValue, srcCapacity) = source
        val Bucket(destValue, destCapacity) = dest
        val remCapacity = destCapacity - destValue
        val transferValue = scala.math.min(srcValue, remCapacity)
        
        return (Bucket(srcValue - transferValue, srcCapacity), Bucket(destValue + transferValue, destCapacity))
    }
}

object MoveMaker {
    case class Move(state: State)
}