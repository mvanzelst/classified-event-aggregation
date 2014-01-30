<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="content-type" content="text/html; charset=utf-8" />
<base href="${pageContext.request.contextPath}/" />
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
</head>
<body>
	<button onclick="transition()">Update</button>
	<script src="js/d3.v3.min.js"></script>
	<script>

	var layers = [
		{
			"name": "apples",
			"values": [
				{ "x": 0, "y":	91},
				{ "x": 1, "y": 290},
				{ "x": 2, "y": 380},
				{ "x": 3, "y": 480},
				{ "x": 4, "y": 580},
				{ "x": 5, "y": 680},
				{ "x": 6, "y": 780},
				
			]
		},
		{	
			"name": "oranges",
			"values": [
				{ "x": 0, "y":	9},
				{ "x": 1, "y": 49},
				{ "x": 2, "y": 380},
				{ "x": 3, "y": 480},
				{ "x": 4, "y": 580},
				{ "x": 5, "y": 680},
				{ "x": 6, "y": 780},
			]
		}
	];
	var m = 7;
	
	var width = 960,
	height = 500;
	
	var x = d3.scale.linear()
	.domain([0, m - 1])
	.range([0, width]);

	var y = d3.scale.linear()
	.domain([0, d3.max(layers, function(d) { return d.y0 + d.y; })])
	.range([height, 0]);
	
	var stack = d3.layout.stack()
		.offset("expand")
		.values(function(d) { return d.values; });
	
	var svg = d3.select("body").append("svg")
	.attr("width", width)
	.attr("height", height);
	
	var color = d3.scale.linear()
	.range(["#aad", "#556"]);
	
	var area = d3.svg.area()
		.x(function(d) { return x(d.x); })
		.y0(function(d) { return y(d.y0); })
		.y1(function(d) { return y(d.y0 + d.y); });
	
	svg.selectAll("path")
		.data(stack(layers))
		.enter().append("path")
			.attr("d", function(d) { return area(d.values); })
		.append("title")
		.text(function(d) { return d.name; });
	
	function order(data) {
		return d3.range(data.length);
	}

	</script>
</body>
</html>