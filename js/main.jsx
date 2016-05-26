var React = require('react');
var ReactDOM = require('react-dom');
var EventEmitter = require('events');
var Simulation = require('./simulation');
var tree = require('./tree');

var dispatcher = new EventEmitter();
var simulation = Simulation(dispatcher);

var Controls = React.createClass({
    getInitialState: function () {
	return {
	    capacityA: 0,
	    capacityB: 0,
	    goal: 0	    
	};
    },
    handleChange: function (event) {
	var id = event.target.id;
	var value = event.target.value;
	var newState = {};

	if (value >= 0) {
	    newState[id] = value;
	    
	    this.setState(newState);
	}
    },
    handleSubmit: function (event) {
	var capacityA = parseInt(this.state.capacityA);
	var capacityB = parseInt(this.state.capacityB);
	var goal = parseInt(this.state.goal);

	event.preventDefault();

	dispatcher.emit('start', {
	    a: capacityA,
	    b: capacityB,
	    goal: goal
	});
    },
    render: function () {
	var capacityA = this.state.capacityA;
	var capacityB = this.state.capacityB;
	var goal = this.state.goal;
	return (<form className="form-inline" onSubmit={ this.handleSubmit }>
		<div className="form-group">
		<label className="control-label">Bucket A</label>
		<input type="number" className="form-control" id="capacityA" min="0" onChange={ this.handleChange } value={ capacityA } />
		</div>
		
		<div className="form-group">
		<label className="control-label">Bucket B</label>
		<input type="number" className="form-control" id="capacityB" min="0" onChange={ this.handleChange } value={ capacityB } />
		</div>
		
		<div className="form-group">
		<label className="control-label">Goal</label>
		<input type="number" className="form-control" id="goal" min="0" onChange={ this.handleChange } value={ goal } />
		</div>
		
		<button type="submit" className="btn btn-primary">Start</button>
		</form>
	       );
    }
});

var Tree = React.createClass({
    componentDidMount: function () {
	this.tree = tree(ReactDOM.findDOMNode(this));
    },
    componentDidUpdate: function (prevProps) {
	var level = prevProps.level;
	var root = prevProps.root;

	if (root !== this.props.root) {
	    this.tree.clear();
	}
	
	if (level) {
	    Object.keys(level)
		.map(name => level[name])
		.forEach(parent => this.tree.update(parent));
	}

	if (this.props.solution) {
	    this.tree.solve(this.props.solution);
	}
    },
    render: function () {
	return (<div className="tree" style={ { overflow: 'auto' } }></div>);
    }
});

var App = React.createClass({
    getInitialState: function () {
	return {
	    tree: null,
	    level: {},
	    solution: null
	};
    },
    handleLevel: function (states) {
	var tree = this.state.tree;
	var level = this.state.level;
	if (!tree) {
	    tree = { name: getName(states[0]), children: [] };
	    level[tree.name] = tree;
	    this.setState({
		tree: tree,
		level: level
	    });
	} else {
	    var nextLevel = Object.keys(level)
		    .map(key => level[key])
		    .reduce((acc, parent) => {
			parent.children.forEach(child => acc[child.name] = child);
			return acc;
		    }, {});
	    this.setState({
		tree: tree,
		level: nextLevel
	    });
	}
    },
    handleNext: function (states) {
	var level = this.state.level;
	var parent = level[getName(states[0])];
	var children = states.slice(1)
		.map(state => ({ name: getName(state), children: [] }));
	parent.children = children;
    },
    componentWillMount: function () {
	dispatcher.on('start', settings => {
	    this.setState(this.getInitialState());
	    simulation.start(settings);
	});
	
	dispatcher.on('level', debounce(this.handleLevel));
	
	dispatcher.on('next', debounce(this.handleNext));
	
	dispatcher.on('result', debounce(states => {
	    console.log(states);
	    this.setState({
		solution: states.map(getName)
	    });
	}));
    },
    render: function () {
	var solution = this.state.solution;
	return (<div className="container">
		<Controls />
		<div class="row">
		<div class="col-md-12">
		<Tree root={ this.state.tree } level={ this.state.level } solution={ solution }/>
		</div>
		</div>
		</div>);
    }
});

ReactDOM.render(<App />, document.getElementById('content'));

function getName(state) {
    return state[0] + ', ' + state[1];
}

function debounce(fn) {
    return function () {
	setTimeout(() => fn.apply(null, arguments), 200);
    };
}

