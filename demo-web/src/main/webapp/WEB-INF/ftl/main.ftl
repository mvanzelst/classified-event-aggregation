<!DOCTYPE html>
<html lang="en">
<head>
	<#include "includes/header.ftl">
	<#import "/spring.ftl" as spring>
	<script> 
		// wait for the DOM to be loaded
		$(document).ready(function() { 
			$('#userForm').submit(function(event) {
				event.preventDefault();
				$('#userInfoTable').hide();
				retrieveUser()();
			});
		});

		function failedToRetrieveUser(){
			return function(){
				$('#progressIndicator').hide();
				$('#errorMessage').show();
			}
		}

		function userRetrieved(){
			return function(data){
				$('#userInfo').html("\
					<td>" + data.userName + "</td>\
					<td>" + data.firstName + "</td>\
					<td>" + data.surName + "</td>\
					<td>" + data.address + "</td>\
					<td>" + data.country + "</td>\
					<td>" + data.birthDay + "</td>\
				");
				$('#progressIndicator').hide();
				$('#userInfoTable').show();
			}
		}

		function retrieveUser(){
			$('#errorMessage').hide();
			$('#progressIndicator').show();
			return function(){
				var uuid = $('#userForm #user').val();
				$.ajax({
					url: "<@spring.url "/rest/user/"/>" + uuid,
					error: failedToRetrieveUser(),
					success: userRetrieved(),
					cache: false
				});
			};
		}
	</script>
</head>
<body>
	<div class="container">
		<h1>User management</h1>
		<p>&nbsp;</p>
		<div class="box"> 
			<form id="userForm" action="" method="post"> 
				<div class="header">User form</div>
					<div class="form-group">
						<label for="user">Select user</label>
						<select name="user" id="user" class="form-control" style="width: 100px;">
							<#list userNameByUuid?keys as uuid>
								<option value="${uuid?html}">${userNameByUuid[uuid]?html}</option>
							</#list>
						</select>
					</div>
				<input type="submit" value="Retrieve user information" class="btn btn-primary">
			</form>
		</div>
		<p>&nbsp;</p>
		<div class="box">
			<div class="header">User information</div>
			<div style="display:none" id="errorMessage" class="text-danger">Failed to load user</div>
			<div style="display:none" id="progressIndicator">Loading user information... <img src="<@spring.url "/images/"/>loader.gif" /></div>
			<div style="display:none" id="userInfoTable">
				<table class="table">
					<thead>
						<tr>
							<th>User Name</th>
							<th>First Name</th>
							<th>Sur Name</th>
							<th>Address</th>
							<th>Country</th>
							<th>Birth date</th>
						</tr>
					</thead>
					<tbody>
						<tr id="userInfo">
						</tr>
					</tbody>
				</table>
			</div>
		</div>
	</div>
</body>
</html>