package uk.nhs.careconnect.ri.gatewaylib.provider;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.annotation.*;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.exceptions.InternalErrorException;
import org.apache.camel.*;
import org.hl7.fhir.dstu3.model.*;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.nhs.careconnect.ri.lib.OperationOutcomeFactory;

import javax.servlet.http.HttpServletRequest;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class BundleResourceProvider implements IResourceProvider {

    @Autowired
    CamelContext context;

    @Autowired
    FhirContext ctx;

    private Map<String,Resource> resourceMap;

    private Bundle bundle;

    private static final Logger log = LoggerFactory.getLogger(BundleResourceProvider.class);

    @Override
    public Class<Bundle> getResourceType() {
        return Bundle.class;
    }

    @Create
    public MethodOutcome create(HttpServletRequest httpRequest, @ResourceParam Bundle bundle) {


        // Example message https://gist.github.com/IOPS-DEV/1a532eb43b226dcd6ce26a6b698019f4#file-ec_edischarge_full_payload_example-01

        resourceMap = new HashMap<>();

        MethodOutcome method = new MethodOutcome();
        method.setCreated(true);
        this.bundle = bundle;

        // Process resources
        for (Bundle.BundleEntryComponent entry : bundle.getEntry()) {
            Resource resource = entry.getResource();

            // Look for existing resources. Ideally we should not need to add Patient, Practitioner, Organization, etc
            // These should be using well known identifiers and ideally will be present on our system.

            searchAddResource(resource.getId());

        }

        OperationOutcome opOutcome = new OperationOutcome();

        method.setOperationOutcome(opOutcome);


        return method;
    }

    public Reference getReference(Resource resource) {
        Reference reference = new Reference();
        if (resource instanceof Practitioner) { reference.setReference("Practitioner/"+resource.getId()); }
        else if (resource instanceof Patient) { reference.setReference("Patient/"+resource.getId()); }
        else if (resource instanceof Organization) { reference.setReference("Organization/"+resource.getId()); }
        else if (resource instanceof Encounter) { reference.setReference("Encounter/"+resource.getId()); }
        else if (resource instanceof Location) { reference.setReference("Location/"+resource.getId()); }
        else if (resource instanceof Observation) { reference.setReference("Observation/"+resource.getId()); }
        return reference;
    }

    public Resource searchAddResource(String referenceId) {

        Resource resource = resourceMap.get(referenceId);
        // Don't process, if already processed.
        if (resource !=null) return resource;

        for (Bundle.BundleEntryComponent entry : bundle.getEntry()) {
            if (entry.getResource().getId().equals(referenceId)) {
                Resource iResource = entry.getResource();
                if (iResource instanceof Patient) { resource = searchAddPatient(referenceId); }
                else if (iResource instanceof Practitioner) { resource = searchAddPractitioner(referenceId); }
                else if (iResource instanceof Encounter) { resource = searchAddEncounter(referenceId); }
                else if (iResource instanceof Organization) { resource = searchAddOrganisation(referenceId); }
                else if (iResource instanceof Location) { resource = searchAddLocation(referenceId); }
                else if (iResource instanceof Observation) { resource = searchAddObservation(referenceId); }
                else if (iResource instanceof AllergyIntolerance) { resource = searchAddAllergyIntolerance(referenceId); }
                else if (iResource instanceof Condition) { resource = searchAddCondition(referenceId); }
                else if (iResource instanceof Procedure) { resource = searchAddProcedure(referenceId); }
                else if (iResource instanceof Composition) { resource = searchAddComposition(referenceId); }
                else {
                    log.info( "Not processed " + iResource.getClass());
                }
            }
        }
        return resource;
    }

    public Practitioner searchAddPractitioner(String practitionerId) {

        log.info("Practitioner searchAdd " +practitionerId);
        Practitioner practitioner = null;
        for (Bundle.BundleEntryComponent entry : bundle.getEntry()) {
            if (entry.getResource().getId().equals(practitionerId)) {
                practitioner = (Practitioner) entry.getResource();
            }
        }
        if (practitioner == null) throw new InternalErrorException("Bundle processing error");

        Practitioner eprPractitioner = (Practitioner) resourceMap.get(practitionerId);

        // Practitioner already processed, quit with Practitioner
        if (eprPractitioner != null) return eprPractitioner;

        ProducerTemplate template = context.createProducerTemplate();

        InputStream inputStream = null;

        // Prevent re-adding the same Practitioner
        if (practitioner.getIdentifier().size() == 0) {
            practitioner.addIdentifier()
                    .setSystem("urn:uuid")
                    .setValue(practitioner.getId());
        }

        for (Identifier identifier : practitioner.getIdentifier()) {
            Exchange exchange = template.send("direct:FHIRPractitioner", ExchangePattern.InOut, new Processor() {
                public void process(Exchange exchange) throws Exception {
                    exchange.getIn().setHeader(Exchange.HTTP_QUERY, "?identifier=" + identifier.getSystem() + "|" + identifier.getValue());
                    exchange.getIn().setHeader(Exchange.HTTP_METHOD, "GET");
                    exchange.getIn().setHeader(Exchange.HTTP_PATH, "Practitioner");
                }
            });
            inputStream = (InputStream) exchange.getIn().getBody();
            Reader reader = new InputStreamReader(inputStream);
            IBaseResource iresource = null;
            try {
                iresource = ctx.newJsonParser().parseResource(reader);
            } catch(Exception ex) {
                log.error("JSON Parse failed " + ex.getMessage());
                throw new InternalErrorException(ex.getMessage());
            }
            if (iresource instanceof Bundle) {
                Bundle returnedBundle = (Bundle) iresource;
                if (returnedBundle.getEntry().size() > 0) {
                    eprPractitioner = (Practitioner) returnedBundle.getEntry().get(0).getResource();
                    log.info("Found Practitioner = " + eprPractitioner.getId());
                }
            }
        }


        if (eprPractitioner != null) {
            resourceMap.put(practitionerId,eprPractitioner);
            return eprPractitioner;
        }

        // Practitioner not found. Add to database


        IBaseResource iResource = null;
        String jsonResource = ctx.newJsonParser().encodeResourceToString(practitioner);
        try {
            Exchange exchange = template.send("direct:FHIRPractitioner", ExchangePattern.InOut, new Processor() {
                public void process(Exchange exchange) throws Exception {
                    exchange.getIn().setHeader(Exchange.HTTP_QUERY, "");
                    exchange.getIn().setHeader(Exchange.HTTP_METHOD, "POST");
                    exchange.getIn().setHeader(Exchange.HTTP_PATH, "Practitioner");
                    exchange.getIn().setHeader(Exchange.CONTENT_TYPE, "application/fhir+json");
                    exchange.getIn().setBody(jsonResource);
                }
            });
            inputStream = (InputStream) exchange.getIn().getBody();

            Reader reader = new InputStreamReader(inputStream);
            iResource = ctx.newJsonParser().parseResource(reader);
        } catch(Exception ex) {
            log.error("JSON Parse failed " + ex.getMessage());
            throw new InternalErrorException(ex.getMessage());
        }
        if (iResource instanceof Practitioner) {
            eprPractitioner = (Practitioner) iResource;
            if (resourceMap.get(practitionerId) != null) { resourceMap.replace(practitionerId,eprPractitioner); }
            else { resourceMap.put(practitionerId,eprPractitioner); }

        } else if (iResource instanceof OperationOutcome)
        {
            OperationOutcome operationOutcome = (OperationOutcome) iResource;
            log.info("Sever Returned: "+ctx.newJsonParser().encodeResourceToString(operationOutcome));
            OperationOutcomeFactory.convertToException(operationOutcome);
        } else {
            throw new InternalErrorException("Unknown Error");
        }

        return eprPractitioner;
    }

    public Organization searchAddOrganisation(String organisationId) {
        log.info("Orgnisation searchAdd " +organisationId);
        Organization organisation = null;
        for (Bundle.BundleEntryComponent entry : bundle.getEntry()) {
            if (entry.getResource().getId().equals(organisationId)) {
                organisation = (Organization) entry.getResource();
            }
        }
        if (organisation == null) throw new InternalErrorException("Bundle processing error");

        Organization eprOrganization = (Organization) resourceMap.get(organisationId);

        // Organization already processed, quit with Organization
        if (eprOrganization != null) return eprOrganization;

        ProducerTemplate template = context.createProducerTemplate();

        InputStream inputStream = null;

        for (Identifier identifier : organisation.getIdentifier()) {
            Exchange exchange = template.send("direct:FHIROrganisation", ExchangePattern.InOut, new Processor() {
                public void process(Exchange exchange) throws Exception {
                    exchange.getIn().setHeader(Exchange.HTTP_QUERY, "?identifier=" + identifier.getSystem() + "|" + identifier.getValue());
                    exchange.getIn().setHeader(Exchange.HTTP_METHOD, "GET");
                    exchange.getIn().setHeader(Exchange.HTTP_PATH, "Organization");
                }
            });
            inputStream = (InputStream) exchange.getIn().getBody();
            Reader reader = new InputStreamReader(inputStream);
            IBaseResource iresource = null;
            try {
                iresource = ctx.newJsonParser().parseResource(reader);
            } catch(Exception ex) {
                log.error("JSON Parse failed " + ex.getMessage());
                throw new InternalErrorException(ex.getMessage());
            }
            if (iresource instanceof Bundle) {
                Bundle returnedBundle = (Bundle) iresource;
                if (returnedBundle.getEntry().size()>0) {
                    eprOrganization = (Organization) returnedBundle.getEntry().get(0).getResource();
                    log.info("Found Organization = " + eprOrganization.getId());
                }
            }
        }
        // Organization found do not add
        if (eprOrganization != null) {
            resourceMap.put(organisationId,eprOrganization);
            return eprOrganization;
        }

        // Organization not found. Add to database

        if (organisation.getPartOf().getReference() != null) {
            Organization partOfOrganisation = searchAddOrganisation(organisation.getPartOf().getReference());
            log.info("Found PartOfOrganization = "+partOfOrganisation.getId());
            organisation.setPartOf(new Reference("Organization/"+partOfOrganisation.getId()));
        }

        IBaseResource iResource = null;
        String jsonResource = ctx.newJsonParser().encodeResourceToString(organisation);
        Exchange exchange = null;
        try {
            exchange = template.send("direct:FHIROrganisation", ExchangePattern.InOut, new Processor() {
                public void process(Exchange exchange) throws Exception {
                    exchange.getIn().setHeader(Exchange.HTTP_QUERY, "");
                    exchange.getIn().setHeader(Exchange.HTTP_METHOD, "POST");
                    exchange.getIn().setHeader(Exchange.HTTP_PATH, "Organization");
                    exchange.getIn().setHeader(Exchange.CONTENT_TYPE, "application/fhir+json");
                    exchange.getIn().setBody(jsonResource);
                }
            });
            inputStream = (InputStream) exchange.getIn().getBody();

            Reader reader = new InputStreamReader(inputStream);
            iResource = ctx.newJsonParser().parseResource(reader);
        } catch(Exception ex) {
            if (exchange !=null) {
                try {
                    String string = (String) exchange.getIn().getBody();
                    log.error("JSON Parse part 1 = "+string);
                } catch (Exception ex1) {

                }
            }
            log.error("JSON Parse failed " + ex.getMessage());
            throw new InternalErrorException(ex.getMessage());
        }
        if (iResource instanceof Organization) {
            eprOrganization = (Organization) iResource;
            if (resourceMap.get(organisationId) != null) { resourceMap.replace(organisationId,eprOrganization); }
            else { resourceMap.put(organisationId,eprOrganization); }

        } else if (iResource instanceof OperationOutcome)
        {
            OperationOutcome operationOutcome = (OperationOutcome) iResource;
            log.info("Sever Returned: "+ctx.newJsonParser().encodeResourceToString(operationOutcome));
            OperationOutcomeFactory.convertToException(operationOutcome);
        } else {
            throw new InternalErrorException("Unknown Error");
        }


        return eprOrganization;
    }

    public Location searchAddLocation(String locationId) {
        log.info("Location searchAdd " +locationId);
        Location location = null;
        for (Bundle.BundleEntryComponent entry : bundle.getEntry()) {
            if (entry.getResource().getId().equals(locationId)) {
                location = (Location) entry.getResource();
            }
        }
        if (location == null) throw new InternalErrorException("Bundle processing error");

        Location eprLocation = (Location) resourceMap.get(locationId);

        // Organization already processed, quit with Organization
        if (eprLocation != null) return eprLocation;

        // Prevent re-adding the same Practitioner
        if (location.getIdentifier().size() == 0) {
            location.addIdentifier()
                    .setSystem("urn:uuid")
                    .setValue(location.getId());
        }

        ProducerTemplate template = context.createProducerTemplate();

        InputStream inputStream = null;

        for (Identifier identifier : location.getIdentifier()) {
            Exchange exchange = template.send("direct:FHIRLocation", ExchangePattern.InOut, new Processor() {
                public void process(Exchange exchange) throws Exception {
                    exchange.getIn().setHeader(Exchange.HTTP_QUERY, "?identifier=" + identifier.getSystem() + "|" + identifier.getValue());
                    exchange.getIn().setHeader(Exchange.HTTP_METHOD, "GET");
                    exchange.getIn().setHeader(Exchange.HTTP_PATH, "Location");
                }
            });
            inputStream = (InputStream) exchange.getIn().getBody();
            Reader reader = new InputStreamReader(inputStream);
            IBaseResource iresource = null;
            try {
                iresource = ctx.newJsonParser().parseResource(reader);
            } catch(Exception ex) {
                log.error("JSON Parse failed " + ex.getMessage());
                throw new InternalErrorException(ex.getMessage());
            }
            if (iresource instanceof Bundle) {
                Bundle returnedBundle = (Bundle) iresource;
                if (returnedBundle.getEntry().size()>0) {
                    eprLocation = (Location) returnedBundle.getEntry().get(0).getResource();
                    log.info("Found Location = " + eprLocation.getId());
                }
            }
        }
        // Location found do not add
        if (eprLocation != null) {
            resourceMap.put(locationId,eprLocation);
            return eprLocation;
        }

        // Location not found. Add to database

        if (location.getManagingOrganization().getReference() != null) {
            Resource resource = searchAddResource(location.getManagingOrganization().getReference());
            location.setManagingOrganization(getReference(resource));
        }

        IBaseResource iResource = null;
        String jsonResource = ctx.newJsonParser().encodeResourceToString(location);
        try {
            Exchange exchange = template.send("direct:FHIRLocation", ExchangePattern.InOut, new Processor() {
                public void process(Exchange exchange) throws Exception {
                    exchange.getIn().setHeader(Exchange.HTTP_QUERY, "");
                    exchange.getIn().setHeader(Exchange.HTTP_METHOD, "POST");
                    exchange.getIn().setHeader(Exchange.HTTP_PATH, "Location");
                    exchange.getIn().setHeader(Exchange.CONTENT_TYPE, "application/fhir+json");
                    exchange.getIn().setBody(jsonResource);
                }
            });
            inputStream = (InputStream) exchange.getIn().getBody();

            Reader reader = new InputStreamReader(inputStream);
            iResource = ctx.newJsonParser().parseResource(reader);
        } catch(Exception ex) {
            log.error("JSON Parse failed " + ex.getMessage());
            throw new InternalErrorException(ex.getMessage());
        }
        if (iResource instanceof Location) {
            eprLocation = (Location) iResource;
            if (resourceMap.get(locationId) != null) { resourceMap.replace(locationId,eprLocation); }
            else { resourceMap.put(locationId,eprLocation); }


        } else if (iResource instanceof OperationOutcome)
        {
            OperationOutcome operationOutcome = (OperationOutcome) iResource;
            log.info("Sever Returned: "+ctx.newJsonParser().encodeResourceToString(operationOutcome));
            OperationOutcomeFactory.convertToException(operationOutcome);
        } else {
            throw new InternalErrorException("Unknown Error");
        }

        return eprLocation;
    }

    public AllergyIntolerance searchAddAllergyIntolerance(String allergyIntoleranceId) {
        log.info("AllergyIntolerance searchAdd " +allergyIntoleranceId);
        AllergyIntolerance allergyIntolerance = null;
        for (Bundle.BundleEntryComponent entry : bundle.getEntry()) {
            if (entry.getResource().getId().equals(allergyIntoleranceId)) {
                allergyIntolerance = (AllergyIntolerance) entry.getResource();
            }
        }
        if (allergyIntolerance == null) throw new InternalErrorException("Bundle processing error");

        AllergyIntolerance eprAllergyIntolerance = (AllergyIntolerance) resourceMap.get(allergyIntoleranceId);

        // Organization already processed, quit with Organization
        if (eprAllergyIntolerance != null) return eprAllergyIntolerance;

        // Prevent re-adding the same Practitioner
        if (allergyIntolerance.getIdentifier().size() == 0) {
            allergyIntolerance.addIdentifier()
                    .setSystem("urn:uuid")
                    .setValue(allergyIntolerance.getId());
        }

        ProducerTemplate template = context.createProducerTemplate();

        InputStream inputStream = null;

        for (Identifier identifier : allergyIntolerance.getIdentifier()) {
            Exchange exchange = template.send("direct:FHIRAllergyIntolerance", ExchangePattern.InOut, new Processor() {
                public void process(Exchange exchange) throws Exception {
                    exchange.getIn().setHeader(Exchange.HTTP_QUERY, "?identifier=" + identifier.getSystem() + "|" + identifier.getValue());
                    exchange.getIn().setHeader(Exchange.HTTP_METHOD, "GET");
                    exchange.getIn().setHeader(Exchange.HTTP_PATH, "AllergyIntolerance");
                }
            });
            inputStream = (InputStream) exchange.getIn().getBody();
            Reader reader = new InputStreamReader(inputStream);
            IBaseResource iresource = null;
            try {
                iresource = ctx.newJsonParser().parseResource(reader);
            } catch(Exception ex) {
                log.error("JSON Parse failed " + ex.getMessage());
                throw new InternalErrorException(ex.getMessage());
            }
            if (iresource instanceof Bundle) {
                Bundle returnedBundle = (Bundle) iresource;
                if (returnedBundle.getEntry().size()>0) {
                    eprAllergyIntolerance = (AllergyIntolerance) returnedBundle.getEntry().get(0).getResource();
                    log.info("Found AllergyIntolerance = " + eprAllergyIntolerance.getId());
                }
            }
        }

        if (allergyIntolerance.getAsserter().getReference() != null) {

            Resource resource = searchAddResource(allergyIntolerance.getAsserter().getReference());
            log.info("Found Practitioner = " + resource.getId());
            allergyIntolerance.setAsserter(getReference(resource));

        }
        if (allergyIntolerance.getPatient() != null) {
            Resource resource = searchAddResource(allergyIntolerance.getPatient().getReference());
            allergyIntolerance.setPatient(getReference(resource));
        }

        IBaseResource iResource = null;
        String xhttpMethod = "POST";
        String xhttpPath = "AllergyIntolerance";
        // Location found do not add
        if (eprAllergyIntolerance != null) {
            xhttpMethod="PUT";
            // Want id value, no path or resource
            xhttpPath = "AllergyIntolerance/"+eprAllergyIntolerance.getIdElement().getIdPart();
            allergyIntolerance.setId(eprAllergyIntolerance.getId());
        }
        String httpBody = ctx.newJsonParser().encodeResourceToString(allergyIntolerance);
        String httpMethod= xhttpMethod;
        String httpPath = xhttpPath;
        try {
            Exchange exchange = template.send("direct:FHIRAllergyIntolerance", ExchangePattern.InOut, new Processor() {
                public void process(Exchange exchange) throws Exception {
                    exchange.getIn().setHeader(Exchange.HTTP_QUERY, "");
                    exchange.getIn().setHeader(Exchange.HTTP_METHOD, httpMethod);
                    exchange.getIn().setHeader(Exchange.HTTP_PATH, httpPath);
                    exchange.getIn().setHeader(Exchange.CONTENT_TYPE, "application/fhir+json");
                    exchange.getIn().setBody(httpBody);
                }
            });
            inputStream = (InputStream) exchange.getIn().getBody();

            Reader reader = new InputStreamReader(inputStream);
            iResource = ctx.newJsonParser().parseResource(reader);
        } catch(Exception ex) {
            log.error("JSON Parse failed " + ex.getMessage());
            throw new InternalErrorException(ex.getMessage());
        }
        if (iResource instanceof AllergyIntolerance) {
            eprAllergyIntolerance = (AllergyIntolerance) iResource;
            if (resourceMap.get(allergyIntoleranceId) != null) { resourceMap.replace(allergyIntoleranceId,eprAllergyIntolerance); }
            else { resourceMap.put(allergyIntoleranceId,eprAllergyIntolerance); }

        } else if (iResource instanceof OperationOutcome)
        {
            OperationOutcome operationOutcome = (OperationOutcome) iResource;
            log.info("Sever Returned: "+ctx.newJsonParser().encodeResourceToString(operationOutcome));
            OperationOutcomeFactory.convertToException(operationOutcome);
        } else {
            throw new InternalErrorException("Unknown Error");
        }

        return eprAllergyIntolerance;
    }

    public Observation searchAddObservation(String observationId) {
        log.info("Observation searchAdd " +observationId);
        Observation observation = null;
        for (Bundle.BundleEntryComponent entry : bundle.getEntry()) {
            if (entry.getResource().getId().equals(observationId)) {
                observation = (Observation) entry.getResource();
            }
        }
        if (observation == null) throw new InternalErrorException("Bundle processing error");

        Observation eprObservation = (Observation) resourceMap.get(observationId);

        // Organization already processed, quit with Organization
        if (eprObservation != null) return eprObservation;

        // Prevent re-adding the same Practitioner
        if (observation.getIdentifier().size() == 0) {
            observation.addIdentifier()
                    .setSystem("urn:uuid")
                    .setValue(observation.getId());
        }

        ProducerTemplate template = context.createProducerTemplate();

        InputStream inputStream = null;

        for (Identifier identifier : observation.getIdentifier()) {
            Exchange exchange = template.send("direct:FHIRObservation", ExchangePattern.InOut, new Processor() {
                public void process(Exchange exchange) throws Exception {
                    exchange.getIn().setHeader(Exchange.HTTP_QUERY, "?identifier=" + identifier.getSystem() + "|" + identifier.getValue());
                    exchange.getIn().setHeader(Exchange.HTTP_METHOD, "GET");
                    exchange.getIn().setHeader(Exchange.HTTP_PATH, "Observation");
                }
            });
            inputStream = (InputStream) exchange.getIn().getBody();
            Reader reader = new InputStreamReader(inputStream);
            IBaseResource iresource = null;
            try {
                iresource = ctx.newJsonParser().parseResource(reader);
            } catch(Exception ex) {
                log.error("JSON Parse failed " + ex.getMessage());
                throw new InternalErrorException(ex.getMessage());
            }
            if (iresource instanceof Bundle) {
                Bundle returnedBundle = (Bundle) iresource;
                if (returnedBundle.getEntry().size()>0) {
                    eprObservation = (Observation) returnedBundle.getEntry().get(0).getResource();
                    log.info("Found Observation = " + eprObservation.getId());
                }
            }
        }


        // Location not found. Add to database

        List<Reference> performers = new ArrayList<>();
        for (Reference reference : observation.getPerformer()) {
            Resource resource = searchAddResource(reference.getReference());
            if (resource!=null) performers.add(getReference(resource));
        }
        observation.setPerformer(performers);

        if (observation.hasSubject()) {
            Resource resource = searchAddResource(observation.getSubject().getReference());
            observation.setSubject(getReference(resource));
        }
        if (observation.hasContext()) {
            Resource resource = searchAddResource(observation.getContext().getReference());
            observation.setContext(getReference(resource));
        }

        IBaseResource iResource = null;

        String xhttpMethod = "POST";
        String xhttpPath = "Observation";
        // Location found do not add
        if (eprObservation != null) {
            xhttpMethod="PUT";
            // Want id value, no path or resource
            xhttpPath = "Observation/"+eprObservation.getIdElement().getIdPart();
            observation.setId(eprObservation.getId());
        }
        String httpBody = ctx.newJsonParser().encodeResourceToString(observation);
        String httpMethod= xhttpMethod;
        String httpPath = xhttpPath;
        try {
            Exchange exchange = template.send("direct:FHIRObservation", ExchangePattern.InOut, new Processor() {
                public void process(Exchange exchange) throws Exception {
                    exchange.getIn().setHeader(Exchange.HTTP_QUERY, "");
                    exchange.getIn().setHeader(Exchange.HTTP_METHOD, httpMethod);
                    exchange.getIn().setHeader(Exchange.HTTP_PATH, httpPath);
                    exchange.getIn().setHeader(Exchange.CONTENT_TYPE, "application/fhir+json");
                    exchange.getIn().setBody(httpBody);
                }
            });
            inputStream = (InputStream) exchange.getIn().getBody();

            Reader reader = new InputStreamReader(inputStream);
            iResource = ctx.newJsonParser().parseResource(reader);
        } catch(Exception ex) {
            log.error("JSON Parse failed " + ex.getMessage());
            throw new InternalErrorException(ex.getMessage());
        }
        if (iResource instanceof Observation) {
            eprObservation = (Observation) iResource;
            if (resourceMap.get(observationId) != null) { resourceMap.replace(observationId,eprObservation); }
            else { resourceMap.put(observationId,eprObservation); }

        } else if (iResource instanceof OperationOutcome)
        {
            OperationOutcome operationOutcome = (OperationOutcome) iResource;
            log.info("Sever Returned: "+ctx.newJsonParser().encodeResourceToString(operationOutcome));
            OperationOutcomeFactory.convertToException(operationOutcome);
        } else {
            throw new InternalErrorException("Unknown Error");
        }

        return eprObservation;
    }

    public Condition searchAddCondition(String conditionId) {
        log.info("Condition searchAdd " +conditionId);
        Condition condition = null;
        for (Bundle.BundleEntryComponent entry : bundle.getEntry()) {
            if (entry.getResource().getId().equals(conditionId)) {
                condition = (Condition) entry.getResource();
            }
        }
        if (condition == null) throw new InternalErrorException("Bundle processing error");

        Condition eprCondition = (Condition) resourceMap.get(conditionId);

        // Organization already processed, quit with Organization
        if (eprCondition != null) return eprCondition;

        // Prevent re-adding the same Practitioner
        if (condition.getIdentifier().size() == 0) {
            condition.addIdentifier()
                    .setSystem("urn:uuid")
                    .setValue(condition.getId());
        }

        ProducerTemplate template = context.createProducerTemplate();

        InputStream inputStream = null;

        for (Identifier identifier : condition.getIdentifier()) {
            Exchange exchange = template.send("direct:FHIRCondition", ExchangePattern.InOut, new Processor() {
                public void process(Exchange exchange) throws Exception {
                    exchange.getIn().setHeader(Exchange.HTTP_QUERY, "?identifier=" + identifier.getSystem() + "|" + identifier.getValue());
                    exchange.getIn().setHeader(Exchange.HTTP_METHOD, "GET");
                    exchange.getIn().setHeader(Exchange.HTTP_PATH, "Condition");
                }
            });
            inputStream = (InputStream) exchange.getIn().getBody();
            Reader reader = new InputStreamReader(inputStream);
            IBaseResource iresource = null;
            try {
                iresource = ctx.newJsonParser().parseResource(reader);
            } catch(Exception ex) {
                log.error("JSON Parse failed " + ex.getMessage());
                throw new InternalErrorException(ex.getMessage());
            }
            if (iresource instanceof Bundle) {
                Bundle returnedBundle = (Bundle) iresource;
                if (returnedBundle.getEntry().size()>0) {
                    eprCondition = (Condition) returnedBundle.getEntry().get(0).getResource();
                    log.info("Found Condition = " + eprCondition.getId());
                }
            }
        }


        if (condition.getAsserter().getReference() != null) {

                Resource resource = searchAddResource(condition.getAsserter().getReference());
                log.info("Found Resource = " + resource.getId());
                condition.setAsserter(getReference(resource));
        }
        if (condition.getSubject() != null) {
            Resource resource = searchAddResource(condition.getSubject().getReference());
            condition.setSubject(getReference(resource));
        }
        if (condition.getContext().getReference() != null) {
            Resource resource = searchAddResource(condition.getContext().getReference());
            condition.setContext(getReference(resource));
        }

        IBaseResource iResource = null;
        String xhttpMethod = "POST";
        String xhttpPath = "Condition";
        // Location found do not add
        if (eprCondition != null) {
            xhttpMethod="PUT";
            // Want id value, no path or resource
            xhttpPath = "Condition/"+eprCondition.getIdElement().getIdPart();
            condition.setId(eprCondition.getId());
        }
        String httpBody = ctx.newJsonParser().encodeResourceToString(condition);
        String httpMethod= xhttpMethod;
        String httpPath = xhttpPath;
        try {
            Exchange exchange = template.send("direct:FHIRCondition", ExchangePattern.InOut, new Processor() {
                public void process(Exchange exchange) throws Exception {
                    exchange.getIn().setHeader(Exchange.HTTP_QUERY, "");
                    exchange.getIn().setHeader(Exchange.HTTP_METHOD, httpMethod);
                    exchange.getIn().setHeader(Exchange.HTTP_PATH, httpPath);
                    exchange.getIn().setHeader(Exchange.CONTENT_TYPE, "application/fhir+json");
                    exchange.getIn().setBody(httpBody);
                }
            });
            inputStream = (InputStream) exchange.getIn().getBody();

            Reader reader = new InputStreamReader(inputStream);
            iResource = ctx.newJsonParser().parseResource(reader);
        } catch(Exception ex) {
            log.error("JSON Parse failed " + ex.getMessage());
            throw new InternalErrorException(ex.getMessage());
        }
        if (iResource instanceof Condition) {
            eprCondition = (Condition) iResource;
            if (resourceMap.get(conditionId) != null) { resourceMap.replace(conditionId,eprCondition); }
            else { resourceMap.put(conditionId,eprCondition); }


        } else if (iResource instanceof OperationOutcome)
        {
            OperationOutcome operationOutcome = (OperationOutcome) iResource;
            log.info("Sever Returned: "+ctx.newJsonParser().encodeResourceToString(operationOutcome));
            OperationOutcomeFactory.convertToException(operationOutcome);
        } else {
            throw new InternalErrorException("Unknown Error");
        }

        return eprCondition;
    }

    public Composition searchAddComposition(String compositionId) {
        log.info("Composition searchAdd " +compositionId);
        Composition composition = null;
        for (Bundle.BundleEntryComponent entry : bundle.getEntry()) {
            if (entry.getResource().getId().equals(compositionId)) {
                composition = (Composition) entry.getResource();
            }
        }
        if (composition == null) throw new InternalErrorException("Bundle processing error");

        Composition eprComposition = (Composition) resourceMap.get(compositionId);

        // Organization already processed, quit with Organization
        if (eprComposition != null) return eprComposition;

        // Prevent re-adding the same Practitioner
        if (composition.getIdentifier() == null) {
            composition.getIdentifier()
                    .setSystem("urn:uuid")
                    .setValue(composition.getId());
        } else {
            if (composition.getIdentifier().getSystem() == null) {
                composition.getIdentifier()
                        .setSystem("urn:uuid");
            }
        }

        ProducerTemplate template = context.createProducerTemplate();

        InputStream inputStream = null;

        String identifierUrl = "?identifier=" + composition.getIdentifier().getSystem() + "|" + composition.getIdentifier().getValue();
        Exchange exchange = template.send("direct:FHIRComposition", ExchangePattern.InOut, new Processor() {
            public void process(Exchange exchange) throws Exception {
                exchange.getIn().setHeader(Exchange.HTTP_QUERY, identifierUrl);
                exchange.getIn().setHeader(Exchange.HTTP_METHOD, "GET");
                exchange.getIn().setHeader(Exchange.HTTP_PATH, "Composition");
            }
        });
        inputStream = (InputStream) exchange.getIn().getBody();
        Reader reader = new InputStreamReader(inputStream);
        IBaseResource iresource = null;
        try {
            iresource = ctx.newJsonParser().parseResource(reader);
        } catch(Exception ex) {
            log.error("JSON Parse failed " + ex.getMessage());
            throw new InternalErrorException(ex.getMessage());
        }
        if (iresource instanceof Bundle) {
            Bundle returnedBundle = (Bundle) iresource;
            if (returnedBundle.getEntry().size()>0) {
                eprComposition = (Composition) returnedBundle.getEntry().get(0).getResource();
                log.info("Found Composition = " + eprComposition.getId());
            }
        }

        // Location not found. Add to database
        List<Reference> authors = new ArrayList<>();
        for (Reference reference : composition.getAuthor()) {
            Resource resource = searchAddResource(reference.getReference());
            if (resource != null) log.info("Found Resource = " + resource.getId());
            authors.add(getReference(resource));
        }
        composition.setAuthor(authors);

        if (composition.getSubject() != null) {
            Resource resource = searchAddResource(composition.getSubject().getReference());
            if (resource != null) log.info("Patient resource = "+resource.getId());
            composition.setSubject(getReference(resource));
        }
        if (composition.getEncounter().getReference() != null) {
            Resource resource = searchAddResource(composition.getEncounter().getReference());
            composition.setEncounter(getReference(resource));
        }
        if (composition.getCustodian() != null) {
            Resource resource = searchAddResource(composition.getCustodian().getReference());
            composition.setCustodian(getReference(resource));
        }

        IBaseResource iResource = null;
        String xhttpMethod = "POST";
        String xhttpPath = "Composition";
        // Location found do not add
        if (eprComposition != null) {
            xhttpMethod="PUT";
            // Want id value, no path or resource
            xhttpPath = "Composition/"+eprComposition.getIdElement().getIdPart();
            composition.setId(eprComposition.getId());
        }
        String httpBody = ctx.newJsonParser().encodeResourceToString(composition);
        String httpMethod= xhttpMethod;
        String httpPath = xhttpPath;
        try {
            exchange = template.send("direct:FHIRComposition", ExchangePattern.InOut, new Processor() {
                public void process(Exchange exchange) throws Exception {
                    exchange.getIn().setHeader(Exchange.HTTP_QUERY, "");
                    exchange.getIn().setHeader(Exchange.HTTP_METHOD, httpMethod);
                    exchange.getIn().setHeader(Exchange.HTTP_PATH, httpPath);
                    exchange.getIn().setHeader(Exchange.CONTENT_TYPE, "application/fhir+json");
                    exchange.getIn().setBody(httpBody);
                }
            });
            inputStream = (InputStream) exchange.getIn().getBody();

            reader = new InputStreamReader(inputStream);
            iResource = ctx.newJsonParser().parseResource(reader);
        } catch(Exception ex) {
            log.error("JSON Parse failed " + ex.getMessage());
            throw new InternalErrorException(ex.getMessage());
        }
        if (iResource instanceof Composition) {
            eprComposition = (Composition) iResource;
            if (resourceMap.get(compositionId) != null) { resourceMap.replace(compositionId,eprComposition); }
            else { resourceMap.put(compositionId,eprComposition); }

        } else if (iResource instanceof OperationOutcome)
        {
            OperationOutcome operationOutcome = (OperationOutcome) iResource;
            log.info("Sever Returned: "+ctx.newJsonParser().encodeResourceToString(operationOutcome));
            OperationOutcomeFactory.convertToException(operationOutcome);
        } else {
            throw new InternalErrorException("Unknown Error");
        }

        return eprComposition;
    }

    public Procedure searchAddProcedure(String procedureId) {
        log.info("Procedure searchAdd " +procedureId);
        Procedure procedure = null;
        for (Bundle.BundleEntryComponent entry : bundle.getEntry()) {
            if (entry.getResource().getId().equals(procedureId)) {
                procedure = (Procedure) entry.getResource();
            }
        }
        if (procedure == null) throw new InternalErrorException("Bundle processing error");

        Procedure eprProcedure = (Procedure) resourceMap.get(procedureId);

        // Organization already processed, quit with Organization
        if (eprProcedure != null) return eprProcedure;

        // Prevent re-adding the same Practitioner
        if (procedure.getIdentifier().size() == 0) {
            procedure.addIdentifier()
                    .setSystem("urn:uuid")
                    .setValue(procedure.getId());
        }

        ProducerTemplate template = context.createProducerTemplate();

        InputStream inputStream = null;

        for (Identifier identifier : procedure.getIdentifier()) {
            Exchange exchange = template.send("direct:FHIRProcedure", ExchangePattern.InOut, new Processor() {
                public void process(Exchange exchange) throws Exception {
                    exchange.getIn().setHeader(Exchange.HTTP_QUERY, "?identifier=" + identifier.getSystem() + "|" + identifier.getValue());
                    exchange.getIn().setHeader(Exchange.HTTP_METHOD, "GET");
                    exchange.getIn().setHeader(Exchange.HTTP_PATH, "Procedure");
                }
            });
            inputStream = (InputStream) exchange.getIn().getBody();
            Reader reader = new InputStreamReader(inputStream);
            IBaseResource iresource = null;
            try {
                iresource = ctx.newJsonParser().parseResource(reader);
            } catch(Exception ex) {
                log.error("JSON Parse failed " + ex.getMessage());
                throw new InternalErrorException(ex.getMessage());
            }
            if (iresource instanceof Bundle) {
                Bundle returnedBundle = (Bundle) iresource;
                if (returnedBundle.getEntry().size()>0) {
                    eprProcedure = (Procedure) returnedBundle.getEntry().get(0).getResource();
                    log.info("Found Procedure = " + eprProcedure.getId());
                }
            }
        }
        /*
        // Location found do not add
        if (eprProcedure != null) {
            resourceMap.put(procedure.getId(),eprProcedure);
            return eprProcedure;
        }
        */

        // Location not found. Add to database

        for (Procedure.ProcedurePerformerComponent performer : procedure.getPerformer()) {
            Reference reference = performer.getActor();
            Resource resource = searchAddResource(reference.getReference());
            log.info("Found Resource = " + resource.getId());
            performer.setActor(getReference(resource));

        }
        if (procedure.getSubject() != null) {
            Resource resource = searchAddResource(procedure.getSubject().getReference());
            procedure.setSubject(new Reference ("Patient/"+resource.getId()));
        }

        IBaseResource iResource = null;
        String xhttpMethod = "POST";
        String xhttpPath = "Procedure";
        // Location found do not add
        if (eprProcedure != null) {
            xhttpMethod="PUT";
            // Want id value, no path or resource
            xhttpPath = "Procedure/"+eprProcedure.getIdElement().getIdPart();
            procedure.setId(eprProcedure.getId());
        }
        String httpBody = ctx.newJsonParser().encodeResourceToString(procedure);
        String httpMethod= xhttpMethod;
        String httpPath = xhttpPath;
        try {
            Exchange exchange = template.send("direct:FHIRProcedure", ExchangePattern.InOut, new Processor() {
                public void process(Exchange exchange) throws Exception {
                    exchange.getIn().setHeader(Exchange.HTTP_QUERY, "");
                    exchange.getIn().setHeader(Exchange.HTTP_METHOD, httpMethod);
                    exchange.getIn().setHeader(Exchange.HTTP_PATH, httpPath);
                    exchange.getIn().setHeader(Exchange.CONTENT_TYPE, "application/fhir+json");
                    exchange.getIn().setBody(httpBody);
                }
            });
            inputStream = (InputStream) exchange.getIn().getBody();

            Reader reader = new InputStreamReader(inputStream);
            iResource = ctx.newJsonParser().parseResource(reader);
        } catch(Exception ex) {
            log.error("JSON Parse failed " + ex.getMessage());
            throw new InternalErrorException(ex.getMessage());
        }
        if (iResource instanceof Procedure) {
            eprProcedure = (Procedure) iResource;
            if (resourceMap.get(procedureId) != null) { resourceMap.replace(procedureId,eprProcedure); }
            else { resourceMap.put(procedureId,eprProcedure); }

        } else if (iResource instanceof OperationOutcome)
        {
            OperationOutcome operationOutcome = (OperationOutcome) iResource;
            log.info("Sever Returned: "+ctx.newJsonParser().encodeResourceToString(operationOutcome));
            OperationOutcomeFactory.convertToException(operationOutcome);
        } else {
            throw new InternalErrorException("Unknown Error");
        }

        return eprProcedure;
    }

    public Encounter searchAddEncounter(String encounterId) {
        log.info("Encounter searchAdd " +encounterId);
        Encounter encounter
                = null;
        for (Bundle.BundleEntryComponent entry : bundle.getEntry()) {
            if (entry.getResource().getId().equals(encounterId)) {
                encounter = (Encounter) entry.getResource();
            }
        }
        if (encounter == null) throw new InternalErrorException("Bundle processing error");

        Encounter eprEncounter = (Encounter) resourceMap.get(encounterId);

        // Organization already processed, quit with Organization
        if (eprEncounter != null) return eprEncounter;

        // Prevent re-adding the same Practitioner
        if (encounter.getIdentifier().size() == 0) {
            encounter.addIdentifier()
                    .setSystem("urn:uuid")
                    .setValue(encounter.getId());
        }

        ProducerTemplate template = context.createProducerTemplate();

        InputStream inputStream = null;

        for (Identifier identifier : encounter.getIdentifier()) {
            Exchange exchange = template.send("direct:FHIREncounter", ExchangePattern.InOut, new Processor() {
                public void process(Exchange exchange) throws Exception {
                    exchange.getIn().setHeader(Exchange.HTTP_QUERY, "?identifier=" + identifier.getSystem() + "|" + identifier.getValue());
                    exchange.getIn().setHeader(Exchange.HTTP_METHOD, "GET");
                    exchange.getIn().setHeader(Exchange.HTTP_PATH, "Encounter");
                }
            });
            inputStream = (InputStream) exchange.getIn().getBody();
            Reader reader = new InputStreamReader(inputStream);
            IBaseResource iresource = null;
            try {
                iresource = ctx.newJsonParser().parseResource(reader);
            } catch(Exception ex) {
                log.error("JSON Parse failed " + ex.getMessage());
                throw new InternalErrorException(ex.getMessage());
            }
            if (iresource instanceof Bundle) {
                Bundle returnedBundle = (Bundle) iresource;
                if (returnedBundle.getEntry().size()>0) {
                    eprEncounter = (Encounter) returnedBundle.getEntry().get(0).getResource();
                    log.info("Found Encounter = " + eprEncounter.getId());
                }
            }
        }
        /*
        // Location found do not add
        if (eprEncounter != null) {
            resourceMap.put(encounter.getId(),eprEncounter);
            return eprEncounter;
        }
        */

        // Location not found. Add to database

        // Create new list add in supported resources and discard unsupported
        List<Encounter.EncounterParticipantComponent> performertemp = new ArrayList<>();
        for (Encounter.EncounterParticipantComponent performer : encounter.getParticipant()) {
            Reference reference = performer.getIndividual();
            Resource resource = searchAddResource(reference.getReference());

            if (resource != null) {
                log.info("Found Resource = " + resource.getId());
                performer.setIndividual(getReference(resource));
                performertemp.add(performer);
            } else {
                log.info("Not processed "+reference.getReference());
            }
        }
        encounter.setParticipant(performertemp);

        if (encounter.getSubject() != null) {
            Resource resource = searchAddResource(encounter.getSubject().getReference());
            encounter.setSubject(getReference(resource));
        }

        IBaseResource iResource = null;
        String xhttpMethod = "POST";
        String xhttpPath = "Encounter";
        // Location found do not add
        if (eprEncounter != null) {
            xhttpMethod="PUT";
            // Want id value, no path or resource
            xhttpPath = "Encounter/"+eprEncounter.getIdElement().getIdPart();
            encounter.setId(eprEncounter.getId());
        }
        String httpBody = ctx.newJsonParser().encodeResourceToString(encounter);
        String httpMethod= xhttpMethod;
        String httpPath = xhttpPath;
        try {
            Exchange exchange = template.send("direct:FHIREncounter", ExchangePattern.InOut, new Processor() {
                public void process(Exchange exchange) throws Exception {
                    exchange.getIn().setHeader(Exchange.HTTP_QUERY, "");
                    exchange.getIn().setHeader(Exchange.HTTP_METHOD, httpMethod);
                    exchange.getIn().setHeader(Exchange.HTTP_PATH, httpPath);
                    exchange.getIn().setHeader(Exchange.CONTENT_TYPE, "application/fhir+json");
                    exchange.getIn().setBody(httpBody);
                }
            });
            inputStream = (InputStream) exchange.getIn().getBody();

            Reader reader = new InputStreamReader(inputStream);
            iResource = ctx.newJsonParser().parseResource(reader);
        } catch(Exception ex) {
            log.error("JSON Parse failed " + ex.getMessage());
            throw new InternalErrorException(ex.getMessage());
        }
        if (iResource instanceof Encounter) {
            eprEncounter = (Encounter) iResource;
            if (resourceMap.get(encounterId) != null) { resourceMap.replace(encounterId,eprEncounter); }
            else { resourceMap.put(encounterId,eprEncounter); }
        } else if (iResource instanceof OperationOutcome)
        {
            OperationOutcome operationOutcome = (OperationOutcome) iResource;
            log.info("Sever Returned: "+ctx.newJsonParser().encodeResourceToString(operationOutcome));
            OperationOutcomeFactory.convertToException(operationOutcome);
        } else {
            throw new InternalErrorException("Unknown Error");
        }

        return eprEncounter;
    }


    public Patient searchAddPatient(String patientId) {
        log.info("Patient searchAdd " +patientId);
        Patient patient = null;
        for (Bundle.BundleEntryComponent entry : bundle.getEntry()) {
            if (entry.getResource().getId().equals(patientId)) {
                patient = (Patient) entry.getResource();
            }
        }
        if (patient == null) throw new InternalErrorException("Bundle processing error");

        Patient eprPatient = (Patient) resourceMap.get(patientId);

        // Patient already processed, quit with Patient
        if (eprPatient != null) return eprPatient;

        ProducerTemplate template = context.createProducerTemplate();

        InputStream inputStream = null;

        for (Identifier identifier : patient.getIdentifier()) {
            Exchange exchange = template.send("direct:FHIRPatient", ExchangePattern.InOut, new Processor() {
                public void process(Exchange exchange) throws Exception {
                    exchange.getIn().setHeader(Exchange.HTTP_QUERY, "?identifier=" + identifier.getSystem() + "|" + identifier.getValue());
                    exchange.getIn().setHeader(Exchange.HTTP_METHOD, "GET");
                    exchange.getIn().setHeader(Exchange.HTTP_PATH, "Patient");
                }
            });
            inputStream = (InputStream) exchange.getIn().getBody();
            Reader reader = new InputStreamReader(inputStream);
            IBaseResource iresource = null;
            try {
                iresource = ctx.newJsonParser().parseResource(reader);
            } catch(Exception ex) {
                log.error("JSON Parse failed " + ex.getMessage());
                throw new InternalErrorException(ex.getMessage());
            }
            if (iresource instanceof Bundle) {
                Bundle returnedBundle = (Bundle) iresource;
                if (returnedBundle.getEntry().size() > 0) {
                    eprPatient = (Patient) returnedBundle.getEntry().get(0).getResource();
                    log.info("Found Patient = " + eprPatient.getId());
                }
            }
        }
        // Patient found do not add
        if (eprPatient != null) {
            resourceMap.put(patientId,eprPatient);
            return eprPatient;
        }

        // Location not found. Add to database

        if (patient.getManagingOrganization().getReference() != null) {
            Organization managingOrganisation = searchAddOrganisation(patient.getManagingOrganization().getReference());
            log.info("Found ManagingOrganization = "+managingOrganisation.getId());
            patient.setManagingOrganization(new Reference("Organization/"+managingOrganisation.getId()));
        }
        for (Reference reference : patient.getGeneralPractitioner()) {
            Practitioner practitioner = searchAddPractitioner(reference.getReference());
            log.info("Found Patient Practitioner = "+practitioner.getId());
            // This resets the first gp only (should only be one gp)
            patient.getGeneralPractitioner().get(0).setReference("Practitioner/"+practitioner.getId());
        }

        IBaseResource iResource = null;
        String jsonResource = ctx.newJsonParser().encodeResourceToString(patient);
        try {
            Exchange exchange = template.send("direct:FHIRPatient", ExchangePattern.InOut, new Processor() {
                public void process(Exchange exchange) throws Exception {
                    exchange.getIn().setHeader(Exchange.HTTP_QUERY, "");
                    exchange.getIn().setHeader(Exchange.HTTP_METHOD, "POST");
                    exchange.getIn().setHeader(Exchange.HTTP_PATH, "Patient");
                    exchange.getIn().setHeader(Exchange.CONTENT_TYPE, "application/fhir+json");
                    exchange.getIn().setBody(jsonResource);
                }
            });
            inputStream = (InputStream) exchange.getIn().getBody();

            Reader reader = new InputStreamReader(inputStream);
            iResource = ctx.newJsonParser().parseResource(reader);
        } catch(Exception ex) {
            log.error("JSON Parse failed " + ex.getMessage());
            throw new InternalErrorException(ex.getMessage());
        }
        if (iResource instanceof Patient) {
            eprPatient = (Patient) iResource;
            if (resourceMap.get(patientId) != null) { resourceMap.replace(patientId,eprPatient); }
            else { resourceMap.put(patientId,eprPatient); }
        } else if (iResource instanceof OperationOutcome)
        {
            OperationOutcome operationOutcome = (OperationOutcome) iResource;
            log.info("Sever Returned: "+ctx.newJsonParser().encodeResourceToString(operationOutcome));
            OperationOutcomeFactory.convertToException(operationOutcome);
        } else {
            throw new InternalErrorException("Unknown Error");
        }


        return eprPatient;
    }

}
