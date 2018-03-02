package uk.nhs.careconnect.ri.extranet.providers;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.param.TokenParam;

import org.hl7.fhir.dstu3.model.*;

import org.hl7.fhir.utilities.xhtml.XhtmlDocument;
import org.hl7.fhir.utilities.xhtml.XhtmlNode;
import org.hl7.fhir.utilities.xhtml.XhtmlParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import uk.org.hl7.fhir.core.Stu3.CareConnectSystem;


import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

@Component
public class CompositionDao implements IComposition {

    @Autowired
    private TemplateEngine templateEngine;


    Context ctxThymeleaf = new Context();

    FhirContext ctx = FhirContext.forDstu3();

   // private IGenericClient client;

    private XhtmlParser xhtmlParser = new XhtmlParser();

    private ClassLoader getContextClassLoader() {
        return Thread.currentThread().getContextClassLoader();
    }

    public static boolean isNumeric(String s) {
        return s != null && s.matches("[-+]?\\d*\\.?\\d+");
    }


    private static final Logger log = LoggerFactory.getLogger(CompositionDao.class);


    Map<String,String> referenceMap = new HashMap<>();

    DateFormat df = new SimpleDateFormat("HHmm_dd_MM_yyyy");

    final String uuidtag = "urn:uuid:";

    @Override
    public List<Resource> search(FhirContext ctx, TokenParam resid, ReferenceParam patient) {

        List<Resource> resources = new ArrayList<>();



        return resources;
    }

    @Override
    public Composition read(FhirContext ctx, IdType theId) {

           return null;
    }




    @Override
    public Bundle readDocument(FhirContext ctx, IdType theId) {
        // Search for document bundle rather than composition (this contains a link to the Composition

        // {'entry.objectId': ObjectId("5a95166bbc5b249440975d8f"), 'entry.resourceType' : 'Composition'}
        Bundle bundle = new Bundle();
        bundle.setType(Bundle.BundleType.DOCUMENT);


        return bundle;
    }

