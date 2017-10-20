package uk.nhs.careconnect.cli;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.FhirVersionEnum;
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
import org.apache.commons.io.Charsets;
import org.apache.commons.io.IOUtils;
import org.hl7.fhir.dstu3.model.*;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.instance.model.api.IIdType;
import uk.org.hl7.fhir.core.Stu3.CareConnectProfile;
import uk.org.hl7.fhir.core.Stu3.CareConnectSystem;

import java.io.*;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

public class UploadExamples extends BaseCommand {

	private static final org.slf4j.Logger ourLog = org.slf4j.LoggerFactory.getLogger(UploadExamples.class);

	private ArrayList<IIdType> myExcludes = new ArrayList<>();

    private ArrayList<IBaseResource> resources = new ArrayList<>();

    FhirContext ctx ;

    IGenericClient client;

/* PROGRAM ARGUMENTS

upload-examples
-t
http://127.0.0.1:8080/careconnect-ri/STU3

    */

	@Override
	public String getCommandDescription() {
		return "Uploads sample resources.";
	}

	@Override
	public String getCommandName() {
		return "upload-examples";
	}

	@Override
	public Options getOptions() {
		Options options = new Options();
		Option opt;

		addFhirVersionOption(options);

		opt = new Option("t", "target", true, "Base URL for the target server (e.g. \"http://example.com/fhir\")");
		opt.setRequired(true);
		options.addOption(opt);

		return options;
	}

	@Override
	public void run(CommandLine theCommandLine) throws ParseException {
		String targetServer = theCommandLine.getOptionValue("t");
		if (isBlank(targetServer)) {
			throw new ParseException("No target server (-t) specified");
		} else if (targetServer.startsWith("http") == false) {
			throw new ParseException("Invalid target server specified, must begin with 'http'");
		}

		ctx = getSpecVersionContext(theCommandLine);
		String exclude = theCommandLine.getOptionValue("e");

		if (isNotBlank(exclude)) {
			for (String next : exclude.split(",")) {
				if (isNotBlank(next)) {
					IIdType id = ctx.getVersion().newIdType();
					id.setValue(next);
					myExcludes.add(id);
				}
			}
		}

		if (ctx.getVersion().getVersion() == FhirVersionEnum.DSTU3) {
            client = ctx.newRestfulGenericClient(targetServer);


            try {
                String path = "/examples/observations/";
                List<String> files = getResourceFiles(path);
                for (String file : files) {
                    System.out.println(file);
                    String contents = IOUtils.toString(new InputStreamReader(new FileInputStream(UploadExamples.class.getResource(path+file).getFile()), "UTF-8"));
                    IBaseResource localProfileResource = ca.uhn.fhir.rest.api.EncodingEnum.detectEncodingNoDefault(contents).newParser(ctx).parseResource(contents);
                    client.create().resource(localProfileResource).execute();
                }

            } catch (Exception ex) {
                ourLog.error(ex.getMessage());
            }
            try {
                String path = "/examples/nokia/";
                List<String> files = getResourceFiles(path);
                for (String file : files) {
                    if (file.equals("weight.csv")) {
                        System.out.println(file);
                        IRecordHandler handler = null;

                        handler = new WeightHandler();
                        processWeightCSV(handler,  ctx, ',', QuoteMode.NON_NUMERIC, UploadExamples.class.getResource(path+file).getFile());
                        for (IBaseResource resource : resources) {
                            client.create().resource(resource).execute();
                        }
                        resources.clear();
                    }
                    if (file.equals("blood_pressure.csv")) {
                        System.out.println(file);
                        IRecordHandler handler = null;

                        handler = new BloodPressureHandler();
                        processBloodPressureCSV(handler,  ctx, ',', QuoteMode.NON_NUMERIC, UploadExamples.class.getResource(path+file).getFile());
                        for (IBaseResource resource : resources) {
                            client.create().resource(resource).execute();
                        }
                        resources.clear();
                    }
                }

            } catch (Exception ex) {
                ourLog.error(ex.getMessage());
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


    private void processWeightCSV(IRecordHandler handler, FhirContext ctx, char theDelimiter, QuoteMode theQuoteMode, String filename) throws CommandFailureException {


        Boolean found = false;
        try {

            ourLog.info("Processing file {}", filename);
            found = true;

            Reader reader = null;
            CSVParser parsed = null;
            try {
                reader = new InputStreamReader(new FileInputStream(filename), Charsets.UTF_8);
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
                        ourLog.info(" * Processed {} records in {}", count, filename);
                    }
                }

            } catch (IOException e) {
                throw new InternalErrorException(e);
            }
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }

    private void processBloodPressureCSV(IRecordHandler handler, FhirContext ctx, char theDelimiter, QuoteMode theQuoteMode, String filename) throws CommandFailureException {


        Boolean found = false;
        try {

            ourLog.info("Processing file {}", filename);
            found = true;

            Reader reader = null;
            CSVParser parsed = null;
            try {
                reader = new InputStreamReader(new FileInputStream(filename), Charsets.UTF_8);
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
                        ourLog.info(" * Processed {} records in {}", count, filename);
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

    public class BloodPressureHandler implements IRecordHandler {
        @Override
        public void accept(CSVRecord theRecord) {

            if (!theRecord.get("HeartRate").isEmpty()) {
                try {

                    Observation observation = newBasicObservation(theRecord.get("Date"), "364066008", "Cardiovascular observable");

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

                    Observation observation = newBasicObservation(theRecord.get("Date"), "364066008", "Cardiovascular observable");

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

                Observation observation = newBasicObservation(theRecord.get("Date"), "248326004", "Body measure");

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
                Observation  fatMassobservation = newBasicObservation(theRecord.get("Date"),"365605003","Body measurement finding");

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

    private Observation newBasicObservation(String dateString, String categoryCode, String categoryDesc) {
        Observation observation = null;
        try {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        Date date = format.parse(dateString);

        observation = new Observation();
        observation.setMeta(new Meta().addProfile(CareConnectProfile.Observation_1));
        observation.setStatus(Observation.ObservationStatus.FINAL);
        CodeableConcept category = observation.addCategory();
        category.addCoding().setSystem(CareConnectSystem.SNOMEDCT).setCode(categoryCode).setDisplay(categoryDesc);

        observation.setSubject(new Reference("Patient/2"));

        observation.setEffective(new DateTimeType(date));

        observation.getPerformer().add(new Reference("Patient/2"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return observation;

    }



}

