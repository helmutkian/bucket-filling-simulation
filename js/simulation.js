function createSimulation(dispatcher) {
    var connection = null;

    return {
	start: start
    };

    function start(settings) {
	if (!connection) {
	    connection = new WebSocket('ws:localhost:8888/ws/');
	    connection.onopen = () => connection.send(JSON.stringify(settings));
	    connection.onclose = () => connection = null;
	    connection.onmessage = e => {
		var msg = JSON.parse(e.data);
		dispatcher.emit(msg.topic, msg.data);
	    };
	} else {
	    connection.send(JSON.stringify(settings));
	}
    }
}

module.exports = createSimulation;
