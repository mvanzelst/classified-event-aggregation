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
		// Graph cannot handle zeroes
     	for( var i = 0; i < values.length; i++){
     		if(values[i] == 0){
     			values[i] = 0.1;
     		}
     	}

		// Formatters for counts and times (converting numbers to Dates).
		var formatCount = d3.format(",.0f");
		
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
			.bins(x.ticks(Math.min(Math.ceil(max / 10) * 10, 20)))
			(values);
		
		var y = d3.scale.linear()
			.domain([0, d3.max(data, function(d) { return d.y; })])
			.range([height, 0]);
		
		var xAxis = d3.svg.axis()
			.scale(x)
			.orient("bottom");
		
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
		$("form.algorithm-threshold-form").submit(function(event) {
			event.preventDefault();
			var form = $(event.target);
			var formData = {};
			$.each(form.serializeArray(), function() {
				formData[this.name] = this.value;
			});
			if(formData.action == "delete"){
				if(formData.id == "")
					return;
				
				formData['_method'] = "DELETE";
			} else if(formData.id == ""){
				delete formData['id']
				formData['_method'] = "POST";
			} else {
				formData['_method'] = "PUT";
			}
		    $.ajax({
	           type: "POST",
	           url: "<@spring.url "/rest/threshold" />",
	           data: formData,
	           success: function(data){
	           		if(formData.action == "delete"){
	           			form.find(input[name="thresholdValue"]).set("");
	           			form.find(input[name="id"]).set("");
	           		} else {
	           			console.log(data);
	           		}
	           }
	         });
			
		    return false; // avoid to execute the actual submit of the form.
		});
		
		<#if dimensionlessStatistics??>
			<#list dimensionlessStatistics as dimensionlessStatistic>
				createHistogram("div#histogram-${dimensionlessStatistic["type"]}",  JSON.parse("${dimensionlessStatistic["stats"]}"));
			</#list>
		</#if>
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

$
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
				<#if dimensionlessStatistics??>
					<#list dimensionlessStatistics as dimensionlessStatistic>
						<div class="algorithm-statistics">
							<p>${dimensionlessStatistic["name"]}</p>
							<div id="histogram-${dimensionlessStatistic["type"]}"></div>
							<div class="algorithm-threshold">
								<form class="algorithm-threshold-form">
									<input type="hidden" name="id" value="<#if dimensionlessStatistic["threshold"]?? >${dimensionlessStatistic["threshold"].id}</#if>" />
									<input type="hidden" name="algorithmName" value="${dimensionlessStatistic["type"]}" />
									<input type="hidden" name="applicationName" value="${applicationName}" />
									<input type="hidden" name="sequenceName" value="${sequenceName}" />
									<input type="text" name="thresholdValue" value="<#if dimensionlessStatistic["threshold"]?? >${dimensionlessStatistic["threshold"].thresholdValue}</#if>" />
									<input type="submit" name="action" value="set" />
									<input type="submit" name="action" value="delete" />
								</form>
							</div>
						</div>
					</#list>
				</#if>
			</div>
		</div>
	</div>
</body>
</html>