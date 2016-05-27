package com.bucketfiller
 
 import org.mashupbots.socko.routes._
  import org.mashupbots.socko.infrastructure.Logger
  import org.mashupbots.socko.webserver.WebServer
  import org.mashupbots.socko.webserver.WebServerConfig

  import akka.actor.ActorSystem
  import akka.actor.Props
  import org.mashupbots.socko.events.HttpResponseStatus
  import org.mashupbots.socko.events.WebSocketFrameEvent
  
  import scala.collection._

  object SimulationServer extends Logger {
    val system = ActorSystem("ServerSystem")

    val routes = Routes({  
      // HTTP
    case HttpRequest(httpRequest) => httpRequest match {
        case Path("/favicon.ico") => httpRequest.response.write(HttpResponseStatus.NOT_FOUND)
    }
      
    // WebSocket connection
      case WebSocketHandshake(wsHandshake) => wsHandshake match {
        case Path("/ws/") => {
          val wsId = wsHandshake.webSocketId
            wsHandshake.authorize()
            }
        }       
        // WebSocket Incoming
        case WebSocketFrame(wsFrame) => handleWebSocket(wsFrame)
    })
    
   val webServer = new WebServer(WebServerConfig(), routes, system)

    def handleWebSocket(event: WebSocketFrameEvent) {
      val webSocketHandler = system.actorOf(Props(new WebSocketHandler(webServer)))

      webSocketHandler ! event
    }
    
    
    def main(args: Array[String]) {
      webServer.start()

      Runtime.getRuntime.addShutdownHook(new Thread {
        override def run { webServer.stop() }
      })
    }
  }