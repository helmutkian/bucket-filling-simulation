package com.bucketfiller

import akka.actor.{ActorSystem, Actor, Props}
import akka.testkit.{ TestActors, TestKit, ImplicitSender, TestProbe }
import org.scalatest.WordSpecLike
import org.scalatest.Matchers
import org.scalatest.BeforeAndAfterAll
import scala.concurrent.duration._
import Simulation._
 
class BucketFillerSpec(_system: ActorSystem) extends TestKit(_system) with ImplicitSender
  with WordSpecLike with Matchers with BeforeAndAfterAll {
 
  def this() = this(ActorSystem("TestSpec"))
 
  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }
 
  "A MoveMaker actor" must {
    "Generate the correct moves" in {
        val probe = TestProbe()
        val moveMaker = system.actorOf(Props(new MoveMaker(probe.ref)))
        
        val s0 = State(Bucket(0, 3), Bucket(0, 5))
        val r0 = Set(
            State(Bucket(3, 3), Bucket(0, 5)), 
            State(Bucket(0,3), Bucket(5, 5))
        )
        
        moveMaker ! MoveMaker.Move(s0)
        probe.expectMsgPF() {
            case GraphManager.Visit(s, r) => s == s0 && r == r0
        }
        
        val s1 = State(Bucket(0, 3), Bucket(5, 5))
        val r1 = Set(
            State(Bucket(0, 3), Bucket(0, 5)),
            State(Bucket(3, 3), Bucket(2, 5)),
            State(Bucket(3, 3), Bucket(5, 5))
        )
        
        moveMaker ! MoveMaker.Move(s1)
        probe.expectMsgPF() {
            case GraphManager.Visit(s, r) => s == s1 && r == r1
        }
    }
  }
  
  "A GraphManager actor" must {
      "Keep track of visted states" in {
        val probe = TestProbe()
        val graphManager = system.actorOf(Props(new GraphManager(probe.ref)))
          
        val s0 = State(Bucket(0, 3), Bucket(0, 5))
        val r0 = Set(
            State(Bucket(3, 3), Bucket(0, 5)), 
            State(Bucket(0,3), Bucket(5, 5))
        )
          
        graphManager ! GraphManager.Visit(s0, r0)
        probe.expectMsgPF() {
              case Frontier(s, r) => s == s0 && r == r0
        }
          
        val s1 = State(Bucket(0, 3), Bucket(5, 5))
        val r1 = Set(
            State(Bucket(3, 3), Bucket(2, 5)),
            State(Bucket(3, 3), Bucket(5, 5))
        )

        graphManager ! GraphManager.Visit(s1, r1)
        probe.expectMsgPF() {
            case Frontier(s, r) => s == s1 && r == r1
         
        }   
        graphManager ! GraphManager.Visit(s1, r1)
        probe.expectMsgPF() {
            case Frontier(s, r) => s == s1 && r.isEmpty
        }
      }
      
      "Reply with a path to a given state" in {
          val probe = TestProbe()
          val graphManager = system.actorOf(Props(new GraphManager(probe.ref)))
          
          val s0 = State(Bucket(0, 3), Bucket(0, 5))
          val s1 = State(Bucket(3, 3), Bucket(0, 5))
          val r0 = Set(
            s1, 
            State(Bucket(0,3), Bucket(5, 5))
          )
          val path = List(s0, s1)
          
          graphManager ! GraphManager.Visit(s0, r0)
          graphManager ! GraphManager.FindPath(s0, s1)
          
          expectMsg(GraphManager.Path(path))
      }
  }
  
  "A LevelManager actor" must {
      "Synchronize iterations" in {
          val probe = TestProbe()
          val levelManager = system.actorOf(Props(new LevelManager(probe.ref)))
          
          val s0 = State(Bucket(0, 0), Bucket(0, 0))
          val r0 = Set(
            State(Bucket(3, 3), Bucket(0, 5)), 
            State(Bucket(0,3), Bucket(5, 5))
          )
       
          levelManager ! LevelManager.Init(Set(s0))
          probe.expectMsg(MoveMaker.Move(s0))
          
          levelManager ! LevelManager.Next(s0, r0)
          probe.expectMsgAllOf(
              MoveMaker.Move(State(Bucket(3, 3), Bucket(0, 5))), 
              MoveMaker.Move(State(Bucket(0,3), Bucket(5, 5)))
           )
      }
  }
  
  "A BucketFiller actor" must {
      "Find the optimal path if it exists" in {
          val player = TestProbe()
          val bucketFiller = system.actorOf(Props(new Simulation(player.ref)))
          
          val capacityA = 3
          val capacityB = 5
          val goal = 4
          
          val path = List(
              State(Bucket(0,3),Bucket(0,5)),
              State(Bucket(0,3),Bucket(5,5)),
              State(Bucket(3,3),Bucket(2,5)),
              State(Bucket(0,3),Bucket(2,5)),
              State(Bucket(2,3),Bucket(0,5)),
              State(Bucket(2,3),Bucket(5,5)),
              State(Bucket(3,3),Bucket(4,5))
            )
          
          bucketFiller ! Start(capacityA, capacityB, goal)
          player.expectMsg(Result(path))
      }
  }
}

