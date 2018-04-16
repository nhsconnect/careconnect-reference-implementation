package uk.nhs.careconnect.ri.gateway.https;


import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.rest.RestBindingMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import uk.nhs.careconnect.ri.gatewaylib.interceptor.GatewayCamelPostProcessor;
import uk.nhs.careconnect.ri.gatewaylib.interceptor.GatewayCamelProcessor;

import java.io.InputStream;

@Component
public class CamelRoute extends RouteBuilder {

	@Autowired
	protected Environment env;

	@Value("${fhir.restserver.serverBase}")
	private String serverBase;

	@Value("${fhir.restserver.edmsBase}")
	private String edmsBase;
	
    @Override
    public void configure() 
    {

		GatewayCamelProcessor camelProcessor = new GatewayCamelProcessor();

		GatewayCamelPostProcessor camelPostProcessor = new GatewayCamelPostProcessor();


		restConfiguration()
				.component("servlet")
				.contextPath("oauth2")
				.dataFormatProperty("prettyPrint", "true")
				.enableCORS(true);


		rest("/smartonfhir/").description("Auth Token")
				.get("token")
					.to("direct:authtoken")
				.post("token")
					.to("direct:authtoken");


		from("direct:FHIRPatient")
			.routeId("Gateway Patient")
				.to("direct:HAPIServer");

		from("direct:FHIRPractitioner")
				.routeId("Gateway Practitioner")
				.to("direct:HAPIServer");

        from("direct:FHIRPractitionerRole")
                .routeId("Gateway PractitionerRole")
                .to("direct:HAPIServer");

        from("direct:FHIROrganisation")
                .routeId("Gateway Organisation")
                .to("direct:HAPIServer");

        from("direct:FHIRLocation")
                .routeId("Gateway Location")
                .to("direct:HAPIServer");

		from("direct:FHIRObservation")
			.routeId("Gateway Observation")
			.to("direct:HAPIServer");

		from("direct:FHIREncounter")
				.routeId("Gateway Encounter")
				.to("direct:HAPIServer");

		from("direct:FHIRCondition")
				.routeId("Gateway Condition")
				.to("direct:HAPIServer");

		from("direct:FHIRProcedure")
				.routeId("Gateway Procedure")
				.to("direct:HAPIServer");

		from("direct:FHIRMedicationRequest")
				.routeId("Gateway MedicationRequest")
				.to("direct:HAPIServer");

		from("direct:FHIRMedicationStatement")
				.routeId("Gateway MedicationStatement")
				.to("direct:HAPIServer");

		from("direct:FHIRImmunization")
				.routeId("Gateway Immunization")
				.to("direct:HAPIServer");

		from("direct:FHIREpisodeOfCare")
				.routeId("Gateway EpisodeOfCare")
				.to("direct:HAPIServer");

		from("direct:FHIRAllergyIntolerance")
				.routeId("Gateway AllergyIntolerance")
				.to("direct:HAPIServer");

		from("direct:FHIRCapabilityStatement")
				.routeId("Gateway CapabilityStatement")
				.to("direct:HAPIServer");

		from("direct:FHIRBundle")
				.routeId("Gateway Bundle")
				.to("direct:HAPIServer");

		from("direct:FHIRComposition")
				.routeId("Gateway Composition")
				.to("direct:HAPIServer");

		from("direct:FHIRDiagnosticReport")
				.routeId("Gateway DiagnosticReport")
				.to("direct:HAPIServer");

		from("direct:FHIRCarePlan")
				.routeId("Gateway CarePlan")
				.to("direct:HAPIServer");
		from("direct:FHIRDocumentReference")
				.routeId("Gateway DocumentReference")
				.to("direct:HAPIServer");

		from("direct:FHIRBinary")
				.routeId("Gateway Binary")
				.to("direct:EDMSServer");

		from("direct:EDMSServer")
				.routeId("INT EDMS Server")
				.process(camelProcessor)
				.setHeader(Exchange.CONTENT_TYPE, simple("application/fhir+json"))
				.to("log:uk.nhs.careconnect.FHIRGateway.start?level=INFO&showHeaders=true&showExchangeId=true")
				.to(edmsBase)
				.process(camelPostProcessor)
				.to("log:uk.nhs.careconnect.FHIRGateway.complete?level=INFO&showHeaders=true&showExchangeId=true")
				.convertBodyTo(InputStream.class);

		from("direct:HAPIServer")
            .routeId("INT FHIR Server")
				.process(camelProcessor)
				.to("log:uk.nhs.careconnect.FHIRGateway.start?level=INFO&showHeaders=true&showExchangeId=true")
                .to(serverBase)
				.process(camelPostProcessor)
                .to("log:uk.nhs.careconnect.FHIRGateway.complete?level=INFO&showHeaders=true&showExchangeId=true")
				.convertBodyTo(InputStream.class);

		from("direct:authtoken")
				.routeId("auth Server")
				.to("log:uk.nhs.careconnect.smartOnFhir.PRE?level=INFO&showHeaders=true&showExchangeId=true")
				.to("http4:localhost:20080/token?throwExceptionOnFailure=false&bridgeEndpoint=true")
				//.to("http4:purple.testlab.nhs.uk:20080/token?throwExceptionOnFailure=false&bridgeEndpoint=true")
				.to("log:uk.nhs.careconnect.smartOnFhir.POST?level=INFO&showHeaders=true&showExchangeId=true");

    }
}
