package uk.nhs.careconnect.ri.lib.gateway.provider;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.api.Include;
import ca.uhn.fhir.rest.annotation.*;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.param.DateParam;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.exceptions.InternalErrorException;
import org.apache.camel.*;
import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.Slot;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.nhs.careconnect.ri.lib.server.ProviderResponseLibrary;

import javax.servlet.http.HttpServletRequest;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Component
public class SlotResourceProvider implements IResourceProvider {

    @Autowired
    CamelContext context;

    @Autowired
    FhirContext ctx;


    private static final Logger log = LoggerFactory.getLogger(SlotResourceProvider.class);

    @Override
    public Class<Slot> getResourceType() {
        return Slot.class;
    }


    @Read
    public Slot getSlotById(HttpServletRequest httpRequest, @IdParam IdType internalId) throws Exception {

        ProducerTemplate template = context.createProducerTemplate();


        Slot slot = null;
        IBaseResource resource = null;
        try {
            InputStream inputStream = null;
            if (httpRequest != null) {
                inputStream = (InputStream) template.sendBody("direct:FHIRSlot",
                        ExchangePattern.InOut, httpRequest);
            } else {
                Exchange exchange = template.send("direct:FHIRSlot",ExchangePattern.InOut, new Processor() {
                    public void process(Exchange exchange) throws Exception {
                        exchange.getIn().setHeader(Exchange.HTTP_QUERY, null);
                        exchange.getIn().setHeader(Exchange.HTTP_METHOD, "GET");
                        exchange.getIn().setHeader(Exchange.HTTP_PATH, "/"+internalId.getValue());
                    }
                });
                inputStream = (InputStream) exchange.getIn().getBody();
            }
            Reader reader = new InputStreamReader(inputStream);
            resource = ctx.newJsonParser().parseResource(reader);
        } catch(Exception ex) {
            log.error("JSON Parse failed " + ex.getMessage());
            throw new InternalErrorException(ex.getMessage());
        }
        if (resource instanceof Slot) {
            slot = (Slot) resource;
        } else {
            ProviderResponseLibrary.createException(ctx,resource);
        }
        return slot;
    }

    @Create
    public MethodOutcome create(HttpServletRequest httpRequest, @ResourceParam Slot slot) throws Exception {



        ProducerTemplate template = context.createProducerTemplate();

        IBaseResource resource = null;
        Exchange exchangeBundle = null;
        try {
            InputStream inputStream = null;
            String newXmlResource = ctx.newXmlParser().encodeResourceToString(slot);

            exchangeBundle = template.send("direct:FHIRSlot", ExchangePattern.InOut, new Processor() {
                public void process(Exchange exchange) throws Exception {
                    exchange.getIn().setBody(newXmlResource);
                    exchange.getIn().setHeader("Prefer", "return=representation");
                    exchange.getIn().setHeader(Exchange.CONTENT_TYPE, "application/fhir+xml");
                    exchange.getIn().setHeader(Exchange.HTTP_QUERY, null);
                    exchange.getIn().setHeader(Exchange.HTTP_METHOD, "POST");
                    exchange.getIn().setHeader(Exchange.HTTP_PATH, "Slot");
                }
            });

            // This response is coming from an external FHIR Server, so uses inputstream
            resource = ProviderResponseLibrary.processMessageBody(ctx,resource,exchangeBundle.getIn().getBody());

        } catch(Exception ex) {
            log.error("XML Parse failed " + ex.getMessage());
            if (exchangeBundle != null) {
                log.error("Error Response = " + exchangeBundle.getIn());
            }
            throw new InternalErrorException(ex.getMessage());
        }
        log.trace("RETURNED Resource "+resource.getClass().getSimpleName());

        if (resource instanceof Slot) {
            slot = (Slot) resource;
        } else {
            ProviderResponseLibrary.createException(ctx,resource);
        }

        MethodOutcome method = new MethodOutcome();

        ProviderResponseLibrary.setMethodOutcome(resource,method);

        return method;
    }

/*    @Search
    public List<Slot> searchSlot(HttpServletRequest httpRequest,

                                 @OptionalParam(name = Slot.SP_IDENTIFIER) TokenParam identifier,
                                 @OptionalParam(name = Slot.SP_START) DateParam start,
                                 @OptionalParam(name = Slot.SP_STATUS) StringParam status,
                                 @OptionalParam(name = Slot.SP_RES_ID) TokenParam id,
                                 @OptionalParam(name =Slot.SP_SCHEDULE) ReferenceParam schedule,
                                 @IncludeParam(allow = { "Slot:schedule", "Schedule:actor:service"}) Set<Include> theIncludes

    ) throws Exception
    {

        List<Slot> results = new ArrayList<>();

        ProducerTemplate template = context.createProducerTemplate();

        InputStream inputStream = (InputStream) template.sendBody("direct:FHIRSlot",
                ExchangePattern.InOut,httpRequest);

        Bundle bundle = null;
        Reader reader = new InputStreamReader(inputStream);
        IBaseResource resource = null;
        try {
            resource = ctx.newJsonParser().parseResource(reader);
        } catch(Exception ex) {
            log.error("JSON Parse failed " + ex.getMessage());
            throw new InternalErrorException(ex.getMessage());
        }
        if (resource instanceof Bundle) {
            bundle = (Bundle) resource;
            for (Bundle.BundleEntryComponent entry : bundle.getEntry()) {
                Slot patient = (Slot) entry.getResource();
                results.add(patient);
            }
        } else {
            ProviderResponseLibrary.createException(ctx,resource);
        }

        return results;

    }*/


