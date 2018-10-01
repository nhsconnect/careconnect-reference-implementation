package uk.nhs.careconnect.cli;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.FhirVersionEnum;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.server.exceptions.InternalErrorException;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.csv.QuoteMode;
import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.dstu3.model.*;
import org.hl7.fhir.instance.model.Conformance;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.instance.model.api.IIdType;
import uk.org.hl7.fhir.core.Stu3.CareConnectExtension;
import uk.org.hl7.fhir.core.Stu3.CareConnectITKProfile;
import uk.org.hl7.fhir.core.Stu3.CareConnectProfile;
import uk.org.hl7.fhir.core.Stu3.CareConnectSystem;

import java.io.*;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static org.apache.commons.lang3.StringUtils.defaultIfBlank;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.swapCase;

public class   UploadExamples extends BaseCommand {

	private static final org.slf4j.Logger ourLog = org.slf4j.LoggerFactory.getLogger(UploadExamples.class);

	private ArrayList<IIdType> myExcludes = new ArrayList<>();

    private ArrayList<IBaseResource> resources = new ArrayList<>();

    private Map<String,String> orgMap = new HashMap<>();

    private Map<String,String> docMap = new HashMap<>();

    private Map<String,Patient> patientMap = new HashMap<>();

    private Map<String, Address> addressMap = new HashMap<>();

    private ArrayList<PractitionerRole> roles = new ArrayList<>();

    private static String nokiaObs = "https://fhir.health.phr.example.com/Id/observation";

    FhirContext ctx ;

    IGenericClient client;

    IGenericClient odsClient;

/* PROGRAM ARGUMENTS

upload-examples
-t
http://127.0.0.1:8080/careconnect-ri/STU3

    */

    private String Inicaps(String string) {
        String result = null;
        String[] array = string.split(" ");

        for (int f=0; f<array.length;f++) {
            if (f==0) {
                result = StringUtils.capitalize(StringUtils.lowerCase(array[f]));
            } else
            {
                result = result + " "+ StringUtils.capitalize(StringUtils.lowerCase(array[f]));
            }
        }
        return result;
    }

	@Override
	public String getCommandDescription() {
		return "Uploads sample resources.";
	}

	@Override
	public String getCommandName() {
		return "upload-examples";
	}

	public String stripChar(String string) {
	    string =string.replace(" ","");
        string =string.replace(":","");
        string =string.replace("-","");
        return string;
    }

	@Override
	public Options getOptions() {
		Options options = new Options();
		Option opt;

		addFhirVersionOption(options);

        opt = new Option("prac", "practitioner", false, "Practitioner Examples upload files");
        opt.setRequired(false);
        options.addOption(opt);


		opt = new Option("t", "target", true, "Base URL for the target server (e.g. \"http://example.com/fhir\")");
		opt.setRequired(true);
		options.addOption(opt);

        opt = new Option("a", "all", false, "All upload files");
        opt.setRequired(false);
        options.addOption(opt);

        opt = new Option("g", "gp", false, "GP upload files");
        opt.setRequired(false);
        options.addOption(opt);

        opt = new Option("phr", "phr", false, "PHR Examples upload files");
        opt.setRequired(false);
        options.addOption(opt);

        opt = new Option("pat", "patient", false, "Patient Examples upload files");
        opt.setRequired(false);
        options.addOption(opt);

        opt = new Option("o", "obs", false, "Observation Examples upload files");
        opt.setRequired(false);
        options.addOption(opt);

        opt = new Option("e", "encounter", false, "Encounter Examples upload files");
        opt.setRequired(false);
        options.addOption(opt);

        opt = new Option("c", "condition", false, "Condition Examples upload files");
        opt.setRequired(false);
        options.addOption(opt);

        opt = new Option("allergy", "allergyintolerance", false, "AllergyIntolerance Examples upload files");
        opt.setRequired(false);
        options.addOption(opt);

        opt = new Option("imms", "immunisations", false, "Immunization Examples upload files");
        opt.setRequired(false);
        options.addOption(opt);

        opt = new Option("proc", "procedures", false, "Procedure Examples upload files");
        opt.setRequired(false);
        options.addOption(opt);

        opt = new Option("pres", "prescriptions", false, "MedciationRequest Examples upload files");
        opt.setRequired(false);
        options.addOption(opt);

        return options;
	}

	@Override
	public void run(CommandLine theCommandLine) throws ParseException {
		String targetServer = theCommandLine.getOptionValue("t");
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
		if (isBlank(targetServer)) {
			throw new ParseException("No target server (-t) specified");
		} else if (targetServer.startsWith("http") == false) {
			throw new ParseException("Invalid target server specified, must begin with 'http'");
		}

		ctx = getSpecVersionContext(theCommandLine);

        ClassLoader classLoader = getClass().getClassLoader();

		if (ctx.getVersion().getVersion() == FhirVersionEnum.DSTU3) {

            client = ctx.newRestfulGenericClient(targetServer);

            odsClient = ctx.newRestfulGenericClient("https://directory.spineservices.nhs.uk/STU3/");

            System.out.println("HAPI Client created");

            CapabilityStatement capabilityStatement = null;
            Integer retries = 15; // This is 15 mins before giving up
            while (capabilityStatement == null && retries > 0) {
                try {
                    capabilityStatement = client.fetchConformance().ofType(org.hl7.fhir.dstu3.model.CapabilityStatement.class).execute();
                } catch (Exception ex) {
                    ourLog.warn("Failed to load conformance statement, error was: {}", ex.toString());
                    System.out.println("Sleeping for a minute");
                    retries--;
                    try {
                        TimeUnit.MINUTES.sleep(1);
                    } catch (Exception ex1) {

                    }
                }
            }
            if (capabilityStatement == null) {
                System.out.println("Max number of attempts to connect exceeded. Aborting");
                return;
            }
            Integer resourceCount = 0;

            Bundle pastResults = client
                    .search()
                    .forResource(Practitioner.class)
                    .where(Practitioner.IDENTIFIER.exactly().code("G9910371"))
                    .returnBundle(Bundle.class)
                    .execute();

            if (pastResults.getEntry().size()==0 && (theCommandLine.hasOption("a") ||theCommandLine.hasOption("prac"))) {
                try {
                    System.out.println("Practitioner.csv");
                    resources.clear();
                    IRecordHandler handler = null;

                    handler = new PractitionerHandler();
                    processPractitionerCSV(handler, ctx, ',', QuoteMode.NON_NUMERIC, classLoader.getResourceAsStream("Examples/Practitioner.csv"));
                    for (IBaseResource resource : resources) {
                        Practitioner practitioner = (Practitioner) resource;
                        MethodOutcome outcome = client.update().resource(resource)
                                .conditionalByUrl("Practitioner?identifier=" + practitioner.getIdentifier().get(0).getSystem() + "%7C" +practitioner.getIdentifier().get(0).getValue())
                                .execute();

                        if (outcome.getId() != null ) {
                            practitioner.setId(outcome.getId().getIdPart());
                        }
                    }
                    resources.clear();
                    System.out.println("Consultant.csv");
                    handler = new ConsultantHandler();
                    processPractitionerCSV(handler, ctx, ',', QuoteMode.NON_NUMERIC, classLoader.getResourceAsStream("Examples/Consultant.csv"));
                    for (IBaseResource resource : resources) {
                        Practitioner practitioner = (Practitioner) resource;
                        MethodOutcome outcome = client.update().resource(resource)
                                .conditionalByUrl("Practitioner?identifier=" + practitioner.getIdentifier().get(0).getSystem() + "%7C" +practitioner.getIdentifier().get(0).getValue())
                                .execute();

                        if (outcome.getId() != null ) {
                            practitioner.setId(outcome.getId().getIdPart());
                        }
                    }
                    resources.clear();

                    for (PractitionerRole practitionerRole : roles) {


                        if (practitionerRole.getPractitioner() != null) {
                            String practitionerId = docMap.get(practitionerRole.getPractitioner().getReference());
                            if (practitionerId == null) {
                                practitionerId = getPractitioner(practitionerRole.getPractitioner().getReference());
                            }
                            if (practitionerId != null) {
                                practitionerRole.setPractitioner(new Reference("Practitioner/"+practitionerId));
                            }
                        }
                        MethodOutcome outcome = client.update().resource(practitionerRole)
                                .conditionalByUrl("PractitionerRole?identifier=" + practitionerRole.getIdentifier().get(0).getSystem() + "%7C" +practitionerRole.getIdentifier().get(0).getValue())
                                .execute();
                        //     System.out.println(outcome.getId());
                        if (outcome.getId() != null ) {
                            practitionerRole.setId(outcome.getId().getIdPart());

                        }
                    }
                    roles.clear();


                } catch (Exception ex) {
                    ourLog.error(ex.getMessage());
                }
            }


            // Check patient
             pastResults = client
                    .search()
                    .forResource(Patient.class)
                    .where(Patient.IDENTIFIER.exactly().code("LOCAL1172"))
                    .returnBundle(Bundle.class)
                    .execute();

            // BA Patient data file
            if (pastResults.getEntry().size()==0 && (theCommandLine.hasOption("a") ||theCommandLine.hasOption("pat"))) {
                try {

                    System.out.println("Patient.csv");
                    resources.clear();
                    IRecordHandler handler = null;

                    handler = new PatientIdentifierHandler(ctx, client);
                    processPatientIdentifierCSV(handler, ctx, ',', QuoteMode.NON_NUMERIC, classLoader.getResourceAsStream("Examples/PatientIdentifier.csv"));

                    handler = new PatientHandler(ctx, client);
                    processPatientCSV(handler, ctx, ',', QuoteMode.NON_NUMERIC, classLoader.getResourceAsStream("Examples/Patient.csv"));

                    handler = new PatientNameHandler(ctx, client);
                    processPatientNameCSV(handler, ctx, ',', QuoteMode.NON_NUMERIC, classLoader.getResourceAsStream("Examples/PatientName.csv"));

                    handler = new AddressHandler(ctx, client);
                    processAddressCSV(handler, ctx, ',', QuoteMode.NON_NUMERIC, classLoader.getResourceAsStream("Examples/Address.csv"));

                    handler = new PatientAddressHandler(ctx, client);
                    processPatientAddressCSV(handler, ctx, ',', QuoteMode.NON_NUMERIC, classLoader.getResourceAsStream("Examples/PatientAddress.csv"));

                    handler = new PatientTelecomHandler(ctx, client);
                    processPatientTelecom(handler, ctx, ',', QuoteMode.NON_NUMERIC, classLoader.getResourceAsStream("Examples/PatientTelecom.csv"));

                    for (IBaseResource resource : resources) {
                        Patient patient = (Patient) resource;
                        Identifier identifier = null;
                        for (Identifier ident : patient.getIdentifier()) {
                            if (ident.getSystem().contains("PPMIdentifier")) identifier = ident;
                        }
                        //System.out.println(ctx.newJsonParser().setPrettyPrint(true).encodeResourceToString(patient));

                        //System.out.println("Patient?identifier=" + identifier.getSystem() + "%7C" + identifier.getValue());

                        if (identifier != null) {
                            MethodOutcome outcome = client.update().resource(resource)
                                    .conditionalByUrl("Patient?identifier=" + identifier.getSystem() + "%7C" + identifier.getValue())
                                    .execute();

                            if (outcome.getId() != null) {
                                patient.setId(outcome.getId().getIdPart());
                            }
                        }
                    }


                    resources.clear();


                } catch (Exception ex) {
                    ourLog.error(ex.getMessage());
                }
            }

            // Check for signs observations has already run

            pastResults = client
                    .search()
                    .forResource(Observation.class)
                    .where(Observation.CODE.exactly().code("86290005"))
                    .returnBundle(Bundle.class)
                    .execute();

            if (pastResults.getEntry().size()==0 && (theCommandLine.hasOption("a") ||theCommandLine.hasOption("o"))) {
                try {

                    //File file = new File(classLoader.getResourceAsStream ("Examples/Obs.csv").getFile());
                    resources.clear();
                    System.out.println("Obs.csv");

                    IRecordHandler handler = null;

                    handler = new ObsHandler();
                    processObsCSV(handler, ctx, ',', QuoteMode.NON_NUMERIC, classLoader.getResourceAsStream("Examples/Obs.csv"));
                    for (IBaseResource resource : resources) {
                        Observation observation = (Observation) resource;
                        MethodOutcome outcome = client.update().resource(resource)
                                .conditionalByUrl("Observation?identifier=" + observation.getIdentifier().get(0).getSystem() + "%7C" +observation.getIdentifier().get(0).getValue())
                                .execute();

                        if (outcome.getId() != null ) {
                            observation.setId(outcome.getId().getIdPart());
                        }
                    }
                    resources.clear();


                } catch (Exception ex) {
                    ourLog.error(ex.getMessage());
                }
            }

            if (theCommandLine.hasOption("a") ||theCommandLine.hasOption("e")) {
                try {
                    System.out.println("Encounter.csv");
                    resources.clear();
                    IRecordHandler handler = null;

                    handler = new EncounterHandler();
                    processEncounterCSV(handler, ctx, ',', QuoteMode.NON_NUMERIC, classLoader.getResourceAsStream("Examples/Encounter.csv"));
                    for (IBaseResource resource : resources) {
                        Encounter encounter = (Encounter) resource;
                        MethodOutcome outcome = client.update().resource(resource)
                                .conditionalByUrl("Encounter?identifier=" + encounter.getIdentifier().get(0).getSystem() + "%7C" +encounter.getIdentifier().get(0).getValue())
                                .execute();

                        if (outcome.getId() != null ) {
                            encounter.setId(outcome.getId().getIdPart());
                        }
                    }
                    resources.clear();


                } catch (Exception ex) {
                    ourLog.error(ex.getMessage());
                }
            }

            if (theCommandLine.hasOption("a") ||theCommandLine.hasOption("c")) {
                try {
                    System.out.println("Condition.csv");
                    resources.clear();
                    IRecordHandler handler = null;

                    handler = new ConditionHandler();
                    processConditionCSV(handler, ctx, ',', QuoteMode.NON_NUMERIC, classLoader.getResourceAsStream("Examples/Condition.csv"));
                    for (IBaseResource resource : resources) {
                        Condition condition = (Condition) resource;
                   //     System.out.println("Condition?identifier=" + condition.getIdentifier().get(0).getSystem() + "%7C" +condition.getIdentifier().get(0).getValue());
                        MethodOutcome outcome = client.update().resource(resource)
                                .conditionalByUrl("Condition?identifier=" + condition.getIdentifier().get(0).getSystem() + "%7C" +condition.getIdentifier().get(0).getValue())
                                .execute();

                        if (outcome.getId() != null ) {
                            condition.setId(outcome.getId().getIdPart());
                        }
                    }
                    resources.clear();


                } catch (Exception ex) {
                    ourLog.error(ex.getMessage());
                }
            }
            if (theCommandLine.hasOption("a") ||theCommandLine.hasOption("allergy")) {
                try {
                    System.out.println("AllergyIntolerance.csv");
                    resources.clear();
                    IRecordHandler handler = null;

                    handler = new AllergyHandler();
                    processAllergyCSV(handler, ctx, ',', QuoteMode.NON_NUMERIC, classLoader.getResourceAsStream("Examples/AllergyIntolerance.csv"));

                    handler = new AllergyReactionsHandler();
                    processAllergyReactionsCSV(handler, ctx, ',', QuoteMode.NON_NUMERIC, classLoader.getResourceAsStream("Examples/AllergyIntolerance.reactions.csv"));
                    for (IBaseResource resource : resources) {
                        AllergyIntolerance allergy = (AllergyIntolerance) resource;
                        MethodOutcome outcome = client.update().resource(resource)
                                .conditionalByUrl("AllergyIntolerance?identifier=" + allergy.getIdentifier().get(0).getSystem() + "%7C" +allergy.getIdentifier().get(0).getValue())
                                .execute();

                        if (outcome.getId() != null ) {
                            allergy.setId(outcome.getId().getIdPart());
                        }
                    }
                    resources.clear();


                } catch (Exception ex) {
                    ourLog.error(ex.getMessage());
                }
            }
            if (theCommandLine.hasOption("a") ||theCommandLine.hasOption("imms")) {
                try {
                    System.out.println("Immunisation.csv");
                    resources.clear();
                    IRecordHandler handler = null;

                    handler = new ImmunisationHandler();
                    processImmunisationCSV(handler, ctx, ',', QuoteMode.NON_NUMERIC, classLoader.getResourceAsStream("Examples/Immunisation.csv"));

                    for (IBaseResource resource : resources) {
                        Immunization immunization = (Immunization) resource;
                        MethodOutcome outcome = client.update().resource(resource)
                                .conditionalByUrl("Immunization?identifier=" + immunization.getIdentifier().get(0).getSystem() + "%7C" +immunization.getIdentifier().get(0).getValue())
                                .execute();

                        if (outcome.getId() != null ) {
                            immunization.setId(outcome.getId().getIdPart());
                        }
                    }
                    resources.clear();


                } catch (Exception ex) {
                    ourLog.error(ex.getMessage());
                }
            }
            if (theCommandLine.hasOption("proc") ||theCommandLine.hasOption("a")) {
                try {
                    System.out.println("Procedure.csv");
                    resources.clear();
                    IRecordHandler handler = null;

                    handler = new ProcedureHandler();
                    processProcedureCSV(handler, ctx, ',', QuoteMode.NON_NUMERIC, classLoader.getResourceAsStream("Examples/Procedure.csv"));

                    for (IBaseResource resource : resources) {
                        Procedure procedure = (Procedure) resource;
                        MethodOutcome outcome = client.update().resource(resource)
                                .conditionalByUrl("Procedure?identifier=" + procedure.getIdentifier().get(0).getSystem() + "%7C" +procedure.getIdentifier().get(0).getValue())
                                .execute();

                        if (outcome.getId() != null ) {
                            procedure.setId(outcome.getId().getIdPart());
                        }
                    }
                    resources.clear();


                } catch (Exception ex) {
                    ourLog.error(ex.getMessage());
                }
            }

            if (theCommandLine.hasOption("pres") ||theCommandLine.hasOption("a")) {
                try {
                    System.out.println("MedicationRequest.csv");
                    resources.clear();
                    IRecordHandler handler = null;

                    handler = new PrescriptionHandler();

                    processPrescriptionCSV(handler, ctx, ',', QuoteMode.NON_NUMERIC, classLoader.getResourceAsStream("Examples/MedicationRequest.csv"));

                    for (IBaseResource resource : resources) {

                        MedicationRequest prescription = (MedicationRequest) resource;
                        MethodOutcome outcome = client.update().resource(resource)
                                .conditionalByUrl("MedicationRequest?identifier=" + prescription.getIdentifier().get(0).getSystem() + "%7C" +prescription.getIdentifier().get(0).getValue())
                                .execute();

                        if (outcome.getId() != null ) {
                            prescription.setId(outcome.getId().getIdPart());
                        }
                    }
                    resources.clear();

                    handler = new StatementHandler();

                    processPrescriptionCSV(handler, ctx, ',', QuoteMode.NON_NUMERIC, classLoader.getResourceAsStream("Examples/MedicationRequest.csv"));

                    for (IBaseResource resource : resources) {

                        MedicationStatement statement = (MedicationStatement) resource;
                        MethodOutcome outcome = client.update().resource(resource)
                                .conditionalByUrl("MedicationStatement?identifier=" + statement.getIdentifier().get(0).getSystem() + "%7C" +statement.getIdentifier().get(0).getValue())
                                .execute();

                        if (outcome.getId() != null ) {
                            statement.setId(outcome.getId().getIdPart());
                        }
                    }
                    resources.clear();


                } catch (Exception ex) {
                    ourLog.error(ex.getMessage());
                }
            }
            /* Developer loads - remove for now
            try {
                File file = new File(classLoader.getResource("Examples/observations").getFile());

                for (File fileD : file.listFiles()) {
                    System.out.println(fileD.getName());
                    String contents = IOUtils.toString(new InputStreamReader(new FileInputStream(fileD), "UTF-8"));
                    IBaseResource localProfileResource = ca.uhn.fhir.rest.api.EncodingEnum.detectEncodingNoDefault(contents).newParser(ctx).parseResource(contents);
                    client.create().resource(localProfileResource).execute();
                }
            } catch (Exception ex) {
                ourLog.error(ex.getMessage());

            }
            */

            // Nokia

            // Don't reload patient 2
            pastResults = client
                    .search()
                    .forResource(Observation.class)
                    .where(Observation.PATIENT.hasId("2"))
                    .returnBundle(Bundle.class)
                    .execute();

            if (pastResults.getEntry().size()==0 && (theCommandLine.hasOption("a") ||theCommandLine.hasOption("phr"))) {

                try {

                    // File file = new File(classLoader.getResource("Examples/nokia/weight.csv").getFile());

                    System.out.println("activities.csv");
                    IRecordHandler handler = null;
                    resources.clear();
                    handler = new ActivitiesHandler();
                    processActivitiesCSV(handler, ctx, ',', QuoteMode.NON_NUMERIC, classLoader.getResourceAsStream("Examples/nokia/activities.csv"));
                    for (IBaseResource resource : resources) {
                        Observation observation = (Observation) resource;
                        MethodOutcome outcome = client.update().resource(resource)
                                .conditionalByUrl("Observation?identifier=" + observation.getIdentifier().get(0).getSystem() + "%7C" +observation.getIdentifier().get(0).getValue())
                                .execute();

                        if (outcome.getId() != null ) {
                            observation.setId(outcome.getId().getIdPart());
                        }
                    }
                    resources.clear();

                    System.out.println("weight.csv");


                    handler = new WeightHandler();
                    processWeightCSV(handler, ctx, ',', QuoteMode.NON_NUMERIC, classLoader.getResourceAsStream("Examples/nokia/weight.csv"));
                    for (IBaseResource resource : resources) {
                        Observation observation = (Observation) resource;
                        MethodOutcome outcome = client.update().resource(resource)
                                .conditionalByUrl("Observation?identifier=" + observation.getIdentifier().get(0).getSystem() + "%7C" +observation.getIdentifier().get(0).getValue())
                                .execute();

                        if (outcome.getId() != null ) {
                            observation.setId(outcome.getId().getIdPart());
                        }
                    }
                    resources.clear();

                    // file = new File(classLoader.getResource("Examples/nokia/blood_pressure.csv").getFile());

                    System.out.println("blood_pressure.csv");
                    handler = null;
                    resources.clear();
                    handler = new BloodPressureHandler();
                    processBloodPressureCSV(handler, ctx, ',', QuoteMode.NON_NUMERIC, classLoader.getResourceAsStream("Examples/nokia/blood_pressure.csv"));
                    for (IBaseResource resource : resources) {
                        Observation observation = (Observation) resource;
                        MethodOutcome outcome = client.update().resource(resource)
                                .conditionalByUrl("Observation?identifier=" + observation.getIdentifier().get(0).getSystem() + "%7C" +observation.getIdentifier().get(0).getValue())
                                .execute();

                        if (outcome.getId() != null ) {
                            observation.setId(outcome.getId().getIdPart());
                        }
                    }
                    resources.clear();


                } catch (Exception ex) {
                    ourLog.error(ex.getMessage());
                }
            }

        }

	}



