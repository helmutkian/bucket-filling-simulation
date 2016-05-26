Bucket Filling Simulation
=========================

Bucket filling simulation

## Install & Run

This project requires running two separate servers. A NodeJs HTTP server to deliver static content for the web client and a Scala/Akka WebSocket server for streaming the simulation.

* Clone repo
* Install JavaScript depedencies:
````
cd js/
npm install browserify
npm install reactify
npm install
````
* Build web client:
````
browserify -t reactify main.jsx -o public/bundle.js
````
* Start static content server
````
node server.js
````
* Run Scala/Akka server
````
cd scala/
sbt run
````
* Open browser and navigate to ``localhost:3000``
