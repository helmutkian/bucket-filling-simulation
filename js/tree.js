var d3 = require('d3');

function createTree(domNode) {
    var margin = {top: 20, right: 120, bottom: 20, left: 120};
    var width = 1160 - margin.right - margin.left;
    var height = 700 - margin.top - margin.bottom;
    var i = 0;
    var duration = 750;

    var tree = d3.layout.tree()
	    .size([height, width]);

    var diagonal = d3.svg.diagonal()
	    .projection(d => [d.y, d.x]);

    var svg = d3.select(domNode)
	    .append('svg')
	    .attr('width', width + margin.right + margin.left)
	    .attr('height', height + margin.top + margin.bottom)
	    .append('g')
	    .attr('transform', 'translate(' + margin.left + ',' + margin.top + ')');

    var root = null;

    return {
	update: update,
	clear: clear,
	solve: solve
    };

    function update(source) {
	if (!root) {
	    root = source;
	    root.y0 = 0;
	    root.x0 = height / 2;
	}
	
	var nodes = tree.nodes(root).reverse();
	var links = tree.links(nodes);

	nodes.forEach(d => d.y = d.depth * 60);

	var node = svg.selectAll('g.node')
		.data(nodes, d => d.id || (d.id = ++i));

	var nodeEnter = node.enter()
		.append('g')
		.attr('class', 'node')
		.attr('transform', d => 'translate(' + source.y0 + ',' + source.x0 + ')');
	
	nodeEnter.append('circle')
	    .attr('r', 8)
	    .style('fill', 'lightsteelblue');

	nodeEnter.append('text')
	    .attr('x', -10)
	    .attr('y', 15)
	    .attr('dy', '.35em')
	    .attr('text-anchor', 'center')
	    .text(d => d.name);

	var nodeUpdate = node
		.transition().duration(duration)
		.attr('transform', d => 'translate(' + d.y + ', ' + d.x + ')');

	var link = svg.selectAll('path.link')
		.data(links, d => d.target.id);

	link.enter().insert('path', 'g')
	    .attr('class', 'link')
	    .attr('d', d => {
		var o = { x: source.x0, y: source.y0 };
		return diagonal({ source: o, target: o });
	    })
	    .style('fill', 'none')
	    .style('stroke', 'lightgrey')
	    .style('stroke-width', '2px');

	link
	    .transition().duration(duration)
	    .attr('d', diagonal);

	nodes.forEach(function(d) {
	    d.x0 = d.x;
	    d.y0 = d.y;
	});

	var maxWidth = Math.max(width, d3.max(nodes, d => d.depth) * 60);
	
	d3.select('svg')
	    .attr('width', maxWidth + margin.left + margin.right);

	tree.size([height, maxWidth]);
    }

    function clear() {
	d3.selectAll('.node, .link').remove();
	d3.select('svg').attr('width', width + margin.left + margin.right);
	tree.size([height, width]);
	root = null;
    }

    function solve(path) {
	svg.selectAll('.node')
	    .filter(d => !path.length || path.indexOf(d.name) >= 0)
	    .select('circle')
	    .transition().duration(duration)
	    .style('fill', () => path.length ? 'green' : 'red');
    }
}

module.exports = createTree;