    private List<String> getResourceFiles(String path ) throws IOException {
        List<String> filenames = new ArrayList<>();

        try(
                InputStream in = getResourceAsStream( path );
                BufferedReader br = new BufferedReader( new InputStreamReader( in ) ) ) {
            String resource;

            while( (resource = br.readLine()) != null ) {
                filenames.add( resource );
            }
        }

        return filenames;
    }

    private InputStream getResourceAsStream(String resource ) {
        final InputStream in
                = getContextClassLoader().getResourceAsStream( resource );

        return in == null ? getClass().getResourceAsStream( resource ) : in;
    }

    private ClassLoader getContextClassLoader() {
        return Thread.currentThread().getContextClassLoader();
    }

    private void processPatientCSV(IRecordHandler handler, FhirContext ctx, char theDelimiter, QuoteMode theQuoteMode, InputStream file) throws CommandFailureException {

        Boolean found = false;
        try {

            //  ourLog.info("Processing file {}", file.getName());
            found = true;

            Reader reader = null;
            CSVParser parsed = null;
            try {
                reader = new InputStreamReader(file);
                CSVFormat format = CSVFormat
                        .newFormat(theDelimiter)
                        .withAllowMissingColumnNames()
                        .withSkipHeaderRecord(true)
                        .withHeader("PATIENT_ID"
                                ,"RES_DELETED"
                                ,"RES_CREATED"
                                ,"RES_MESSAGE_REF"
                                ,"RES_UPDATED"
                                ,"active"
                                ,"date_of_birth"
                                ,"gender"
                                ,"registration_end"
                                ,"registration_start"
                                ,"NHSverification"
                                ,"ethnic"
                                ,"GP_ID"
                                ,"marital"
                                ,"PRACTICE_ID"
                        );
                if (theQuoteMode != null) {
                    format = format.withQuote('"').withQuoteMode(theQuoteMode);
                }
                parsed = new CSVParser(reader, format);
                Iterator<CSVRecord> iter = parsed.iterator();
                ourLog.debug("Header map: {}", parsed.getHeaderMap());

                int count = 0;

                int nextLoggedCount = 0;
                while (iter.hasNext()) {
                    CSVRecord nextRecord = iter.next();
                    handler.accept(nextRecord);
                    count++;
                    if (count >= nextLoggedCount) {
                        ourLog.info(" * Processed {} records", count);
                    }
                }

            } catch (IOException e) {
                throw new InternalErrorException(e);
            }
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }

    private void processPatientNameCSV(IRecordHandler handler, FhirContext ctx, char theDelimiter, QuoteMode theQuoteMode, InputStream file) throws CommandFailureException {

        Boolean found = false;
        try {

            //  ourLog.info("Processing file {}", file.getName());
            found = true;

            Reader reader = null;
            CSVParser parsed = null;
            try {
                reader = new InputStreamReader(file);
                CSVFormat format = CSVFormat
                        .newFormat(theDelimiter)
                        .withAllowMissingColumnNames()
                        .withSkipHeaderRecord(true)
                        .withHeader("PATIENT_NAME_ID"
                                ,"RES_DELETED"
                                ,"RES_CREATED"
                                ,"RES_MESSAGE_REF"
                                ,"RES_UPDATED"
                                ,"family_name"
                                ,"given_name"
                                ,"nameUse"
                                ,"prefix"
                                ,"suffix"
                                ,"PATIENT_ID"

                        );
                if (theQuoteMode != null) {
                    format = format.withQuote('"').withQuoteMode(theQuoteMode);
                }
                parsed = new CSVParser(reader, format);
                Iterator<CSVRecord> iter = parsed.iterator();
                ourLog.debug("Header map: {}", parsed.getHeaderMap());

                int count = 0;

                int nextLoggedCount = 0;
                while (iter.hasNext()) {
                    CSVRecord nextRecord = iter.next();
                    handler.accept(nextRecord);
                    count++;
                    if (count >= nextLoggedCount) {
                        ourLog.info(" * Processed {} records", count);
                    }
                }

            } catch (IOException e) {
                throw new InternalErrorException(e);
            }
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }

    private void processPatientTelecom(IRecordHandler handler, FhirContext ctx, char theDelimiter, QuoteMode theQuoteMode, InputStream file) throws CommandFailureException {

        Boolean found = false;
        try {

            //  ourLog.info("Processing file {}", file.getName());
            found = true;

            Reader reader = null;
            CSVParser parsed = null;
            try {
                reader = new InputStreamReader(file);
                CSVFormat format = CSVFormat
                        .newFormat(theDelimiter)
                        .withAllowMissingColumnNames()
                        .withSkipHeaderRecord(true)
                        .withHeader("PATIENT_TELECOM_ID"
                                ,"system"
                                ,"telecomUse"
                                ,"value"
                                ,"PATIENT_ID"
                        );
                if (theQuoteMode != null) {
                    format = format.withQuote('"').withQuoteMode(theQuoteMode);
                }
                parsed = new CSVParser(reader, format);
                Iterator<CSVRecord> iter = parsed.iterator();
                ourLog.debug("Header map: {}", parsed.getHeaderMap());

                int count = 0;

                int nextLoggedCount = 0;
                while (iter.hasNext()) {
                    CSVRecord nextRecord = iter.next();
                    handler.accept(nextRecord);
                    count++;
                    if (count >= nextLoggedCount) {
                        ourLog.info(" * Processed {} records", count);
                    }
                }

            } catch (IOException e) {
                throw new InternalErrorException(e);
            }
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }

    private void processPatientAddressCSV(IRecordHandler handler, FhirContext ctx, char theDelimiter, QuoteMode theQuoteMode, InputStream file) throws CommandFailureException {

        Boolean found = false;
        try {

            //  ourLog.info("Processing file {}", file.getName());
            found = true;

            Reader reader = null;
            CSVParser parsed = null;
            try {
                reader = new InputStreamReader(file);
                CSVFormat format = CSVFormat
                        .newFormat(theDelimiter)
                        .withAllowMissingColumnNames()
                        .withSkipHeaderRecord(true)
                        .withHeader("PATIENT_ADDRESS_ID"
                                ,"AddressType"
                                ,"addressUse"
                                ,"ADDRESS_ID"
                                ,"PATIENT_ID"
                        );
                if (theQuoteMode != null) {
                    format = format.withQuote('"').withQuoteMode(theQuoteMode);
                }
                parsed = new CSVParser(reader, format);
                Iterator<CSVRecord> iter = parsed.iterator();
                ourLog.debug("Header map: {}", parsed.getHeaderMap());

                int count = 0;

                int nextLoggedCount = 0;
                while (iter.hasNext()) {
                    CSVRecord nextRecord = iter.next();
                    handler.accept(nextRecord);
                    count++;
                    if (count >= nextLoggedCount) {
                        ourLog.info(" * Processed {} records", count);
                    }
                }

            } catch (IOException e) {
                throw new InternalErrorException(e);
            }
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }

    private void processAddressCSV(IRecordHandler handler, FhirContext ctx, char theDelimiter, QuoteMode theQuoteMode, InputStream file) throws CommandFailureException {

        Boolean found = false;
        try {

            //  ourLog.info("Processing file {}", file.getName());
            found = true;

            Reader reader = null;
            CSVParser parsed = null;
            try {
                reader = new InputStreamReader(file);
                CSVFormat format = CSVFormat
                        .newFormat(theDelimiter)
                        .withAllowMissingColumnNames()
                        .withSkipHeaderRecord(true)
                        .withHeader("ADDRESS_ID"
                                ,"RES_DELETED"
                                ,"RES_CREATED"
                                ,"RES_MESSAGE_REF"
                                ,"RES_UPDATED"
                                ,"address_1"
                                ,"address_2"
                                ,"address_3"
                                ,"address_4"
                                ,"address_5"
                                ,"city"
                                ,"county"
                                ,"postcode"
                        );
                if (theQuoteMode != null) {
                    format = format.withQuote('"').withQuoteMode(theQuoteMode);
                }
                parsed = new CSVParser(reader, format);
                Iterator<CSVRecord> iter = parsed.iterator();
                ourLog.debug("Header map: {}", parsed.getHeaderMap());

                int count = 0;

                int nextLoggedCount = 0;
                while (iter.hasNext()) {
                    CSVRecord nextRecord = iter.next();
                    handler.accept(nextRecord);
                    count++;
                    if (count >= nextLoggedCount) {
                        ourLog.info(" * Processed {} records", count);
                    }
                }

            } catch (IOException e) {
                throw new InternalErrorException(e);
            }
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }
    private void processPatientIdentifierCSV(IRecordHandler handler, FhirContext ctx, char theDelimiter, QuoteMode theQuoteMode, InputStream file) throws CommandFailureException {

        Boolean found = false;
        try {

            //  ourLog.info("Processing file {}", file.getName());
            found = true;

            Reader reader = null;
            CSVParser parsed = null;
            try {
                reader = new InputStreamReader(file);
                CSVFormat format = CSVFormat
                        .newFormat(theDelimiter)
                        .withAllowMissingColumnNames()
                        .withSkipHeaderRecord(true)
                        .withHeader("PATIENT_IDENTIFIER_ID"
                                ,"identifierUse"
                                ,"listOrder"
                                ,"value","SYSTEM_ID"
                                ,"PATIENT_ID"
                        );
                if (theQuoteMode != null) {
                    format = format.withQuote('"').withQuoteMode(theQuoteMode);
                }
                parsed = new CSVParser(reader, format);
                Iterator<CSVRecord> iter = parsed.iterator();
                ourLog.debug("Header map: {}", parsed.getHeaderMap());

                int count = 0;

                int nextLoggedCount = 0;
                while (iter.hasNext()) {
                    CSVRecord nextRecord = iter.next();
                    handler.accept(nextRecord);
                    count++;
                    if (count >= nextLoggedCount) {
                        ourLog.info(" * Processed {} records", count);
                    }
                }

            } catch (IOException e) {
                throw new InternalErrorException(e);
            }
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }

    private void processObsCSV(IRecordHandler handler, FhirContext ctx, char theDelimiter, QuoteMode theQuoteMode, InputStream file) throws CommandFailureException {

        Boolean found = false;
        try {

          //  ourLog.info("Processing file {}", file.getName());
            found = true;

            Reader reader = null;
            CSVParser parsed = null;
            try {
                reader = new InputStreamReader(file);
                CSVFormat format = CSVFormat
                        .newFormat(theDelimiter)
                        .withAllowMissingColumnNames()
                        .withSkipHeaderRecord(true)
                        .withHeader("ObservationID"
                                ,"PatientID"
                                ,"EffectiveDateTime"
                                ,"Code"
                                ,"CodeDisplay"
                                ,"Method"
                                ,"MethodDisplay"
                                ,"BodySite"
                                ,"BodySiteDescription"
                                ,"ValueQuantity"
                                ,"ValueCode"
                                ,"ValueString"
                                ,"ValueUnitOfMeasure"
                                ,"LowRange"
                                ,"HighRange"
                                ,"Interpretation"
                                ,"Age"
                                ,"FHIRCategory"
                                ,"PerformerType"
                                ,"PerformerIdentifier"
                        );
                if (theQuoteMode != null) {
                    format = format.withQuote('"').withQuoteMode(theQuoteMode);
                }
                parsed = new CSVParser(reader, format);
                Iterator<CSVRecord> iter = parsed.iterator();
                ourLog.debug("Header map: {}", parsed.getHeaderMap());

                int count = 0;

                int nextLoggedCount = 0;
                while (iter.hasNext()) {
                    CSVRecord nextRecord = iter.next();
                    handler.accept(nextRecord);
                    count++;
                    if (count >= nextLoggedCount) {
                        ourLog.info(" * Processed {} records", count);
                    }
                }

            } catch (IOException e) {
                throw new InternalErrorException(e);
            }
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }

    private void processEncounterCSV(IRecordHandler handler, FhirContext ctx, char theDelimiter, QuoteMode theQuoteMode, InputStream file) throws CommandFailureException {

        Boolean found = false;
        try {

            //  ourLog.info("Processing file {}", file.getName());
            found = true;

            Reader reader = null;
            CSVParser parsed = null;
            try {
                reader = new InputStreamReader(file);
                CSVFormat format = CSVFormat
                        .newFormat(theDelimiter)
                        .withAllowMissingColumnNames()
                        .withSkipHeaderRecord(true)
                        .withHeader("encounterID"
                                ,"period.startDate"
                                ,"period.startTime"
                                ,"period.endDate"
                                ,"period.endTime"
                                ,"patientID"
                                ,"participant.type"
                                ,"resource.type"
                                ,"participent.individual"
                                ,"serviceProvider"
                        );
                if (theQuoteMode != null) {
                    format = format.withQuote('"').withQuoteMode(theQuoteMode);
                }
                parsed = new CSVParser(reader, format);
                Iterator<CSVRecord> iter = parsed.iterator();
                ourLog.debug("Header map: {}", parsed.getHeaderMap());

                int count = 0;

                int nextLoggedCount = 0;
                while (iter.hasNext()) {
                    CSVRecord nextRecord = iter.next();
                    handler.accept(nextRecord);
                    count++;
                    if (count >= nextLoggedCount) {
                        ourLog.info(" * Processed {} records", count);
                    }
                }

            } catch (IOException e) {
                throw new InternalErrorException(e);
            }
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }

    private void processPractitionerCSV(IRecordHandler handler, FhirContext ctx, char theDelimiter, QuoteMode theQuoteMode, InputStream file) throws CommandFailureException {

        Boolean found = false;
        try {

            //  ourLog.info("Processing file {}", file.getName());
            found = true;

            Reader reader = null;
            CSVParser parsed = null;
            try {
                reader = new InputStreamReader(file);
                CSVFormat format = CSVFormat
                        .newFormat(theDelimiter)
                        .withAllowMissingColumnNames()
                        .withSkipHeaderRecord(true)
                        .withHeader("OrganisationCode"
                                ,"Name"
                                ,"NationalGrouping"
                                ,"HighLevelHealthGeography"
                                ,"AddressLine_1"
                                ,"AddressLine_2"
                                ,"AddressLine_3"
                                ,"AddressLine_4"
                                ,"AddressLine_5"
                                ,"Postcode"
                                ,"OpenDate"
                                ,"CloseDate"
                                ,"Fld13"
                                ,"OrganisationSubTypeCode"
                                ,"Commissioner"
                                ,"Fld16"
                                ,"Fld17"
                                ,"ContactTelephoneNumber"
                        );
                if (theQuoteMode != null) {
                    format = format.withQuote('"').withQuoteMode(theQuoteMode);
                }
                parsed = new CSVParser(reader, format);
                Iterator<CSVRecord> iter = parsed.iterator();
                ourLog.debug("Header map: {}", parsed.getHeaderMap());

                int count = 0;

                int nextLoggedCount = 0;
                while (iter.hasNext()) {
                    CSVRecord nextRecord = iter.next();
                    handler.accept(nextRecord);
                    count++;
                    if (count >= nextLoggedCount) {
                        ourLog.info(" * Processed {} records", count);
                    }
                }

            } catch (IOException e) {
                throw new InternalErrorException(e);
            }
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }


    private void processProcedureCSV(IRecordHandler handler, FhirContext ctx, char theDelimiter, QuoteMode theQuoteMode, InputStream file) throws CommandFailureException {

        Boolean found = false;
        try {

            found = true;

            Reader reader = null;
            CSVParser parsed = null;
            try {
                reader = new InputStreamReader(file);
                CSVFormat format = CSVFormat
                        .newFormat(theDelimiter)
                        .withAllowMissingColumnNames()
                        .withSkipHeaderRecord(true)
                        .withHeader("identifier"
                                ,"subject"
                                ,"status"
                                ,"code.coding.code"
                                ,"code.coding.display"
                                ,"performed date"
                                ,"performed time"
                                ,"category.code.coding.code"
                                ,"category.code.coding.display"
                                ,"notPerformed"
                                ,"Performer Type"
                                ,"performer"
                                ,"encounter"
                                ,"outcome.coding.code"
                                ,"ouctome.coding.display"
                        );
                if (theQuoteMode != null) {
                    format = format.withQuote('"').withQuoteMode(theQuoteMode);
                }
                parsed = new CSVParser(reader, format);
                Iterator<CSVRecord> iter = parsed.iterator();
                ourLog.debug("Header map: {}", parsed.getHeaderMap());

                int count = 0;

                int nextLoggedCount = 0;
                while (iter.hasNext()) {
                    CSVRecord nextRecord = iter.next();
                    handler.accept(nextRecord);
                    count++;
                    if (count >= nextLoggedCount) {
                        ourLog.info(" * Processed {} records", count);
                    }
                }

            } catch (IOException e) {
                throw new InternalErrorException(e);
            }
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }

    private void processImmunisationCSV(IRecordHandler handler, FhirContext ctx, char theDelimiter, QuoteMode theQuoteMode, InputStream file) throws CommandFailureException {

        Boolean found = false;
        try {

            found = true;

            Reader reader = null;
            CSVParser parsed = null;
            try {
                reader = new InputStreamReader(file);
                CSVFormat format = CSVFormat
                        .newFormat(theDelimiter)
                        .withAllowMissingColumnNames()
                        .withSkipHeaderRecord(true)
                        .withHeader("identifier"
                                ,"patientID"
                                ,"parentPresent"
                                ,"dateRecorded"
                                ,"status"
                                ,"dateAdministered"
                                ,"timeAdministered"
                                ,"vaccineCode.coding"
                                ,"notGiven"
                                ,"encounter"
                                ,"primarySource"
                                ,"reportOrigin"
                                ,"location"
                                ,"lotNumber"
                                ,"expirationDate"
                                ,"vaccineCode.coding.display"
                        );
                if (theQuoteMode != null) {
                    format = format.withQuote('"').withQuoteMode(theQuoteMode);
                }
                parsed = new CSVParser(reader, format);
                Iterator<CSVRecord> iter = parsed.iterator();
                ourLog.debug("Header map: {}", parsed.getHeaderMap());

                int count = 0;

                int nextLoggedCount = 0;
                while (iter.hasNext()) {
                    CSVRecord nextRecord = iter.next();
                    handler.accept(nextRecord);
                    count++;
                    if (count >= nextLoggedCount) {
                        ourLog.info(" * Processed {} records", count);
                    }
                }

            } catch (IOException e) {
                throw new InternalErrorException(e);
            }
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }

    private void processAllergyCSV(IRecordHandler handler, FhirContext ctx, char theDelimiter, QuoteMode theQuoteMode, InputStream file) throws CommandFailureException {

        Boolean found = false;
        try {

            found = true;

            Reader reader = null;
            CSVParser parsed = null;
            try {
                reader = new InputStreamReader(file);
                CSVFormat format = CSVFormat
                        .newFormat(theDelimiter)
                        .withAllowMissingColumnNames()
                        .withSkipHeaderRecord(true)
                        .withHeader("identifier"
                                ,"encounter"
                                ,"patient"
                                ,"substance.coding.code"
                                ,"substance.coding.display"
                                ,"clinicalStatus"
                                ,"verificationStatus"
                                ,"onset.DateTime"
                                ,"assertedDate"
                                ,"recorder"
                                ,"lastOccurrence"

                        );
                if (theQuoteMode != null) {
                    format = format.withQuote('"').withQuoteMode(theQuoteMode);
                }
                parsed = new CSVParser(reader, format);
                Iterator<CSVRecord> iter = parsed.iterator();
                ourLog.debug("Header map: {}", parsed.getHeaderMap());

                int count = 0;

                int nextLoggedCount = 0;
                while (iter.hasNext()) {
                    CSVRecord nextRecord = iter.next();
                    handler.accept(nextRecord);
                    count++;
                    if (count >= nextLoggedCount) {
                        ourLog.info(" * Processed {} records", count);
                    }
                }

            } catch (IOException e) {
                throw new InternalErrorException(e);
            }
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }

    private void processAllergyReactionsCSV(IRecordHandler handler, FhirContext ctx, char theDelimiter, QuoteMode theQuoteMode, InputStream file) throws CommandFailureException {

        Boolean found = false;
        try {

            found = true;

            Reader reader = null;
            CSVParser parsed = null;
            try {
                reader = new InputStreamReader(file);
                CSVFormat format = CSVFormat
                        .newFormat(theDelimiter)
                        .withAllowMissingColumnNames()
                        .withSkipHeaderRecord(true)
                        .withHeader("identifier"
                                ,"allergy.identifier"
                                ,"reaction.manifestation"
                                ,"reaction.manifestation.display"
                        );
                if (theQuoteMode != null) {
                    format = format.withQuote('"').withQuoteMode(theQuoteMode);
                }
                parsed = new CSVParser(reader, format);
                Iterator<CSVRecord> iter = parsed.iterator();
                ourLog.debug("Header map: {}", parsed.getHeaderMap());

                int count = 0;

                int nextLoggedCount = 0;
                while (iter.hasNext()) {
                    CSVRecord nextRecord = iter.next();
                    handler.accept(nextRecord);
                    count++;
                    if (count >= nextLoggedCount) {
                        ourLog.info(" * Processed {} records", count);
                    }
                }

            } catch (IOException e) {
                throw new InternalErrorException(e);
            }
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }

    private void processConditionCSV(IRecordHandler handler, FhirContext ctx, char theDelimiter, QuoteMode theQuoteMode, InputStream file) throws CommandFailureException {

        Boolean found = false;
        try {

            found = true;

            Reader reader = null;
            CSVParser parsed = null;
            try {
                reader = new InputStreamReader(file);
                CSVFormat format = CSVFormat
                        .newFormat(theDelimiter)
                        .withAllowMissingColumnNames()
                        .withSkipHeaderRecord(true)
                        .withHeader("identifier"
                                ,"patient ID"
                                ,"encounter"
                                ,"asserter"
                                ,"dateRecorded"
                                ,"code.coding.code"
                                ,"code.coding.display"
                                ,"category.code"
                                ,"category.description"
                                ,"clinicalStatus"
                                ,"verificationStatus"
                                ,"severity.code.coding.code"
                                ,"serverity.code.coding.description"
                        );
                if (theQuoteMode != null) {
                    format = format.withQuote('"').withQuoteMode(theQuoteMode);
                }
                parsed = new CSVParser(reader, format);
                Iterator<CSVRecord> iter = parsed.iterator();
                ourLog.debug("Header map: {}", parsed.getHeaderMap());

                int count = 0;

                int nextLoggedCount = 0;
                while (iter.hasNext()) {
                    CSVRecord nextRecord = iter.next();
                    handler.accept(nextRecord);
                    count++;
                    if (count >= nextLoggedCount) {
                        ourLog.info(" * Processed {} records", count);
                    }
                }

            } catch (IOException e) {
                throw new InternalErrorException(e);
            }
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }
    private void processWeightCSV(IRecordHandler handler, FhirContext ctx, char theDelimiter, QuoteMode theQuoteMode, InputStream file) throws CommandFailureException {


        Boolean found = false;
        try {


            found = true;

            Reader reader = null;
            CSVParser parsed = null;
            try {
                reader = new InputStreamReader(file);
                CSVFormat format = CSVFormat
                        .newFormat(theDelimiter)
                        .withAllowMissingColumnNames()
                        .withSkipHeaderRecord(true)
                        .withHeader("Date"
                                ,"Weight"
                                ,"FatMass"
                                ,"BoneMass"
                                ,"MuscleMass"
                                ,"Hydration"
                                ,"Comments"
                        );
                if (theQuoteMode != null) {
                    format = format.withQuote('"').withQuoteMode(theQuoteMode);
                }
                parsed = new CSVParser(reader, format);
                Iterator<CSVRecord> iter = parsed.iterator();
                ourLog.debug("Header map: {}", parsed.getHeaderMap());

                int count = 0;

                int nextLoggedCount = 0;
                while (iter.hasNext()) {
                    CSVRecord nextRecord = iter.next();
                    handler.accept(nextRecord);
                    count++;
                    if (count >= nextLoggedCount) {
                        ourLog.info(" * Processed {} records", count);
                    }
                }

            } catch (IOException e) {
                throw new InternalErrorException(e);
            }
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }

    private void processPrescriptionCSV(IRecordHandler handler, FhirContext ctx, char theDelimiter, QuoteMode theQuoteMode, InputStream file) throws CommandFailureException {


        Boolean found = false;
        try {


            found = true;

            Reader reader = null;
            CSVParser parsed = null;
            try {
                reader = new InputStreamReader(file);
                CSVFormat format = CSVFormat
                        .newFormat(theDelimiter)
                        .withAllowMissingColumnNames()
                        .withSkipHeaderRecord(true)
                        .withHeader("identifier"
                                ,"subject (patientID)"
                                ,"status"
                                ,"intent"
                                ,"priority"
                                ,"medication"
                                ,"context"
                                ,"authoredOn Date"
                                ,"authoredOn Time"
                                ,"requester.agent type"
                                ,"requester.agent"
                                ,"reasonCode Type"
                                ,"reasonCode"
                                ,"dosage.text"
                                ,"dosage.additionalInstruction"
                                ,"dosage.timing"
                                ,"dosage.asNeeded.asNeededBoolean"
                                ,"dosage.asNeeded.asNeededCodableConcept"
                                ,"dosage.route"
                                ,"dosage.dose.doseRange.low.value"
                                ,"dosage.dose.doseRange.low.units"
                                ,"dosage.dose.doseRange.high.value"
                                ,"dosage.dose.doseRange.high.units"
                                ,"dosage.dose.doseQuantity.value"
                                ,"dosage.dose.doseQuantity.units"
                                ,"dispenseRequest.validityPeriod.start"
                                ,"dispenseRequest.validityPeriod.end"
                                ,"dispenseRequest.numberOfRepeatsAllowed"
                                ,"dispenseRequest.quantity.value"
                                ,"dispenseRequest.quantity.units"
                                ,"dispenseRequest.expectedSupplyDuration.value"
                                ,"dispenseRequest.expectedSupplyDuration.units"
                        );
                if (theQuoteMode != null) {
                    format = format.withQuote('"').withQuoteMode(theQuoteMode);
                }
                parsed = new CSVParser(reader, format);
                Iterator<CSVRecord> iter = parsed.iterator();
                ourLog.debug("Header map: {}", parsed.getHeaderMap());

                int count = 0;

                int nextLoggedCount = 0;
                while (iter.hasNext()) {
                    CSVRecord nextRecord = iter.next();
                    System.out.println("On nextRecord");
                    handler.accept(nextRecord);
                    count++;
                    if (count >= nextLoggedCount) {
                        ourLog.info(" * Processed {} records", count);
                    }
                }

            } catch (IOException e) {
                throw new InternalErrorException(e);
            }
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }



    public String getPractitioner (String identifier) {
	    String practitionerId = null;
        Bundle results = client
                .search()
                .forResource(Practitioner.class)
                .where(Practitioner.IDENTIFIER.exactly().code(identifier))
                .returnBundle(Bundle.class)
                .execute();
        //   System.out.println(results.getEntry().size());
        if (results.getEntry().size() == 0) {

            // Get result from ODS API

            System.out.println("Practitioner "+identifier);

            /*
            Practitioner odsPractitioner = odsClient
                    .read().resource(Practitioner.class).withId(identifier)
                    .execute();
            if (odsPractitioner != null) {
                MethodOutcome outcome = client.create().resource(odsPractitioner).execute();
                if (outcome.getId() != null) {
                    practitionerId = outcome.getId().getIdPart();
                    docMap.put(identifier, practitionerId);
                }
            }*/
        } else {
            Practitioner prac = (Practitioner) results.getEntry().get(0).getResource();
            practitionerId = prac.getIdElement().getIdPart();
            docMap.put(identifier, practitionerId);
        }
        return practitionerId;
    }

    public String getOrganisation (String identifier) {
        String organisationId = null;


        Bundle results = client
                .search()
                .forResource(Organization.class)
                .where(Organization.IDENTIFIER.exactly().code(identifier))
                .returnBundle(Bundle.class)
                .execute();
        //   System.out.println(results.getEntry().size());
        if (results.getEntry().size() == 0) {

            // Get result from ODS API
            System.out.println("Looking up Organisation "+identifier);
            try {
                Organization odsOrganisation = odsClient
                        .read().resource(Organization.class).withId(identifier)
                        .execute();
                if (odsOrganisation != null) {
                    MethodOutcome outcome = client.create().resource(odsOrganisation).execute();


                    if (outcome.getId() != null) {
                        organisationId = outcome.getId().getIdPart();
                        orgMap.put(identifier, organisationId);
                    }
                }
            } catch(Exception ex) {
                System.out.println("Not found " +identifier);
            }
         } else {
            Organization org = (Organization) results.getEntry().get(0).getResource();
            organisationId = org.getIdElement().getIdPart();
            orgMap.put(identifier, organisationId);
        }

        return organisationId;
    }

    public String getPatientId(String ppmId ) {
        String patientId = null;

        Bundle results = client
                .search()
                .forResource(Patient.class)
                .where(Patient.IDENTIFIER.exactly().systemAndCode("https://fhir.leedsth.nhs.uk/Id/PPMIdentifier" ,ppmId))
                .returnBundle(Bundle.class)
                .execute();
        //   System.out.println(results.getEntry().size());
        if (results.getEntry().size() > 0) {
            patientId = results.getEntry().get(0).getResource().getId();
        }
        return patientId;
    }

    private void processActivitiesCSV(IRecordHandler handler, FhirContext ctx, char theDelimiter, QuoteMode theQuoteMode, InputStream file) throws CommandFailureException {


        Boolean found = false;
        try {


            found = true;

            Reader reader = null;
            CSVParser parsed = null;
            try {
                reader = new InputStreamReader(file);
                CSVFormat format = CSVFormat
                        .newFormat(theDelimiter)
                        .withAllowMissingColumnNames()
                        .withSkipHeaderRecord(true)
                        .withHeader("Date"
                                ,"Steps"
                                ,"Distance (m)"
                                ,"Elevation (m)"
                                ,"Active calories"
                        );
                if (theQuoteMode != null) {
                    format = format.withQuote('"').withQuoteMode(theQuoteMode);
                }
                parsed = new CSVParser(reader, format);
                Iterator<CSVRecord> iter = parsed.iterator();
                ourLog.debug("Header map: {}", parsed.getHeaderMap());

                int count = 0;

                int nextLoggedCount = 0;
                while (iter.hasNext()) {
                    CSVRecord nextRecord = iter.next();
                    handler.accept(nextRecord);
                    count++;
                    if (count >= nextLoggedCount) {
                        ourLog.info(" * Processed {} records", count);
                    }
                }

            } catch (IOException e) {
                throw new InternalErrorException(e);
            }
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }

    private void processBloodPressureCSV(IRecordHandler handler, FhirContext ctx, char theDelimiter, QuoteMode theQuoteMode, InputStream file) throws CommandFailureException {


        Boolean found = false;
        try {

            //ourLog.info("Processing file {}", file.getName());
            found = true;

            Reader reader = null;
            CSVParser parsed = null;
            try {
                reader = new InputStreamReader(file);
                CSVFormat format = CSVFormat
                        .newFormat(theDelimiter)
                        .withAllowMissingColumnNames()
                        .withSkipHeaderRecord(true)
                        .withHeader("Date"
                                ,"HeartRate",
                                "Systolic"
                                ,"Diastolic"
                                ,"Comments"
                        );
                if (theQuoteMode != null) {
                    format = format.withQuote('"').withQuoteMode(theQuoteMode);
                }
                parsed = new CSVParser(reader, format);
                Iterator<CSVRecord> iter = parsed.iterator();
                ourLog.debug("Header map: {}", parsed.getHeaderMap());

                int count = 0;

                int nextLoggedCount = 0;
                while (iter.hasNext()) {
                    CSVRecord nextRecord = iter.next();
                    handler.accept(nextRecord);
                    count++;
                    if (count >= nextLoggedCount) {
                        ourLog.info(" * Processed {} records", count, file);
                    }
                }

            } catch (IOException e) {
                throw new InternalErrorException(e);
            }
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }


    private interface IRecordHandler {
        void accept(CSVRecord theRecord);
    }

    public class ObsHandler implements  IRecordHandler {
        @Override
        public void accept(CSVRecord theRecord) {
           // System.out.println(theRecord.toString());

            Observation observation = newBasicObservation2(theRecord.get("EffectiveDateTime"), theRecord.get("FHIRCategory"), theRecord.get("FHIRCategory"),CareConnectSystem.FHIRObservationCategory);

            observation.addIdentifier()
                    .setSystem("https://fhir.leedsth.nhs.uk/Id/observation")
                    .setValue(theRecord.get("ObservationID"));
            CodeableConcept code = observation.getCode();
            code.addCoding().setSystem(CareConnectSystem.SNOMEDCT).setCode(theRecord.get("Code")).setDisplay(theRecord.get("CodeDisplay"));

            observation.setSubject(new Reference("Patient/"+getPatientId(theRecord.get("PatientID"))));
            if (!theRecord.get("ValueQuantity").isEmpty()) {
                Quantity quantity = new Quantity();
                quantity
                        .setValue(new BigDecimal(theRecord.get("ValueQuantity")))
                        .setUnit(theRecord.get("ValueUnitOfMeasure"))
                        .setCode(theRecord.get("ValueUnitOfMeasure"))
                        .setSystem(CareConnectSystem.UnitOfMeasure);
                observation.setValue(quantity);
            }
            if (!theRecord.get("ValueCode").isEmpty()) {
                CodeableConcept valueCode = new CodeableConcept();
                valueCode.addCoding().setDisplay(theRecord.get("ValueString")).setCode(theRecord.get("ValueCode")).setSystem(CareConnectSystem.SNOMEDCT);
                observation.setValue(valueCode);
            }
            if (!theRecord.get("Method").isEmpty()) {
                observation.getMethod().addCoding().setSystem(CareConnectSystem.SNOMEDCT).setCode(theRecord.get("Method")).setDisplay(theRecord.get("MethodDisplay"));
            }
            if (!theRecord.get("BodySite").isEmpty()) {
                observation.getBodySite().addCoding().setSystem(CareConnectSystem.SNOMEDCT).setCode(theRecord.get("BodySite")).setDisplay(theRecord.get("BodySiteDescription"));
            }
            if (!theRecord.get("Interpretation").isEmpty()) {
                observation.getInterpretation().addCoding().setSystem(CareConnectSystem.HL7v2Table0078).setCode(theRecord.get("Interpretation")).setDisplay(theRecord.get("Interpretation"));
            }
            if (!theRecord.get("LowRange").isEmpty() || !theRecord.get("HighRange").isEmpty()) {
                Observation.ObservationReferenceRangeComponent range = observation.addReferenceRange();
                if (!theRecord.get("LowRange").isEmpty()) {
                    SimpleQuantity low = new SimpleQuantity();
                    low.setValue(new BigDecimal(theRecord.get("LowRange")));
                    range.setLow(low);
                }
                if (!theRecord.get("HighRange").isEmpty()) {

                    SimpleQuantity high = new SimpleQuantity();
                    high.setValue(new BigDecimal(theRecord.get("HighRange")));
                    range.setHigh(high);
                }
            }
            if (!theRecord.get("PerformerType").isEmpty()) {
             //   System.out.println(theRecord.get("PerformerType"));
            //    System.out.println(theRecord.get("PerformerIdentifier"));
                switch (theRecord.get("PerformerType")) {
                    case "practitioner" :
                        String practitioner = docMap.get(theRecord.get("PerformerIdentifier"));
                        if (practitioner == null) {
                            practitioner = getPractitioner(theRecord.get("PerformerIdentifier"));
                        }
                        if (practitioner != null) {
                            observation.addPerformer(new Reference("Practitioner/" + practitioner));
                        }

                        break;

                    case "Organization" :

                        String organization= orgMap.get(theRecord.get("PerformerIdentifier"));
                        if (organization == null) {
                           organization = getOrganisation(theRecord.get("PerformerIdentifier"));
                        }
                        if (organization != null) {
                            observation.addPerformer(new Reference("Organization/" + organization));
                        }
                        break;

                }
            }

            //System.out.println(ctx.newJsonParser().setPrettyPrint(true).encodeResourceToString(observation));
           resources.add(observation);
        }
    }

    public class ConsultantHandler implements IRecordHandler {
        @Override
        public void accept(CSVRecord theRecord) {
            // System.out.println(theRecord.toString());
            Practitioner practitioner = new Practitioner();
            practitioner.setId("dummy");
            practitioner.setMeta(new Meta().addProfile(CareConnectProfile.Practitioner_1));

            practitioner.addIdentifier()
                    .setSystem(CareConnectSystem.SDSUserId)
                    .setValue(theRecord.get(1));

            practitioner.setActive(true);

            if (!theRecord.get(2).isEmpty()) {

                HumanName name = new HumanName();
                practitioner.getName().add(name);
                name.setFamily(Inicaps(theRecord.get(2)));
                name.addPrefix("Dr");

                if (!theRecord.get(3).isEmpty()) {
                    name.addGiven(theRecord.get(3));
                }
            }
            if (!theRecord.get(4).isEmpty()) {
                switch (theRecord.get(4)) {
                    case "M" : practitioner.setGender(Enumerations.AdministrativeGender.MALE);
                        break;
                    case "F" : practitioner.setGender(Enumerations.AdministrativeGender.FEMALE);
                        break;
                }
            }


            resources.add(practitioner);

            // TODO Missing addition of specialty field 5 and organisation field 7

            PractitionerRole role = new PractitionerRole();

            if (!theRecord.get(7).isEmpty()) {
                String parentOrgId = orgMap.get(theRecord.get(7));
                if (parentOrgId == null) {
                    parentOrgId = getOrganisation(theRecord.get(7));
                }
                if (parentOrgId != null) {
                    role.setOrganization(new Reference("Organization/"+parentOrgId));
                }
            }
            role.addIdentifier()
                    .setSystem(CareConnectSystem.SDSUserId)
                    .setValue(theRecord.get(1));
            // Make a note of the practitioner. Will need to change to correct code
            role.setPractitioner(new Reference(theRecord.get(1)));

            /* TODO basic ConceptMapping */

            switch(theRecord.get(5)) {
                case "101":
                    CodeableConcept specialty = new CodeableConcept();
                    specialty.addCoding()
                            .setSystem(CareConnectSystem.SNOMEDCT)
                            .setCode("394612005")
                            .setDisplay("Urology (qualifier value)");
                    role.getSpecialty().add(specialty);
                    break;
                case "320":
                    CodeableConcept concept = new CodeableConcept();
                    concept.addCoding()
                            .setSystem(CareConnectSystem.SNOMEDCT)
                            .setCode("394579002")
                            .setDisplay("Cardiology (qualifier value)");
                    role.getSpecialty().add(concept);
                    break;
            }
            roles.add(role);
        }

    }


    public class PractitionerHandler implements IRecordHandler {
        @Override
        public void accept(CSVRecord theRecord) {

            Practitioner practitioner = new Practitioner();
            practitioner.setId("dummy");
            practitioner.setMeta(new Meta().addProfile(CareConnectProfile.Practitioner_1));

            practitioner.addIdentifier()
                    .setSystem(CareConnectSystem.SDSUserId)
                    .setValue(theRecord.get("OrganisationCode"));

            if (!theRecord.get("ContactTelephoneNumber").isEmpty()) {
                practitioner.addTelecom()
                        .setUse(ContactPoint.ContactPointUse.WORK)
                        .setValue(theRecord.get("ContactTelephoneNumber"))
                        .setSystem(ContactPoint.ContactPointSystem.PHONE);
            }
            practitioner.setActive(true);
            if (!theRecord.get("CloseDate").isEmpty()) {
                practitioner.setActive(false);
            }
            practitioner.addAddress()
                    .setUse(Address.AddressUse.WORK)
                    .addLine(Inicaps(theRecord.get("AddressLine_1")))
                    .addLine(Inicaps(theRecord.get("AddressLine_2")))
                    .addLine(Inicaps(theRecord.get("AddressLine_3")))
                    .setCity(Inicaps(theRecord.get("AddressLine_4")))
                    .setDistrict(Inicaps(theRecord.get("AddressLine_5")))
                    .setPostalCode(theRecord.get("Postcode"));

            if (!theRecord.get("Name").isEmpty()) {
                String[] nameStr = theRecord.get("Name").split(" ");

                if (nameStr.length>0) {
                    HumanName name = new HumanName();
                    practitioner.getName().add(name);
                    name.setFamily(Inicaps(nameStr[0]));
                    name.addPrefix("Dr");
                    String foreName = "";
                    for (Integer f=1; f<nameStr.length;f++) {
                        if (f==1) {
                            foreName = nameStr[1];
                        } else {
                            foreName = foreName + " " + nameStr[f];
                        }
                    }
                    if (!foreName.isEmpty()) {
                        name.addGiven(foreName);
                    }
                }
            }
            resources.add(practitioner);

            PractitionerRole role = new PractitionerRole();

            if (!theRecord.get("Commissioner").isEmpty()) {
                String parentOrg = orgMap.get(theRecord.get("Commissioner"));
                if (parentOrg == null) {
                    parentOrg = getOrganisation(theRecord.get("Commissioner"));
                }
                if (parentOrg != null) {
                    role.setOrganization(new Reference("Organization/"+parentOrg));
                }

            }
            role.addIdentifier()
                    .setSystem(CareConnectSystem.SDSUserId)
                    .setValue(theRecord.get("OrganisationCode"));
            // Make a note of the practitioner. Will need to change to correct code
            role.setPractitioner(new Reference(theRecord.get("OrganisationCode")));
            if (!theRecord.get("OrganisationSubTypeCode").isEmpty()) {
                switch (theRecord.get("OrganisationSubTypeCode")) {
                    case "O":
                    case "P":
                        CodeableConcept concept = new CodeableConcept();
                        concept.addCoding()
                                .setSystem(CareConnectSystem.SDSJobRoleName)
                                .setCode("R0260")
                                .setDisplay("General Medical Practitioner");
                        role.getCode().add(concept);
                }
            }
            CodeableConcept specialty = new CodeableConcept();
            specialty.addCoding()
                    .setSystem(CareConnectSystem.SNOMEDCT)
                    .setCode("394814009")
                    .setDisplay("General practice (specialty) (qualifier value)");
            role.getSpecialty().add(specialty);
            // System.out.println(ctx.newXmlParser().setPrettyPrint(true).encodeResourceToString(role));
            roles.add(role);

        }

    }

    public class EncounterHandler implements  IRecordHandler {
        @Override
        public void accept(CSVRecord theRecord) {
            Encounter encounter = new Encounter();


            encounter.setMeta(new Meta().addProfile(CareConnectProfile.Encounter_1));

            encounter.addIdentifier()
                    .setSystem("https://fhir.leedsth.nhs.uk/Id/encounter")
                    .setValue(theRecord.get("encounterID"));

            Period period = new Period();

            String dateString = theRecord.get("period.startDate");
            if (!dateString.isEmpty()) {
                if (!theRecord.get("period.startTime").isEmpty())
                    dateString = dateString + " " + theRecord.get("period.startTime");
                try {
                    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    format.setTimeZone(TimeZone.getTimeZone("UTC"));
                    // turn off set linient
                    period.setStart(format.parse(dateString));
                } catch (Exception e) {
                    try {
                        //  System.out.println(dateString);
                        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                        format.setTimeZone(TimeZone.getTimeZone("UTC"));
                        period.setStart(format.parse(dateString));
                    } catch (Exception e2) {
                        e.printStackTrace();
                    }
                }
            }

            dateString = theRecord.get("period.endDate");
            if (!dateString.isEmpty()) {
                if (!theRecord.get("period.endTime").isEmpty())
                    dateString = dateString + " " + theRecord.get("period.endTime");
                try {
                    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    format.setTimeZone(TimeZone.getTimeZone("UTC"));
                    // turn off set linient
                    period.setEnd(format.parse(dateString));
                } catch (Exception e) {
                    try {
                        //  System.out.println(dateString);
                        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                        format.setTimeZone(TimeZone.getTimeZone("UTC"));
                        period.setEnd(format.parse(dateString));
                    } catch (Exception e2) {
                        e.printStackTrace();
                    }
                }
            }
            encounter.setPeriod(period);
            encounter.setSubject(new Reference("Patient/"+getPatientId(theRecord.get("patientID"))));

            /* TODO
            if (!theRecord.get("status").isEmpty()) {
                // TODO
            } else {
                // Mandatory field default to finished

            } */

            encounter.setStatus(Encounter.EncounterStatus.FINISHED);


            if (!theRecord.get("resource.type").isEmpty() && !theRecord.get("participent.individual").isEmpty()) {


                if (theRecord.get("resource.type").equals("Practitioner")) {
                    String practitioner = docMap.get(theRecord.get("participent.individual"));
                    if (practitioner == null) {
                       practitioner = getPractitioner(theRecord.get("participent.individual"));
                    }
                    if (practitioner != null) {
                        Encounter.EncounterParticipantComponent participant = encounter.addParticipant()
                                .setIndividual(new Reference("Practitioner/" + practitioner));
                        if (!theRecord.get("participant.type").isEmpty()) {
                            CodeableConcept type = new CodeableConcept();
                            switch (theRecord.get("participant.type")) {
                                case "PRF":
                                    type.addCoding().setSystem("http://hl7.org/fhir/v3/ParticipationType").setCode("PPRF");
                                    break;
                                default:
                                    System.out.println("participant.type=" + theRecord.get("participant.type"));

                            }
                            if (type.getCoding().size()>0) {
                                participant.addType(type);
                            }

                        }
                    }
                }
            }
/*
            System.out.println("participant.type=" + theRecord.get("participant.type"));
            System.out.println("resource.type="+theRecord.get("resource.type"));
            System.out.println("participant.individual="+theRecord.get("participent.individual"));
            System.out.println("serviceProvider="+theRecord.get("serviceProvider"));
*/
            if (!theRecord.get("serviceProvider").isEmpty()) {
                String organization= orgMap.get(theRecord.get("serviceProvider"));
                if (organization == null) {
                    organization = getOrganisation(theRecord.get("serviceProvider"));
                }
                if (organization != null) {
                    encounter
                            .setServiceProvider(new Reference("Organization/"+organization));

                }
            }

            //System.out.println(ctx.newJsonParser().setPrettyPrint(true).encodeResourceToString(encounter));
            resources.add(encounter);
        }
    }

    public class ConditionHandler implements  IRecordHandler {
        @Override
        public void accept(CSVRecord theRecord) {
           Condition condition = new Condition();

           condition.addIdentifier()
                   .setSystem("https://fhir.leedsth.nhs.uk/Id/condition")
                   .setValue(theRecord.get("identifier"));

           condition.setSubject(new Reference("Patient/" + getPatientId(theRecord.get("patient ID"))));

            if (!theRecord.get("encounter").isEmpty()) {
                Bundle results = client
                        .search()
                        .forResource(Encounter.class)
                        .where(Encounter.IDENTIFIER.exactly().code(theRecord.get("encounter")))
                        .returnBundle(Bundle.class)
                        .execute();
                //   System.out.println(results.getEntry().size());
                if (results.getEntry().size() > 0) {
                    Encounter encounter = (Encounter) results.getEntry().get(0).getResource();

                    condition.setContext(new Reference("Encounter/" + encounter.getIdElement().getIdPart()));
                }
            }
            if (!theRecord.get("asserter").isEmpty()) {

                String practitionerId = docMap.get(theRecord.get("asserter"));
                if (practitionerId == null) {
                    practitionerId = getPractitioner(theRecord.get("asserter"));
                }

                if (practitionerId != null) {
                    condition.setAsserter(new Reference("Practitioner/" + practitionerId));
                }
            }

            String dateString = theRecord.get("dateRecorded");

            if (!dateString.isEmpty()) {

                try {
                    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    // turn off set linient
                    condition.setAssertedDate(format.parse(dateString));
                } catch (Exception e) {
                    try {
                        //  System.out.println(dateString);
                        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                        format.setTimeZone(TimeZone.getTimeZone("UTC"));
                        //System.out.println(format.parse(dateString).toString());
                        condition.setAssertedDate(format.parse(dateString));
                    } catch (Exception e2) {
                        e.printStackTrace();
                    }
                }
            }
            if (!theRecord.get("code.coding.code").isEmpty()) {
                condition.getCode().addCoding()
                        .setCode(theRecord.get("code.coding.code"))
                        .setSystem(CareConnectSystem.SNOMEDCT)
                        .setDisplay(theRecord.get("code.coding.display"));
            }

            if (!theRecord.get("category.code").isEmpty()) {
                switch (theRecord.get("category.code")) {
                    case "diagnosis":
                        condition.addCategory().addCoding()
                            .setSystem("http://hl7.org/fhir/condition-category")
                                .setCode("encounter-diagnosis")
                                .setDisplay("Encounter Diagnosis");
                        break;

                }
            }
            if (!theRecord.get("clinicalStatus").isEmpty()) {
                switch (theRecord.get("clinicalStatus")) {
                    case "Resolved":
                        condition.setClinicalStatus(Condition.ConditionClinicalStatus.RESOLVED);
                        break;
                    case "Active":
                        condition.setClinicalStatus(Condition.ConditionClinicalStatus.ACTIVE);
                        break;
                    case "Remission":
                        condition.setClinicalStatus(Condition.ConditionClinicalStatus.REMISSION);
                        break;
                    case "Relapse":
                        condition.setClinicalStatus(Condition.ConditionClinicalStatus.RECURRENCE);
                        break;
                    case "Inactive":
                        condition.setClinicalStatus(Condition.ConditionClinicalStatus.INACTIVE);
                        break;
                        default:
                            System.out.println("****** = "+theRecord.get("clinicalStatus"));

                }
            }
            if (!theRecord.get("verificationStatus").isEmpty()) {
                switch (theRecord.get("verificationStatus")) {
                    case "Confirmed" :
                        condition.setVerificationStatus(Condition.ConditionVerificationStatus.CONFIRMED);
                        break;
                    case "Provisional" :
                        condition.setVerificationStatus(Condition.ConditionVerificationStatus.PROVISIONAL);
                        break;
                    case "Entered-In-Error" :
                        condition.setVerificationStatus(Condition.ConditionVerificationStatus.ENTEREDINERROR);
                        break;
                    default:
                        System.out.println("****** verificationStatus = " + theRecord.get("verificationStatus"));

                }

            }
            if (!theRecord.get("severity.code.coding.code").isEmpty()) {
                condition.getSeverity().addCoding()
                        .setSystem(CareConnectSystem.SNOMEDCT)
                        .setCode(theRecord.get("severity.code.coding.code"))
                        .setDisplay(theRecord.get("serverity.code.coding.description"));
                //System.out.println("****** severity.code.coding.code = " + theRecord.get("severity.code.coding.code"));
            }

            //System.out.println(ctx.newJsonParser().setPrettyPrint(true).encodeResourceToString(condition));
            resources.add(condition);
        }
    }

    public class AllergyHandler implements  IRecordHandler {
        @Override
        public void accept(CSVRecord theRecord) {
            AllergyIntolerance allergy = new AllergyIntolerance();

            allergy.addIdentifier()
                    .setSystem("https://fhir.leedsth.nhs.uk/Id/allergy")
                    .setValue(theRecord.get("identifier"));

            allergy.setPatient(new Reference("Patient/" + getPatientId(theRecord.get("patient"))));

            if (!theRecord.get("encounter").isEmpty()) {
                Bundle results = client
                        .search()
                        .forResource(Encounter.class)
                        .where(Encounter.IDENTIFIER.exactly().code(theRecord.get("encounter")))
                        .returnBundle(Bundle.class)
                        .execute();

                if (results.getEntry().size() > 0) {
                    Encounter encounter = (Encounter) results.getEntry().get(0).getResource();
                    allergy.addExtension()
                            .setUrl(CareConnectExtension.UrlAssociatedEncounter)
                            .setValue(new Reference("Encounter/"+encounter.getIdElement().getIdPart()));

                }

            }

            //  AllergyIntolerance.AllergyIntoleranceReactionComponent reaction = allergy.addReaction();
            if (!theRecord.get("substance.coding.code").isEmpty()) {
                allergy.getCode().addCoding()
                        .setCode(theRecord.get("substance.coding.code"))
                        .setDisplay(theRecord.get("substance.coding.display"))
                        .setSystem(CareConnectSystem.SNOMEDCT);
            }

            if (!theRecord.get("clinicalStatus").isEmpty()) {
                switch (theRecord.get("clinicalStatus")) {
                    case "Active":
                        allergy.setClinicalStatus(AllergyIntolerance.AllergyIntoleranceClinicalStatus.ACTIVE);
                        break;
                    case "Inactive" :
                        allergy.setClinicalStatus(AllergyIntolerance.AllergyIntoleranceClinicalStatus.INACTIVE);
                        break;
                    case "Resolved" :
                        allergy.setClinicalStatus(AllergyIntolerance.AllergyIntoleranceClinicalStatus.RESOLVED);
                        break;
                    default :
                        System.out.println("***** clinicalStatus = "+theRecord.get("clinicalStatus"));

                }
            }

            if (!theRecord.get("verificationStatus").isEmpty()) {
                switch (theRecord.get("verificationStatus")) {
                    case "Unconfirmed":
                        allergy.setVerificationStatus(AllergyIntolerance.AllergyIntoleranceVerificationStatus.UNCONFIRMED);
                        break;
                    case "Confirmed" :
                        allergy.setVerificationStatus(AllergyIntolerance.AllergyIntoleranceVerificationStatus.CONFIRMED);
                        break;
                    case "Refuted" :
                        allergy.setVerificationStatus(AllergyIntolerance.AllergyIntoleranceVerificationStatus.REFUTED);
                        break;
                    case "Entered-In-Error" :
                        allergy.setVerificationStatus(AllergyIntolerance.AllergyIntoleranceVerificationStatus.ENTEREDINERROR);
                        break;
                    default :
                        System.out.println("***** verificationStatus = "+theRecord.get("verificationStatus"));

                }
            }

            if (!theRecord.get("onset.DateTime").isEmpty()) {
                try {
                    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                    format.setTimeZone(TimeZone.getTimeZone("UTC"));

                    allergy.setOnset(new DateTimeType(format.parse(theRecord.get("onset.DateTime"))));
                } catch (Exception e) {
                    try {
                        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM");
                        format.setTimeZone(TimeZone.getTimeZone("UTC"));

                        allergy.setOnset(new DateTimeType(format.parse(theRecord.get("onset.DateTime"))));
                    } catch (Exception e1) {
                        try {
                            SimpleDateFormat format = new SimpleDateFormat("yyyy");
                            format.setTimeZone(TimeZone.getTimeZone("UTC"));

                            allergy.setOnset(new DateTimeType(format.parse(theRecord.get("onset.DateTime"))));
                        } catch (Exception e2) {

                        }
                    }

                }
            }

            if (!theRecord.get("assertedDate").isEmpty()) {
                try {
                    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                    format.setTimeZone(TimeZone.getTimeZone("UTC"));

                    allergy.setAssertedDate(format.parse(theRecord.get("assertedDate")));
                } catch (Exception e) {


                }
            }

            if (!theRecord.get("lastOccurrence").isEmpty()) {
                try {
                    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                    format.setTimeZone(TimeZone.getTimeZone("UTC"));

                    allergy.setLastOccurrence(format.parse(theRecord.get("lastOccurrence")));
                } catch (Exception e) {
                    try {
                        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM");
                        format.setTimeZone(TimeZone.getTimeZone("UTC"));

                        allergy.setLastOccurrence(format.parse(theRecord.get("lastOccurrence")));
                    } catch (Exception e1) {
                        try {
                            SimpleDateFormat format = new SimpleDateFormat("yyyy");
                            format.setTimeZone(TimeZone.getTimeZone("UTC"));

                            allergy.setLastOccurrence(format.parse(theRecord.get("lastOccurrence")));
                        } catch (Exception e2) {

                        }
                    }
                }
            }
            if (!theRecord.get("recorder").isEmpty()) {

               String practitionerId= docMap.get(theRecord.get("recorder"));

               if (practitionerId == null) {
                   practitionerId = getPractitioner(theRecord.get("recorder"));
               }

                if (practitionerId != null) {
                    allergy.setAsserter(new Reference("Practitioner/" + practitionerId));
                }
            }

         //   System.out.println(ctx.newJsonParser().setPrettyPrint(true).encodeResourceToString(allergy));
            resources.add(allergy);
        }
    }



    public class ProcedureHandler implements  IRecordHandler {
        @Override
        public void accept(CSVRecord theRecord) {
            Procedure procedure = new Procedure();

            procedure.addIdentifier()
                    .setSystem("https://fhir.leedsth.nhs.uk/Id/procedure")
                    .setValue(theRecord.get("identifier"));

            procedure.setSubject(new Reference("Patient/" + getPatientId(theRecord.get("subject"))));
            if (!theRecord.get("status").isEmpty()) {
                switch (theRecord.get("status")) {
                    case "Completed" :
                        procedure.setStatus(Procedure.ProcedureStatus.COMPLETED);
                        break;
                }
            }
            if (!theRecord.get("code.coding.code").isEmpty()) {
                procedure.getCode().addCoding()
                        .setCode(theRecord.get("code.coding.code"))
                        .setSystem(CareConnectSystem.SNOMEDCT)
                        .setDisplay(theRecord.get("code.coding.display"));
            }
            if (!theRecord.get("category.code.coding.code").isEmpty()) {
                procedure.getCategory().addCoding()
                        .setCode(theRecord.get("category.code.coding.code"))
                        .setSystem(CareConnectSystem.SNOMEDCT)
                        .setDisplay(theRecord.get("category.code.coding.display"));
            }

            String dateString = theRecord.get("performed date");
            if (!theRecord.get("performed time").isEmpty()) dateString = dateString + " " +"performed time";

            if (!dateString.isEmpty()) {

                try {
                    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    format.setTimeZone(TimeZone.getTimeZone("UTC"));
                    // turn off set linient
                    procedure.setPerformed(new DateTimeType(format.parse(dateString)));
                } catch (Exception e) {
                    try {
                        //  System.out.println(dateString);
                        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                        format.setTimeZone(TimeZone.getTimeZone("UTC"));
                        procedure.setPerformed(new DateTimeType(format.parse(dateString)));
                    } catch (Exception e2) {
                        e.printStackTrace();
                    }
                }
            }
            if (!theRecord.get("notPerformed").isEmpty()) {
                switch (theRecord.get("notPerformed")) {
                    case "FALSE" :
                        procedure.setNotDone(false);
                        break;
                    case "TRUE" :
                        procedure.setNotDone(true);
                        break;
                }
            }
            if (!theRecord.get("Performer Type").isEmpty()) {
                switch (theRecord.get("Performer Type")) {
                    case "Practitioner":

                        String practitionerId = docMap.get(theRecord.get("performer"));

                        if (practitionerId == null) {
                            practitionerId =  getPractitioner(theRecord.get("performer"));
                        }

                        if (practitionerId != null) {
                            Procedure.ProcedurePerformerComponent performer = procedure.addPerformer();
                            performer.setActor(new Reference("Practitioner/" + practitionerId));
                        }
                        break;
                    case "Organization":
                        String organisationId = docMap.get(theRecord.get("performer"));

                        if (organisationId == null) {
                            organisationId =  getOrganisation(theRecord.get("performer"));
                        }

                        if (organisationId != null) {
                            Procedure.ProcedurePerformerComponent performer = procedure.addPerformer();
                            performer.setActor(new Reference("Organization/" + organisationId));
                        }
                }
            }
            if (!theRecord.get("encounter").isEmpty()) {
                Bundle results = client
                        .search()
                        .forResource(Encounter.class)
                        .where(Encounter.IDENTIFIER.exactly().code(theRecord.get("encounter")))
                        .returnBundle(Bundle.class)
                        .execute();
                //   System.out.println(results.getEntry().size());
                if (results.getEntry().size() > 0) {
                    Encounter encounter = (Encounter) results.getEntry().get(0).getResource();
              procedure.setContext(new Reference("Encounter/" + encounter.getIdElement().getIdPart()));
                }
            }
            if (!theRecord.get("outcome.coding.code").isEmpty()) {
                procedure.getOutcome().addCoding()
                        .setCode(theRecord.get("outcome.coding.code"))
                        .setSystem(CareConnectSystem.SNOMEDCT)
                        .setDisplay(theRecord.get("ouctome.coding.display"));
            }

           // System.out.println(ctx.newJsonParser().setPrettyPrint(true).encodeResourceToString(procedure));
            resources.add(procedure);
        }
    }

    public class AllergyReactionsHandler implements  IRecordHandler {
        @Override
        public void accept(CSVRecord theRecord) {

            for (IBaseResource resource : resources) {
                if (resource instanceof AllergyIntolerance) {
                    AllergyIntolerance allergy = (AllergyIntolerance) resource;
                    if (allergy.getIdentifier().size()>0 && theRecord.get("allergy.identifier").equals(allergy.getIdentifier().get(0).getValue())) {
                        AllergyIntolerance.AllergyIntoleranceReactionComponent reaction = allergy.addReaction();
                        reaction.addManifestation().addCoding()
                                .setSystem(CareConnectSystem.SNOMEDCT)
                                .setDisplay(theRecord.get("reaction.manifestation.display"))
                                .setCode(theRecord.get("reaction.manifestation"));
                   //     System.out.println(ctx.newJsonParser().setPrettyPrint(true).encodeResourceToString(allergy));
                    }
                }
            }

        }
    }


    public class ImmunisationHandler implements  IRecordHandler {
        @Override
        public void accept(CSVRecord theRecord) {
            if (!theRecord.get("patientID").isEmpty()) {
                Immunization immunisation = new Immunization();

                immunisation.addIdentifier()
                        .setSystem("https://fhir.leedsth.nhs.uk/Id/immunisation")
                        .setValue(theRecord.get("identifier"));

                immunisation.setPatient(new Reference("Patient/" + getPatientId(theRecord.get("patientID"))));



                if (!theRecord.get("dateRecorded").isEmpty()) {
                    try {
                        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                        format.setTimeZone(TimeZone.getTimeZone("UTC"));

                        immunisation.addExtension()
                                .setUrl(CareConnectExtension.UrlImmunizationDateRecorded)
                                .setValue(new DateTimeType(format.parse(theRecord.get("dateRecorded"))));
                    } catch (Exception e) {

                    }
                }
                if (!theRecord.get("parentPresent").isEmpty()) {

                    switch (theRecord.get("parentPresent")) {
                        case "FALSE":
                            immunisation.addExtension()
                                    .setUrl(CareConnectExtension.UrlImmunizationParentPresent)
                                    .setValue(new BooleanType(false));
                            break;
                        case "TRUE":
                            immunisation.addExtension()
                                    .setUrl(CareConnectExtension.UrlImmunizationParentPresent)
                                    .setValue(new BooleanType(true));
                            break;
                    }
                }
                if (!theRecord.get("status").isEmpty()) {
                    switch (theRecord.get("status")) {
                        case "Completed":
                            immunisation.setStatus(Immunization.ImmunizationStatus.COMPLETED);
                            break;
                    }
                }

                String dateString = theRecord.get("dateAdministered");
                if (!dateString.isEmpty() && !theRecord.get("timeAdministered").isEmpty()) {
                    dateString = dateString + " " + theRecord.get("timeAdministered");
                }

                if (!dateString.isEmpty()) {

                    try {
                        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        format.setTimeZone(TimeZone.getTimeZone("UTC"));
                        // turn off set linient
                        immunisation.setDate(format.parse(dateString));
                    } catch (Exception e) {
                        try {
                            //  System.out.println(dateString);
                            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                            format.setTimeZone(TimeZone.getTimeZone("UTC"));
                            immunisation.setDate(format.parse(dateString));
                        } catch (Exception e2) {
                            e.printStackTrace();
                        }
                    }
                }
                if (!theRecord.get("vaccineCode.coding").isEmpty()) {
                    immunisation.getVaccineCode().addCoding()
                            .setSystem(CareConnectSystem.SNOMEDCT)
                            .setCode(theRecord.get("vaccineCode.coding"))
                            .setDisplay(theRecord.get("vaccineCode.coding.display"));

                }
                if (!theRecord.get("notGiven").isEmpty()) {

                    switch (theRecord.get("notGiven")) {
                        case "FALSE":
                            immunisation.setNotGiven(false);
                            break;
                        case "TRUE":
                            immunisation.setNotGiven(true);
                            break;
                    }
                }

                if (!theRecord.get("encounter").isEmpty()) {
                    Bundle results = client
                            .search()
                            .forResource(Encounter.class)
                            .where(Encounter.IDENTIFIER.exactly().code(theRecord.get("encounter")))
                            .returnBundle(Bundle.class)
                            .execute();
                    //    System.out.println("***** Encounter ID = "+theRecord.get("encounter")+ " Results = "+results.getEntry().size());
                    if (results.getEntry().size() > 0) {
                        Encounter encounter = (Encounter) results.getEntry().get(0).getResource();

                        immunisation.setEncounter(new Reference("Encounter/" + encounter.getIdElement().getIdPart()));
                    }
                }


                if (!theRecord.get("primarySource").isEmpty()) {

                    switch (theRecord.get("primarySource")) {
                        case "FALSE":
                            immunisation.setPrimarySource(false);
                            break;
                        case "TRUE":
                            immunisation.setPrimarySource(true);
                            break;
                    }
                }

                if (!theRecord.get("reportOrigin").isEmpty()) {

                    switch (theRecord.get("reportOrigin")) {
                        case "provider":
                            immunisation.getReportOrigin().addCoding()
                                    .setCode("provider")
                                    .setSystem("http://hl7.org/fhir/immunization-origin")
                                    .setDisplay("Provider");
                            break;

                    }
                }

                if (!theRecord.get("location").isEmpty()) {
                    Bundle results = client
                            .search()
                            .forResource(Location.class)
                            .where(Location.IDENTIFIER.exactly().code(theRecord.get("location")))
                            .returnBundle(Bundle.class)
                            .execute();
                    if (results.getEntry().size() > 0) {
                        Location location = (Location) results.getEntry().get(0).getResource();

                        immunisation.setLocation(new Reference("Location" +
                                "/" + location.getIdElement().getIdPart()));
                    }
                }
                if (!theRecord.get("lotNumber").isEmpty()) {
                    immunisation.setLotNumber(theRecord.get("lotNumber"));
                }


                if (!theRecord.get("expirationDate").isEmpty()) {
                    try {
                        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                        format.setTimeZone(TimeZone.getTimeZone("UTC"));

                        immunisation.setExpirationDate(format.parse(theRecord.get("expirationDate")));
                    } catch (Exception e) {

                    }
                }
                // TODO


                //    System.out.println(ctx.newJsonParser().setPrettyPrint(true).encodeResourceToString(immunisation));

                resources.add(immunisation);
            }
        }
    }

    public class PatientHandler implements  IRecordHandler {

        FhirContext ctx;
        IGenericClient client;

	    PatientHandler(FhirContext ctx,IGenericClient client) {
	        this.ctx = ctx;
	        this.client = client;
        }
         @Override
        public void accept(CSVRecord theRecord) {

	        //Patient patient = (Patient) client.read().resource(Patient.class).withId(theRecord.get("PATIENT_ID")).execute();
            // KGM 18/12/2017 Removed the old update existing patient to move to bulk upload format.
             Patient patient = patientMap.get(theRecord.get("PATIENT_ID"));

             if (patient == null) {
                 patient = new Patient();
                 patient.setId(theRecord.get("PATIENT_ID"));
                 patientMap.put(patient.getId(),patient);
                 resources.add(patient);
             }


             if (theRecord.get("PRACTICE_ID") != null && !theRecord.get("PRACTICE_ID").isEmpty()) {
                 String organisationId = docMap.get(theRecord.get("PRACTICE_ID"));

                 if (organisationId == null) {
                     organisationId = getOrganisation(theRecord.get("PRACTICE_ID"));
                 }
                 if (organisationId != null) {

                    patient.setManagingOrganization(new Reference( "Organization/"+organisationId));
                }

            }
            if (theRecord.get("GP_ID") != null && !theRecord.get("GP_ID").isEmpty()) {

                String practitionerId = docMap.get(theRecord.get("GP_ID"));

                if (practitionerId == null) {
                    practitionerId = getPractitioner(theRecord.get("GP_ID"));
                }
                if (practitionerId != null) {
                     patient.addGeneralPractitioner(new Reference( "Practitioner/"+practitionerId));
                 }

             }

            Boolean found = false;
	        for (Identifier identifier : patient.getIdentifier()) {
	            if (identifier.getSystem().contains("https://fhir.leedsth.nhs.uk/Id/PPMIdentifier")) {
	                found = true;
                }
            }
            if (!found) {
	            patient.addIdentifier()
                        .setSystem("https://fhir.leedsth.nhs.uk/Id/PPMIdentifier")
                        .setValue(theRecord.get("PATIENT_ID"));
            }
            if (!theRecord.get("active").isEmpty() ) {
	            switch(theRecord.get("active")) {
                    case "1" :
                        patient.setActive(true);
                        break;
                    default:
                        patient.setActive(false);
                }
            }

            String dateString = theRecord.get("date_of_birth");
             if (!dateString.isEmpty()) {

                 try {
                     SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                     format.setTimeZone(TimeZone.getTimeZone("UTC"));
                     // turn off set linient
                     patient.setBirthDate(format.parse(dateString));
                 } catch (Exception e) {
                         e.printStackTrace();
                 }
             }
             if (!theRecord.get("gender").isEmpty() ) {
                 switch(theRecord.get("gender")) {
                     case "FEMALE" :
                         patient.setGender(Enumerations.AdministrativeGender.FEMALE);
                         break;
                     case "MALE" :
                         patient.setGender(Enumerations.AdministrativeGender.MALE);
                         break;
                     case "OTHER" :
                         patient.setGender(Enumerations.AdministrativeGender.OTHER);
                         break;
                 }
             }
             // TODO registration_end,
             // TODO registration_start,

             if (!theRecord.get("NHSverification").isEmpty()) {
                 String NHSverification = theRecord.get("NHSverification");
                 switch (theRecord.get("NHSverification")) {
                     case "79" :
                         NHSverification = "01";
                         break;
                     case "80" :
                         NHSverification = "02";
                         break;
                     case "81" :
                         NHSverification = "03";
                         break;
                     case "82" :
                         NHSverification = "04";
                         break;
                     case "83" :
                         NHSverification = "05";
                         break;
                     case "84" :
                         NHSverification = "06";
                         break;
                     case "85":
                         NHSverification = "07";
                         break;
                 }
                 for (Identifier identifier: patient.getIdentifier()) {
                     if (identifier.getSystem().equals(CareConnectSystem.NHSNumber)) {
                         CodeableConcept verificationStatusCode = new CodeableConcept();
                         verificationStatusCode
                                 .addCoding()
                                 .setSystem(CareConnectSystem.NHSNumberVerificationStatus)
                                 .setCode(NHSverification);
                         Extension verificationStatus = new Extension()
                                 .setUrl(CareConnectExtension.UrlNHSNumberVerificationStatus)
                                 .setValue(verificationStatusCode);
                         identifier.addExtension(verificationStatus);
                     }
                 }
             }
             if (!theRecord.get("ethnic").isEmpty() ) {
                 String ethnic= theRecord.get("ethnic");
                 switch (theRecord.get("ethnic")) {
                     case "11":
                         ethnic = "A";
                         break;
                     case "45":
                         ethnic = "GC";
                         break;
                     case "75":
                         ethnic = "SC";
                         break;
                     case "67":
                         ethnic = "PB";
                         break;
                     case "76":
                         ethnic = "SD";
                         break;
                     case "77":
                         ethnic = "SE";
                         break;
                     case "35":
                         ethnic = "CV";
                         break;
                     case "69":
                         ethnic = "PD";
                         break;
                     case "78":
                         ethnic = "Z";
                         break;
                     case "27":
                         ethnic = "CM";
                         break;
                     case "74":
                         ethnic = "SB";
                         break;
                     case "31":
                         ethnic = "CR";
                         break;
                     case "25":
                         ethnic = "CK";
                         break;
                     case "57":
                         ethnic = "LE";
                         break;
                     case "13":
                         ethnic = "C";
                         break;
                     case "68":
                         ethnic = "PC";
                         break;
                     case "15":
                         ethnic = "C3";
                         break;
                     case "23":
                         ethnic = "CH";
                         break;
                     case "30":
                         ethnic = "CQ";
                         break;
                     case "29":
                         ethnic = "CP";
                         break;
                     case "71":
                         ethnic = "R";
                         break;
                     case "22":
                         ethnic = "CG";
                         break;
                     case "58":
                         ethnic = "LF";
                         break;
                     case "49":
                         ethnic = "H";
                         break;
                     case "54":
                         ethnic = "LB";
                         break;
                     case "19":
                         ethnic = "CD";
                         break;



                 }
                 CodeableConcept ethnicCode = new CodeableConcept();
                 ethnicCode
                         .addCoding()
                         .setSystem(CareConnectSystem.EthnicCategory)
                         .setCode(ethnic);
                 Extension ethnicExtension = new Extension()
                         .setUrl(CareConnectExtension.UrlEthnicCategory)
                         .setValue(ethnicCode);
                 patient.addExtension(ethnicExtension);
             }
             if (!theRecord.get("marital").isEmpty() ) {
                 String maritalCode = theRecord.get("marital");
                 switch (theRecord.get("marital")) {
                     case "1":
                         maritalCode = "A";
                         break;
                     case "2":
                         maritalCode = "D";
                         break;
                     case "3":
                         maritalCode = "I";
                         break;
                     case "4":
                         maritalCode = "L";
                         break;
                     case "5":
                         maritalCode = "M";
                         break;
                     case "6":
                         maritalCode = "P";
                         break;
                     case "7":
                         maritalCode = "S";
                         break;
                     case "8":
                         maritalCode = "T";
                         break;
                     case "9":
                         maritalCode = "U";
                         break;
                     case "10":
                         maritalCode = "W";
                         break;
                 }
                 CodeableConcept marital = new CodeableConcept();
                 marital.addCoding()
                         .setSystem(CareConnectSystem.HL7v3MaritalStatus)
                         .setCode(maritalCode);
                 patient.setMaritalStatus(marital);
             }



        }
    }

    public class PatientNameHandler implements  IRecordHandler {

        FhirContext ctx;
        IGenericClient client;

        PatientNameHandler(FhirContext ctx,IGenericClient client) {
            this.ctx = ctx;
            this.client = client;
        }
        @Override
        public void accept(CSVRecord theRecord) {


            Patient patient = patientMap.get(theRecord.get("PATIENT_ID"));

            if (patient == null) {
                patient = new Patient();
                patient.setId(theRecord.get("PATIENT_ID"));
                patientMap.put(patient.getId(),patient);
                resources.add(patient);
            }
            HumanName name = patient.addName();
            if (!theRecord.get("family_name").isEmpty()) {
                name.setFamily(theRecord.get("family_name"));
            }
            if (!theRecord.get("given_name").isEmpty()) {
                name.addGiven(theRecord.get("given_name"));
            }
            if (!theRecord.get("prefix").isEmpty()) {
                name.addPrefix(theRecord.get("prefix"));
            }
            if (!theRecord.get("nameUse").isEmpty()) {
                switch (theRecord.get("nameUse")) {
                    case "0" :
                        name.setUse(HumanName.NameUse.USUAL);
                        break;
                    case "3" :
                        name.setUse(HumanName.NameUse.NICKNAME);
                        break;
                }

            }
            // TODO
        //                    ,"suffix"


        }
    }

    public class PatientTelecomHandler implements  IRecordHandler {

        FhirContext ctx;
        IGenericClient client;

        PatientTelecomHandler(FhirContext ctx,IGenericClient client) {
            this.ctx = ctx;
            this.client = client;
        }
        @Override
        public void accept(CSVRecord theRecord) {


            Patient patient = patientMap.get(theRecord.get("PATIENT_ID"));

            if (patient == null) {
                patient = new Patient();
                patient.setId(theRecord.get("PATIENT_ID"));
                patientMap.put(patient.getId(),patient);
                resources.add(patient);
            }
            ContactPoint contact = patient.addTelecom();
            if (!theRecord.get("value").isEmpty()) {
                contact.setValue(theRecord.get("value"));
            }
            if (!theRecord.get("system").isEmpty()) {
                switch (theRecord.get("system")) {
                    case "0" :
                        contact.setSystem(ContactPoint.ContactPointSystem.PHONE);
                        break;
                    case "2" :
                        contact.setSystem(ContactPoint.ContactPointSystem.EMAIL);
                        break;
                }
            }
            if (!theRecord.get("telecomUse").isEmpty()) {
                switch (theRecord.get("telecomUse")) {
                    case "0" :
                        contact.setUse(ContactPoint.ContactPointUse.HOME);
                        break;
                    case "1" :
                        contact.setUse(ContactPoint.ContactPointUse.WORK);
                        break;
                    case "2" :
                        contact.setUse(ContactPoint.ContactPointUse.TEMP
                        );
                        break;
                    case "3" :
                        contact.setUse(ContactPoint.ContactPointUse.OLD);
                        break;
                    case "4" :
                        contact.setUse(ContactPoint.ContactPointUse.MOBILE);
                        break;
                }
            }
        }
    }


    public class PatientAddressHandler implements  IRecordHandler {

        FhirContext ctx;
        IGenericClient client;

        PatientAddressHandler(FhirContext ctx,IGenericClient client) {
            this.ctx = ctx;
            this.client = client;
        }
        @Override
        public void accept(CSVRecord theRecord) {
            Patient patient = patientMap.get(theRecord.get("PATIENT_ID"));

            Address adr = addressMap.get(theRecord.get("ADDRESS_ID"));

            if (!theRecord.get("AddressType").isEmpty()) {
                switch (theRecord.get("AddressType")) {
                    case "0":
                        adr.setType(Address.AddressType.POSTAL);
                        break;
                    case "1":
                        adr.setType(Address.AddressType.PHYSICAL);
                        break;
                    case "2":
                        adr.setType(Address.AddressType.BOTH);
                        break;
                }
            }
            if (!theRecord.get("addressUse").isEmpty()) {
                switch (theRecord.get("addressUse")) {
                    case "0":
                        adr.setUse(Address.AddressUse.HOME);
                        break;
                    case "1":
                        adr.setUse(Address.AddressUse.WORK);
                        break;
                    case "2":
                        adr.setUse(Address.AddressUse.TEMP);
                        break;
                }
            }

            patient.addAddress(adr);


        }
    }

    public class PatientIdentifierHandler implements  IRecordHandler {

        FhirContext ctx;
        IGenericClient client;

        PatientIdentifierHandler(FhirContext ctx,IGenericClient client) {
            this.ctx = ctx;
            this.client = client;
        }
        @Override
        public void accept(CSVRecord theRecord) {

            Patient patient = patientMap.get(theRecord.get("PATIENT_ID"));

            if (patient == null) {
                patient = new Patient();
                patient.setId(theRecord.get("PATIENT_ID"));
                patientMap.put(patient.getId(),patient);
                resources.add(patient);
            }


            if (!theRecord.get("SYSTEM_ID").isEmpty() && !theRecord.get("value").isEmpty()) {
                switch (theRecord.get("SYSTEM_ID")) {
                    case "1001":
                        patient.addIdentifier()
                                .setSystem("https://fhir.leedsth.nhs.uk/Id/pas-number")
                                .setValue(theRecord.get("value"));
                        break;

                    case "1":
                        patient.addIdentifier()
                                .setSystem(CareConnectSystem.NHSNumber)
                                .setValue(theRecord.get("value"));
                        break;
                }
            }


            // PATIENT_IDENTIFIER_ID,identifierUse,listOrder,,,
        }
    }

    public class AddressHandler implements  IRecordHandler {

        FhirContext ctx;
        IGenericClient client;

        AddressHandler(FhirContext ctx,IGenericClient client) {
            this.ctx = ctx;
            this.client = client;
        }
        @Override
        public void accept(CSVRecord theRecord) {
            Address adr = new Address();
            addressMap.put(theRecord.get("ADDRESS_ID"),adr);
            // TODO

            adr.setId(theRecord.get("ADDRESS_ID"));
            if (!theRecord.get("address_1").isEmpty()) {
                adr.addLine(theRecord.get("address_1"));
            }
            if (!theRecord.get("address_2").isEmpty()) {
                adr.addLine(theRecord.get("address_2"));
            }
            if (!theRecord.get("address_3").isEmpty()) {
                adr.addLine(theRecord.get("address_3"));
            }
            if (!theRecord.get("address_4").isEmpty()) {
                adr.addLine(theRecord.get("address_4"));
            }
            if (!theRecord.get("address_5").isEmpty()) {
                adr.addLine(theRecord.get("address_5"));
            }
            if (!theRecord.get("city").isEmpty()) {
                adr.setCity(theRecord.get("city"));
            }
            if (!theRecord.get("county").isEmpty()) {
                adr.setDistrict(theRecord.get("county"));
            }
            if (!theRecord.get("postcode").isEmpty()) {
                adr.setPostalCode(theRecord.get("postcode"));
            }

        }
    }

    public class PrescriptionHandler implements  IRecordHandler {



        @Override
        public void accept(CSVRecord theRecord) {


            MedicationRequest prescription = new MedicationRequest();

            if (!theRecord.get("identifier").isEmpty()) {

                prescription.addIdentifier()
                        .setSystem("https://fhir.leedsth.nhs.uk/Id/prescription")
                        .setValue(theRecord.get("identifier"));
            }
            if (!theRecord.get("subject (patientID)").isEmpty()) {

                prescription.setSubject(new Reference("Patient/" + getPatientId(theRecord.get("subject (patientID)"))));
            }
            if (!theRecord.get("status").isEmpty()) {
                switch (theRecord.get("status")) {
                    case "completed" :
                        prescription.setStatus(MedicationRequest.MedicationRequestStatus.COMPLETED);
                }

            }
            if (!theRecord.get("intent").isEmpty()) {
                switch (theRecord.get("intent")) {
                    case "order" :
                        prescription.setIntent(MedicationRequest.MedicationRequestIntent.ORDER);
                }

            }
            if (!theRecord.get("priority").isEmpty()) {
                switch (theRecord.get("priority")) {
                    case "routine" :
                        prescription.setPriority(MedicationRequest.MedicationRequestPriority.ROUTINE);
                }

            }
            if (!theRecord.get("medication").isEmpty()) {
                CodeableConcept med = new CodeableConcept();
                med.addCoding().setCode(theRecord.get("medication")).setSystem(CareConnectSystem.SNOMEDCT);

                prescription.setMedication(med);
            }
            if (!theRecord.get("context").isEmpty()) {
                Bundle results = client
                        .search()
                        .forResource(Encounter.class)
                        .where(Encounter.IDENTIFIER.exactly().code(theRecord.get("context")))
                        .returnBundle(Bundle.class)
                        .execute();

                if (results.getEntry().size() > 0) {
                    Encounter encounter = (Encounter) results.getEntry().get(0).getResource();

                    prescription.setContext(new Reference("Encounter/" + encounter.getIdElement().getIdPart()));
                }
            }

            String dateString = theRecord.get("authoredOn Date");
            if (!theRecord.get("authoredOn Time").isEmpty()) dateString = dateString + " " +"authoredOn Time";

            if (!dateString.isEmpty()) {

                try {
                    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    format.setTimeZone(TimeZone.getTimeZone("UTC"));
                    // turn off set linient
                    prescription.setAuthoredOn(format.parse(dateString));
                } catch (Exception e) {
                    try {
                        //  System.out.println(dateString);
                        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                        format.setTimeZone(TimeZone.getTimeZone("UTC"));
                        prescription.setAuthoredOn(format.parse(dateString));
                    } catch (Exception e2) {
                        e.printStackTrace();
                    }
                }
            }
            if (!theRecord.get("requester.agent type").isEmpty() && !theRecord.get("requester.agent").isEmpty()) {
                switch (theRecord.get("requester.agent type")) {
                    case "Practitioner":
                        String practitionerId = docMap.get(theRecord.get("requester.agent"));
                        if (practitionerId == null) {
                            practitionerId = getPractitioner(theRecord.get("requester.agent"));
                        }
                        if (practitionerId != null) {

                            prescription.getRequester().setAgent(new Reference("Practitioner/" + practitionerId));
                        }
                        break;

                    case "Organization":
                        String organisationId = docMap.get(theRecord.get("requester.agent"));
                        if (organisationId == null) {
                            organisationId = getOrganisation(theRecord.get("requester.agent"));
                        }
                        if (organisationId != null) {
                            prescription.getRequester().setAgent(new Reference("Organization/" + organisationId));
                        }
                        break;

                }
            }
            if (!theRecord.get("reasonCode Type").isEmpty() && !theRecord.get("reasonCode").isEmpty()) {
                switch (theRecord.get("reasonCode Type")) {
                    case "Condition":
                        Bundle results = client
                                .search()
                                .forResource(Condition.class)
                                .where(Condition.IDENTIFIER.exactly().code(theRecord.get("reasonCode")))
                                .returnBundle(Bundle.class)
                                .execute();

                        if (results.getEntry().size() > 0) {
                            Condition condition = (Condition) results.getEntry().get(0).getResource();

                            prescription.addReasonReference(new Reference("Condition/" + condition.getIdElement().getIdPart()));
                        }
                        break;
                }
            }
            Dosage dosage= prescription.addDosageInstruction();

            if (!theRecord.get("dosage.text").isEmpty()) {
                dosage.setText(theRecord.get("dosage.text"));
            }
            if (!theRecord.get("dosage.additionalInstruction").isEmpty()) {
                CodeableConcept additional  = new CodeableConcept();
                additional.addCoding().setSystem(CareConnectSystem.SNOMEDCT).setCode(theRecord.get("dosage.additionalInstruction"));
                dosage.getAdditionalInstruction().add(additional);
            }

            if (!theRecord.get("dosage.asNeeded.asNeededBoolean").isEmpty()) {
                switch (theRecord.get("dosage.asNeeded.asNeededBoolean")) {
                    case "FALSE":
                        dosage.setAsNeeded(new BooleanType(false));
                        break;
                    case "TRUE":
                        dosage.setAsNeeded(new BooleanType(true));
                        break;
                }
            }
            if (!theRecord.get("dosage.asNeeded.asNeededCodableConcept").isEmpty()) {
                CodeableConcept additional  = new CodeableConcept();
                additional.addCoding().setSystem(CareConnectSystem.SNOMEDCT).setCode(theRecord.get("dosage.asNeeded.asNeededCodableConcept"));
                dosage.setAsNeeded(additional);
            }
            if (!theRecord.get("dosage.route").isEmpty()) {
                CodeableConcept additional  = new CodeableConcept();
                additional.addCoding().setSystem(CareConnectSystem.SNOMEDCT).setCode(theRecord.get("dosage.route"));
                dosage.setRoute(additional);
            }
            Range range = new Range();
            dosage.setDose(range);

            if (!theRecord.get("dosage.dose.doseRange.low.value").isEmpty()) {
                SimpleQuantity qty = new SimpleQuantity();
                qty.setValue(new BigDecimal(theRecord.get("dosage.dose.doseRange.low.value")));
                qty.setCode(theRecord.get("dosage.dose.doseRange.low.units"));
                qty.setSystem(CareConnectSystem.SNOMEDCT);
                range.setLow(qty);
            }

            if (!theRecord.get("dosage.dose.doseRange.high.value").isEmpty()) {
                SimpleQuantity qty = new SimpleQuantity();
                qty.setValue(new BigDecimal(theRecord.get("dosage.dose.doseRange.high.value")));
                qty.setCode(theRecord.get("dosage.dose.doseRange.high.units"));
                qty.setSystem(CareConnectSystem.SNOMEDCT);
                range.setHigh(qty);
            }
            if (!theRecord.get("dosage.dose.doseQuantity.value").isEmpty() && !theRecord.get("dosage.dose.doseQuantity.units").isEmpty()) {
                SimpleQuantity qty = new SimpleQuantity();
                qty.setValue(new BigDecimal(theRecord.get("dosage.dose.doseQuantity.value")));
                qty.setCode(theRecord.get("dosage.dose.doseQuantity.units"));
                qty.setSystem(CareConnectSystem.SNOMEDCT);
                dosage.setDose(qty);
            }
            MedicationRequest.MedicationRequestDispenseRequestComponent dispense =  prescription.getDispenseRequest();

            Period period = dispense.getValidityPeriod();

            dateString = theRecord.get("dispenseRequest.validityPeriod.start");
            if (!dateString.isEmpty()) {
                try {
                    //  System.out.println(dateString);
                    SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
                    format.setTimeZone(TimeZone.getTimeZone("UTC"));
                    period.setStart(format.parse(dateString));
                } catch (Exception e2) {
                    e2.printStackTrace();
                }

            }
            dateString = theRecord.get("dispenseRequest.validityPeriod.end");
            if (!dateString.isEmpty()) {
                try {
                    //  System.out.println(dateString);
                    SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
                    format.setTimeZone(TimeZone.getTimeZone("UTC"));
                    period.setEnd(format.parse(dateString));
                } catch (Exception e2) {
                    e2.printStackTrace();
                }

            }
            if (!theRecord.get("dispenseRequest.numberOfRepeatsAllowed").isEmpty() ) {

                dispense.setNumberOfRepeatsAllowed(Integer.parseInt(theRecord.get("dispenseRequest.numberOfRepeatsAllowed")));
            }
            if (!theRecord.get("dispenseRequest.quantity.value").isEmpty() && !theRecord.get("dispenseRequest.quantity.units").isEmpty()) {
                SimpleQuantity qty = new SimpleQuantity();
                qty.setValue(new BigDecimal(theRecord.get("dispenseRequest.quantity.value")));
                qty.setCode(theRecord.get("dispenseRequest.quantity.units"));
                qty.setSystem(CareConnectSystem.SNOMEDCT);
                dispense.setQuantity(qty);
            }
            if (!theRecord.get("dispenseRequest.expectedSupplyDuration.value").isEmpty() && !theRecord.get("dispenseRequest.expectedSupplyDuration.units").isEmpty()) {
                Duration duration = new Duration();
                duration.setValue(new BigDecimal(theRecord.get("dispenseRequest.expectedSupplyDuration.value")));
                duration.setCode(theRecord.get("dispenseRequest.expectedSupplyDuration.units"));
                duration.setSystem(CareConnectSystem.UnitOfMeasure);
                dispense.setExpectedSupplyDuration(duration);
            }
            /*
    TODO                              ,"dosage.timing"
             */

       //     System.out.println(ctx.newJsonParser().setPrettyPrint(true).encodeResourceToString(prescription));

            resources.add(prescription);

        }
    }

    public class StatementHandler implements  IRecordHandler {



        @Override
        public void accept(CSVRecord theRecord) {


            MedicationStatement statement = new MedicationStatement();

            if (!theRecord.get("identifier").isEmpty()) {

                statement.addIdentifier()
                        .setSystem("https://fhir.leedsth.nhs.uk/Id/statement")
                        .setValue(theRecord.get("identifier"));
            }
            if (!theRecord.get("subject (patientID)").isEmpty()) {

                statement.setSubject(new Reference("Patient/" + getPatientId(theRecord.get("subject (patientID)"))));
            }
            if (!theRecord.get("status").isEmpty()) {
                switch (theRecord.get("status")) {
                    case "completed" :
                        statement.setStatus(MedicationStatement.MedicationStatementStatus.COMPLETED);
                }

            }
            if (!theRecord.get("intent").isEmpty()) {
                switch (theRecord.get("intent")) {
                    case "order" :
                        statement.setStatus(MedicationStatement.MedicationStatementStatus.INTENDED);
                }

            }

            if (!theRecord.get("medication").isEmpty()) {
                CodeableConcept med = new CodeableConcept();
                med.addCoding().setCode(theRecord.get("medication")).setSystem(CareConnectSystem.SNOMEDCT);

                statement.setMedication(med);
            }
            if (!theRecord.get("context").isEmpty()) {
                Bundle results = client
                        .search()
                        .forResource(Encounter.class)
                        .where(Encounter.IDENTIFIER.exactly().code(theRecord.get("context")))
                        .returnBundle(Bundle.class)
                        .execute();

                if (results.getEntry().size() > 0) {
                    Encounter encounter = (Encounter) results.getEntry().get(0).getResource();

                    statement.setContext(new Reference("Encounter/" + encounter.getIdElement().getIdPart()));
                }
            }

            String dateString = theRecord.get("authoredOn Date");
            if (!theRecord.get("authoredOn Time").isEmpty()) dateString = dateString + " " +"authoredOn Time";

            if (!dateString.isEmpty()) {

                try {
                    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    format.setTimeZone(TimeZone.getTimeZone("UTC"));
                    // turn off set linient
                    statement.setEffective(new DateTimeType(format.parse(dateString)));
                } catch (Exception e) {
                    try {
                        //  System.out.println(dateString);
                        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                        format.setTimeZone(TimeZone.getTimeZone("UTC"));
                        statement.setEffective(new DateTimeType(format.parse(dateString)));
                    } catch (Exception e2) {
                        e.printStackTrace();
                    }
                }
            }
    // Set to date of data load
            statement.setDateAsserted(new Date());

            if (!theRecord.get("requester.agent type").isEmpty() && !theRecord.get("requester.agent").isEmpty()) {
                switch (theRecord.get("requester.agent type")) {
                    case "Practitioner":
                        String practitionerId= docMap.get(theRecord.get("requester.agent"));
                        if (practitionerId == null) {
                            practitionerId = getPractitioner(theRecord.get("requester.agent"));
                        }
                        if (practitionerId !=null) {
                            statement.setInformationSource(new Reference("Practitioner/" + practitionerId));
                        }
                        break;

                    case "Organization":
                        String organisationId = orgMap.get(theRecord.get("requester.agent"));
                        if (organisationId == null) {
                            organisationId = getOrganisation(theRecord.get("requester.agent"));
                        }
                        if (organisationId !=null) {
                            statement.setInformationSource(new Reference("Organization/" + organisationId));
                        }
                        break;

                }
            }
            if (!theRecord.get("reasonCode Type").isEmpty() && !theRecord.get("reasonCode").isEmpty()) {
                switch (theRecord.get("reasonCode Type")) {
                    case "Condition":
                        Bundle results = client
                                .search()
                                .forResource(Condition.class)
                                .where(Condition.IDENTIFIER.exactly().code(theRecord.get("reasonCode")))
                                .returnBundle(Bundle.class)
                                .execute();

                        if (results.getEntry().size() > 0) {
                            Condition condition = (Condition) results.getEntry().get(0).getResource();

                            statement.addReasonReference(new Reference("Condition/" + condition.getIdElement().getIdPart()));
                        }
                        break;
                }
            }
            Dosage dosage= statement.addDosage();

            if (!theRecord.get("dosage.text").isEmpty()) {
                dosage.setText(theRecord.get("dosage.text"));
            }
            if (!theRecord.get("dosage.additionalInstruction").isEmpty()) {
                CodeableConcept additional  = new CodeableConcept();
                additional.addCoding().setSystem(CareConnectSystem.SNOMEDCT).setCode(theRecord.get("dosage.additionalInstruction"));
                dosage.getAdditionalInstruction().add(additional);
            }

            if (!theRecord.get("dosage.asNeeded.asNeededBoolean").isEmpty()) {
                switch (theRecord.get("dosage.asNeeded.asNeededBoolean")) {
                    case "FALSE":
                        dosage.setAsNeeded(new BooleanType(false));
                        break;
                    case "TRUE":
                        dosage.setAsNeeded(new BooleanType(true));
                        break;
                }
            }
            if (!theRecord.get("dosage.asNeeded.asNeededCodableConcept").isEmpty()) {
                CodeableConcept additional  = new CodeableConcept();
                additional.addCoding().setSystem(CareConnectSystem.SNOMEDCT).setCode(theRecord.get("dosage.asNeeded.asNeededCodableConcept"));
                dosage.setAsNeeded(additional);
            }
            if (!theRecord.get("dosage.route").isEmpty()) {
                CodeableConcept additional  = new CodeableConcept();
                additional.addCoding().setSystem(CareConnectSystem.SNOMEDCT).setCode(theRecord.get("dosage.route"));
                dosage.setRoute(additional);
            }
            Range range = new Range();
            dosage.setDose(range);

            if (!theRecord.get("dosage.dose.doseRange.low.value").isEmpty()) {
                SimpleQuantity qty = new SimpleQuantity();
                qty.setValue(new BigDecimal(theRecord.get("dosage.dose.doseRange.low.value")));
                qty.setCode(theRecord.get("dosage.dose.doseRange.low.units"));
                qty.setSystem(CareConnectSystem.SNOMEDCT);
                range.setLow(qty);
            }

            if (!theRecord.get("dosage.dose.doseRange.high.value").isEmpty()) {
                SimpleQuantity qty = new SimpleQuantity();
                qty.setValue(new BigDecimal(theRecord.get("dosage.dose.doseRange.high.value")));
                qty.setCode(theRecord.get("dosage.dose.doseRange.high.units"));
                qty.setSystem(CareConnectSystem.SNOMEDCT);
                range.setHigh(qty);
            }
            if (!theRecord.get("dosage.dose.doseQuantity.value").isEmpty() && !theRecord.get("dosage.dose.doseQuantity.units").isEmpty()) {
                SimpleQuantity qty = new SimpleQuantity();
                qty.setValue(new BigDecimal(theRecord.get("dosage.dose.doseQuantity.value")));
                qty.setCode(theRecord.get("dosage.dose.doseQuantity.units"));
                qty.setSystem(CareConnectSystem.SNOMEDCT);
                dosage.setDose(qty);
            }

            switch (statement.getStatus()) {
                case COMPLETED: statement.setTaken(MedicationStatement.MedicationStatementTaken.Y);
                    break;
                case INTENDED: statement.setTaken(MedicationStatement.MedicationStatementTaken.NA);
                default:
                    statement.setTaken(MedicationStatement.MedicationStatementTaken.UNK);
            }
            /*
    TODO                              ,"dosage.timing"
             */

            //     System.out.println(ctx.newJsonParser().setPrettyPrint(true).encodeResourceToString(statement));

            resources.add(statement);

        }
    }



    public class BloodPressureHandler implements IRecordHandler {
        @Override
        public void accept(CSVRecord theRecord) {

            if (!theRecord.get("HeartRate").isEmpty()) {
                try {

                    Observation observation = newBasicObservation(theRecord.get("Date"), "364066008", "Cardiovascular observable",CareConnectSystem.SNOMEDCT);
                    observation.addIdentifier()
                            .setSystem(nokiaObs)
                            .setValue("CD"+stripChar(theRecord.get("Date")));
                    CodeableConcept code = observation.getCode();
                    code.addCoding().setSystem(CareConnectSystem.SNOMEDCT).setCode("364075005").setDisplay("Heart rate");

                    Quantity quantity = new Quantity();
                    quantity
                            .setValue(new BigDecimal(theRecord.get("HeartRate")))
                            .setUnit("{beats}/min")
                            .setCode("{beats}/min")
                            .setSystem(CareConnectSystem.UnitOfMeasure);
                    observation.setValue(quantity);

                    resources.add(observation);


                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            if (!theRecord.get("Systolic").isEmpty() && !theRecord.get("Diastolic").isEmpty()) {
                try {

                    Observation observation = newBasicObservation(theRecord.get("Date"), "364066008", "Cardiovascular observable",CareConnectSystem.SNOMEDCT);
                    observation.addIdentifier()
                            .setSystem(nokiaObs)
                            .setValue("BP"+stripChar(theRecord.get("Date")));
                    CodeableConcept code = observation.getCode();
                    code.addCoding().setSystem(CareConnectSystem.SNOMEDCT).setCode("75367002").setDisplay("Blood pressure");

                    Observation.ObservationComponentComponent systolic = observation.addComponent();
                    systolic.getCode().addCoding().setSystem(CareConnectSystem.SNOMEDCT).setCode("271649006").setDisplay("Systolic blood pressure");

                    Quantity quantity = new Quantity();
                    quantity
                            .setValue(new BigDecimal(theRecord.get("Systolic")))
                            .setUnit("mm[Hg]")
                            .setCode("mm[Hg]")
                            .setSystem(CareConnectSystem.UnitOfMeasure);
                    systolic.setValue(quantity);

                    Observation.ObservationComponentComponent diastolic = observation.addComponent();
                    diastolic.getCode().addCoding().setSystem(CareConnectSystem.SNOMEDCT).setCode("271650006").setDisplay("Diastolic blood pressure");

                    Quantity diaQuantity = new Quantity();
                    diaQuantity
                            .setValue(new BigDecimal(theRecord.get("Diastolic")))
                            .setUnit("mm[Hg]")
                            .setCode("mm[Hg]")
                            .setSystem(CareConnectSystem.UnitOfMeasure);
                    diastolic.setValue(diaQuantity);

                    resources.add(observation);


                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

    }

    public class WeightHandler implements IRecordHandler {
        @Override
        public void accept(CSVRecord theRecord) {
            if (!theRecord.get("Weight").isEmpty()) {

                Observation observation = newBasicObservation(theRecord.get("Date"), "248326004", "Body measure",CareConnectSystem.SNOMEDCT);
                observation.addIdentifier()
                        .setSystem(nokiaObs)
                        .setValue("WT"+stripChar(theRecord.get("Date")));
                CodeableConcept code = observation.getCode();
                code.addCoding().setSystem(CareConnectSystem.SNOMEDCT).setCode("27113001").setDisplay("Body Weight");

                Quantity quantity = new Quantity();
                quantity
                        .setValue(new BigDecimal(theRecord.get("Weight")))
                        .setUnit("kg")
                        .setCode("kg")
                        .setSystem(CareConnectSystem.UnitOfMeasure);
                observation.setValue(quantity);

                resources.add(observation);

            }
            if (!theRecord.get("FatMass").isEmpty()) {
                Observation  fatMassobservation = newBasicObservation(theRecord.get("Date"),"365605003","Body measurement finding",CareConnectSystem.SNOMEDCT);

                fatMassobservation.addIdentifier()
                        .setSystem(nokiaObs)
                        .setValue("LD"+stripChar(theRecord.get("Date")));
                CodeableConcept code =fatMassobservation.getCode();

                code.addCoding().setSystem(CareConnectSystem.SNOMEDCT).setCode("248363008").setDisplay("Fat-free mass");

                Quantity quantity= new Quantity();
                quantity
                        .setValue(new BigDecimal(theRecord.get("FatMass")))
                        .setUnit("kg")
                        .setCode("kg")
                        .setSystem(CareConnectSystem.UnitOfMeasure);
                fatMassobservation.setValue(quantity);

                resources.add(fatMassobservation);
            }

        }

    }

    public class ActivitiesHandler implements IRecordHandler {
        @Override
        public void accept(CSVRecord theRecord) {
            if (!theRecord.get("Active calories").isEmpty()) {

                Observation observation = newBasicObservation(theRecord.get("Date"), "therapy","Therapy","http://hl7.org/fhir/observation-category");
                observation.addIdentifier()
                        .setSystem(nokiaObs)
                        .setValue("AT"+stripChar(theRecord.get("Date")));
                CodeableConcept code = observation.getCode();
                code.addCoding().setSystem(CareConnectSystem.LOINC).setCode("41981-2").setDisplay("Calories burned");

                Quantity quantity = new Quantity();
                quantity
                        .setValue(new BigDecimal(theRecord.get("Active calories")))
                        .setUnit("cal")
                        .setCode("cal")
                        .setSystem(CareConnectSystem.UnitOfMeasure);
                observation.setValue(quantity);
               // System.out.println(ctx.newJsonParser().setPrettyPrint(true).encodeResourceToString(observation));
                resources.add(observation);

            }
            if (!theRecord.get("Steps").isEmpty()) {
                Observation stepobservation = newBasicObservation(theRecord.get("Date"), "therapy","Therapy","http://hl7.org/fhir/observation-category");

                stepobservation.addIdentifier()
                        .setSystem(nokiaObs)
                        .setValue("SP"+stripChar(theRecord.get("Date")));
                CodeableConcept code =stepobservation.getCode();

                code.addCoding().setSystem(CareConnectSystem.LOINC).setCode("41950-7").setDisplay("Number of steps in 24 hour Measured");

                Quantity quantity= new Quantity();
                quantity
                        .setValue(new BigDecimal(theRecord.get("Steps")))
                        .setUnit("/d")
                        .setCode("/d")
                        .setSystem(CareConnectSystem.UnitOfMeasure);
                stepobservation.setValue(quantity);
             //   System.out.println(ctx.newJsonParser().setPrettyPrint(true).encodeResourceToString(stepobservation));
                resources.add(stepobservation);
            }

        }

    }


    private Observation newBasicObservation2(String dateString, String categoryCode, String categoryDesc, String categorySystem) {
        Observation observation = null;
        Date date = null;
        try {
            SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss a");
            format.setTimeZone(TimeZone.getTimeZone("UTC"));
            // turn off set linient
            date = format.parse(dateString);
        } catch (Exception e) {
            try {
              //  System.out.println(dateString);
                SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy");
                format.setTimeZone(TimeZone.getTimeZone("UTC"));
                date = format.parse(dateString);
            } catch (Exception e2) {
                e.printStackTrace();
            }
        }

        observation = new Observation();
        //observation.setMeta(new Meta().addProfile(CareConnectITKProfile.Observation_1));
        observation.setStatus(Observation.ObservationStatus.FINAL);
        CodeableConcept category = observation.addCategory();
        category.addCoding().setSystem(categorySystem).setCode(categoryCode).setDisplay(categoryDesc);

        observation.setEffective(new DateTimeType(date));


        return observation;

    }

    private Observation newBasicObservation(String dateString, String categoryCode, String categoryDesc, String categorySystem) {
        Observation observation = null;

        observation = new Observation();
        Date date = null;
        try {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
            format.setTimeZone(TimeZone.getTimeZone("UTC"));
            date = format.parse(dateString);
        }
        catch (Exception e) {
                try {
                    //  System.out.println(dateString);
                    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                    format.setTimeZone(TimeZone.getTimeZone("UTC"));
                    date = format.parse(dateString);
                } catch (Exception e2) {
                    e.printStackTrace();
                }
            }


       // observation.setMeta(new Meta().addProfile(CareConnectITKProfile.Observation_1));
        observation.setStatus(Observation.ObservationStatus.FINAL);
        CodeableConcept category = observation.addCategory();
        category.addCoding().setSystem(categorySystem).setCode(categoryCode).setDisplay(categoryDesc);

        observation.setSubject(new Reference("Patient/2"));

        observation.setEffective(new DateTimeType(date));

        observation.getPerformer().add(new Reference("Patient/2"));

        return observation;

    }



}