    @Search
    public List<Slot> searchSlot(HttpServletRequest httpRequest,

                                 @OptionalParam(name = Slot.SP_IDENTIFIER) TokenParam identifier,
                                 @OptionalParam(name = Slot.SP_START) DateParam start,
                                 @OptionalParam(name = Slot.SP_STATUS) StringParam status,
                                 @OptionalParam(name = Slot.SP_RES_ID) TokenParam id,
                                 @OptionalParam(name =Slot.SP_SCHEDULE) ReferenceParam schedule,
                                 @IncludeParam(allow = { "Slot:schedule", "Schedule:actor:service"}) Set<Include> theIncludes

    ) throws Exception
    {

        List<Slot> results = new ArrayList<>();

        ProducerTemplate template = context.createProducerTemplate();

        InputStream inputStream = (InputStream) template.sendBody("direct:FHIRSlot",
                ExchangePattern.InOut,httpRequest);

        Bundle bundle = null;
        Reader reader = new InputStreamReader(inputStream);
        IBaseResource resource = null;
        try {
            resource = ctx.newJsonParser().parseResource(reader);
        } catch(Exception ex) {
            log.error("JSON Parse failed " + ex.getMessage());
            throw new InternalErrorException(ex.getMessage());
        }
        if (resource instanceof Bundle) {
            bundle = (Bundle) resource;
            for (Bundle.BundleEntryComponent entry : bundle.getEntry()) {
                Slot patient = (Slot) entry.getResource();
                results.add(patient);
            }
        } else {
            ProviderResponseLibrary.createException(ctx,resource);
        }

        return results;

    }


/*    @Search
    public Bundle getSlotByIds(@OptionalParam(name = "start") DateParam startDate,
                               @OptionalParam(name = "end") DateParam endDate,
                               @OptionalParam(name = "status") String status,
                               @IncludeParam(allow = { "Slot:schedule", "Schedule:actor:HealthcareService"}) Set<Include> theIncludes) {

        Bundle bundle = new Bundle();
        boolean actorHealthcareService = false;
        boolean actorLocation = false;
        String bookingOdsCode = "";
        String bookingOrgType = "";

*//*        if (!status.equals("free")) {
            throwInvalidParameterOperationalOutcome("Status incorrect: Must be equal to free");
        }*//*

*//*        try {
            startDate.isEmpty();
            endDate.isEmpty();
        } catch (Exception e) {
            throwInvalidParameterOperationalOutcome("Start Date and End Date must be populated with a correct date format");
        }*//*

*//*        if (startDate.getPrefix() != ParamPrefixEnum.GREATERTHAN_OR_EQUALS
                || endDate.getPrefix() != ParamPrefixEnum.LESSTHAN_OR_EQUALS) {
            throwInvalidParameterOperationalOutcome("Invalid Prefix used");

        }*//*

*//*        validateStartDateParamAndEndDateParam(startDate, endDate);*//*



*//*        try {
            bookingOdsCode.isEmpty();
            bookingOrgType.isEmpty();
        } catch (Exception e) {
            throwInvalidParameterOperationalOutcome("The ODS code and organisation type for the booking organisation must be supplied.");
        }*//*

        for (Include include : theIncludes) {

            if (include.getValue().equals("Schedule:actor:HealthcareService")) {
                actorHealthcareService = true;
            }

        }
        startDate.getValueAsInstantDt().getValue();
        getScheduleOperation.populateBundle(bundle, new OperationOutcome(), startDate.getValueAsInstantDt().getValue(),
                endDate.getValueAsInstantDt().getValue(), actorHealthcareService, bookingOdsCode, bookingOrgType);

        return bundle;

    }*/






}
