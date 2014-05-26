<#import "/spring.ftl" as spring />
<h4 style="padding:5px 0 15px 20px;">Applications</h4>
<ul class="nav nav-list">
	<#list applications as application>
		<li><label class="tree-toggler nav-header">${application.name}</label>
			<ul class="nav nav-list tree">
				<li>
					<a 
						href="<@spring.url "/application/monitor?applicationName=${application.name?url}" />"
						<#if currenturl == "/application/monitor?applicationName=${application.name}">class="current"</#if>
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
										href="<@spring.url "/application/monitor/sequence?applicationName=${application.name?url}&sequenceName=${sequence.name?url}" />"
										<#if currenturl == "/application/monitor/sequence?applicationName=${application.name}&sequenceName=${sequence.name}">class="current"</#if>
									>
									Monitor
									</a>
								</li>
								<li>
									<a 
										href="<@spring.url "/application/sequence/thresholds?applicationName=${application.name?url}&sequenceName=${sequence.name?url}" />"
										<#if currenturl == "/application/sequence/thresholds?applicationName=${application.name}&sequenceName=${sequence.name}">class="current"</#if>
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