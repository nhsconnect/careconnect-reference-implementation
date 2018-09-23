package uk.nhs.careconnect.ri.lib.gateway.camel.processor;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.server.exceptions.BaseServerResponseException;
import ca.uhn.fhir.rest.server.exceptions.InternalErrorException;
import org.apache.camel.*;
import org.hl7.fhir.dstu3.model.*;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.nhs.careconnect.ri.lib.server.OperationOutcomeFactory;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BundleCore {

    public BundleCore(FhirContext ctx, CamelContext camelContext, Bundle bundle) {
        this.ctx = ctx;
        this.bundle = bundle;
        this.context = camelContext;
        this.client = FhirContext.forDstu3().newRestfulGenericClient("https://directory.spineservices.nhs.uk/STU3");
    }

    CamelContext context;

    FhirContext ctx;

    IGenericClient client;

/*
    private FHIRMedicationStatementToFHIRMedicationRequestTransformer
            fhirMedicationStatementToFHIRMedicationRequestTransformer = new  FHIRMedicationStatementToFHIRMedicationRequestTransformer();
*/

    private Map<String,Resource> resourceMap = new HashMap<>();;

    private Bundle bundle;

    private OperationOutcome operationOutcome = null;

    private static final Logger log = LoggerFactory.getLogger(BundleCore.class);

    public Reference getReference(Resource resource) {
        Reference reference = new Reference();
        reference.setReference(resource.getId());
        return reference;
    }

    public OperationOutcome getOperationOutcome() {
        return operationOutcome;
    }

    public Resource setOperationOutcome(OperationOutcome operationOutcome) {
        this.operationOutcome = operationOutcome;
        return null;
    }

    public Bundle getUpdatedBundle() {
        //
        Bundle updatedBundle = new Bundle();
        updatedBundle.setType(this.bundle.getType());
        updatedBundle.setIdentifier(this.bundle.getIdentifier());
        for (Bundle.BundleEntryComponent entry : bundle.getEntry()) {
            Resource iResource = resourceMap.get(entry.getResource().getId());
            if (iResource == null) {
                iResource = searchAddResource(entry.getResource().getId());
            }
            if (iResource != null) {
                updatedBundle.addEntry().setResource(iResource);
            } else {
                log.warn("Not found "+entry.getResource().getClass().getSimpleName() + " Reference " + entry.getResource().getId());
                updatedBundle.addEntry().setResource(entry.getResource());
            }
        }
        return updatedBundle;
    }

    public Bundle getBundle() {
        return bundle;
    }

    public Boolean checkCircularReference(Encounter encounter) {
        Boolean found = false;
        log.info("Checking Encounter id="+encounter.getId());
        log.info("Checking Encounter idElement="+encounter.getIdElement());
        if (encounter.hasDiagnosis()) {
            for (Bundle.BundleEntryComponent entry : bundle.getEntry()) {
                if (entry.getResource() instanceof Condition) {
                    Condition condition = (Condition) entry.getResource();
                    log.info("Check condition = "+condition.getId());
                    if (condition.hasContext()) {
                        for (Encounter.DiagnosisComponent diagnosis : encounter.getDiagnosis()) {
                            log.info("Check encounter.diagnosis = "+diagnosis.getCondition().getReference());
                            if (diagnosis.getCondition().getReference().equals(condition.getId())) {

                                if (condition.getContext().getReference().equals(encounter.getId())) {
                                    OperationOutcome outcome = new OperationOutcome();
                                    outcome.addIssue()
                                            .setCode(OperationOutcome.IssueType.BUSINESSRULE)
                                            .setSeverity(OperationOutcome.IssueSeverity.FATAL)
                                            .setDiagnostics("Encounter "+encounter.getId()+" has a circular diagnosis reference to Condition "+condition.getId())
                                            .setDetails(
                                                    new CodeableConcept().setText("Circular Reference")
                                            );
                                    setOperationOutcome(outcome);
                                    OperationOutcomeFactory.convertToException(outcome);
                                }
                            }
                        }
                    }
                }
            }
        }
        return found;
    }

    public Resource searchAddResource(String referenceId) {
        try {
            log.info("searchAddResource " + referenceId);
            if (referenceId == null) {
                return null; //throw new InternalErrorException("Null Reference");
            }
            Resource resource = resourceMap.get(referenceId);
            // Don't process, if already processed.
            if (resource != null) {
                log.info("Already Processed " + resource.getId());
                return resource;
            }

            if (referenceId.contains("demographics.spineservices.nhs.uk")) {
                //
                log.info("NHS Number detected");
            }
            if (referenceId.contains("directory.spineservices.nhs.uk")) {
                if (referenceId.contains("Organization")) {
                    String sdsCode = referenceId.replace("https://directory.spineservices.nhs.uk/STU3/Organization/","");
                    Organization sdsOrganization = client.read().resource(Organization.class).withId(sdsCode).execute();

                    if (sdsOrganization != null) {
                        resource = searchAddOrganisation(referenceId, sdsOrganization);
                    }
                }
                /*
                if (referenceId.contains("Practitioner")) {
                    String sdsCode = referenceId.replace("https://directory.spineservices.nhs.uk/STU3/Practitioner/","");
                    Practitioner sdsPractitioner = client.read().resource(Practitioner.class).withId(sdsCode).execute();

                    if (sdsPractitioner != null) {
                        resource = searchAddPractitioner(referenceId, sdsPractitioner);
                    }
                }
                */
            } else {

                for (Bundle.BundleEntryComponent entry : bundle.getEntry()) {
                    Resource iResource = null;
                    if ((entry.getFullUrl() != null && entry.getFullUrl().equals(referenceId)) || (iResource == null && entry.getResource() != null && entry.getResource().getId() != null && entry.getResource().getId().equals(referenceId))) {
                        iResource = entry.getResource();

                        if (iResource instanceof Patient) {
                            resource = searchAddPatient(referenceId, (Patient) iResource);
                        } else if (iResource instanceof Practitioner) {
                            resource = searchAddPractitioner(referenceId, (Practitioner) iResource);
                        } else if (iResource instanceof Encounter) {
                            resource = searchAddEncounter(referenceId, (Encounter) iResource);
                        } else if (iResource instanceof Organization) {
                            resource = searchAddOrganisation(referenceId, (Organization) iResource);
                        } else if (iResource instanceof Location) {
                            resource = searchAddLocation(referenceId, (Location) iResource);
                        } else if (iResource instanceof Observation) {
                            resource = searchAddObservation(referenceId, (Observation) iResource);
                        } else if (iResource instanceof AllergyIntolerance) {
                            resource = searchAddAllergyIntolerance(referenceId, (AllergyIntolerance) iResource);
                        } else if (iResource instanceof Condition) {
                            resource = searchAddCondition(referenceId, (Condition) iResource);
                        } else if (iResource instanceof Procedure) {
                            resource = searchAddProcedure(referenceId, (Procedure) iResource);
                        } else if (iResource instanceof Composition) {
                            resource = searchAddComposition(referenceId, (Composition) iResource);
                        } else if (iResource instanceof DiagnosticReport) {
                            resource = searchAddDiagnosticReport(referenceId, (DiagnosticReport) iResource);
                        } else if (iResource instanceof MedicationRequest) {
                            resource = searchAddMedicationRequest(referenceId, (MedicationRequest) iResource);
                        } else if (iResource instanceof MedicationStatement) {
                            resource = searchAddMedicationStatement(referenceId, (MedicationStatement) iResource);
                        } else if (iResource instanceof ListResource) {
                            resource = searchAddList(referenceId, (ListResource) iResource);
                        } else if (iResource instanceof Immunization) {
                            resource = searchAddImmunization(referenceId, (Immunization) iResource);

                        } else {

                            switch (iResource.getClass().getSimpleName()) {
                                case "CarePlan":
                                    resource = searchAddCarePlan(referenceId, (CarePlan) iResource);
                                    break;
                                case "CareTeam":
                                    resource = searchAddCareTeam(referenceId, (CareTeam) iResource);
                                    break;
                                case "ClinicalImpression":
                                    resource = searchAddClinicalImpression(referenceId, (ClinicalImpression) iResource);
                                    break;
                                case "Consent":
                                    resource = searchAddConsent(referenceId, (Consent) iResource);
                                    break;
                                case "DocumentReference":
                                    resource = searchAddDocumentReference(referenceId, (DocumentReference) iResource);
                                    break;
                                case "Goal":
                                    resource = searchAddGoal(referenceId, (Goal) iResource);
                                    break;
                                case "HealthcareService":
                                    resource = searchAddHealthcareService(referenceId, (HealthcareService) iResource);
                                    break;
                                case "MedicationDispense":
                                    resource = searchAddMedicationDispense(referenceId, (MedicationDispense) iResource);
                                    break;
                                case "MedicationRequest":
                                    resource = searchAddMedicationRequest(referenceId, (MedicationRequest) iResource);
                                    break;
                                case "QuestionnaireResponse":
                                    resource = searchAddQuestionnaireResponse(referenceId, (QuestionnaireResponse) iResource);
                                    break;
                                case "RelatedPerson":
                                    resource = searchAddRelatedPerson(referenceId, (RelatedPerson) iResource);
                                    break;
                                case "ReferralRequest":
                                    resource = searchAddReferralRequest(referenceId, (ReferralRequest) iResource);
                                    break;
                                case "RiskAssessment":
                                    resource = searchAddRiskAssessment(referenceId, (RiskAssessment) iResource);
                                    break;
                                case "Medication":
                                    resource = searchAddMedication(referenceId, (Medication) iResource);
                                    break;
                                default:
                                    log.info("Found in Bundle. Not processed (" + iResource.getClass());
                            }

                        }
                    }

                    //else if (iResource instanceof PractitionerRole) {
                    //    resource = searchAddReferralRequest(referenceId, (ReferralRequest) iResource);
                    //}
                }
            }
            if (resource == null) log.info("Search Not Found " + referenceId);
            if (this.operationOutcome != null) return operationOutcome;
            return resource;
        } catch (Exception ex) {

            String errorMessage = "Exception while processing reference "+referenceId;
            if (ex instanceof BaseServerResponseException) {
                //log.error("HAPI Exception " +ex.getClass().getSimpleName() );

                errorMessage = errorMessage + " Diagnostics: " + this.operationOutcome.getIssueFirstRep().getDiagnostics();
            } else {
                if (ex.getMessage() != null) {
                    errorMessage = errorMessage + " getMessage: " +ex.getMessage();
                } else {
                    errorMessage = errorMessage + " ExceptionClassname: " +ex.getClass().getSimpleName();
                }

            }
            log.error(errorMessage);
            throw ex;
        }

        //return null;
    }

    private ListResource searchAddList(String listId,ListResource list) {
        log.info("List searchAdd " +listId);

        if (list == null) throw new InternalErrorException("Bundle processing error");

        ListResource eprListResource = (ListResource) resourceMap.get(listId);

        // Organization already processed, quit with Organization
        if (eprListResource != null) return eprListResource;

        // Prevent re-adding the same Practitioner
        if (list.getIdentifier().size() == 0) {
            list.addIdentifier()
                    .setSystem("urn:uuid")
                    .setValue(list.getId());
        }

        ProducerTemplate template = context.createProducerTemplate();

        InputStream inputStream = null;

        for (Identifier identifier : list.getIdentifier()) {
            Exchange exchange = template.send("direct:FHIRList", ExchangePattern.InOut, new Processor() {
                public void process(Exchange exchange) throws Exception {
                    exchange.getIn().setHeader(Exchange.HTTP_QUERY, "identifier=" + identifier.getSystem() + "|" + identifier.getValue());
                    exchange.getIn().setHeader(Exchange.HTTP_METHOD, "GET");
                    exchange.getIn().setHeader(Exchange.HTTP_PATH, "List");
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
            if (iresource instanceof OperationOutcome) {
                processOperationOutcome((OperationOutcome) iresource);
            } else
            if (iresource instanceof Bundle) {
                Bundle returnedBundle = (Bundle) iresource;
                if (returnedBundle.getEntry().size()>0) {
                    eprListResource = (ListResource) returnedBundle.getEntry().get(0).getResource();
                    log.info("Found ListResource = " + eprListResource.getId());
                }
            }
        }

        if (list.hasEncounter()) {
            Resource resource = searchAddResource(list.getEncounter().getReference());

            if (resource == null) referenceMissing(list, list.getEncounter().getReference());
            list.setEncounter(getReference(resource));
        }
        if (list.hasSubject()) {
            Resource resource = searchAddResource(list.getSubject().getReference());

            if (resource == null) referenceMissing(list, list.getSubject().getReference());
            list.setSubject(getReference(resource));
        }

        for (ListResource.ListEntryComponent listEntry : list.getEntry()) {
            if (listEntry.hasItem()) {
                Resource resource = searchAddResource(listEntry.getItem().getReference());
                if (resource == null) referenceMissing(list, listEntry.getItem().getReference());
                if (resource != null) listEntry.setItem(getReference(resource));
            }
        }


        IBaseResource iResource = null;
        String xhttpMethod = "POST";
        String xhttpPath = "List";
        // Location found do not add
        if (eprListResource != null) {
            xhttpMethod="PUT";
            // Want id value, no path or resource
            xhttpPath = "List/"+eprListResource.getIdElement().getIdPart();
            list.setId(eprListResource.getId());
        }
        String httpBody = ctx.newJsonParser().encodeResourceToString(list);
        String httpMethod= xhttpMethod;
        String httpPath = xhttpPath;
        try {
            Exchange exchange = template.send("direct:FHIRList", ExchangePattern.InOut, new Processor() {
                public void process(Exchange exchange) throws Exception {
                    exchange.getIn().setHeader(Exchange.HTTP_QUERY, "");
                    exchange.getIn().setHeader(Exchange.HTTP_METHOD, httpMethod);
                    exchange.getIn().setHeader(Exchange.HTTP_PATH, httpPath);
                    exchange.getIn().setHeader("Prefer","return=representation");
                    exchange.getIn().setHeader(Exchange.CONTENT_TYPE, "application/fhir+json");
                    exchange.getIn().setBody(httpBody);
                }
            });
            if (exchange.getIn().getBody() instanceof String ) {
                log.error((String) exchange.getIn().getBody());
                throw new InternalErrorException((String) exchange.getIn().getBody());
            }
            inputStream = (InputStream) exchange.getIn().getBody();

            Reader reader = new InputStreamReader(inputStream);
            iResource = ctx.newJsonParser().parseResource(reader);
        } catch(Exception ex) {
            log.error("JSON Parse failed " + ex.getMessage());
            throw new InternalErrorException(ex.getMessage());
        }
        if (iResource instanceof ListResource) {
            eprListResource = (ListResource) iResource;
            setResourceMap(listId,eprListResource);

        } else if (iResource instanceof OperationOutcome)
        {
            processOperationOutcome((OperationOutcome) iResource);
        } else {
            throw new InternalErrorException("Unknown Error");
        }

        return eprListResource;
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
                    exchange.getIn().setHeader(Exchange.HTTP_QUERY, "identifier=" + identifier.getSystem() + "|" + identifier.getValue());
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
            if (iresource instanceof OperationOutcome) {
                processOperationOutcome((OperationOutcome) iresource);
            } else if (iresource instanceof Bundle) {
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

        // Prevent re-adding the same Organisation
        if (organisation.getIdentifier().size() == 0) {
            organisation.addIdentifier()
                    .setSystem("urn:uuid")
                    .setValue(organisation.getId());
        }

        for (Identifier identifier : organisation.getIdentifier()) {
            Exchange exchange = template.send("direct:FHIROrganisation", ExchangePattern.InOut, new Processor() {
                public void process(Exchange exchange) throws Exception {
                    exchange.getIn().setHeader(Exchange.HTTP_QUERY, "identifier=" + identifier.getSystem() + "|" + identifier.getValue());
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
            if (iresource instanceof OperationOutcome) {
                processOperationOutcome((OperationOutcome) iresource);
            } else if (iresource instanceof Bundle) {
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

            if (resource == null) referenceMissing(organisation, organisation.getPartOf().getReference());
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

    public HealthcareService searchAddHealthcareService(String serviceId,HealthcareService service) {
        log.info("HealthcareService searchAdd " +serviceId);

        if (service== null) throw new InternalErrorException("Bundle processing error");

        HealthcareService eprService = (HealthcareService) resourceMap.get(serviceId);

        // HealthcareService already processed, quit with HealthcareService
        if (eprService != null) return eprService;

        ProducerTemplate template = context.createProducerTemplate();

        InputStream inputStream = null;

        // Prevent re-adding the same HealthcareService
        if (service.getIdentifier().size() == 0) {
            service.addIdentifier()
                    .setSystem("urn:uuid")
                    .setValue(service.getId());
        }

        log.info("Looking up HealthcareService Service " +serviceId);
        for (Identifier identifier : service.getIdentifier()) {
            Exchange exchange = template.send("direct:FHIRHealthcareService", ExchangePattern.InOut, new Processor() {
                public void process(Exchange exchange) throws Exception {
                    exchange.getIn().setHeader(Exchange.HTTP_QUERY, "identifier=" + identifier.getSystem() + "|" + identifier.getValue());
                    exchange.getIn().setHeader(Exchange.HTTP_METHOD, "GET");
                    exchange.getIn().setHeader(Exchange.HTTP_PATH, "HealthcareService");
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
            if (iresource instanceof OperationOutcome) {
                processOperationOutcome((OperationOutcome) iresource);
            } else  {
                Bundle returnedBundle = (Bundle) iresource;
                if (returnedBundle.getEntry().size()>0) {
                    eprService = (HealthcareService) returnedBundle.getEntry().get(0).getResource();
                    log.info("Found HealthcareService = " + eprService.getId());
                }
            }
        }
        log.info("Adding HealthcareService Service " +serviceId);
        // HealthcareService found do not add
        if (eprService != null) {
            setResourceMap(serviceId,eprService);
            return eprService;
        }

        // HealthcareService not found. Add to database

        if (service.getProvidedBy().getReference() != null) {
            Resource resource = searchAddResource(service.getProvidedBy().getReference());

            log.info("Found PartOf HealthcareService = "+resource.getId());
            if (resource == null) referenceMissing(service, service.getProvidedBy().getReference());
            service.setProvidedBy(getReference(resource));
        }

        List<Reference> locations = new ArrayList<>();
        for (Reference reference : service.getLocation()) {
            if (reference.getReference() != null) {
                Resource resource = searchAddResource(reference.getReference());

                log.info("Found Location Reference HealthcareService = " + resource.getId());
                if (resource == null) referenceMissing(service, reference.getReference());
                locations.add(getReference(resource));
            }
        }
        service.setLocation(locations);


        IBaseResource iResource = null;
        String jsonResource = ctx.newJsonParser().encodeResourceToString(service);
        Exchange exchange = null;
        try {
            exchange = template.send("direct:FHIRHealthcareService", ExchangePattern.InOut, new Processor() {
                public void process(Exchange exchange) throws Exception {
                    exchange.getIn().setHeader(Exchange.HTTP_QUERY, "");
                    exchange.getIn().setHeader(Exchange.HTTP_METHOD, "POST");
                    exchange.getIn().setHeader(Exchange.HTTP_PATH, "HealthcareService");
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
        if (iResource instanceof HealthcareService) {
            eprService = (HealthcareService) iResource;
            setResourceMap(serviceId,eprService);
            return eprService;
        } else if (iResource instanceof OperationOutcome)
        {
            processOperationOutcome((OperationOutcome) iResource);
        } else {
            throw new InternalErrorException("Unknown Error");
        }


        return null;
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
                    exchange.getIn().setHeader(Exchange.HTTP_QUERY, "identifier=" + identifier.getSystem() + "|" + identifier.getValue());
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
            if (iresource instanceof OperationOutcome) {
                processOperationOutcome((OperationOutcome) iresource);
            } else if (iresource instanceof Bundle) {
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

            if (resource == null) referenceMissing(location, location.getManagingOrganization().getReference());
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
                    exchange.getIn().setHeader(Exchange.HTTP_QUERY, "identifier=" + identifier.getSystem() + "|" + identifier.getValue());
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
            if (iresource instanceof OperationOutcome) {
                processOperationOutcome((OperationOutcome) iresource);
            } else
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
            if (resource == null) referenceMissing(allergyIntolerance, allergyIntolerance.getAsserter().getReference());
            allergyIntolerance.setAsserter(getReference(resource));

        }
        if (allergyIntolerance.getPatient() != null) {
            Resource resource = searchAddResource(allergyIntolerance.getPatient().getReference());

            if (resource == null) referenceMissing(allergyIntolerance, allergyIntolerance.getPatient().getReference());
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

    public CarePlan searchAddCarePlan(String carePlanId,CarePlan carePlan) {
        log.info("CarePlan searchAdd " +carePlanId);

        if (carePlan == null) throw new InternalErrorException("Bundle processing error");

        CarePlan eprCarePlan = (CarePlan) resourceMap.get(carePlanId);

        // Organization already processed, quit with Organization
        if (eprCarePlan != null) return eprCarePlan;

        // Prevent re-adding the same Practitioner
        if (carePlan.getIdentifier().size() == 0) {
            carePlan.addIdentifier()
                    .setSystem("urn:uuid")
                    .setValue(carePlan.getId());
        }

        ProducerTemplate template = context.createProducerTemplate();

        InputStream inputStream = null;

        for (Identifier identifier : carePlan.getIdentifier()) {
            Exchange exchange = template.send("direct:FHIRCarePlan", ExchangePattern.InOut, new Processor() {
                public void process(Exchange exchange) throws Exception {
                    exchange.getIn().setHeader(Exchange.HTTP_QUERY, "identifier=" + identifier.getSystem() + "|" + identifier.getValue());
                    exchange.getIn().setHeader(Exchange.HTTP_METHOD, "GET");
                    exchange.getIn().setHeader(Exchange.HTTP_PATH, "CarePlan");
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
            if (iresource instanceof OperationOutcome) {
                processOperationOutcome((OperationOutcome) iresource);
            } else
            if (iresource instanceof Bundle) {
                Bundle returnedBundle = (Bundle) iresource;
                if (returnedBundle.getEntry().size()>0) {
                    eprCarePlan = (CarePlan) returnedBundle.getEntry().get(0).getResource();
                    log.info("Found CarePlan = " + eprCarePlan.getId());
                }
            }
        }

        if (carePlan.getContext().getReference() != null) {
            Resource resource = searchAddResource(carePlan.getContext().getReference());

            if (resource == null) referenceMissing(carePlan, carePlan.getContext().getReference());
            carePlan.setContext(getReference(resource));
        }
        if (carePlan.getSubject() != null) {
            Resource resource = searchAddResource(carePlan.getSubject().getReference());

            if (resource == null) referenceMissing(carePlan, carePlan.getSubject().getReference());
            carePlan.setSubject(getReference(resource));
        }
        List<Reference> references = new ArrayList<>();
        for (Reference reference : carePlan.getAddresses()) {
            Resource resource = searchAddResource(reference.getReference());

            if (resource!=null) references.add(getReference(resource));
        }
        carePlan.setAddresses(references);

        List<Reference> referenceTeam = new ArrayList<>();
        for (Reference reference : carePlan.getCareTeam()) {
            Resource resource = searchAddResource(reference.getReference());
            if (resource!=null) referenceTeam.add(getReference(resource));
        }
        carePlan.setCareTeam(referenceTeam);

        List<Reference> referenceSupporting = new ArrayList<>();
        for (Reference reference : carePlan.getSupportingInfo()) {
            Resource resource = searchAddResource(reference.getReference());
            if (resource == null) referenceMissing(carePlan, reference.getReference());
            if (resource!=null) referenceSupporting.add(getReference(resource));
        }
        carePlan.setSupportingInfo(referenceSupporting);

        IBaseResource iResource = null;
        String xhttpMethod = "POST";
        String xhttpPath = "CarePlan";
        // Location found do not add
        if (eprCarePlan != null) {
            xhttpMethod="PUT";
            // Want id value, no path or resource
            xhttpPath = "CarePlan/"+eprCarePlan.getIdElement().getIdPart();
            carePlan.setId(eprCarePlan.getId());
        }
        String httpBody = ctx.newJsonParser().encodeResourceToString(carePlan);
        String httpMethod= xhttpMethod;
        String httpPath = xhttpPath;
        try {
            Exchange exchange = template.send("direct:FHIRCarePlan", ExchangePattern.InOut, new Processor() {
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
        if (iResource instanceof CarePlan) {
            eprCarePlan = (CarePlan) iResource;
            setResourceMap(carePlanId,eprCarePlan);

        } else if (iResource instanceof OperationOutcome)
        {
            processOperationOutcome((OperationOutcome) iResource);
        } else {
            throw new InternalErrorException("Unknown Error");
        }

        return eprCarePlan;
    }

    public CareTeam searchAddCareTeam(String careTeamId,CareTeam careTeam) {
        log.info("CareTeam searchAdd " +careTeamId);

        if (careTeam == null) throw new InternalErrorException("Bundle processing error");

        CareTeam eprCareTeam = (CareTeam) resourceMap.get(careTeamId);

        // Organization already processed, quit with Organization
        if (eprCareTeam != null) return eprCareTeam;

        // Prevent re-adding the same Practitioner
        if (careTeam.getIdentifier().size() == 0) {
            careTeam.addIdentifier()
                    .setSystem("urn:uuid")
                    .setValue(careTeam.getId());
        }

        ProducerTemplate template = context.createProducerTemplate();

        InputStream inputStream = null;

        for (Identifier identifier : careTeam.getIdentifier()) {
            Exchange exchange = template.send("direct:FHIRCareTeam", ExchangePattern.InOut, new Processor() {
                public void process(Exchange exchange) throws Exception {
                    exchange.getIn().setHeader(Exchange.HTTP_QUERY, "identifier=" + identifier.getSystem() + "|" + identifier.getValue());
                    exchange.getIn().setHeader(Exchange.HTTP_METHOD, "GET");
                    exchange.getIn().setHeader(Exchange.HTTP_PATH, "CareTeam");
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
            if (iresource instanceof OperationOutcome) {
                processOperationOutcome((OperationOutcome) iresource);
            } else
            if (iresource instanceof Bundle) {
                Bundle returnedBundle = (Bundle) iresource;
                if (returnedBundle.getEntry().size()>0) {
                    eprCareTeam = (CareTeam) returnedBundle.getEntry().get(0).getResource();
                    log.info("Found CareTeam = " + eprCareTeam.getId());
                }
            }
        }

        if (careTeam.getContext().getReference() != null) {
            Resource resource = searchAddResource(careTeam.getContext().getReference());

            if (resource == null) referenceMissing(careTeam, careTeam.getContext().getReference());
            careTeam.setContext(getReference(resource));
        }
        if (careTeam.getSubject() != null) {
            Resource resource = searchAddResource(careTeam.getSubject().getReference());

            if (resource == null) referenceMissing(careTeam, careTeam.getSubject().getReference());
            careTeam.setSubject(getReference(resource));
        }
        List<Reference> references = new ArrayList<>();
        for (Reference reference : careTeam.getReasonReference()) {
            Resource resource = searchAddResource(reference.getReference());

            if (resource!=null) references.add(getReference(resource));
        }
        careTeam.setReasonReference(references);

        IBaseResource iResource = null;
        String xhttpMethod = "POST";
        String xhttpPath = "CareTeam";
        // Location found do not add
        if (eprCareTeam != null) {
            xhttpMethod="PUT";
            // Want id value, no path or resource
            xhttpPath = "CareTeam/"+eprCareTeam.getIdElement().getIdPart();
            careTeam.setId(eprCareTeam.getId());
        }
        String httpBody = ctx.newJsonParser().encodeResourceToString(careTeam);
        String httpMethod= xhttpMethod;
        String httpPath = xhttpPath;
        try {
            Exchange exchange = template.send("direct:FHIRCareTeam", ExchangePattern.InOut, new Processor() {
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
        if (iResource instanceof CareTeam) {
            eprCareTeam = (CareTeam) iResource;
            setResourceMap(careTeamId,eprCareTeam);

        } else if (iResource instanceof OperationOutcome)
        {
            processOperationOutcome((OperationOutcome) iResource);
        } else {
            throw new InternalErrorException("Unknown Error");
        }

        return eprCareTeam;
    }


    public QuestionnaireResponse searchAddQuestionnaireResponse(String formId,QuestionnaireResponse form) {
        log.info("QuestionnaireResponse searchAdd " +formId);

        if (form == null) throw new InternalErrorException("Bundle processing error");

        QuestionnaireResponse eprQuestionnaireResponse = (QuestionnaireResponse) resourceMap.get(formId);

        // Organization already processed, quit with Organization
        if (eprQuestionnaireResponse != null) return eprQuestionnaireResponse;

        // Prevent re-adding the same Practitioner
        if (!form.hasIdentifier()) {
            form.getIdentifier()
                    .setSystem("urn:uuid")
                    .setValue(form.getId());
        }

        ProducerTemplate template = context.createProducerTemplate();

        InputStream inputStream = null;

        Identifier identifier = form.getIdentifier();
        Exchange exchange = template.send("direct:FHIRQuestionnaireResponse", ExchangePattern.InOut, new Processor() {
            public void process(Exchange exchange) throws Exception {
                exchange.getIn().setHeader(Exchange.HTTP_QUERY, "identifier=" + identifier.getSystem() + "|" + identifier.getValue());
                exchange.getIn().setHeader(Exchange.HTTP_METHOD, "GET");
                exchange.getIn().setHeader(Exchange.HTTP_PATH, "QuestionnaireResponse");
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
        if (iresource instanceof OperationOutcome) {
            processOperationOutcome((OperationOutcome) iresource);
        } else
        if (iresource instanceof Bundle) {
            Bundle returnedBundle = (Bundle) iresource;
            if (returnedBundle.getEntry().size()>0) {
                eprQuestionnaireResponse = (QuestionnaireResponse) returnedBundle.getEntry().get(0).getResource();
                log.info("Found QuestionnaireResponse = " + eprQuestionnaireResponse.getId());
            }
        }


        if (form.getContext().getReference() != null) {
            Resource resource = searchAddResource(form.getContext().getReference());

            if (resource == null) referenceMissing(form, form.getContext().getReference());
            form.setContext(getReference(resource));
        }
        if (form.getSubject() != null) {
            Resource resource = searchAddResource(form.getSubject().getReference());

            if (resource == null) referenceMissing(form, form.getSubject().getReference());
            form.setSubject(getReference(resource));
        }
        if (form.hasSource()) {
            Resource resource = searchAddResource(form.getSource().getReference());

            if (resource == null) referenceMissing(form, form.getSource().getReference());
            form.setSource(getReference(resource));
        }
        if (form.hasAuthor()) {
            Resource resource = searchAddResource(form.getAuthor().getReference());

            if (resource == null) referenceMissing(form, form.getAuthor().getReference());
            form.setAuthor(getReference(resource));
        }


        IBaseResource iResource = null;
        String xhttpMethod = "POST";
        String xhttpPath = "QuestionnaireResponse";
        // Location found do not add
        if (eprQuestionnaireResponse != null) {
            xhttpMethod="PUT";
            // Want id value, no path or resource
            xhttpPath = "QuestionnaireResponse/"+eprQuestionnaireResponse.getIdElement().getIdPart();
            form.setId(eprQuestionnaireResponse.getId());
        }
        String httpBody = ctx.newJsonParser().encodeResourceToString(form);
        String httpMethod= xhttpMethod;
        String httpPath = xhttpPath;
        try {
            exchange = template.send("direct:FHIRQuestionnaireResponse", ExchangePattern.InOut, new Processor() {
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
        if (iResource instanceof QuestionnaireResponse) {
            eprQuestionnaireResponse = (QuestionnaireResponse) iResource;
            setResourceMap(formId,eprQuestionnaireResponse);

        } else if (iResource instanceof OperationOutcome)
        {
            processOperationOutcome((OperationOutcome) iResource);
        } else {
            throw new InternalErrorException("Unknown Error");
        }

        return eprQuestionnaireResponse;
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
                    exchange.getIn().setHeader(Exchange.HTTP_QUERY, "identifier=" + identifier.getSystem() + "|" + identifier.getValue());
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
            if (iresource instanceof OperationOutcome) {
                processOperationOutcome((OperationOutcome) iresource);
            } else
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

            if (resource == null) referenceMissing(observation, observation.getSubject().getReference());
            observation.setSubject(getReference(resource));
        }
        if (observation.hasContext()) {
            Resource resource = searchAddResource(observation.getContext().getReference());

            if (resource == null) referenceMissing(observation, observation.getContext().getReference());
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
                    exchange.getIn().setHeader(Exchange.HTTP_QUERY, "identifier=" + identifier.getSystem() + "|" + identifier.getValue());
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
            if (iresource instanceof OperationOutcome) {
                processOperationOutcome((OperationOutcome) iresource);
            }  else {
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

            if (resource == null) referenceMissing(diagnosticReport, performer.getActor().getReference());
            performer.setActor(getReference(resource));
        }


        if (diagnosticReport.hasSubject()) {
            Resource resource = searchAddResource(diagnosticReport.getSubject().getReference());

            if (resource == null) referenceMissing(diagnosticReport, diagnosticReport.getSubject().getReference());
            diagnosticReport.setSubject(getReference(resource));
        }
        if (diagnosticReport.hasContext()) {
            Resource resource = searchAddResource(diagnosticReport.getContext().getReference());
            if (resource == null) referenceMissing(diagnosticReport, diagnosticReport.getContext().getReference());
            diagnosticReport.setContext(getReference(resource));
        }

        List<Reference> results = new ArrayList<>();
        for (Reference reference : diagnosticReport.getResult()) {
            Resource resource = searchAddResource(reference.getReference());
            if (resource == null) referenceMissing(diagnosticReport, reference.getReference());
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

    // KGM 22/Jan/2018 Added Immunization processing

    public Immunization searchAddImmunization(String immunisationId,Immunization immunisation) {
        log.info("Immunization searchAdd " +immunisationId);

        if (immunisation == null) throw new InternalErrorException("Bundle processing error");

        Immunization eprImmunization = (Immunization) resourceMap.get(immunisationId);

        // Organization already processed, quit with Organization
        if (eprImmunization != null) return eprImmunization;

        // Prevent re-adding the same Practitioner
        if (immunisation.getIdentifier().size() == 0) {
            // Use a custom identifier
            immunisation.addIdentifier()
                    .setSystem("urn:uuid")
                    .setValue(immunisation.getDate().toString()+"-"+immunisation.getVaccineCode().getCodingFirstRep().getCode());
        }

        ProducerTemplate template = context.createProducerTemplate();

        InputStream inputStream = null;

        for (Identifier identifier : immunisation.getIdentifier()) {
            Exchange exchange = template.send("direct:FHIRImmunization", ExchangePattern.InOut, new Processor() {
                public void process(Exchange exchange) throws Exception {
                    exchange.getIn().setHeader(Exchange.HTTP_QUERY, "identifier=" + identifier.getSystem() + "|" + identifier.getValue());
                    exchange.getIn().setHeader(Exchange.HTTP_METHOD, "GET");
                    exchange.getIn().setHeader(Exchange.HTTP_PATH, "Immunization");
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
            if (iresource instanceof OperationOutcome) {
                processOperationOutcome((OperationOutcome) iresource);
            } else
            if (iresource instanceof Bundle) {
                Bundle returnedBundle = (Bundle) iresource;
                if (returnedBundle.getEntry().size()>0) {
                    eprImmunization = (Immunization) returnedBundle.getEntry().get(0).getResource();
                    log.info("Found Immunization = " + eprImmunization.getId());
                }
            }
        }


        // Location not found. Add to database


        for (Immunization.ImmunizationPractitionerComponent reference : immunisation.getPractitioner()) {
            Resource resource = searchAddResource(reference.getActor().getReference());

            if (resource!=null) reference.setActor(getReference(resource));
        }

        if (immunisation.hasPatient()) {
            Resource resource = searchAddResource(immunisation.getPatient().getReference());
            if (resource == null) referenceMissing(immunisation, immunisation.getPatient().getReference());
            immunisation.setPatient(getReference(resource));
        }
        if (immunisation.hasEncounter()) {
            Resource resource = searchAddResource(immunisation.getEncounter().getReference());
            if (resource == null) referenceMissing(immunisation, immunisation.getEncounter().getReference());
            immunisation.setEncounter(getReference(resource));
        }

        IBaseResource iResource = null;

        String xhttpMethod = "POST";
        String xhttpPath = "Immunization";
        // Location found do not add
        if (eprImmunization != null) {
            xhttpMethod="PUT";
            // Want id value, no path or resource
            xhttpPath = "Immunization/"+eprImmunization.getIdElement().getIdPart();
            immunisation.setId(eprImmunization.getId());
        }
        String httpBody = ctx.newJsonParser().encodeResourceToString(immunisation);
        String httpMethod= xhttpMethod;
        String httpPath = xhttpPath;
        try {
            Exchange exchange = template.send("direct:FHIRImmunization", ExchangePattern.InOut, new Processor() {
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
        if (iResource instanceof Immunization) {
            eprImmunization = (Immunization) iResource;
            setResourceMap(immunisationId,eprImmunization);

        } else if (iResource instanceof OperationOutcome)
        {
            processOperationOutcome((OperationOutcome) iResource);
        } else {
            throw new InternalErrorException("Unknown Error");
        }

        return eprImmunization;
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
                    exchange.getIn().setHeader(Exchange.HTTP_QUERY, "identifier=" + identifier.getSystem() + "|" + identifier.getValue());
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
            if (iresource instanceof OperationOutcome) {
                processOperationOutcome((OperationOutcome) iresource);
            } else
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
            if (resource == null) referenceMissing(medicationRequest, medicationRequest.getSubject().getReference());
            medicationRequest.setSubject(getReference(resource));
        }
        if (medicationRequest.hasContext()) {
            Resource resource = searchAddResource(medicationRequest.getContext().getReference());
            if (resource == null) referenceMissing(medicationRequest, medicationRequest.getContext().getReference());
            medicationRequest.setContext(getReference(resource));
        }

        if (medicationRequest.hasRequester()) {
            Resource resource = searchAddResource(medicationRequest.getRequester().getAgent().getReference());
            if (resource == null) referenceMissing(medicationRequest, medicationRequest.getRequester().getAgent().getReference());
            medicationRequest.getRequester().setAgent(getReference(resource));
        }

        if (medicationRequest.hasMedicationReference()) {
            Resource resource = null;
            String reference = "";
            try {
                reference = medicationRequest.getMedicationReference().getReference();
                resource = searchAddResource(reference);
                if (resource == null) referenceMissing(medicationRequest, medicationRequest.getMedicationReference().getReference());
                medicationRequest.setMedication(getReference(resource));
            } catch (Exception exMed) {}

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

    public Medication searchAddMedication(String MedicationId,Medication Medication) {
        log.info("Medication searchAdd " +MedicationId);

        if (Medication == null) throw new InternalErrorException("Bundle processing error");

        Medication eprMedication = (Medication) resourceMap.get(MedicationId);

        // Organization already processed, quit with Organization
        if (eprMedication != null) return eprMedication;


        ProducerTemplate template = context.createProducerTemplate();

        InputStream inputStream = null;

        for (Coding code : Medication.getCode().getCoding()) {
            Exchange exchange = template.send("direct:FHIRMedication", ExchangePattern.InOut, new Processor() {
                public void process(Exchange exchange) throws Exception {
                    exchange.getIn().setHeader(Exchange.HTTP_QUERY, "code=" + code.getSystem() + "|" + code.getCode());
                    exchange.getIn().setHeader(Exchange.HTTP_METHOD, "GET");
                    exchange.getIn().setHeader(Exchange.HTTP_PATH, "Medication");
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
            if (iresource instanceof OperationOutcome) {
                processOperationOutcome((OperationOutcome) iresource);
            } else
            if (iresource instanceof Bundle) {
                Bundle returnedBundle = (Bundle) iresource;
                if (returnedBundle.getEntry().size()>0) {
                    eprMedication = (Medication) returnedBundle.getEntry().get(0).getResource();
                    log.info("Found Medication = " + eprMedication.getId());
                }
            }
        }


        // Location not found. Add to database





        IBaseResource iResource = null;

        String xhttpMethod = "POST";
        String xhttpPath = "Medication";
        // Location found do not add
        if (eprMedication != null) {
            xhttpMethod="PUT";
            // Want id value, no path or resource
            xhttpPath = "Medication/"+eprMedication.getIdElement().getIdPart();
            Medication.setId(eprMedication.getId());
        }
        String httpBody = ctx.newJsonParser().encodeResourceToString(Medication);
        String httpMethod= xhttpMethod;
        String httpPath = xhttpPath;
        try {
            Exchange exchange = template.send("direct:FHIRMedication", ExchangePattern.InOut, new Processor() {
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
        if (iResource instanceof Medication) {
            eprMedication = (Medication) iResource;
            setResourceMap(MedicationId,eprMedication);

        } else if (iResource instanceof OperationOutcome)
        {
            processOperationOutcome((OperationOutcome) iResource);
        } else {
            throw new InternalErrorException("Unknown Error");
        }

        return eprMedication;
    }



    public RiskAssessment searchAddRiskAssessment(String riskAssessmentId,RiskAssessment riskAssessment) {
        log.info("RiskAssessment searchAdd " +riskAssessmentId);

        if (riskAssessment == null) throw new InternalErrorException("Bundle processing error");

        RiskAssessment eprRiskAssessment = (RiskAssessment) resourceMap.get(riskAssessmentId);

        // Organization already processed, quit with Organization
        if (eprRiskAssessment != null) return eprRiskAssessment;

        // Prevent re-adding the same Practitioner
        if (!riskAssessment.hasIdentifier()) {
            riskAssessment.getIdentifier()
                    .setSystem("urn:uuid")
                    .setValue(riskAssessment.getId());
        }

        ProducerTemplate template = context.createProducerTemplate();

        InputStream inputStream = null;

        if (riskAssessment.hasIdentifier()) {
            Identifier identifier = riskAssessment.getIdentifier();
            Exchange exchange = template.send("direct:FHIRRiskAssessment", ExchangePattern.InOut, new Processor() {
                public void process(Exchange exchange) throws Exception {
                    exchange.getIn().setHeader(Exchange.HTTP_QUERY, "identifier=" + identifier.getSystem() + "|" + identifier.getValue());
                    exchange.getIn().setHeader(Exchange.HTTP_METHOD, "GET");
                    exchange.getIn().setHeader(Exchange.HTTP_PATH, "RiskAssessment");
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
            if (iresource instanceof OperationOutcome) {
                processOperationOutcome((OperationOutcome) iresource);
            } else
            if (iresource instanceof Bundle) {
                Bundle returnedBundle = (Bundle) iresource;
                if (returnedBundle.getEntry().size()>0) {
                    eprRiskAssessment = (RiskAssessment) returnedBundle.getEntry().get(0).getResource();
                    log.info("Found RiskAssessment = " + eprRiskAssessment.getId());
                }
            }
        }


        // Location not found. Add to database



        if (riskAssessment.hasSubject()) {
            Resource resource = searchAddResource(riskAssessment.getSubject().getReference());
            if (resource == null) referenceMissing(riskAssessment, riskAssessment.getSubject().getReference());
            riskAssessment.setSubject(getReference(resource));
        }
        if (riskAssessment.hasContext()) {
            Resource resource = searchAddResource(riskAssessment.getContext().getReference());
            if (resource == null) referenceMissing(riskAssessment, riskAssessment.getContext().getReference());
            riskAssessment.setContext(getReference(resource));
        }
        if (riskAssessment.hasCondition()) {
            Resource resource = searchAddResource(riskAssessment.getCondition().getReference());
            if (resource == null) referenceMissing(riskAssessment, riskAssessment.getCondition().getReference());
            riskAssessment.setCondition(getReference(resource));
        }

        IBaseResource iResource = null;

        String xhttpMethod = "POST";
        String xhttpPath = "RiskAssessment";
        // Location found do not add
        if (eprRiskAssessment != null) {
            xhttpMethod="PUT";
            // Want id value, no path or resource
            xhttpPath = "RiskAssessment/"+eprRiskAssessment.getIdElement().getIdPart();
            riskAssessment.setId(eprRiskAssessment.getId());
        }
        String httpBody = ctx.newJsonParser().encodeResourceToString(riskAssessment);
        String httpMethod= xhttpMethod;
        String httpPath = xhttpPath;
        try {
            Exchange exchange = template.send("direct:FHIRRiskAssessment", ExchangePattern.InOut, new Processor() {
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
        if (iResource instanceof RiskAssessment) {
            eprRiskAssessment = (RiskAssessment) iResource;
            setResourceMap(riskAssessmentId,eprRiskAssessment);

        } else if (iResource instanceof OperationOutcome)
        {
            processOperationOutcome((OperationOutcome) iResource);
        } else {
            throw new InternalErrorException("Unknown Error");
        }

        return eprRiskAssessment;
    }


    public ClinicalImpression searchAddClinicalImpression(String impressionId,ClinicalImpression impression) {
        log.info("ClinicalImpression searchAdd " +impressionId);

        if (impression == null) throw new InternalErrorException("Bundle processing error");

        ClinicalImpression eprClinicalImpression = (ClinicalImpression) resourceMap.get(impressionId);

        // Organization already processed, quit with Organization
        if (eprClinicalImpression != null) return eprClinicalImpression;

        // Prevent re-adding the same Practitioner
        if (!impression.hasIdentifier()) {
            impression.addIdentifier()
                    .setSystem("urn:uuid")
                    .setValue(impression.getId());
        }

        ProducerTemplate template = context.createProducerTemplate();

        InputStream inputStream = null;

        for (Identifier identifier : impression.getIdentifier()) {
            Exchange exchange = template.send("direct:FHIRClinicalImpression", ExchangePattern.InOut, new Processor() {
                public void process(Exchange exchange) throws Exception {
                    exchange.getIn().setHeader(Exchange.HTTP_QUERY, "identifier=" + identifier.getSystem() + "|" + identifier.getValue());
                    exchange.getIn().setHeader(Exchange.HTTP_METHOD, "GET");
                    exchange.getIn().setHeader(Exchange.HTTP_PATH, "ClinicalImpression");
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
            if (iresource instanceof OperationOutcome) {
                processOperationOutcome((OperationOutcome) iresource);
            } else
            if (iresource instanceof Bundle) {
                Bundle returnedBundle = (Bundle) iresource;
                if (returnedBundle.getEntry().size()>0) {
                    eprClinicalImpression = (ClinicalImpression) returnedBundle.getEntry().get(0).getResource();
                    log.info("Found ClinicalImpression = " + eprClinicalImpression.getId());
                }
            }
        }


        // Location not found. Add to database



        if (impression.hasSubject()) {
            Resource resource = searchAddResource(impression.getSubject().getReference());
            if (resource == null) referenceMissing(impression, impression.getSubject().getReference());
            impression.setSubject(getReference(resource));
        }
        if (impression.hasContext()) {
            Resource resource = searchAddResource(impression.getContext().getReference());
            if (resource == null) referenceMissing(impression, impression.getContext().getReference());
            impression.setContext(getReference(resource));
        }
        if (impression.hasAssessor()) {
            Resource resource = searchAddResource(impression.getAssessor().getReference());
            if (resource == null) referenceMissing(impression, impression.getAssessor().getReference());
            impression.setAssessor(getReference(resource));
        }


        IBaseResource iResource = null;

        String xhttpMethod = "POST";
        String xhttpPath = "ClinicalImpression";
        // Location found do not add
        if (eprClinicalImpression != null) {
            xhttpMethod="PUT";
            // Want id value, no path or resource
            xhttpPath = "ClinicalImpression/"+eprClinicalImpression.getIdElement().getIdPart();
            impression.setId(eprClinicalImpression.getId());
        }
        String httpBody = ctx.newJsonParser().encodeResourceToString(impression);
        String httpMethod= xhttpMethod;
        String httpPath = xhttpPath;
        try {
            Exchange exchange = template.send("direct:FHIRClinicalImpression", ExchangePattern.InOut, new Processor() {
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
        if (iResource instanceof ClinicalImpression) {
            eprClinicalImpression = (ClinicalImpression) iResource;
            setResourceMap(impressionId,eprClinicalImpression);

        } else if (iResource instanceof OperationOutcome)
        {
            processOperationOutcome((OperationOutcome) iResource);
        } else {
            throw new InternalErrorException("Unknown Error");
        }

        return eprClinicalImpression;
    }

    public Consent searchAddConsent(String consentId,Consent consent) {
        log.info("Consent searchAdd " +consentId);

        if (consent == null) throw new InternalErrorException("Bundle processing error");

        Consent eprConsent = (Consent) resourceMap.get(consentId);

        // Organization already processed, quit with Organization
        if (eprConsent != null) return eprConsent;

        // Prevent re-adding the same Practitioner
        if (!consent.hasIdentifier()) {
            consent.getIdentifier()
                    .setSystem("urn:uuid")
                    .setValue(consent.getId());
        }

        ProducerTemplate template = context.createProducerTemplate();

        InputStream inputStream = null;

        if (consent.hasIdentifier()) {
            Identifier identifier = consent.getIdentifier();
            Exchange exchange = template.send("direct:FHIRConsent", ExchangePattern.InOut, new Processor() {
                public void process(Exchange exchange) throws Exception {
                    exchange.getIn().setHeader(Exchange.HTTP_QUERY, "identifier=" + identifier.getSystem() + "|" + identifier.getValue());
                    exchange.getIn().setHeader(Exchange.HTTP_METHOD, "GET");
                    exchange.getIn().setHeader(Exchange.HTTP_PATH, "Consent");
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
            if (iresource instanceof OperationOutcome) {
                processOperationOutcome((OperationOutcome) iresource);
            } else
            if (iresource instanceof Bundle) {
                Bundle returnedBundle = (Bundle) iresource;
                if (returnedBundle.getEntry().size()>0) {
                    eprConsent = (Consent) returnedBundle.getEntry().get(0).getResource();
                    log.info("Found Consent = " + eprConsent.getId());
                }
            }
        }


        // Location not found. Add to database



        if (consent.hasPatient()) {
            Resource resource = searchAddResource(consent.getPatient().getReference());
            if (resource == null) referenceMissing(consent, consent.getPatient().getReference());
            consent.setPatient(getReference(resource));
        }
        if (consent.hasOrganization()) {
            List<Reference> organisations = new ArrayList<>();
            for (Reference reference : consent.getOrganization()) {
                Resource resource = searchAddResource(reference.getReference());
                if (resource == null) referenceMissing(consent, reference.getReference());
                organisations.add(getReference(resource));
            }
            consent.setOrganization(organisations);

        }
        if (consent.hasConsentingParty()) {
            List<Reference> parties = new ArrayList<>();
            for (Reference reference : consent.getConsentingParty()) {
                Resource resource = searchAddResource(reference.getReference());
                if (resource == null) referenceMissing(consent, reference.getReference());
                parties.add(getReference(resource));
            }
            consent.setConsentingParty(parties);
        }

        if (consent.hasActor()) {

            for (Consent.ConsentActorComponent consentActorComponent : consent.getActor()) {
                if (consentActorComponent.hasReference()) {
                    Resource resource = searchAddResource(consentActorComponent.getReference().getReference());
                    if (resource == null) referenceMissing(consent, consentActorComponent.getReference().getReference());
                    consentActorComponent.setReference(getReference(resource));
                }
            }
        }

        IBaseResource iResource = null;

        String xhttpMethod = "POST";
        String xhttpPath = "Consent";
        // Location found do not add
        if (eprConsent != null) {
            xhttpMethod="PUT";
            // Want id value, no path or resource
            xhttpPath = "Consent/"+eprConsent.getIdElement().getIdPart();
            consent.setId(eprConsent.getId());
        }
        String httpBody = ctx.newJsonParser().encodeResourceToString(consent);
        String httpMethod= xhttpMethod;
        String httpPath = xhttpPath;
        try {
            Exchange exchange = template.send("direct:FHIRConsent", ExchangePattern.InOut, new Processor() {
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
        if (iResource instanceof Consent) {
            eprConsent = (Consent) iResource;
            setResourceMap(consentId,eprConsent);

        } else if (iResource instanceof OperationOutcome)
        {
            processOperationOutcome((OperationOutcome) iResource);
        } else {
            throw new InternalErrorException("Unknown Error");
        }

        return eprConsent;
    }

    public Goal searchAddGoal(String goalId,Goal goal) {
        log.info("Goal searchAdd " +goalId);

        if (goal == null) throw new InternalErrorException("Bundle processing error");

        Goal eprGoal = (Goal) resourceMap.get(goalId);

        // Organization already processed, quit with Organization
        if (eprGoal != null) return eprGoal;

        // Prevent re-adding the same Practitioner
        if (goal.getIdentifier().size() == 0) {
            goal.addIdentifier()
                    .setSystem("urn:uuid")
                    .setValue(goal.getId());
        }

        ProducerTemplate template = context.createProducerTemplate();

        InputStream inputStream = null;

        for (Identifier identifier : goal.getIdentifier()) {
            Exchange exchange = template.send("direct:FHIRGoal", ExchangePattern.InOut, new Processor() {
                public void process(Exchange exchange) throws Exception {
                    exchange.getIn().setHeader(Exchange.HTTP_QUERY, "identifier=" + identifier.getSystem() + "|" + identifier.getValue());
                    exchange.getIn().setHeader(Exchange.HTTP_METHOD, "GET");
                    exchange.getIn().setHeader(Exchange.HTTP_PATH, "Goal");
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
            if (iresource instanceof OperationOutcome) {
                processOperationOutcome((OperationOutcome) iresource);
            } else
            if (iresource instanceof Bundle) {
                Bundle returnedBundle = (Bundle) iresource;
                if (returnedBundle.getEntry().size()>0) {
                    eprGoal = (Goal) returnedBundle.getEntry().get(0).getResource();
                    log.info("Found Goal = " + eprGoal.getId());
                }
            }
        }


        // Location not found. Add to database



        if (goal.hasSubject()) {
            Resource resource = searchAddResource(goal.getSubject().getReference());
            if (resource == null) referenceMissing(goal, goal.getSubject().getReference());
            goal.setSubject(getReference(resource));
        }


        IBaseResource iResource = null;

        String xhttpMethod = "POST";
        String xhttpPath = "Goal";
        // Location found do not add
        if (eprGoal != null) {
            xhttpMethod="PUT";
            // Want id value, no path or resource
            xhttpPath = "Goal/"+eprGoal.getIdElement().getIdPart();
            goal.setId(eprGoal.getId());
        }
        String httpBody = ctx.newJsonParser().encodeResourceToString(goal);
        String httpMethod= xhttpMethod;
        String httpPath = xhttpPath;
        try {
            Exchange exchange = template.send("direct:FHIRGoal", ExchangePattern.InOut, new Processor() {
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
        if (iResource instanceof Goal) {
            eprGoal = (Goal) iResource;
            setResourceMap(goalId,eprGoal);

        } else if (iResource instanceof OperationOutcome)
        {
            processOperationOutcome((OperationOutcome) iResource);
        } else {
            throw new InternalErrorException("Unknown Error");
        }

        return eprGoal;
    }

    public MedicationDispense searchAddMedicationDispense(String medicationDispenseId,MedicationDispense medicationDispense) {
        log.info("MedicationDispense searchAdd " +medicationDispenseId);

        if (medicationDispense == null) throw new InternalErrorException("Bundle processing error");

        MedicationDispense eprMedicationDispense = (MedicationDispense) resourceMap.get(medicationDispenseId);

        // Organization already processed, quit with Organization
        if (eprMedicationDispense != null) return eprMedicationDispense;

        // Prevent re-adding the same Practitioner
        if (medicationDispense.getIdentifier().size() == 0) {
            medicationDispense.addIdentifier()
                    .setSystem("urn:uuid")
                    .setValue(medicationDispense.getId());
        }

        ProducerTemplate template = context.createProducerTemplate();

        InputStream inputStream = null;

        for (Identifier identifier : medicationDispense.getIdentifier()) {
            Exchange exchange = template.send("direct:FHIRMedicationDispense", ExchangePattern.InOut, new Processor() {
                public void process(Exchange exchange) throws Exception {
                    exchange.getIn().setHeader(Exchange.HTTP_QUERY, "identifier=" + identifier.getSystem() + "|" + identifier.getValue());
                    exchange.getIn().setHeader(Exchange.HTTP_METHOD, "GET");
                    exchange.getIn().setHeader(Exchange.HTTP_PATH, "MedicationDispense");
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
            if (iresource instanceof OperationOutcome) {
                processOperationOutcome((OperationOutcome) iresource);
            } else
            if (iresource instanceof Bundle) {
                Bundle returnedBundle = (Bundle) iresource;
                if (returnedBundle.getEntry().size()>0) {
                    eprMedicationDispense = (MedicationDispense) returnedBundle.getEntry().get(0).getResource();
                    log.info("Found MedicationDispense = " + eprMedicationDispense.getId());
                }
            }
        }


        // Location not found. Add to database



        if (medicationDispense.hasSubject()) {
            Resource resource = searchAddResource(medicationDispense.getSubject().getReference());
            if (resource == null) referenceMissing(medicationDispense, medicationDispense.getSubject().getReference());
            medicationDispense.setSubject(getReference(resource));
        }
        if (medicationDispense.hasContext()) {
            Resource resource = searchAddResource(medicationDispense.getContext().getReference());
            if (resource == null) referenceMissing(medicationDispense, medicationDispense.getContext().getReference());
            medicationDispense.setContext(getReference(resource));
        }

        if (medicationDispense.hasAuthorizingPrescription()) {
            List<Reference> pres = new ArrayList<>();
            for (Reference reference : medicationDispense.getAuthorizingPrescription()) {
                Resource resource = searchAddResource(reference.getReference());
                if (resource == null) referenceMissing(medicationDispense, reference.getReference());
                pres.add(getReference(resource));
            }
            medicationDispense.setAuthorizingPrescription(pres);
        }

        if (medicationDispense.hasPerformer()) {
            if (medicationDispense.getPerformerFirstRep().hasActor()) {
                Resource resource = searchAddResource(medicationDispense.getPerformerFirstRep().getActor().getReference());
                if (resource == null) referenceMissing(medicationDispense, medicationDispense.getPerformerFirstRep().getActor().getReference());
                medicationDispense.getPerformerFirstRep().setActor(getReference(resource));
            }
            if (medicationDispense.getPerformerFirstRep().hasOnBehalfOf()) {
                Resource resource = searchAddResource(medicationDispense.getPerformerFirstRep().getOnBehalfOf().getReference());
                if (resource == null) referenceMissing(medicationDispense, medicationDispense.getPerformerFirstRep().getOnBehalfOf().getReference());
                medicationDispense.getPerformerFirstRep().setOnBehalfOf(getReference(resource));
            }
        }

        if (medicationDispense.hasReceiver()) {
            List<Reference> recv = new ArrayList<>();
            for (Reference reference : medicationDispense.getReceiver()) {
                Resource resource = searchAddResource(reference.getReference());
                if (resource == null) referenceMissing(medicationDispense, reference.getReference());
                recv.add(getReference(resource));
            }
            medicationDispense.setReceiver(recv);
        }

        if (medicationDispense.hasMedicationReference()) {
            Resource resource = null;
            String reference = "";
           try {
               reference = medicationDispense.getMedicationReference().getReference();
               resource = searchAddResource(reference);
               if (resource == null) referenceMissing(medicationDispense, medicationDispense.getMedicationReference().getReference());
               medicationDispense.setMedication(getReference(resource));
           } catch (Exception ex) {}

        }

        IBaseResource iResource = null;

        String xhttpMethod = "POST";
        String xhttpPath = "MedicationDispense";
        // Location found do not add
        if (eprMedicationDispense != null) {
            xhttpMethod="PUT";
            // Want id value, no path or resource
            xhttpPath = "MedicationDispense/"+eprMedicationDispense.getIdElement().getIdPart();
            medicationDispense.setId(eprMedicationDispense.getId());
        }
        String httpBody = ctx.newJsonParser().encodeResourceToString(medicationDispense);
        String httpMethod= xhttpMethod;
        String httpPath = xhttpPath;
        try {
            Exchange exchange = template.send("direct:FHIRMedicationDispense", ExchangePattern.InOut, new Processor() {
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
        if (iResource instanceof MedicationDispense) {
            eprMedicationDispense = (MedicationDispense) iResource;
            setResourceMap(medicationDispenseId,eprMedicationDispense);

        } else if (iResource instanceof OperationOutcome)
        {
            processOperationOutcome((OperationOutcome) iResource);
        } else {
            throw new InternalErrorException("Unknown Error");
        }

        return eprMedicationDispense;
    }

    public MedicationStatement searchAddMedicationStatement(String medicationStatementId,MedicationStatement medicationStatement) {
        log.info("MedicationStatement searchAdd " +medicationStatementId);

        if (medicationStatement == null) throw new InternalErrorException("Bundle processing error");

        MedicationStatement eprMedicationStatement = (MedicationStatement) resourceMap.get(medicationStatementId);

        // Organization already processed, quit with Organization
        if (eprMedicationStatement != null) return eprMedicationStatement;

        // Prevent re-adding the same Practitioner
        if (medicationStatement.getIdentifier().size() == 0) {
            medicationStatement.addIdentifier()
                    .setSystem("urn:uuid")
                    .setValue(medicationStatement.getId());
        }

        ProducerTemplate template = context.createProducerTemplate();

        InputStream inputStream = null;

        for (Identifier identifier : medicationStatement.getIdentifier()) {
            Exchange exchange = template.send("direct:FHIRMedicationStatement", ExchangePattern.InOut, new Processor() {
                public void process(Exchange exchange) throws Exception {
                    exchange.getIn().setHeader(Exchange.HTTP_QUERY, "identifier=" + identifier.getSystem() + "|" + identifier.getValue());
                    exchange.getIn().setHeader(Exchange.HTTP_METHOD, "GET");
                    exchange.getIn().setHeader(Exchange.HTTP_PATH, "MedicationStatement");
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
            if (iresource instanceof OperationOutcome) {
                processOperationOutcome((OperationOutcome) iresource);
            } else
            if (iresource instanceof Bundle) {
                Bundle returnedBundle = (Bundle) iresource;
                if (returnedBundle.getEntry().size()>0) {
                    eprMedicationStatement = (MedicationStatement) returnedBundle.getEntry().get(0).getResource();
                    log.info("Found MedicationStatement = " + eprMedicationStatement.getId());
                }
            }
        }


        // Location not found. Add to database

        if (medicationStatement.hasSubject()) {
            Resource resource = searchAddResource(medicationStatement.getSubject().getReference());
            if (resource == null) referenceMissing(medicationStatement, medicationStatement.getSubject().getReference());
            medicationStatement.setSubject(getReference(resource));
        }
        if (medicationStatement.hasContext()) {
            Resource resource = searchAddResource(medicationStatement.getContext().getReference());
            if (resource == null) referenceMissing(medicationStatement, medicationStatement.getContext().getReference());
            medicationStatement.setContext(getReference(resource));
        }


        if (medicationStatement.hasMedicationReference()) {
            Resource resource = null;
            String reference = "";
            try {
                reference = medicationStatement.getMedicationReference().getReference();
                resource = searchAddResource(reference);
                if (resource == null) referenceMissing(medicationStatement, medicationStatement.getMedicationReference().getReference());
                medicationStatement.setMedication(getReference(resource));
            } catch (Exception ex) {}
        }
        List<Reference> based = new ArrayList<>();
        if (medicationStatement.hasBasedOn()) {
            for (Reference ref : medicationStatement.getBasedOn()) {

                    Resource resource = searchAddResource(ref.getReference());
                    if (resource == null) referenceMissing(medicationStatement, ref.getReference());
                    based.add(getReference(resource));
            }
        }
        medicationStatement.setBasedOn(based);

        List<Reference> derived = new ArrayList<>();
        if (medicationStatement.hasDerivedFrom()) {
            for (Reference ref : medicationStatement.getDerivedFrom()) {

                Resource resource = searchAddResource(ref.getReference());
                if (resource == null) referenceMissing(medicationStatement, ref.getReference());
                derived.add(getReference(resource));
            }
        }
        medicationStatement.setDerivedFrom(derived);

        List<Reference> reason = new ArrayList<>();
        if (medicationStatement.hasReasonReference()) {
            for (Reference ref : medicationStatement.getReasonReference()) {

                Resource resource = searchAddResource(ref.getReference());
                if (resource == null) referenceMissing(medicationStatement, ref.getReference());
                reason.add(getReference(resource));
            }
        }
        medicationStatement.setReasonReference(reason);

        List<Reference> parts = new ArrayList<>();
        if (medicationStatement.hasPartOf()) {
            for (Reference ref : medicationStatement.getPartOf()) {

                Resource resource = searchAddResource(ref.getReference());
                if (resource == null) referenceMissing(medicationStatement, ref.getReference());
                parts.add(getReference(resource));
            }
        }
        medicationStatement.setPartOf(parts);

        IBaseResource iResource = null;

        String xhttpMethod = "POST";
        String xhttpPath = "MedicationStatement";
        // Location found do not add
        if (eprMedicationStatement != null) {
            xhttpMethod="PUT";
            // Want id value, no path or resource
            xhttpPath = "MedicationStatement/"+eprMedicationStatement.getIdElement().getIdPart();
            medicationStatement.setId(eprMedicationStatement.getId());
        }
        String httpBody = ctx.newJsonParser().encodeResourceToString(medicationStatement);
        String httpMethod= xhttpMethod;
        String httpPath = xhttpPath;
        try {
            Exchange exchange = template.send("direct:FHIRMedicationStatement", ExchangePattern.InOut, new Processor() {
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
        if (iResource instanceof MedicationStatement) {
            eprMedicationStatement = (MedicationStatement) iResource;
            setResourceMap(medicationStatementId,eprMedicationStatement);

        } else if (iResource instanceof OperationOutcome)
        {
            processOperationOutcome((OperationOutcome) iResource);
        } else {
            throw new InternalErrorException("Unknown Error");
        }

        return eprMedicationStatement;
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
                    exchange.getIn().setHeader(Exchange.HTTP_QUERY, "identifier=" + identifier.getSystem() + "|" + identifier.getValue());
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
            if (iresource instanceof OperationOutcome) {
                processOperationOutcome((OperationOutcome) iresource);
            } else
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
            if (resource == null) referenceMissing(condition, condition.getAsserter().getReference());
            log.info("Found Resource = " + resource.getId());
            condition.setAsserter(getReference(resource));
        }
        if (condition.getSubject() != null) {
            Resource resource = searchAddResource(condition.getSubject().getReference());
            if (resource == null) referenceMissing(condition, condition.getSubject().getReference());
            condition.setSubject(getReference(resource));
        }
        if (condition.getContext().getReference() != null) {
            Resource resource = searchAddResource(condition.getContext().getReference());
            if (resource == null) referenceMissing(condition, condition.getContext().getReference());
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

        String identifierUrl = "identifier=" + composition.getIdentifier().getSystem() + "|" + composition.getIdentifier().getValue();
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
        if (iresource instanceof OperationOutcome) {
            processOperationOutcome((OperationOutcome) iresource);
        } else
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
            if (resource == null) referenceMissing(composition, composition.getEncounter().getReference());
            composition.setEncounter(getReference(resource));
        }
        if (composition.hasAttester()) {
            for (Composition.CompositionAttesterComponent attester : composition.getAttester()){
                Resource resource = searchAddResource(attester.getParty().getReference());
                if (resource == null) referenceMissing(composition, attester.getParty().getReference());
                attester.setParty(getReference(resource));
            }
        }
        if (composition.hasCustodian()) {
            Resource resource = searchAddResource(composition.getCustodian().getReference());
            if (resource == null) referenceMissing(composition, composition.getCustodian().getReference());
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

    public DocumentReference searchAddDocumentReference(String documentReferenceId,DocumentReference documentReference) {
        log.info("DocumentReference searchAdd " +documentReferenceId);

        if (documentReference == null) throw new InternalErrorException("Bundle processing error");

        DocumentReference eprDocumentReference = (DocumentReference) resourceMap.get(documentReferenceId);

        // Organization already processed, quit with Organization
        if (eprDocumentReference != null) return eprDocumentReference;

        // Prevent re-adding the same Document

        if (documentReference.getIdentifier().size() == 0) {
            documentReference.addIdentifier()
                    .setSystem("urn:uuid")
                    .setValue(documentReference.getId());
        }


        ProducerTemplate template = context.createProducerTemplate();

        InputStream inputStream = null;

        String identifierUrl = "identifier=" + documentReference.getIdentifierFirstRep().getSystem() + "|" + documentReference.getIdentifierFirstRep().getValue();
        Exchange exchange = template.send("direct:FHIRDocumentReference", ExchangePattern.InOut, new Processor() {
            public void process(Exchange exchange) throws Exception {
                exchange.getIn().setHeader(Exchange.HTTP_QUERY, identifierUrl);
                exchange.getIn().setHeader(Exchange.HTTP_METHOD, "GET");
                exchange.getIn().setHeader(Exchange.HTTP_PATH, "DocumentReference");
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
        if (iresource instanceof OperationOutcome) {
            processOperationOutcome((OperationOutcome) iresource);
        } else
        if (iresource instanceof Bundle) {
            Bundle returnedBundle = (Bundle) iresource;
            if (returnedBundle.getEntry().size()>0) {
                eprDocumentReference = (DocumentReference) returnedBundle.getEntry().get(0).getResource();
                log.info("Found DocumentReference = " + eprDocumentReference.getId());
            }
        }

        // Location not found. Add to database
        List<Reference> authors = new ArrayList<>();
        for (Reference reference : documentReference.getAuthor()) {
            Resource resource = searchAddResource(reference.getReference());
            if (resource != null) {
                log.info("Found Resource = " + resource.getId());
                authors.add(getReference(resource));
            }

        }
        documentReference.setAuthor(authors);

        if (documentReference.getSubject() != null) {
            Resource resource = searchAddResource(documentReference.getSubject().getReference());
            if (resource != null) {
                log.info("Patient resource = "+resource.getId());
            }
            documentReference.setSubject(getReference(resource));
        }

        if (documentReference.getCustodian() != null) {
            Resource resource = searchAddResource(documentReference.getCustodian().getReference());
            if (resource != null) {
                log.info("Organization resource = "+resource.getId());
                documentReference.setCustodian(getReference(resource));
            }

        }

        IBaseResource iResource = null;
        String xhttpMethod = "POST";
        String xhttpPath = "DocumentReference";
        // Location found do not add
        if (eprDocumentReference != null) {
            xhttpMethod="PUT";
            // Want id value, no path or resource
            xhttpPath = "DocumentReference/"+eprDocumentReference.getIdElement().getIdPart();
            documentReference.setId(eprDocumentReference.getId());
        }
        String httpBody = ctx.newJsonParser().encodeResourceToString(documentReference);
        String httpMethod= xhttpMethod;
        String httpPath = xhttpPath;
        try {
            exchange = template.send("direct:FHIRDocumentReference", ExchangePattern.InOut, new Processor() {
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
        if (iResource instanceof DocumentReference) {
            eprDocumentReference = (DocumentReference) iResource;

            setResourceMap(eprDocumentReference.getId(),eprDocumentReference);

        } else if (iResource instanceof OperationOutcome)
        {
            processOperationOutcome((OperationOutcome) iResource);
        } else {
            throw new InternalErrorException("Unknown Error");
        }

        return eprDocumentReference;
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
                    exchange.getIn().setHeader(Exchange.HTTP_QUERY, "identifier=" + identifier.getSystem() + "|" + identifier.getValue());
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
            if (iresource instanceof OperationOutcome) {
                processOperationOutcome((OperationOutcome) iresource);
            } else
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
            if (resource == null) referenceMissing(procedure, reference.getReference());
            log.info("Found Resource = " + resource.getId());
            performer.setActor(getReference(resource));

        }
        if (procedure.getSubject() != null) {
            Resource resource = searchAddResource(procedure.getSubject().getReference());
            if (resource == null) referenceMissing(procedure, procedure.getSubject().getReference());
            procedure.setSubject(getReference(resource));
        }
        if (procedure.getLocation().getReference() != null) {
            Resource resource = searchAddResource(procedure.getLocation().getReference());
            if (resource == null) referenceMissing(procedure, procedure.getLocation().getReference());
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

    public ReferralRequest searchAddReferralRequest(String referralRequestId,ReferralRequest referralRequest) {
        log.info("ReferralRequest searchAdd " +referralRequestId);

        if (referralRequest == null) throw new InternalErrorException("Bundle processing error");

        ReferralRequest eprReferralRequest = (ReferralRequest) resourceMap.get(referralRequestId);

        // Organization already processed, quit with Organization
        if (eprReferralRequest != null) return eprReferralRequest;

        // Prevent re-adding the same Practitioner
        if (referralRequest.getIdentifier().size() == 0) {
            referralRequest.addIdentifier()
                    .setSystem("urn:uuid")
                    .setValue(referralRequest.getId());
        }

        ProducerTemplate template = context.createProducerTemplate();

        InputStream inputStream = null;

        for (Identifier identifier : referralRequest.getIdentifier()) {
            Exchange exchange = template.send("direct:FHIRReferralRequest", ExchangePattern.InOut, new Processor() {
                public void process(Exchange exchange) throws Exception {
                    exchange.getIn().setHeader(Exchange.HTTP_QUERY, "identifier=" + identifier.getSystem() + "|" + identifier.getValue());
                    exchange.getIn().setHeader(Exchange.HTTP_METHOD, "GET");
                    exchange.getIn().setHeader(Exchange.HTTP_PATH, "ReferralRequest");
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
            if (iresource instanceof OperationOutcome) {
                processOperationOutcome((OperationOutcome) iresource);
            } else
            if (iresource instanceof Bundle) {
                Bundle returnedBundle = (Bundle) iresource;
                if (returnedBundle.getEntry().size()>0) {
                    eprReferralRequest = (ReferralRequest) returnedBundle.getEntry().get(0).getResource();
                    log.info("Found ReferralRequest = " + eprReferralRequest.getId());
                }
            }
        }

        // Location not found. Add to database


        if (referralRequest.getSubject() != null) {
            Resource resource = searchAddResource(referralRequest.getSubject().getReference());
            if (resource == null) referenceMissing(referralRequest, referralRequest.getSubject().getReference());
            referralRequest.setSubject(getReference(resource));
        }

        if (referralRequest.getContext().getReference() != null) {
            Resource resource = searchAddResource(referralRequest.getContext().getReference());
            if (resource == null) referenceMissing(referralRequest, referralRequest.getContext().getReference());
            referralRequest.setContext(getReference(resource));
        }

        if (referralRequest.getRequester().hasAgent()) {
            Resource resource = searchAddResource(referralRequest.getRequester().getAgent().getReference());
            if (resource == null) referenceMissing(referralRequest, referralRequest.getRequester().getAgent().getReference());
            referralRequest.getRequester().setAgent(getReference(resource));
        }

        if (referralRequest.getRequester().hasOnBehalfOf()) {
            Resource resource = searchAddResource(referralRequest.getRequester().getOnBehalfOf().getReference());
            if (resource == null) referenceMissing(referralRequest, referralRequest.getRequester().getOnBehalfOf().getReference());
            referralRequest.getRequester().setOnBehalfOf(getReference(resource));
        }

        List<Reference> recipients = new ArrayList<>();
        for (Reference reference : referralRequest.getRecipient()) {
            Resource resource = searchAddResource(reference.getReference());
            if (resource == null) referenceMissing(referralRequest, reference.getReference());
            if (resource != null) recipients.add(getReference(resource));
        }
        referralRequest.setRecipient(recipients);

        List<Reference> supportingInfo = new ArrayList<>();
        for (Reference reference : referralRequest.getSupportingInfo()) {
            Resource resource = searchAddResource(reference.getReference());
            if (resource == null) referenceMissing(referralRequest, reference.getReference());
            if (resource != null) recipients.add(getReference(resource));
        }
        referralRequest.setSupportingInfo(supportingInfo);


        List<Reference> reasons = new ArrayList<>();
        for (Reference reference : referralRequest.getReasonReference()) {
            Resource resource = searchAddResource(reference.getReference());
            if (resource == null) referenceMissing(referralRequest, reference.getReference());
            if (resource != null) reasons.add(getReference(resource));
        }
        referralRequest.setReasonReference(reasons);

        IBaseResource iResource = null;
        String xhttpMethod = "POST";
        String xhttpPath = "ReferralRequest";
        // Location found do not add
        if (eprReferralRequest != null) {
            xhttpMethod="PUT";
            // Want id value, no path or resource
            xhttpPath = "ReferralRequest/"+eprReferralRequest.getIdElement().getIdPart();
            referralRequest.setId(eprReferralRequest.getId());
        }
        String httpBody = ctx.newJsonParser().encodeResourceToString(referralRequest);
        String httpMethod= xhttpMethod;
        String httpPath = xhttpPath;
        try {
            Exchange exchange = template.send("direct:FHIRReferralRequest", ExchangePattern.InOut, new Processor() {
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
        if (iResource instanceof ReferralRequest) {
            eprReferralRequest = (ReferralRequest) iResource;
            setResourceMap(eprReferralRequest.getId(),eprReferralRequest);


        } else if (iResource instanceof OperationOutcome)
        {
            processOperationOutcome((OperationOutcome) iResource);
        } else {
            throw new InternalErrorException("Unknown Error");
        }

        return eprReferralRequest;
    }

    public Encounter searchAddEncounter(String encounterId,Encounter encounter) {
        log.info("Encounter searchAdd " +encounterId);

        if (encounter == null) throw new InternalErrorException("Bundle processing error");

        // To prevent infinite loop
        checkCircularReference(encounter);

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
                    exchange.getIn().setHeader(Exchange.HTTP_QUERY, "identifier=" + identifier.getSystem() + "|" + identifier.getValue());
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
            if (iresource instanceof OperationOutcome) {
                processOperationOutcome((OperationOutcome) iresource);
            }
            if (iresource instanceof OperationOutcome) {
                processOperationOutcome((OperationOutcome) iresource);
            } else
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
            if (resource == null) referenceMissing(encounter, encounter.getSubject().getReference());
            encounter.setSubject(getReference(resource));
        }

        for (Encounter.DiagnosisComponent component : encounter.getDiagnosis()) {
            if (component.getCondition().getReference() != null) {

                    Resource resource = searchAddResource(component.getCondition().getReference());
                    if (resource == null) referenceMissing(encounter, component.getCondition().getReference());
                    component.setCondition(getReference(resource));
                }
        }

        for (Encounter.EncounterLocationComponent component : encounter.getLocation()) {
            if (component.getLocation().getReference() != null) {
                Resource resource = searchAddResource(component.getLocation().getReference());
                if (resource == null) referenceMissing(encounter, component.getLocation().getReference());
                component.setLocation(getReference(resource));
            }
        }
        if (encounter.hasHospitalization()) {
            if (encounter.getHospitalization().getDestination().getReference() !=null) {
                Resource resource = searchAddResource(encounter.getHospitalization().getDestination().getReference());
                if (resource == null) referenceMissing(encounter, encounter.getHospitalization().getDestination().getReference());
                encounter.getHospitalization().setDestination(getReference(resource));
            }
        }
        if (encounter.hasServiceProvider()) {
            Resource resource = searchAddResource(encounter.getServiceProvider().getReference());
            if (resource == null) {
                // Ideally would be an error but not currently supporting ServiceProvider
                referenceMissingWarn(encounter, encounter.getServiceProvider().getReference());
                encounter.setServiceProvider(null);
            } else {
                encounter.setServiceProvider(getReference(resource));
            }
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

            log.info("Patient searchAdd " + patientId);

            if (patient == null) throw new InternalErrorException("Bundle processing error");

            Patient eprPatient = (Patient) resourceMap.get(patientId);

            // Patient already processed, quit with Patient
            if (eprPatient != null) return eprPatient;

            ProducerTemplate template = context.createProducerTemplate();

            InputStream inputStream = null;

            for (Identifier identifier : patient.getIdentifier()) {
                Exchange exchange = template.send("direct:FHIRPatient", ExchangePattern.InOut, new Processor() {
                    public void process(Exchange exchange) throws Exception {
                        exchange.getIn().setHeader(Exchange.HTTP_QUERY, "identifier=" + identifier.getSystem() + "|" + identifier.getValue());
                        exchange.getIn().setHeader(Exchange.HTTP_METHOD, "GET");
                        exchange.getIn().setHeader(Exchange.HTTP_PATH, "Patient");
                    }
                });
                inputStream = (InputStream) exchange.getIn().getBody();
                Reader reader = new InputStreamReader(inputStream);
                IBaseResource iresource = null;
                try {
                    iresource = ctx.newJsonParser().parseResource(reader);
                } catch (Exception ex) {
                    log.error("JSON Parse failed " + ex.getMessage());
                    throw new InternalErrorException(ex.getMessage());
                }
                if (iresource instanceof OperationOutcome) {
                    processOperationOutcome((OperationOutcome) iresource);
                } else
                if (iresource instanceof Bundle) {
                    Bundle returnedBundle = (Bundle) iresource;
                    if (returnedBundle.getEntry().size() > 0) {
                        eprPatient = (Patient) returnedBundle.getEntry().get(0).getResource();
                        log.info("Found Patient = " + eprPatient.getId());
                        // KGM 31/Jan/2018 Missing break on finding patient
                        break;
                    }
                }
            }
            // Patient found do not add
            if (eprPatient != null) {
                setResourceMap(patientId, eprPatient);

                return eprPatient;
            }

            // Location not found. Add to database

            if (patient.getManagingOrganization().getReference() != null) {
                Resource resource = searchAddResource(patient.getManagingOrganization().getReference());
                log.info("Found ManagingOrganization = " + resource.getId());
                if (resource == null) referenceMissing(patient, patient.getManagingOrganization().getReference());
                patient.setManagingOrganization(getReference(resource));
            }
            for (Reference reference : patient.getGeneralPractitioner()) {
                Resource resource = searchAddResource(reference.getReference());
                if (resource == null) referenceMissing(patient, reference.getReference());
                log.info("Found Patient Practitioner = " + reference.getId());
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
                        exchange.getIn().setHeader("Prefer", "return=representation");
                        exchange.getIn().setHeader(Exchange.CONTENT_TYPE, "application/fhir+json");
                        exchange.getIn().setBody(jsonResource);
                    }
                });
                inputStream = (InputStream) exchange.getIn().getBody();

                Reader reader = new InputStreamReader(inputStream);
                iResource = ctx.newJsonParser().parseResource(reader);
            } catch (Exception ex) {
                log.error("JSON Parse failed " + ex.getMessage());
                throw new InternalErrorException(ex.getMessage());
            }
            if (iResource instanceof Patient) {
                eprPatient = (Patient) iResource;
                setResourceMap(patientId, eprPatient);
            } else if (iResource instanceof OperationOutcome) {
                processOperationOutcome((OperationOutcome) iResource);
            } else {
                throw new InternalErrorException("Unknown Error");
            }

        return eprPatient;
    }

    public RelatedPerson searchAddRelatedPerson(String personId, RelatedPerson person) {

        log.info("RelatedPerson searchAdd " + personId);

        if (person == null) throw new InternalErrorException("Bundle processing error");

        RelatedPerson eprRelatedPerson = (RelatedPerson) resourceMap.get(personId);

        // RelatedPerson already processed, quit with RelatedPerson
        if (eprRelatedPerson != null) return eprRelatedPerson;

        ProducerTemplate template = context.createProducerTemplate();

        InputStream inputStream = null;

        for (Identifier identifier : person.getIdentifier()) {
            Exchange exchange = template.send("direct:FHIRRelatedPerson", ExchangePattern.InOut, new Processor() {
                public void process(Exchange exchange) throws Exception {
                    exchange.getIn().setHeader(Exchange.HTTP_QUERY, "identifier=" + identifier.getSystem() + "|" + identifier.getValue());
                    exchange.getIn().setHeader(Exchange.HTTP_METHOD, "GET");
                    exchange.getIn().setHeader(Exchange.HTTP_PATH, "RelatedPerson");
                }
            });
            inputStream = (InputStream) exchange.getIn().getBody();
            Reader reader = new InputStreamReader(inputStream);
            IBaseResource iresource = null;
            try {
                iresource = ctx.newJsonParser().parseResource(reader);
            } catch (Exception ex) {
                log.error("JSON Parse failed " + ex.getMessage());
                throw new InternalErrorException(ex.getMessage());
            }
            if (iresource instanceof OperationOutcome) {
                processOperationOutcome((OperationOutcome) iresource);
            } else if (iresource instanceof Bundle) {
                Bundle returnedBundle = (Bundle) iresource;
                if (returnedBundle.getEntry().size() > 0) {
                    eprRelatedPerson = (RelatedPerson) returnedBundle.getEntry().get(0).getResource();
                    log.info("Found RelatedPerson = " + eprRelatedPerson.getId());
                    // KGM 31/Jan/2018 Missing break on finding person
                    break;
                }
            }
        }
        // RelatedPerson found do not add
        if (eprRelatedPerson != null) {
            setResourceMap(personId, eprRelatedPerson);

            return eprRelatedPerson;
        }

        if (person.getPatient() != null) {
            Resource resource = searchAddResource(person.getPatient().getReference());
            if (resource == null) referenceMissing(person, person.getPatient().getReference());
            person.setPatient(getReference(resource));
        }

        IBaseResource iResource = null;
        String jsonResource = ctx.newJsonParser().encodeResourceToString(person);
        try {
            Exchange exchange = template.send("direct:FHIRRelatedPerson", ExchangePattern.InOut, new Processor() {
                public void process(Exchange exchange) throws Exception {
                    exchange.getIn().setHeader(Exchange.HTTP_QUERY, "");
                    exchange.getIn().setHeader(Exchange.HTTP_METHOD, "POST");
                    exchange.getIn().setHeader(Exchange.HTTP_PATH, "RelatedPerson");
                    exchange.getIn().setHeader("Prefer", "return=representation");
                    exchange.getIn().setHeader(Exchange.CONTENT_TYPE, "application/fhir+json");
                    exchange.getIn().setBody(jsonResource);
                }
            });
            inputStream = (InputStream) exchange.getIn().getBody();

            Reader reader = new InputStreamReader(inputStream);
            iResource = ctx.newJsonParser().parseResource(reader);
        } catch (Exception ex) {
            log.error("JSON Parse failed " + ex.getMessage());
            throw new InternalErrorException(ex.getMessage());
        }
        if (iResource instanceof RelatedPerson) {
            eprRelatedPerson = (RelatedPerson) iResource;
            setResourceMap(personId, eprRelatedPerson);
        } else if (iResource instanceof OperationOutcome) {
            processOperationOutcome((OperationOutcome) iResource);
        } else {
            throw new InternalErrorException("Unknown Error");
        }

        return eprRelatedPerson;
    }

    private void referenceMissingWarn(Resource resource, String reference) {
        String errMsg = "Unable to resolve reference: "+reference+" In resource "+resource.getClass().getSimpleName()+" id "+resource.getId();
        log.warn(errMsg);

    }
    private void referenceMissing(Resource resource, String reference) {
        String errMsg = "Unable to resolve reference: "+reference+" In resource "+resource.getClass().getSimpleName()+" id "+resource.getId();
        log.error(errMsg);
        OperationOutcome outcome = new OperationOutcome();
        outcome.addIssue()
                .setCode(OperationOutcome.IssueType.BUSINESSRULE)
                .setSeverity(OperationOutcome.IssueSeverity.FATAL)
                .setDiagnostics(errMsg)
                .setDetails(
                        new CodeableConcept().setText("Invalid Reference")
                );
        setOperationOutcome(outcome);
        OperationOutcomeFactory.convertToException(outcome);
    }

    private void processOperationOutcome(OperationOutcome operationOutcome) {
        this.operationOutcome = operationOutcome;
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
