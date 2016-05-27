var d3 = require('d3');

function createMatrix(domNode, rows, cols) {
    var margin = { top: 50, right: 0, bottom: 100, left: 30 };
    var width = 960 - margin.left - margin.right;
    var height = 430 - margin.top - margin.bottom;

    var svg = d3.select(domNode).append('svg')
	    .attr('width', width + margin.left + margin.right)
	    .attr('height', height + margin.top + margin.bottom)
	    .append('g')
	    .attr('transform', 'translate(' + margin.left + ', ' + margin.top + ')');

    for (var i = 0; i < rows; i++) {
	var row = svg.append('g')
		.attr('id', 'row-' + i);
	for (var j = 0; j < cols; j++) {
	    var col = svg.append('rect')
		    .attr('id', 'col-' + i + '-' + j)
		    .attr('height', 10)
		    .attr('width', 10)
		    .attr('y', j * 10)
		    .attr('x', i * 10)
		    .style('fill', 'lightgrey')
		    .style('stroke', '#ccc')
		    .style('stroke-width', '1px');
	}
    }

    function visit(nodes) {
	var nodeIds = nodes.map(node => '.node-' + node.a + node.b).join(',');

	d3.selectAll(nodeIds)
	    .style('fill', 'lightsteelblue');
    }
}

module.exports = createMatrix;
