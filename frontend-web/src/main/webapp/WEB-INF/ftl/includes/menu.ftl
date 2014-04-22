<#import "/spring.ftl" as spring />
<h4 style="padding:5px 0 15px 20px;">Applications</h4>
<ul class="nav nav-list">
	<#list applications as application>
		<li><label class="tree-toggler nav-header">${application.name}</label>
			<ul class="nav nav-list tree">
				<li>
					<a 
						href="<@spring.url "/application/${application.name}/monitor" />"
						<#if currenturl == "/application/${application.name}/monitor">class="current"</#if>
					>
					Monitor
					</a>
				</li>
				<li>
					<label class="tree-toggler nav-header">sequences</label>
					<#list application.sequences as sequence>
					<ul class="nav nav-list tree">
						<li>
							<label class="tree-toggler nav-header">${sequence.name}</label>
							<ul class="nav nav-list tree">
								<li>
									<a 
										href="<@spring.url "/application/${application.name}/sequence/${sequence.name}/monitor" />"
										<#if currenturl == "/application/${application.name}/sequence/${sequence.name}/monitor">class="current"</#if>
									>
									Monitor
									</a>
								</li>
								<li>
									<a 
										href="<@spring.url "/application/${application.name}/sequence/${sequence.name}/thresholds" />"
										<#if currenturl == "/application/${application.name}/sequence/${sequence.name}/thresholds">class="current"</#if>
									>
									Thresholds
									</a>
								</li>
							</ul>
						</li>
					</ul>
					</#list>
				</li>
			</ul>
		</li>
		<#if (application != applications?last)>
			<li class="divider"></li>
		</#if>
	</#list>
</ul>