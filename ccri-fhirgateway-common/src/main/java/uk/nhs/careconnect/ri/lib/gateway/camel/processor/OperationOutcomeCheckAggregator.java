package uk.nhs.careconnect.ri.lib.gateway.camel.processor;

import ca.uhn.fhir.context.FhirContext;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.processor.aggregate.AggregationStrategy;
import org.hl7.fhir.dstu3.model.*;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class OperationOutcomeCheckAggregator implements AggregationStrategy {


    /*

    This takes the original documentBundle, send the FHIR Document to EDMS
    and then takes the response, converts to a DocumentReference and updates
     the DocumentIndex server (ccri - EPR)

     */
    public OperationOutcomeCheckAggregator(FhirContext ctx, String hapiBase) {
        this.ctx = ctx;
        this.hapiBase = hapiBase;
    }

    CamelContext context;

    FhirContext ctx;
    private Map<String,Resource> resourceMap;

    private OperationOutcome operationOutcome;

    private static final Logger log = LoggerFactory.getLogger(OperationOutcomeCheckAggregator.class);

    private String hapiBase;

    @Override
    public Exchange aggregate(Exchange originalExchange, Exchange processExchange) {

        this.context = originalExchange.getContext();


        try {
            IBaseResource resource = ctx.newXmlParser().parseResource((String) processExchange.getIn().getBody());

            if (resource instanceof OperationOutcome) {
                operationOutcome = (OperationOutcome) resource;

                processExchange.getIn().setHeader(Exchange.HTTP_RESPONSE_CODE,422);
                return processExchange;
            } else {
                if (processExchange.getIn().getHeader(Exchange.HTTP_RESPONSE_CODE) != null) {
                    originalExchange.getIn().setHeader(Exchange.HTTP_RESPONSE_CODE, processExchange.getIn().getHeader(Exchange.HTTP_RESPONSE_CODE));
                } else {
                    originalExchange.getIn().setHeader(Exchange.HTTP_RESPONSE_CODE, 200);
                }
            }
        } catch (Exception ex) {
            log.error(ex.getMessage());
            originalExchange.getIn().setHeader(Exchange.HTTP_RESPONSE_CODE,500);
        }

        return originalExchange;
    }
}
