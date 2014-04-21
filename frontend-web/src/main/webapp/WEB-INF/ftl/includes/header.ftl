<#import "/spring.ftl" as spring />

<meta charset="utf-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<meta name="description" content="">
<meta name="author" content="">

<title>Starter Template for Bootstrap</title>

<link href="<@spring.url "/css/sidebar.css" />" rel="stylesheet">

<script
	src="//cdnjs.cloudflare.com/ajax/libs/jquery/1.9.1/jquery.min.js"></script>
<script
	src="//cdnjs.cloudflare.com/ajax/libs/twitter-bootstrap/2.3.1/js/bootstrap.min.js"></script>

<link rel="stylesheet"
	href="//cdnjs.cloudflare.com/ajax/libs/twitter-bootstrap/2.3.1/css/bootstrap.min.css">
<link rel="stylesheet"
	href="//cdnjs.cloudflare.com/ajax/libs/twitter-bootstrap/2.3.1/css/bootstrap-responsive.min.css">

<!-- menu -->
<script type="text/javascript">
	$(document).ready(function() {
		// Hide the menu
		$('ul.tree').hide();

		// Show parents
		$('ul.tree a.current').parents('ul.tree').show();
		
		// Show siblings
		$('ul.tree a.current').parents('ul.tree').find('ul.tree').show();
		
		$('label.tree-toggler').click(function() {
			$(this).parent().children('ul.tree').toggle(300);
		});
	});
</script>