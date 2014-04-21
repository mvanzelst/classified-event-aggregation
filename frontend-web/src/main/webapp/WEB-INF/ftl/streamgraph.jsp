<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

<html xmlns="http://www.w3.org/1999/xhtml">
<head>
	<meta http-equiv="content-type" content="text/html; charset=utf-8"/>
	<base href="${pageContext.request.contextPath}/"/>
	<title>Streamgraph</title>
	<style>
		body {
			font-family: "Helvetica Neue", Helvetica, Arial, sans-serif;
			margin: auto;
			position: relative;
			width: 960px;
		}

		button {
			position: absolute;
			right: 10px;
			top: 10px;
		}
	</style>
	 <script src="//ajax.googleapis.com/ajax/libs/jquery/1.10.2/jquery.min.js"></script>
</head>
<body>
	<button onclick="transition()">Update</button>
	<script src="js/d3.v3.min.js"></script>
	<script>
	var i = 0;
	var n = 5, // number of layers
		m = 5, // number of samples per layer
		stack = d3.layout.stack().offset("wiggle"),
		layerDef = stack(d3.range(n).map(function() { return bumpLayer(m); }));
	
	var width = 960,
		height = 500;

	var x = d3.scale.linear()
		.domain([0, m - 1])
		.range([0, width]);
	
	var y = d3.scale.linear()
		// Seems to get the max y value of both layers
		.domain([0, d3.max(layerDef, function(layer) { return d3.max(layer, function(d) { return d.y0 + d.y; }); })])
		.range([height, 0]);

	var color = d3.scale.linear()
		.range(["#aad", "#556"]);

	var area = d3.svg.area()
		.x(function(d) { return x(d.x); })
		.y0(function(d) { return y(d.y0); })
		.y1(function(d) { return y(d.y0 + d.y); });

	var svg = d3.select("body").append("svg")
		.attr("width", width)
		.attr("height", height);

	svg.selectAll("path")
		.data(layerDef)
		.enter().append("a").attr("xlink:href", function () { return "test" + ++i}).append("path")
		.attr("d", area)
		.style("fill", function() { return color(Math.random()); })
		

	function transition() {
		d3.selectAll("path")
		.data(function() {
			var newLayer = stack(d3.range(6).map(function() { return getData("a"); }));
			var oldLayer = layerDef;
			y = d3.scale.linear()
			// Get the max y value of the new layer
			.domain([0, d3.max(newLayer, function(layer) { return d3.max(layer, function(d) { return d.y0 + d.y; }); })])
			.range([height, 0]);
			return layerDef = newLayer;
		})
		.transition()
		.duration(2500)
		.attr("d", area);
	}

	// Inspired by Lee Byron's test data generator.
	function bumpLayer(n) {

		function bump(a) {
			var x = 1 / (.1 + Math.random()),
				y = 2 * Math.random() - .5,
				z = 10 / (.1 + Math.random());
			for (var i = 0; i < n; i++) {
				var w = (i / n - y) * z;
				a[i] += x * Math.exp(-w * w);
			}
		}

		var a = [], i;
		for (i = 0; i < n; ++i) a[i] = 0;
		for (i = 0; i < 5; ++i) bump(a);
		return a.map(function(d, i) { return {x: i, y: Math.max(0, d)}; });
	}
	
	function getData(classification_key){
		return $.parseJSON('[{"x":0, "y":8},{"x":1, "y":20},{"x":2, "y":0.3},{"x":3, "y":5},{"x":4, "y":7}]');
	}

	</script>
	</body>
</html>