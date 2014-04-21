<!doctype html>
<head>
	<base href="${pageContext.request.contextPath}/"/>

	<link type="text/css" rel="stylesheet" href="http://ajax.googleapis.com/ajax/libs/jqueryui/1.8/themes/base/jquery-ui.css">
	<link type="text/css" rel="stylesheet" href="css/rickshaw.min.css">
	<link type="text/css" rel="stylesheet" href="css/extensions.css?v=2">

	<script src="js/d3.v3.min.js"></script>

	<script src="https://ajax.googleapis.com/ajax/libs/jquery/1.6.2/jquery.min.js"></script>
	<script src="https://ajax.googleapis.com/ajax/libs/jqueryui/1.8.15/jquery-ui.min.js"></script>

	<script src="js/rickshaw.min.js"></script>

	<script src="js/extensions.js"></script>
</head>
<body>

<div id="content">

	<form id="side_panel">
		<h1>LogLevels</h1>
		<section><div id="legend"></div></section>
		<section>
			<div id="renderer_form" class="toggler">
				<input type="radio" name="renderer" id="area" value="area" checked>
				<label for="area">area</label>
				<input type="radio" name="renderer" id="bar" value="bar">
				<label for="bar">bar</label>
				<input type="radio" name="renderer" id="line" value="line">
				<label for="line">line</label>
				<input type="radio" name="renderer" id="scatter" value="scatterplot">
				<label for="scatter">scatter</label>
			</div>
		</section>
		<section>
			<div id="offset_form">
				<label for="stack">
					<input type="radio" name="offset" id="stack" value="zero" checked>
					<span>stack</span>
				</label>
				<label for="stream">
					<input type="radio" name="offset" id="stream" value="wiggle">
					<span>stream</span>
				</label>
				<label for="pct">
					<input type="radio" name="offset" id="pct" value="expand">
					<span>pct</span>
				</label>
				<label for="value">
					<input type="radio" name="offset" id="value" value="value">
					<span>value</span>
				</label>
			</div>
			<div id="interpolation_form">
				<label for="cardinal">
					<input type="radio" name="interpolation" id="cardinal" value="cardinal" checked>
					<span>cardinal</span>
				</label>
				<label for="linear">
					<input type="radio" name="interpolation" id="linear" value="linear">
					<span>linear</span>
				</label>
				<label for="step">
					<input type="radio" name="interpolation" id="step" value="step-after">
					<span>step</span>
				</label>
			</div>
		</section>
		<section>
			<h6>Smoothing</h6>
			<div id="smoother"></div>
		</section>
		<section></section>
	</form>

	<div id="chart_container">
		<div id="chart"></div>
		<div id="timeline"></div>
		<div id="preview"></div>
	</div>

</div>

<script>

Array.prototype.compare = function(arr) {
    if (this.length != arr.length) return false;
    for (var i = 0; i < arr.length; i++) {
        if (this[i].compare) { 
            if (!this[i].compare(arr[i])) return false;
        }
        if (this[i] !== arr[i]) return false;
    }
    return true;
}



/* 
	Convert from: 
		[
			{
				"period_start": 1460498400000,
				"counter": 1,
				"classification_value": "Warn"
			},
			{
				"period_start": 1460498400000,
				"counter": 1,
				"classification_value": "Error"
			}
		]
	to:
		{
			"Warn": [
				{
					"x": 1460498400000, 
					"y": 1
				}
			],
			"Error": [
				{
					"x": 1460498400000, 
					"y": 1
				}
			]
		}
*/
function convertToDataByClassification(data){
	var sets = {};
	data.forEach(function(entry) {
		if(sets[entry.classification_value] === undefined){
			sets[entry.classification_value] = [];
		}
		sets[entry.classification_value].push({x: entry.period_start, y: entry.counter});
	});
	return sets;
}

function zeroFillData(dataByClassification){
	var uniqueXs = [];
	Object.keys(dataByClassification).forEach(function(key) {
		dataByClassification[key].forEach(function(entry) {
			if(uniqueXs.indexOf(entry.x) === -1){
				uniqueXs.push(entry.x);
			}
		});
	});
	Object.keys(dataByClassification).forEach(function(key) {
		uniqueXs.forEach(function(x, i) {
			var amount = $.grep(dataByClassification[key], function(entry) {
				return entry.x === x;
			}).length;
			if(amount === 0){
				dataByClassification[key].push({x: x, y: 0});
			}
		});
	});
}

function sortData(dataByClassification){
	Object.keys(dataByClassification).forEach(function(key) {
		dataByClassification[key].sort(function(a, b){
			return a.x < b.x ? -1 : a.x > b.x ? 1 : 0;
		});
	});
}

function addDataToSeries(currentSeries, dataByClassification){
	Object.keys(dataByClassification).forEach(function(key) {
		var series = $.grep(currentSeries, function(entry) {
			return entry.name === key;
		});
		if(series.length === 0){
			currentSeries.push({
				color: palette.color(),
				data: dataByClassification[key],
				name: key
			});
		} else {
			series[0].data = dataByClassification[key];
		}
	});
}

function sliceSeries(series, start, end){
	Object.keys(series).forEach(function(key) {
		series[key] = series[key].slice(start, end);
	});
}

