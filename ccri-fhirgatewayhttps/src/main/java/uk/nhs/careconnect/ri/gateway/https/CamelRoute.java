package uk.nhs.careconnect.ri.gateway.https;


import ca.uhn.fhir.context.FhirContext;

import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import uk.nhs.careconnect.ri.gatewaylib.camel.interceptor.GatewayPostProcessor;
import uk.nhs.careconnect.ri.gatewaylib.camel.interceptor.GatewayPreProcessor;
import uk.nhs.careconnect.ri.gatewaylib.camel.processor.BinaryResource;
import uk.nhs.careconnect.ri.gatewaylib.camel.processor.BundleMessage;
import uk.nhs.careconnect.ri.gatewaylib.camel.processor.CompositionDocumentBundle;

import java.io.InputStream;

@Component
public class CamelRoute extends RouteBuilder {

	@Autowired
	protected Environment env;

	@Value("${fhir.restserver.serverBase}")
	private String serverBase;

	@Value("${fhir.restserver.edmsBase}")
	private String edmsBase;

	@Value("${fhir.restserver.oauth}")
	private String oauthBase;

	@Value("${fhir.restserver.tieBase}")
	private String tieBase;

	@Value("${fhir.resource.serverBase}")
	private String hapiBase;
	
    @Override
    public void configure() 
    {

		GatewayPreProcessor camelProcessor = new GatewayPreProcessor();

		GatewayPostProcessor camelPostProcessor = new GatewayPostProcessor();

		FhirContext ctx = FhirContext.forDstu3();
		BundleMessage bundleMessage = new BundleMessage(ctx);
		CompositionDocumentBundle compositionDocumentBundle = new CompositionDocumentBundle(ctx, hapiBase);

		BinaryResource binaryResource = new BinaryResource(ctx, hapiBase);
/*
		// OAuth endpoint

		restConfiguration()
				.component("servlet")
				.contextPath("oauth2")
				.dataFormatProperty("prettyPrint", "true")
				.enableCORS(false);


		rest("/").description("OAuth")
				.get("/{action}").to("direct:oauth2")
				.post("/{action}").to("direct:oauth2");

		from("direct:oauth2")
				.routeId("auth Server")
				.setHeader(Exchange.HTTP_PATH,simple("${header.action}"))
				.to("log:uk.nhs.careconnect.smartOnFhir.PRE?level=INFO&showHeaders=true&showExchangeId=true")
				.to(oauthBase)
				.process(camelPostProcessor)
				.to("log:uk.nhs.careconnect.smartOnFhir.POST?level=INFO&showHeaders=true&showExchangeId=true");
*/
		// Validation Service

		from("direct:FHIRBundleValidate")
				.routeId("Bundle Validation")
				.process(camelProcessor) // Add in correlation Id if not present
				.to("direct:TIEServer");

		// Complex processing

		from("direct:FHIRBundleCollection")
				.routeId("Bundle Collection Queue")
				.process(camelProcessor) // Add in correlation Id if not present
				.wireTap("seda:FHIRBundleCollection");

		// This bundle goes to the EDMS Server. See also Binary
		from("direct:FHIRBundleDocument")
				.routeId("Bundle Document")
				.process(camelProcessor) // Add in correlation Id if not present
				.enrich("direct:EDMSServer", compositionDocumentBundle);


		from("seda:FHIRBundleCollection")
				.routeId("Bundle Processing")
				.to("direct:FHIRDocumentReferenceBundle") //, documentReferenceDocumentBundle)
				.process(bundleMessage);

		from("direct:FHIRDocumentReferenceBundle")
				.routeId("Bundle Process Binary")
				.process(binaryResource);

		// Integration Server (TIE)

		from("direct:FHIREncounterDocument")
				.routeId("TIE Encounter")
				.to("direct:TIEServer");

		from("direct:TIEServer")
				.routeId("TIE FHIR Server")
				.process(camelProcessor)
				.to("log:uk.nhs.careconnect.FHIRGateway.start?level=INFO&showHeaders=true&showExchangeId=true")
				.to(tieBase)
				.process(camelPostProcessor)
				.to("log:uk.nhs.careconnect.FHIRGateway.complete?level=INFO&showHeaders=true&showExchangeId=true")
				.convertBodyTo(InputStream.class);


		// EPR Simple processing - low level resource operations

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

		from("direct:FHIRMedication")
				.routeId("Gateway Medication")
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
				.routeId("Int EDMS FHIR Server")
				.to("log:uk.nhs.careconnect.FHIRGateway.start?level=INFO&showHeaders=true&showExchangeId=true")
				.to(edmsBase)
				.process(camelPostProcessor)
				.to("log:uk.nhs.careconnect.FHIRGateway.complete?level=INFO&showHeaders=true&showExchangeId=true")
				.convertBodyTo(InputStream.class);

		from("direct:HAPIServer")
				.routeId("Int FHIR Server")
				.process(camelProcessor)
				.to("log:uk.nhs.careconnect.FHIRGateway.start?level=INFO&showHeaders=true&showExchangeId=true")
				.to(serverBase)
				.process(camelPostProcessor)
				.to("log:uk.nhs.careconnect.FHIRGateway.complete?level=INFO&showHeaders=true&showExchangeId=true")
				.convertBodyTo(InputStream.class);

	}
}
