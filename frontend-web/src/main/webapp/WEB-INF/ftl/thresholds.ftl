<#import "/spring.ftl" as spring />
<!DOCTYPE html>
<html lang="en">
<header>
<#include "includes/header.ftl">

<!--  histogram -->
<script src="http://d3js.org/d3.v2.min.js?2.10.0"></script>
<script type="text/javascript">
	var restUrl = "<@spring.url "/rest" />";
		
	function createHistogram(targetElement, values){		
		// Formatters for counts and times (converting numbers to Dates).
		var formatCount = d3.format(",.0f"),
			formatTime = d3.time.format("%H:%M"),
			formatMinutes = function(d) { return d + "ms" };
		
		var margin = {top: 10, right: 30, bottom: 30, left: 30},
			width = 960 - margin.left - margin.right,
			height = 500 - margin.top - margin.bottom;
		
		// Max value
		var max = d3.max(values);
		
		var x = d3.scale.linear()
			.domain([0, Math.ceil(max / 10) * 10]) // Round to nearest multiple of 10
			.range([0, width]);
		
		// Generate a histogram using twenty uniformly-spaced bins.
		var data = d3.layout.histogram()
			.bins(x.ticks(20))
			(values);
		
		var y = d3.scale.linear()
			.domain([0, d3.max(data, function(d) { return d.y; })])
			.range([height, 0]);
		
		var xAxis = d3.svg.axis()
			.scale(x)
			.orient("bottom")
			.tickFormat(formatMinutes);
		
		var svg = d3.select(targetElement).append("svg")
			.attr("width", width + margin.left + margin.right)
			.attr("height", height + margin.top + margin.bottom)
		  .append("g")
			.attr("transform", "translate(" + margin.left + "," + margin.top + ")");
		
		var bar = svg.selectAll(".bar")
			.data(data)
		  .enter().append("g")
			.attr("class", "bar")
			.attr("transform", function(d) { return "translate(" + x(d.x) + "," + y(d.y) + ")"; });
		
		bar.append("rect")
			.attr("x", 1)
			.attr("width", x(data[0].dx) - 1)
			.attr("height", function(d) { return height - y(d.y); });
		
		bar.append("text")
			.attr("dy", ".75em")
			.attr("y", 6)
			.attr("x", x(data[0].dx) / 2)
			.attr("text-anchor", "middle")
			.text(function(d) { return formatCount(d.y); });
		
		svg.append("g")
			.attr("class", "x axis")
			.attr("transform", "translate(0," + height + ")")
			.call(xAxis);
	}
	$(document).ready(function() {
		createHistogram("div#histogram-durations",  JSON.parse("${durations}"));
		createHistogram("div#histogram-standardScoreOfDuration", JSON.parse("${standardScoreOfDuration}"));
		createHistogram("div#histogram-numExceptions", JSON.parse("${numExceptions}"));
		createHistogram("div#histogram-standardScoreOfnNumExceptions", JSON.parse("${standardScoreOfNumExceptions}"));
	});
</script>

<style>

div#histogram {
  font: 10px sans-serif;
}

.bar rect {
  fill: steelblue;
  shape-rendering: crispEdges;
}

.bar text {
  fill: #fff;
}

.axis path, .axis line {
  fill: none;
  stroke: #000;
  shape-rendering: crispEdges;
}

</style>

</header>
<body>
	<div id="wrapper">
		<!-- Sidebar -->
		<div id="sidebar-wrapper">
			<#include "includes/menu.ftl">
		</div>

		<div id="page-content-wrapper" style="padding-top:10px;">
			<div class="content-header">
				<h1>Task thresholds</h1>
			</div>
			<div class="page-content inset">
				<div id="histogram-durations"></div>
				<div id="histogram-standardScoreOfDuration"></div>
				<div id="histogram-numExceptions"></div>
				<div id="histogram-standardScoreOfnNumExceptions"></div>
			</div>
		</div>
	</div>
</body>
</html>