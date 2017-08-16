package uk.nhs.careconnectapi.camel;


import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.rest.RestBindingMode;
import org.apache.camel.model.rest.RestParamType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
@PropertySource("classpath:careconnectapi.properties")
public class Route extends RouteBuilder {

	@Autowired
	protected Environment env;
	
	
    @Override
    public void configure() 
    {
     	

	    restConfiguration()
	    	.component("servlet")
	    	.bindingMode(RestBindingMode.off)
	    	.contextPath("CareConnectAPI")
	    	.port(8080)
	    	.dataFormatProperty("prettyPrint","true");
	
		
	    rest("/")
			.description("Evaluation EDMS - Acting as a remote trusts EDMS for RLS Demo")

            .get("/MedicationOrder/{_id}")
                .description("Retrieve a MedicationOrder Resrouce")
                .route()
                .routeId("MedicationOrder Read")
                .to("direct:FHIRServer")
                .endRest()
            .get("/MedicationOrder/")
                .description("Search for a MedicationOrder")
                .route()
                .routeId("MedicationOrder Search")
                .process(new Processor() {
                    public void process(Exchange exchange) throws Exception {
                        exchange.getIn().setHeader(Exchange.HTTP_PATH, "MedicationOrder");
                    }})
                .to("direct:FHIRServer")
                .endRest()
            .get("/MedicationStatement/{_id}")
                .description("Retrieve a MedicationStatement Resrouce")
                .route()
                .routeId("MedicationStatement Read")
                .to("direct:FHIRServer")
                .endRest()
            .get("/MedicationStatement/")
                .description("Search for a MedicationStatement")
                .route()
                .routeId("MedicationStatement Search")
                .process(new Processor() {
                    public void process(Exchange exchange) throws Exception {
                        exchange.getIn().setHeader(Exchange.HTTP_PATH, "MedicationStatement");
                    }})
                .to("direct:FHIRServer")
                .endRest()
            .get("/Patient/{_id}")
                .description("Retrieve a Patient Resrouce")
                .route()
                .routeId("Patient Read")
                .to("direct:FHIRServer")
                .endRest()
            .get("/Patient/")
                .description("Search for a Patient")
                .param().type(RestParamType.path).required(false).name("identifier").dataType("string").endParam()
                .param().type(RestParamType.path).required(false).name("address-postcode").dataType("string").endParam()
                .param().type(RestParamType.path).required(false).name("birthdate").dataType("string").endParam()
                .param().type(RestParamType.path).required(false).name("email").dataType("string").endParam()
                .param().type(RestParamType.path).required(false).name("family").dataType("string").endParam()
                .param().type(RestParamType.path).required(false).name("gender").dataType("string").endParam()
                .param().type(RestParamType.path).required(false).name("given").dataType("string").endParam()
                .param().type(RestParamType.path).required(false).name("name").dataType("string").endParam()
                .param().type(RestParamType.path).required(false).name("phone").dataType("string").endParam()
                .route()
                .routeId("Patient Search")
                .process(new Processor() {
                    public void process(Exchange exchange) throws Exception {
                        exchange.getIn().setHeader(Exchange.HTTP_PATH, "Patient");
                    }})
                .to("direct:FHIRServer")
                .endRest()
            .get("/Practitioner/{_id}")
                .description("Retrieve a Practitioner Resrouce")
                .route()
                .routeId("Practitioner Read")
                .to("direct:FHIRServer")
                .endRest()
            .get("/Practitioner/")
                .description("Search for a Practitioner")
                .route()
                .routeId("Practitioner Search")
                .process(new Processor() {
                    public void process(Exchange exchange) throws Exception {
                        exchange.getIn().setHeader(Exchange.HTTP_PATH, "Practitioner");
                    }})
                .to("direct:FHIRServer")
                .endRest();
	
		
		from("direct:FHIRServer")
			.routeId("FHIR Repository")
			.to("log:uk.nhs.careconnectapi?level=INFO&showHeaders=true&showHeaders=true")
			.to("http://localhost:8080/FHIRServer/DSTU2?throwExceptionOnFailure=false&bridgeEndpoint=true");
			

    }
}
