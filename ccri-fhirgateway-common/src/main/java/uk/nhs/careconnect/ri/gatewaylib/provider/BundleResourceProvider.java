package uk.nhs.careconnect.ri.gatewaylib.provider;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.annotation.*;
import ca.uhn.fhir.rest.api.MethodOutcome;
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

            if (resource.getId() != null) searchAddResource(resource.getId());

        }

        OperationOutcome opOutcome = new OperationOutcome();

        method.setOperationOutcome(opOutcome);


        return method;
    }

    public Reference getReference(Resource resource) {
        Reference reference = new Reference();
        reference.setReference(resource.getId());

        return reference;
    }

    public Resource searchAddResource(String referenceId) {

        log.info("Search "+referenceId);
        if (referenceId == null) {
            return null; //throw new InternalErrorException("Null Reference");
        }
        Resource resource = resourceMap.get(referenceId);
        // Don't process, if already processed.
        if (resource !=null)
        {
            log.info("Already Processed "+resource.getId());
            return resource;
        }

        for (Bundle.BundleEntryComponent entry : bundle.getEntry()) {
            Resource iResource = null;
            if ((entry.getFullUrl() !=null && entry.getFullUrl().equals(referenceId)) || (iResource == null && entry.getResource() !=null && entry.getResource().getId() !=null && entry.getResource().getId().equals(referenceId) )) {
                iResource = entry.getResource();
                if (iResource instanceof Patient) { resource = searchAddPatient(referenceId, (Patient) iResource); }
                else if (iResource instanceof Practitioner) { resource = searchAddPractitioner(referenceId, (Practitioner) iResource); }
                else if (iResource instanceof Encounter) { resource = searchAddEncounter(referenceId, (Encounter) iResource); }
                else if (iResource instanceof Organization) { resource = searchAddOrganisation(referenceId, (Organization) iResource); }
                else if (iResource instanceof Location) { resource = searchAddLocation(referenceId, (Location) iResource); }
                else if (iResource instanceof Observation) { resource = searchAddObservation(referenceId, (Observation) iResource); }
                else if (iResource instanceof AllergyIntolerance) { resource = searchAddAllergyIntolerance(referenceId, (AllergyIntolerance) iResource); }
                else if (iResource instanceof Condition) { resource = searchAddCondition(referenceId, (Condition) iResource); }
                else if (iResource instanceof Procedure) { resource = searchAddProcedure(referenceId, (Procedure) iResource); }
                else if (iResource instanceof Composition) { resource = searchAddComposition(referenceId, (Composition) iResource); }
                else if (iResource instanceof DiagnosticReport) { resource = searchAddDiagnosticReport(referenceId, (DiagnosticReport) iResource); }
                else if (iResource instanceof MedicationRequest) { resource = searchAddMedicationRequest(referenceId, (MedicationRequest) iResource); }
                else {
                    log.info( "Found in Bundle. Not processed (" + iResource.getClass());
                }
            }
        }
        if (resource==null) log.info("Search Not Found "+referenceId);
        return resource;
    }

    public Practitioner searchAddPractitioner(String practitionerId,Practitioner practitioner) {

        log.info("Practitioner searchAdd " +practitionerId);

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
            setResourceMap(practitionerId,eprPractitioner);
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
                    exchange.getIn().setHeader("Prefer","return=representation");
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
            setResourceMap(practitionerId,eprPractitioner);

        } else if (iResource instanceof OperationOutcome)
        {
            processOperationOutcome((OperationOutcome) iResource);
        } else {
            throw new InternalErrorException("Unknown Error");
        }

        return eprPractitioner;
    }

    public Organization searchAddOrganisation(String organisationId,Organization organisation) {
        log.info("Orgnisation searchAdd " +organisationId);

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
            setResourceMap(organisationId,eprOrganization);
            return eprOrganization;
        }

        // Organization not found. Add to database

        if (organisation.getPartOf().getReference() != null) {
            Resource resource = searchAddResource(organisation.getPartOf().getReference());
            log.info("Found PartOfOrganization = "+resource.getId());
            organisation.setPartOf(getReference(resource));
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
                    exchange.getIn().setHeader("Prefer","return=representation");
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
            setResourceMap(organisationId,eprOrganization);
        } else if (iResource instanceof OperationOutcome)
        {
            processOperationOutcome((OperationOutcome) iResource);
        } else {
            throw new InternalErrorException("Unknown Error");
        }


        return eprOrganization;
    }

    public Location searchAddLocation(String locationId,Location location) {
        log.info("Location searchAdd " +locationId);

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
            setResourceMap(locationId,eprLocation);
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
                    exchange.getIn().setHeader("Prefer","return=representation");
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
            setResourceMap(locationId,eprLocation);
        } else if (iResource instanceof OperationOutcome)
        {
            processOperationOutcome((OperationOutcome) iResource);
        } else {
            throw new InternalErrorException("Unknown Error");
        }

        return eprLocation;
    }

    public AllergyIntolerance searchAddAllergyIntolerance(String allergyIntoleranceId,AllergyIntolerance allergyIntolerance) {
        log.info("AllergyIntolerance searchAdd " +allergyIntoleranceId);

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
                    exchange.getIn().setHeader("Prefer","return=representation");
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
              setResourceMap(allergyIntoleranceId,eprAllergyIntolerance);

        } else if (iResource instanceof OperationOutcome)
        {
            processOperationOutcome((OperationOutcome) iResource);
        } else {
            throw new InternalErrorException("Unknown Error");
        }

        return eprAllergyIntolerance;
    }

    public Observation searchAddObservation(String observationId,Observation observation) {
        log.info("Observation searchAdd " +observationId);

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
                    exchange.getIn().setHeader("Prefer","return=representation");
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
            setResourceMap(observationId,eprObservation);

        } else if (iResource instanceof OperationOutcome)
        {
            processOperationOutcome((OperationOutcome) iResource);
        } else {
            throw new InternalErrorException("Unknown Error");
        }

        return eprObservation;
    }

    public DiagnosticReport searchAddDiagnosticReport(String diagnosticReportId,DiagnosticReport diagnosticReport) {
        log.info("DiagnosticReport searchAdd " +diagnosticReportId);

        if (diagnosticReport == null) throw new InternalErrorException("Bundle processing error");

        DiagnosticReport eprDiagnosticReport = (DiagnosticReport) resourceMap.get(diagnosticReportId);

        // Organization already processed, quit with Organization
        if (eprDiagnosticReport != null) return eprDiagnosticReport;

        // Prevent re-adding the same Practitioner
        if (diagnosticReport.getIdentifier().size() == 0) {
            diagnosticReport.addIdentifier()
                    .setSystem("urn:uuid")
                    .setValue(diagnosticReport.getId());
        }

        ProducerTemplate template = context.createProducerTemplate();

        InputStream inputStream = null;

        for (Identifier identifier : diagnosticReport.getIdentifier()) {
            Exchange exchange = template.send("direct:FHIRDiagnosticReport", ExchangePattern.InOut, new Processor() {
                public void process(Exchange exchange) throws Exception {
                    exchange.getIn().setHeader(Exchange.HTTP_QUERY, "?identifier=" + identifier.getSystem() + "|" + identifier.getValue());
                    exchange.getIn().setHeader(Exchange.HTTP_METHOD, "GET");
                    exchange.getIn().setHeader(Exchange.HTTP_PATH, "DiagnosticReport");
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
                    eprDiagnosticReport = (DiagnosticReport) returnedBundle.getEntry().get(0).getResource();
                    log.info("Found DiagnosticReport = " + eprDiagnosticReport.getId());
                }
            }
        }


        // Location not found. Add to database


        for (DiagnosticReport.DiagnosticReportPerformerComponent performer : diagnosticReport.getPerformer()) {
            Resource resource = searchAddResource(performer.getActor().getReference());
            performer.setActor(getReference(resource));
        }


        if (diagnosticReport.hasSubject()) {
            Resource resource = searchAddResource(diagnosticReport.getSubject().getReference());
            diagnosticReport.setSubject(getReference(resource));
        }
        if (diagnosticReport.hasContext()) {
            Resource resource = searchAddResource(diagnosticReport.getContext().getReference());
            diagnosticReport.setContext(getReference(resource));
        }

        List<Reference> results = new ArrayList<>();
        for (Reference reference : diagnosticReport.getResult()) {
            Resource resource = searchAddResource(reference.getReference());
            if (resource!=null) results.add(getReference(resource));
        }
        diagnosticReport.setResult(results);

        IBaseResource iResource = null;

        String xhttpMethod = "POST";
        String xhttpPath = "DiagnosticReport";
        // Location found do not add
        if (eprDiagnosticReport != null) {
            xhttpMethod="PUT";
            // Want id value, no path or resource
            xhttpPath = "DiagnosticReport/"+eprDiagnosticReport.getIdElement().getIdPart();
            diagnosticReport.setId(eprDiagnosticReport.getId());
        }
        String httpBody = ctx.newJsonParser().encodeResourceToString(diagnosticReport);
        String httpMethod= xhttpMethod;
        String httpPath = xhttpPath;
        try {
            Exchange exchange = template.send("direct:FHIRDiagnosticReport", ExchangePattern.InOut, new Processor() {
                public void process(Exchange exchange) throws Exception {
                    exchange.getIn().setHeader(Exchange.HTTP_QUERY, "");
                    exchange.getIn().setHeader(Exchange.HTTP_METHOD, httpMethod);
                    exchange.getIn().setHeader(Exchange.HTTP_PATH, httpPath);
                    exchange.getIn().setHeader("Prefer","return=representation");
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
        if (iResource instanceof DiagnosticReport) {
            eprDiagnosticReport = (DiagnosticReport) iResource;
            setResourceMap(diagnosticReportId,eprDiagnosticReport);

        } else if (iResource instanceof OperationOutcome)
        {
            processOperationOutcome((OperationOutcome) iResource);
        } else {
            throw new InternalErrorException("Unknown Error");
        }

        return eprDiagnosticReport;
    }

    public MedicationRequest searchAddMedicationRequest(String medicationRequestId,MedicationRequest medicationRequest) {
        log.info("MedicationRequest searchAdd " +medicationRequestId);

        if (medicationRequest == null) throw new InternalErrorException("Bundle processing error");

        MedicationRequest eprMedicationRequest = (MedicationRequest) resourceMap.get(medicationRequestId);

        // Organization already processed, quit with Organization
        if (eprMedicationRequest != null) return eprMedicationRequest;

        // Prevent re-adding the same Practitioner
        if (medicationRequest.getIdentifier().size() == 0) {
            medicationRequest.addIdentifier()
                    .setSystem("urn:uuid")
                    .setValue(medicationRequest.getId());
        }

        ProducerTemplate template = context.createProducerTemplate();

        InputStream inputStream = null;

        for (Identifier identifier : medicationRequest.getIdentifier()) {
            Exchange exchange = template.send("direct:FHIRMedicationRequest", ExchangePattern.InOut, new Processor() {
                public void process(Exchange exchange) throws Exception {
                    exchange.getIn().setHeader(Exchange.HTTP_QUERY, "?identifier=" + identifier.getSystem() + "|" + identifier.getValue());
                    exchange.getIn().setHeader(Exchange.HTTP_METHOD, "GET");
                    exchange.getIn().setHeader(Exchange.HTTP_PATH, "MedicationRequest");
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
                    eprMedicationRequest = (MedicationRequest) returnedBundle.getEntry().get(0).getResource();
                    log.info("Found MedicationRequest = " + eprMedicationRequest.getId());
                }
            }
        }


        // Location not found. Add to database



        if (medicationRequest.hasSubject()) {
            Resource resource = searchAddResource(medicationRequest.getSubject().getReference());
            medicationRequest.setSubject(getReference(resource));
        }
        if (medicationRequest.hasContext()) {
            Resource resource = searchAddResource(medicationRequest.getContext().getReference());
            medicationRequest.setContext(getReference(resource));
        }

        if (medicationRequest.hasRequester()) {
            Resource resource = searchAddResource(medicationRequest.getRequester().getAgent().getReference());
            medicationRequest.getRequester().setAgent(getReference(resource));
        }

        IBaseResource iResource = null;

        String xhttpMethod = "POST";
        String xhttpPath = "MedicationRequest";
        // Location found do not add
        if (eprMedicationRequest != null) {
            xhttpMethod="PUT";
            // Want id value, no path or resource
            xhttpPath = "MedicationRequest/"+eprMedicationRequest.getIdElement().getIdPart();
            medicationRequest.setId(eprMedicationRequest.getId());
        }
        String httpBody = ctx.newJsonParser().encodeResourceToString(medicationRequest);
        String httpMethod= xhttpMethod;
        String httpPath = xhttpPath;
        try {
            Exchange exchange = template.send("direct:FHIRMedicationRequest", ExchangePattern.InOut, new Processor() {
                public void process(Exchange exchange) throws Exception {
                    exchange.getIn().setHeader(Exchange.HTTP_QUERY, "");
                    exchange.getIn().setHeader(Exchange.HTTP_METHOD, httpMethod);
                    exchange.getIn().setHeader(Exchange.HTTP_PATH, httpPath);
                    exchange.getIn().setHeader("Prefer","return=representation");
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
        if (iResource instanceof MedicationRequest) {
            eprMedicationRequest = (MedicationRequest) iResource;
            setResourceMap(medicationRequestId,eprMedicationRequest);

        } else if (iResource instanceof OperationOutcome)
        {
            processOperationOutcome((OperationOutcome) iResource);
        } else {
            throw new InternalErrorException("Unknown Error");
        }

        return eprMedicationRequest;
    }

    public Condition searchAddCondition(String conditionId, Condition condition) {
        log.info("Condition searchAdd " +conditionId);

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
                    exchange.getIn().setHeader("Prefer","return=representation");
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
            setResourceMap(conditionId,eprCondition);
        } else if (iResource instanceof OperationOutcome)
        {
            processOperationOutcome((OperationOutcome) iResource);
        } else {
            throw new InternalErrorException("Unknown Error");
        }

        return eprCondition;
    }

    public Composition searchAddComposition(String compositionId,Composition composition) {
        log.info("Composition searchAdd " +compositionId);

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
            if (resource != null) {
                log.info("Found Resource = " + resource.getId());
                authors.add(getReference(resource));
            }

        }
        composition.setAuthor(authors);

        if (composition.getSubject() != null) {
            Resource resource = searchAddResource(composition.getSubject().getReference());
            if (resource != null) {
                log.info("Patient resource = "+resource.getId());
            }
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
        for (Composition.SectionComponent section: composition.getSection()) {
            List<Reference> references = new ArrayList<>();
            for (Reference reference : section.getEntry()) {
                Resource resource = searchAddResource(reference.getReference());
                if (resource!=null) references.add(getReference(resource));
            }
            section.setEntry(references);
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
                    exchange.getIn().setHeader("Prefer","return=representation");
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

            setResourceMap(eprComposition.getId(),eprComposition);

        } else if (iResource instanceof OperationOutcome)
        {
            processOperationOutcome((OperationOutcome) iResource);
        } else {
            throw new InternalErrorException("Unknown Error");
        }

        return eprComposition;
    }

    public Procedure searchAddProcedure(String procedureId,Procedure procedure) {
        log.info("Procedure searchAdd " +procedureId);

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

        // Location not found. Add to database

        for (Procedure.ProcedurePerformerComponent performer : procedure.getPerformer()) {
            Reference reference = performer.getActor();
            Resource resource = searchAddResource(reference.getReference());
            log.info("Found Resource = " + resource.getId());
            performer.setActor(getReference(resource));

        }
        if (procedure.getSubject() != null) {
            Resource resource = searchAddResource(procedure.getSubject().getReference());
            procedure.setSubject(getReference(resource));
        }
        if (procedure.getLocation().getReference() != null) {
            Resource resource = searchAddResource(procedure.getLocation().getReference());
            procedure.setLocation(getReference(resource));
        }
        if (procedure.hasContext()) {
            Resource resource = searchAddResource(procedure.getContext().getReference());
            if (resource != null) { procedure.setContext(getReference(resource)); }
            else { procedure.setContext(null); }
        }
        List<Reference> reasons = new ArrayList<>();
        for (Reference reference : procedure.getReasonReference()) {
            Resource resource = searchAddResource(reference.getReference());
            if (resource != null) reasons.add(getReference(resource));
        }
        procedure.setReasonReference(reasons);

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
                    exchange.getIn().setHeader("Prefer","return=representation");
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
            setResourceMap(eprProcedure.getId(),eprProcedure);


        } else if (iResource instanceof OperationOutcome)
        {
            processOperationOutcome((OperationOutcome) iResource);
        } else {
            throw new InternalErrorException("Unknown Error");
        }

        return eprProcedure;
    }

    public Encounter searchAddEncounter(String encounterId,Encounter encounter) {
        log.info("Encounter searchAdd " +encounterId);

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

        for (Encounter.DiagnosisComponent component : encounter.getDiagnosis()) {
            if (component.getCondition().getReference() != null) {
                Resource resource = searchAddResource(component.getCondition().getReference());
                component.setCondition(getReference(resource));
            }
        }

        for (Encounter.EncounterLocationComponent component : encounter.getLocation()) {
            if (component.getLocation().getReference() != null) {
                Resource resource = searchAddResource(component.getLocation().getReference());
                component.setLocation(getReference(resource));
            }
        }
        if (encounter.hasHospitalization()) {
            if (encounter.getHospitalization().getDestination().getReference() !=null) {
                Resource resource = searchAddResource(encounter.getHospitalization().getDestination().getReference());
                encounter.getHospitalization().setDestination(getReference(resource));
            }
        }
        if (encounter.hasServiceProvider()) {
            Resource resource = searchAddResource(encounter.getServiceProvider().getReference());
            encounter.setServiceProvider(getReference(resource));
        }
        if (encounter.hasClass_()) {
            if (encounter.getClass_().getSystem() == null) {
                encounter.getClass_().setSystem("http://hl7.org/fhir/v3/ActCode");

                switch (encounter.getClass_().getCode()) {
                    case "inpatient":
                        encounter.getClass_().setCode("ACUTE");
                        break;
                    case "outpatient":
                        encounter.getClass_().setCode("SS");
                        break;
                    case "ambulatory":
                        encounter.getClass_().setCode("AMB");
                        break;
                    case "emergency":
                        encounter.getClass_().setCode("EMER");
                        break;
                }
            }
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
                    exchange.getIn().setHeader("Prefer","return=representation");
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
            setResourceMap(eprEncounter.getId(),eprEncounter);
        } else if (iResource instanceof OperationOutcome)
        {
            processOperationOutcome((OperationOutcome) iResource);
        } else {
            throw new InternalErrorException("Unknown Error");
        }

        return eprEncounter;
    }


    public Patient searchAddPatient(String patientId, Patient patient) {
        log.info("Patient searchAdd " +patientId);

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
            setResourceMap(patientId,eprPatient);
            return eprPatient;
        }

        // Location not found. Add to database

        if (patient.getManagingOrganization().getReference() != null) {
            Resource resource = searchAddResource(patient.getManagingOrganization().getReference());
            log.info("Found ManagingOrganization = "+resource.getId());
            patient.setManagingOrganization(getReference(resource));
        }
        for (Reference reference : patient.getGeneralPractitioner()) {
            Resource resource = searchAddResource(reference.getReference());
            log.info("Found Patient Practitioner = "+reference.getId());
            // This resets the first gp only (should only be one gp)
            patient.getGeneralPractitioner().get(0).setReference(getReference(resource).getReference());
        }

        IBaseResource iResource = null;
        String jsonResource = ctx.newJsonParser().encodeResourceToString(patient);
        try {
            Exchange exchange = template.send("direct:FHIRPatient", ExchangePattern.InOut, new Processor() {
                public void process(Exchange exchange) throws Exception {
                    exchange.getIn().setHeader(Exchange.HTTP_QUERY, "");
                    exchange.getIn().setHeader(Exchange.HTTP_METHOD, "POST");
                    exchange.getIn().setHeader(Exchange.HTTP_PATH, "Patient");
                    exchange.getIn().setHeader("Prefer","return=representation");
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
            setResourceMap(patientId,eprPatient);
        } else if (iResource instanceof OperationOutcome) {
            processOperationOutcome((OperationOutcome) iResource);
        } else {
            throw new InternalErrorException("Unknown Error");
        }


        return eprPatient;
    }

    private void processOperationOutcome(OperationOutcome operationOutcome) {

        log.info("Server Returned: "+ctx.newJsonParser().encodeResourceToString(operationOutcome));
        OperationOutcomeFactory.convertToException(operationOutcome);
    }
    private void setResourceMap(String referenceId,Resource resource) {
        if (resourceMap.get(referenceId) != null) {
            resourceMap.replace(referenceId, resource);
        } else {
            resourceMap.put(referenceId,resource);

        }
        log.info("setResourceMap = " +resource.getId());
        if (resourceMap.get(resource.getId()) != null) {
            resourceMap.replace(resource.getId(),resource);
        } else {
            resourceMap.put(resource.getId(),resource);
        }
    }
}
