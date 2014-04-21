<!DOCTYPE html>
<html lang="en">

<head>
<meta charset="utf-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<meta name="description" content="">
<meta name="author" content="">

<title>Starter Template for Bootstrap</title>

<!-- Bootstrap core CSS 
    <link href="css/bootstrap.css" rel="stylesheet">

    <!-- Add custom CSS here -->
    <link href="/css/sidebar.css" rel="stylesheet">

<script	src="//cdnjs.cloudflare.com/ajax/libs/jquery/1.9.1/jquery.min.js"></script>
<script	src="//cdnjs.cloudflare.com/ajax/libs/twitter-bootstrap/2.3.1/js/bootstrap.min.js"></script>

<link rel="stylesheet" href="//cdnjs.cloudflare.com/ajax/libs/twitter-bootstrap/2.3.1/css/bootstrap.min.css">
<link rel="stylesheet" href="//cdnjs.cloudflare.com/ajax/libs/twitter-bootstrap/2.3.1/css/bootstrap-responsive.min.css">

<script type="text/javascript">
$(document).ready(function () {
	// Hide the menu
	$('ul.tree').hide();
	
	// Show the link to the current page and it's parents
	$('ul.tree a.current').parents().show();
	
	$('label.tree-toggler').click(function () {
		$(this).parent().children('ul.tree').toggle(300);
	});
});
</script>

</head>

<body>

	<div id="wrapper">

		<!-- Sidebar -->
		<div id="sidebar-wrapper">
			<ul class="nav nav-list">
	            <li><label class="tree-toggler nav-header">App 1</label>
	                <ul class="nav nav-list tree">
	                    <li><a href="#">Link</a></li>
	                    <li><a href="#">Link</a></li>
	                    <li><label class="tree-toggler nav-header">Task 1.1</label>
	                        <ul class="nav nav-list tree">
	                            <li><a class="current" href="#link1.1">Link</a></li>
	                            <li><a href="#">Link</a></li>
	                        </ul>
	                    </li>
	                </ul>
	            </li>
	            <li class="divider"></li>
	            <li><label class="tree-toggler nav-header">App 2</label>
	                <ul class="nav nav-list tree">
	                    <li><a href="#">Link</a></li>
	                    <li><a href="#">Link</a></li>
	                    <li><label class="tree-toggler nav-header">Task 2.1</label>
	                        <ul class="nav nav-list tree">
	                            <li><a href="#">Link</a></li>
	                            <li><a href="#">Link</a></li>
	                        </ul>
	                    </li>
	                </ul>
	            </li>
	        </ul>
		</div>

		<!-- Page content -->
		<div id="page-content-wrapper">
			<div class="content-header">
				<h1>
					<a id="menu-toggle" href="#" class="btn btn-default"><i
						class="icon-reorder"></i></a> Simple Sidebar
				</h1>
			</div>
			<!-- Keep all page content within the page-content inset div! -->
			<div class="page-content inset">
				<div class="row">
					<div class="col-md-12">
						<p class="lead">This simple sidebar template has a hint of
							JavaScript to make the template responsive. It also includes Font
							Awesome icon fonts.</p>
					</div>
					<div class="col-md-6">
						<p class="well">The template still uses the default Bootstrap
							rows and columns.</p>
					</div>
					<div class="col-md-6">
						<p class="well">But the full-width layout means that you wont
							be using containers.</p>
					</div>
					<div class="col-md-4">
						<p class="well">Three Column Example</p>
					</div>
					<div class="col-md-4">
						<p class="well">Three Column Example</p>
					</div>
					<div class="col-md-4">
						<p class="well">You get the idea! Do whatever you want in the
							page content area!</p>
					</div>
				</div>
			</div>
		</div>

	</div>
</body>
</html>