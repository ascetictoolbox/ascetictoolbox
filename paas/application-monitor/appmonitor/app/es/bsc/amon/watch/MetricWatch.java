package es.bsc.amon.watch;

public class MetricWatch {
	String appId;
	String nodeId;
	String deploymentId;
	String slaId;
	String query;


	/*

	<ViolationMessage xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" appId="XXX" deploymentId="4">
		<time>2015-02-25T09:30:47.0Z</time>
		<value id="free">11</value>
		<alert>
			<type>violation</type>
			<slaUUID>f28d4719-5f98-4c87-9365-6be602da9a4a</slaUUID>
			<slaAgreementTerm>power_usage_per_app</slaAgreementTerm>
			<slaGuaranteedState>
				<guaranteedId>power_usage_per_app</guaranteedId>
				<operator>less_than_or_equals</operator>
				<guaranteedValue>10</guaranteedValue>
			</slaGuaranteedState>
		</alert>
	</ViolationMessage>
*/
}
