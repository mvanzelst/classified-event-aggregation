<!DOCTYPE html>
<html lang="en">
<head>
	<#include "includes/header.ftl">
	<script>
	  $(function() {
	    $( "#accordion" ).accordion();
	  });
	</script>
</head>
<body>
	<div id="wrapper">
		<!-- Sidebar -->
		<div id="sidebar-wrapper">
			<#include "includes/menu.ftl">
		</div>

		<div id="page-content-wrapper" style="padding-top:10px;">
			<div class="content-header">
				<h1>Main</h1>
			</div>
			<div class="page-content inset">
				<select>
					<option>Thresholds enabled</option>
					<option>Thresholds disabled</option>
				</select>
				<div style="height:20px;"></div>
				<table class="table table-striped table-bordered">
					<tr>
						<td>Sequence name</td> 
						<td>/Zoek(action,categorieId,eindDag,eindJaar,eindMaand,origin,sa,sc,searchCategorieId,searchTerm,vanafDag,vanafJaar,vanafMaand,x,zoekMethode)</td>
					</tr>
					<tr>
						<td>Sequence id</td> 
						<td><a href="bbc34e48-63a5-4c00-b901-0fdfcc3461a8">bbc34e48-63a5-4c00-b901-0fdfcc3461a8</a></td>
					</tr>
					<tr>
						<td>Start</td> 
						<td>DATE HERE</td>
					</tr>
					<tr>
						<td>End</td> 
						<td>DATE HERE</td>
					</tr>
					<tr>
						<td>Statisic type</td> 
						<td>Standard score of duration</td>
					</tr>
					<tr>
						<td>Statistic value</td> 
						<td>3</td>
					</tr>
				</table>
				<table class="table table-bordered table-striped">
					<tr>
						<td>Sequence name</td> 
						<td>/Zoek(action,categorieId,eindDag,eindJaar,eindMaand,origin,sa,sc,searchCategorieId,searchTerm,vanafDag,vanafJaar,vanafMaand,x,zoekMethode)</td>
					</tr>
					<tr>
						<td>Sequence id</td> 
						<td><a href="bbc34e48-63a5-4c00-b901-0fdfcc3461a8">bbc34e48-63a5-4c00-b901-0fdfcc3461a8</a></td>
					</tr>
					<tr>
						<td>Start</td> 
						<td>DATE HERE</td>
					</tr>
					<tr>
						<td>End</td> 
						<td>DATE HERE</td>
					</tr>
					<tr>
						<td>Statisic type</td> 
						<td>Standard score of duration</td>
					</tr>
					<tr>
						<td>Statistic value</td> 
						<td>3</td>
					</tr>
				</table>
			</div>
		</div>
	</div>
</body>
</html>