    @Override
    public Bundle buildSummaryCareDocument(IGenericClient client, IdType patientId) {

        log.info("SCR for patient = "+patientId.getIdPart());

        //this.client = client;


        if (!isNumeric(patientId.getIdPart())) {
            return null;
        }

        // Create Bundle of type Document
        Bundle fhirDocument = new Bundle()
                .setType(Bundle.BundleType.DOCUMENT);

        fhirDocument.getIdentifier().setValue(UUID.randomUUID().toString()).setSystem("https://tools.ietf.org/html/rfc4122");

        // Main resource of a FHIR Bundle is a Composition
        Composition composition = new Composition();
        composition.setId(UUID.randomUUID().toString());
        fhirDocument.addEntry().setResource(composition).setFullUrl(uuidtag + composition.getId());

        composition.setTitle("Patient Summary Care Record");
        composition.setDate(new Date());
        composition.setStatus(Composition.CompositionStatus.FINAL);

        Organization leedsTH = getOrganization(client,"RR8");
        leedsTH.setId(getNewReferenceUri(leedsTH));
        fhirDocument.addEntry().setResource(leedsTH).setFullUrl(uuidtag + leedsTH.getId());

        composition.addAttester()
                .setParty(new Reference(uuidtag+leedsTH.getId()))
                .addMode(Composition.CompositionAttestationMode.OFFICIAL);


        Device device = new Device();
        device.setId(UUID.randomUUID().toString());
        device.getType().addCoding()
                .setSystem("http://snomed.info/sct")
                .setCode("58153004")
                .setDisplay("Android");
        device.setOwner(new Reference(uuidtag+leedsTH.getId()));
        fhirDocument.addEntry().setResource(device).setFullUrl(uuidtag +device.getId());

        composition.addAuthor(new Reference(uuidtag+device.getId()));


        Patient patient = null;
        Practitioner gp = null;
        Organization practice = null;


        Bundle patientBundle = getPatientBundle(client,patientId.getIdPart());

        for (Bundle.BundleEntryComponent entry : patientBundle.getEntry()) {
            if (entry.getResource() instanceof Patient) {
                patient = (Patient) entry.getResource();



                patient.setId(getNewReferenceUri(patient));

                composition.setSubject(new Reference(uuidtag+patient.getId()));
                fhirDocument.addEntry().setResource(patient).setFullUrl(uuidtag + patient.getId());;
            }
            if (entry.getResource() instanceof Practitioner) {
                gp = (Practitioner) entry.getResource();

                gp.setId(getNewReferenceUri(gp));
                if (patient != null && patient.getGeneralPractitioner().size()>0) {
                    patient.getGeneralPractitioner().get(0).setReference(uuidtag + gp.getId());
                    fhirDocument.addEntry().setResource(gp).setFullUrl(uuidtag + gp.getId());
                }

            }
            if (entry.getResource() instanceof Organization) {
                practice = (Organization) entry.getResource();

                practice.setId(getNewReferenceUri(practice));
                if (patient != null ) {
                    patient.setManagingOrganization(new Reference(uuidtag + practice.getId()));
                }
                fhirDocument.addEntry().setResource(practice).setFullUrl(uuidtag + practice.getId());
            }
        }
        if (patient == null) return null; // 404 Patient not found

        generatePatientHtml(patient,patientBundle);

        /* CONDITION */

        Bundle conditionBundle = getConditionBundle(client,patientId.getIdPart());

        for (Bundle.BundleEntryComponent entry : conditionBundle.getEntry()) {
            if (entry.getResource() instanceof Condition) {
                Condition condition = (Condition) entry.getResource();

                condition.setId(getNewReferenceUri(condition));
                condition.setSubject(new Reference(uuidtag+patient.getId()));
                fhirDocument.addEntry().setResource(entry.getResource()).setFullUrl(uuidtag + condition.getId());
            }
        }
        composition.addSection(getConditionSection(conditionBundle));

        /* MEDICATION STATEMENT */

        Bundle medicationStatementBundle = getMedicationStatementBundle(client,patientId.getIdPart());

        for (Bundle.BundleEntryComponent entry : medicationStatementBundle.getEntry()) {
            if (entry.getResource() instanceof MedicationStatement) {
                MedicationStatement medicationStatement = (MedicationStatement) entry.getResource();

                medicationStatement.setId(getNewReferenceUri(medicationStatement));
                medicationStatement.setSubject(new Reference(uuidtag+patient.getId()));
                fhirDocument.addEntry().setResource(entry.getResource()).setFullUrl(uuidtag + medicationStatement.getId());
                //Date date = medicationStatement.getEffectiveDateTimeType().getValue()
            }
        }
        composition.addSection(getMedicationStatementSection(medicationStatementBundle));


        /* ALLERGY INTOLERANCE */

        Bundle allergyBundle = getAllergyBundle(client,patientId.getIdPart());
        for (Bundle.BundleEntryComponent entry : allergyBundle.getEntry()) {
            if (entry.getResource() instanceof AllergyIntolerance) {
                AllergyIntolerance allergyIntolerance = (AllergyIntolerance) entry.getResource();

                allergyIntolerance.setId(getNewReferenceUri(allergyIntolerance));
                allergyIntolerance.setPatient(new Reference(uuidtag+patient.getId()));
                fhirDocument.addEntry().setResource(entry.getResource()).setFullUrl(uuidtag + allergyIntolerance.getId());
            }
        }
        composition.addSection(getAllergySection(allergyBundle));

        /* ENCOUNTER */

        Bundle encounterBundle = getEncounterBundle(client,patientId.getIdPart());
        for (Bundle.BundleEntryComponent entry : encounterBundle.getEntry()) {
            if (entry.getResource() instanceof Encounter) {
                Encounter encounter = (Encounter) entry.getResource();
                encounter.setId(getNewReferenceUri(encounter));
                encounter.setSubject(new Reference(uuidtag+patient.getId()));
                fhirDocument.addEntry().setResource(entry.getResource()).setFullUrl(uuidtag + encounter.getId());
            }
        }
        composition.addSection(getEncounterSection(encounterBundle));


        log.debug(ctx.newJsonParser().setPrettyPrint(true).encodeResourceToString(fhirDocument));

        return fhirDocument;
    }


