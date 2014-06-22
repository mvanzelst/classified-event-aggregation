<!DOCTYPE html>
<html lang="en">
<head>
	<#include "includes/header.ftl">
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
				<#list logSequenceStatistics as logSequenceStatistic>
				<table class="table table-striped table-bordered">
					<tr>
						<td>Sequence name</td> 
						<td>${logSequenceStatistic.sequenceName}</td>
					</tr>
					<tr>
						<td>Sequence id</td> 
						<td>${logSequenceStatistic.sequenceId}</td>
					</tr>
					<tr>
						<td>Start</td>
						<td>${logSequenceStatistic.startTimestamp?number_to_datetime}</td>
					</tr>
					<tr>
						<td>End</td> 
						<td>${logSequenceStatistic.endTimestamp?number_to_datetime}</td>
					</tr>
					<tr>
						<td>Statisic type</td> 
						<td>${logSequenceStatistic.algorithmName}</td>
					</tr>
					<tr>
						<td>Standard score</td> 
						<td>${logSequenceStatistic.statistics.get("stdScore")}</td>
					</tr>
					<tr>
						<td>LogMessages</td> 
						<td>
							<#list logMessagesBySequenceId[logSequenceStatistic.sequenceId] as logMessage>
								${logMessage}<br />
							</#list>
						</td>
					</tr>
				</table>
				</#list>
			</div>
		</div>
	</div>
</body>
</html>