function mergeDataSeries(seriesA, seriesB){
	var newSeries = {};
	
	// Add serieA to newSeries
	Object.keys(seriesA).forEach(function(key) {
		if(newSeries[key] === undefined){
			newSeries[key] = [];
		}
		
		seriesA[key].forEach(function(entryA) {
			var amount = $.grep(newSeries[key], function(entryNew) {
				return entryA.x === entryNew.x;
			}).length;
			if(amount === 0){
				newSeries[key].push({x: entryA.x, y: entryA.y});
			}
		});
	});

	// Add serieB to newSeries
	Object.keys(seriesB).forEach(function(key) {
		if(newSeries[key] === undefined){
			newSeries[key] = [];
		}
		seriesB[key].forEach(function(entryB) {
			var amount = $.grep(newSeries[key], function(entryNew) {
				return entryB.x === entryNew.x;
			}).length;
			if(amount === 0){
				newSeries[key].push({x: entryB.x, y: entryB.y});
			}
		});
	});
	return newSeries;
}

updateGraph("LogLevel");
setInterval(function(){updateGraph("LogLevel");}, 200);

var lastDataSeries;
var keys;
var graphSeries;
var graph;
function updateGraph(classificationKey){
	$.ajax({
		url: "http://localhost:8080/frontend-web/classification/" + classificationKey + "/second/values?reverse=true&limit=500",
		success: function(data) {
			var currentDataSeries = convertToDataByClassification(data);
			if(graph === undefined){
				zeroFillData(currentDataSeries);
				sortData(currentDataSeries);

				// Only keep latest 10 data points
				sliceSeries(currentDataSeries, -250);

				var series = [];
				addDataToSeries(series, currentDataSeries);
				initiateGraph(series);
				lastDataSeries = currentDataSeries;
			} else {
				// Merge the latest dataset with the previous one
				var mergedData = mergeDataSeries(currentDataSeries, lastDataSeries);
				zeroFillData(mergedData);
				sortData(mergedData);

				// Only keep latest 10 data points
				sliceSeries(mergedData, -250);

				// Add data to existing series
				addDataToSeries(graph.series, mergedData);

				// Reset all sub graphs and dependend elements

				// Update the graph
				graph.update();

				// Store the merged set for future use
				lastDataSeries = mergedData;
			}
		}
	});
}

function compareArray(arr1, arr2){
	for (var i = 0; i < arr1.length; i++) {
		for (var j = 0; j < arr2.length; j++) {
			if (arr1[i] !== arr2[j]) {
				return false;
			}
		}
	}
	return true;
}

var palette = new Rickshaw.Color.Palette( { scheme: 'classic9' } );

// instantiate our graph!
function initiateGraph(series){
	graph = new Rickshaw.Graph({
		element: document.getElementById("chart"),
		width: 900,
		height: 500,
		renderer: 'area',
		stroke: true,
		preserve: true,
		series: series
	});
	graph.render();
	
	var preview = new Rickshaw.Graph.RangeSlider.Preview( {
		graph: graph,
		element: document.getElementById('preview'),
	} );

	var hoverDetail = new Rickshaw.Graph.HoverDetail( {
		graph: graph,
		xFormatter: function(x) {
			return new Date(x).toString();
		}
	} );

	var annotator = new Rickshaw.Graph.Annotate( {
		graph: graph,
		element: document.getElementById('timeline')
	} );

	var legend = new Rickshaw.Graph.Legend( {
		graph: graph,
		element: document.getElementById('legend')

	} );

	var shelving = new Rickshaw.Graph.Behavior.Series.Toggle( {
		graph: graph,
		legend: legend
	} );

	var order = new Rickshaw.Graph.Behavior.Series.Order( {
		graph: graph,
		legend: legend
	} );

	var highlighter = new Rickshaw.Graph.Behavior.Series.Highlight( {
		graph: graph,
		legend: legend
	} );

	var smoother = new Rickshaw.Graph.Smoother( {
		graph: graph,
		element: $('#smoother')
	} );

	var ticksTreatment = 'glow';

	var xAxis = new Rickshaw.Graph.Axis.Time( {
		graph: graph,
		ticksTreatment: ticksTreatment,
		timeFixture: new Rickshaw.Fixtures.Time.Local()
	} );

	xAxis.render();

	var yAxis = new Rickshaw.Graph.Axis.Y( {
		graph: graph,
		tickFormat: Rickshaw.Fixtures.Number.formatKMBT,
		ticksTreatment: ticksTreatment
	} );

	yAxis.render();


	var controls = new RenderControls( {
		element: document.querySelector('form'),
		graph: graph
	} );
	
	// add some data every so often

	var messages = [
		"Changed home page welcome message",
		"Minified JS and CSS",
		"Changed button color from blue to green",
		"Refactored SQL query to use indexed columns",
		"Added additional logging for debugging",
		"Fixed typo",
		"Rewrite conditional logic for clarity",
		"Added documentation for new methods"
	];

	/*
	function addAnnotation(force) {
		if (messages.length > 0 && (force || Math.random() >= 0.95)) {
			annotator.add(seriesData[2][seriesData[2].length-1].x, messages.shift());
			annotator.update();
		}
	}*/

	//addAnnotation(true);
	//setTimeout( function() { setInterval( addAnnotation, 6000 ) }, 6000 );

	var previewXAxis = new Rickshaw.Graph.Axis.Time({
		graph: preview.previews[0],
		timeFixture: new Rickshaw.Fixtures.Time.Local(),
		ticksTreatment: ticksTreatment
	});

	previewXAxis.render();
}





</script>

</body>