    private Bundle getPatientBundle(IGenericClient client, String patientId) {

        return client
                .search()
                .forResource(Patient.class)
                .where(Patient.RES_ID.exactly().code(patientId))
                .include(Patient.INCLUDE_GENERAL_PRACTITIONER)
                .include(Patient.INCLUDE_ORGANIZATION)
                .returnBundle(Bundle.class)
                .execute();
    }
    private Patient generatePatientHtml(Patient patient, Bundle fhirDocument) {
        if (!patient.hasText()) {

            ctxThymeleaf.clearVariables();
            ctxThymeleaf.setVariable("patient", patient);
            for (Bundle.BundleEntryComponent entry : fhirDocument.getEntry()) {
                if (entry.getResource() instanceof Practitioner) ctxThymeleaf.setVariable("gp", entry.getResource());
                if (entry.getResource() instanceof Organization) ctxThymeleaf.setVariable("practice", entry.getResource());
                Practitioner practice;

            }

            patient.getText().setDiv(getDiv("patient")).setStatus(Narrative.NarrativeStatus.GENERATED);
            log.debug(patient.getText().getDiv().getValueAsString());
        }
        return patient;
    }

    private XhtmlNode getDiv(String template) {
        XhtmlNode xhtmlNode = null;
        String processedHtml = templateEngine.process(template, ctxThymeleaf);
        try {
            XhtmlDocument parsed = xhtmlParser.parse(processedHtml, null);
            xhtmlNode = parsed.getDocumentElement();
            log.debug(processedHtml);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return xhtmlNode;
    }

    private Composition.SectionComponent getConditionSection(Bundle bundle) {
        Composition.SectionComponent section = new Composition.SectionComponent();

        ArrayList<Condition>  conditions = new ArrayList<>();

        section.getCode().addCoding()
                .setSystem(CareConnectSystem.SNOMEDCT)
                .setCode("887151000000100")
                .setDisplay("Problems and issues");
        section.setTitle("Problems and issues");

        for (Bundle.BundleEntryComponent entry : bundle.getEntry()) {
            if (entry.getResource() instanceof Condition) {
                Condition condition = (Condition) entry.getResource();
                section.getEntry().add(new Reference("urn:uuid:"+condition.getId()));
                conditions.add(condition);
            }
        }
        ctxThymeleaf.clearVariables();
        ctxThymeleaf.setVariable("conditions", conditions);

        section.getText().setDiv(getDiv("condition")).setStatus(Narrative.NarrativeStatus.GENERATED);

        return section;
    }

    private Composition.SectionComponent getMedicationStatementSection(Bundle bundle) {
        Composition.SectionComponent section = new Composition.SectionComponent();

        ArrayList<MedicationStatement>  medicationStatements = new ArrayList<>();

        section.getCode().addCoding()
                .setSystem(CareConnectSystem.SNOMEDCT)
                .setCode("933361000000108")
                .setDisplay("Medications and medical devices");
        section.setTitle("Medications and medical devices");

        for (Bundle.BundleEntryComponent entry : bundle.getEntry()) {
            if (entry.getResource() instanceof MedicationStatement) {
                MedicationStatement medicationStatement = (MedicationStatement) entry.getResource();
                //medicationStatement.getMedicationReference().getDisplay();
                section.getEntry().add(new Reference("urn:uuid:"+medicationStatement.getId()));
                medicationStatements.add(medicationStatement);

            }
        }
        ctxThymeleaf.clearVariables();
        ctxThymeleaf.setVariable("medicationStatements", medicationStatements);

        section.getText().setDiv(getDiv("medicationStatement")).setStatus(Narrative.NarrativeStatus.GENERATED);

        return section;
    }

    private Composition.SectionComponent getAllergySection(Bundle bundle) {
        Composition.SectionComponent section = new Composition.SectionComponent();

        ArrayList<AllergyIntolerance>  allergyIntolerances = new ArrayList<>();

        section.getCode().addCoding()
                .setSystem(CareConnectSystem.SNOMEDCT)
                .setCode("886921000000105")
                .setDisplay("Allergies and adverse reactions");
        section.setTitle("Allergies and adverse reactions");

        for (Bundle.BundleEntryComponent entry : bundle.getEntry()) {
            if (entry.getResource() instanceof AllergyIntolerance) {
                AllergyIntolerance allergyIntolerance = (AllergyIntolerance) entry.getResource();
                section.getEntry().add(new Reference("urn:uuid:"+allergyIntolerance.getId()));
                allergyIntolerances.add(allergyIntolerance);
            }
        }
        ctxThymeleaf.clearVariables();

        ctxThymeleaf.setVariable("allergies", allergyIntolerances);

        section.getText().setDiv(getDiv("allergy")).setStatus(Narrative.NarrativeStatus.GENERATED);

        return section;
    }

    private Composition.SectionComponent getEncounterSection(Bundle bundle) {
        Composition.SectionComponent section = new Composition.SectionComponent();
        // TODO Get Correct code.
        ArrayList<Encounter>  encounters = new ArrayList<>();

        section.getCode().addCoding()
                .setSystem(CareConnectSystem.SNOMEDCT)
                .setCode("713511000000103")
                .setDisplay("Encounter administration");
        section.setTitle("Encounters");

        for (Bundle.BundleEntryComponent entry : bundle.getEntry()) {
            if (entry.getResource() instanceof Encounter) {
                Encounter encounter = (Encounter) entry.getResource();
                section.getEntry().add(new Reference("urn:uuid:"+encounter.getId()));
                encounters.add(encounter);
            }
        }
        ctxThymeleaf.clearVariables();
        ctxThymeleaf.setVariable("encounters", encounters);

        section.getText().setDiv(getDiv("encounter")).setStatus(Narrative.NarrativeStatus.GENERATED);

        return section;
    }

    private Bundle getConditionBundle(IGenericClient client,String patientId) {

        return client
                .search()
                .forResource(Condition.class)
                .where(Condition.PATIENT.hasId(patientId))
                .and(Condition.CLINICAL_STATUS.exactly().code("active"))
                .returnBundle(Bundle.class)
                .execute();
    }
    private Bundle getEncounterBundle(IGenericClient client,String patientId) {

        return client
                .search()
                .forResource(Encounter.class)
                .where(Encounter.PATIENT.hasId(patientId))
                .count(3) // Last 3 entries same as GP Connect
                .returnBundle(Bundle.class)
                .execute();
    }

    private Organization getOrganization(IGenericClient client,String sdsCode) {
        Organization organization = null;
        Bundle bundle =  client
                .search()
                .forResource(Organization.class)
                .where(Organization.IDENTIFIER.exactly().code(sdsCode))

                .returnBundle(Bundle.class)
                .execute();
        if (bundle.getEntry().size()>0) {
            if (bundle.getEntry().get(0).getResource() instanceof Organization)
                organization = (Organization) bundle.getEntry().get(0).getResource();
        }
        return organization;
    }
    private Bundle getMedicationStatementBundle(IGenericClient client,String patientId) {

        return client
                .search()
                .forResource(MedicationStatement.class)
                .where(MedicationStatement.PATIENT.hasId(patientId))
                .and(MedicationStatement.STATUS.exactly().code("active"))
                .returnBundle(Bundle.class)
                .execute();
    }

    private Bundle getAllergyBundle(IGenericClient client,String patientId) {

        return client
                .search()
                .forResource(AllergyIntolerance.class)
                .where(AllergyIntolerance.PATIENT.hasId(patientId))
                .returnBundle(Bundle.class)
                .execute();
    }

    private String getNewReferenceUri(Resource resource) {
        return getNewReferenceUri(resource.getResourceType().toString()+"/"+resource.getId());
    }
    private String getNewReferenceUri(String reference) {
        String newReference = referenceMap.get(reference);
        if (newReference != null ) return newReference;
        newReference = UUID.randomUUID().toString();
        referenceMap.put(reference,newReference);
        return newReference;
    }
}
