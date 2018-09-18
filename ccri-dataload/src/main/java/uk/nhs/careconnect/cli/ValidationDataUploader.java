package uk.nhs.careconnect.cli;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.FhirVersionEnum;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.IOUtils;

import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.StructureDefinition;
import org.hl7.fhir.dstu3.model.ValueSet;
import org.hl7.fhir.instance.model.api.IIdType;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

public class ValidationDataUploader extends BaseCommand {


	private static final org.slf4j.Logger ourLog = org.slf4j.LoggerFactory.getLogger(ValidationDataUploader.class);

	private ArrayList<IIdType> myExcludes = new ArrayList<>();




	@Override
	public String getCommandDescription() {
		return "Uploads the conformance resources (StructureDefinition and ValueSet) from the official FHIR definitions.";
	}

	@Override
	public String getCommandName() {
		return "upload-definitions";
	}

	@Override
	public Options getOptions() {
		Options options = new Options();
		Option opt;

		addFhirVersionOption(options);

		opt = new Option("t", "target", true, "Base URL for the target server (e.g. \"http://example.com/fhir\")");
		opt.setRequired(true);
		options.addOption(opt);

		opt = new Option("e", "exclude", true, "Exclude uploading the given resources, e.g. \"-e dicom-dcim,foo\"");
		opt.setRequired(false);
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

		FhirContext ctx = getSpecVersionContext(theCommandLine);
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


		if (ctx.getVersion().getVersion() == FhirVersionEnum.DSTU2_HL7ORG) {
		    uploadDefinitionsCareConnectDstu2(targetServer,ctx,"https://fhir.nhs.uk/");
            uploadDefinitionsCareConnectDstu2(targetServer,ctx,"https://fhir-test.hl7.org.uk/");
			uploadDefinitionsDstu2(targetServer, ctx);

		}

	}


    private void uploadDefinitionsCareConnectDstu2(String targetServer, FhirContext ctx,String careConnectServer) throws CommandFailureException {
        IGenericClient client = newClient(ctx, targetServer);

        ourLog.info("Uploading definitions to server: " + targetServer);

       // IGenericClient clientCareConnect = newClient(ctx, careConnectServer);
        ourLog.info("From server: " + careConnectServer);

        long start = System.currentTimeMillis();

        String vsContents;


        Bundle bundle = null;
        try {
            URL url = new URL(careConnectServer+"/ValueSet?_format=xml");

            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestProperty("Accept","application/xml");
            con.setRequestMethod("GET");
            con.setDoOutput(true);
            con.setDoInput(true);
            con.setUseCaches(false);
            con.setConnectTimeout(1000 * 5);
            con.connect();


            int responseCode = con.getResponseCode();

            ourLog.info("Resonse Code "+ responseCode);
            System.out.println("Resonse Code "+ responseCode);


            BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()));


			bundle = ctx.newXmlParser().parseResource(Bundle.class,br);

        } catch (Exception e) {
            throw new CommandFailureException(e.toString());
        }


        int total = bundle.getEntry().size();
        int count = 1;
        for (Bundle.BundleEntryComponent i : bundle.getEntry()) {
            ValueSet next = (ValueSet) i.getResource();
            next.setId(next.getIdElement().toUnqualifiedVersionless());
           // System.out.println("Uploading ValueSet "+ next.getUrl());
            ourLog.info("Uploading ValueSet {}/{} : {}", new Object[]{count, total, next.getIdElement().getValue()});

            try {
                client.update().resource(next).execute();
            }
            catch (Exception ex) {
                System.out.println("ValueSet "+ next.getUrl() + " ERROR " +ex.getMessage());
            }

            count++;
        }



        ourLog.info("Finished uploading ValueSets");



        long delay = System.currentTimeMillis() - start;

        ourLog.info("Finished uploading definitions to server (took {} ms)", delay);
    }

	private void uploadDefinitionsDstu2(String targetServer, FhirContext ctx) throws CommandFailureException {
		IGenericClient client = newClient(ctx, targetServer);
		ourLog.info("Uploading definitions to server: " + targetServer);

		long start = System.currentTimeMillis();

		String vsContents;



		try {
			ctx.getVersion().getPathToSchemaDefinitions();
			vsContents = IOUtils.toString(ValidationDataUploader.class.getResourceAsStream("/org/hl7/fhir/instance/model/valueset/" + "valuesets.xml"), "UTF-8");
		} catch (IOException e) {
			throw new CommandFailureException(e.toString());
		}
		Bundle bundle = ctx.newXmlParser().parseResource(Bundle.class, vsContents);

		int total = bundle.getEntry().size();
		int count = 1;
		for (Bundle.BundleEntryComponent i : bundle.getEntry()) {
			ValueSet next = (ValueSet) i.getResource();
			next.setId(next.getIdElement().toUnqualifiedVersionless());

			ourLog.info("Uploading ValueSet {}/{} : {}", new Object[]{count, total, next.getIdElement().getValue()});
			client.update().resource(next).execute();

			count++;
		}

		try {
			vsContents = IOUtils.toString(ValidationDataUploader.class.getResourceAsStream("/org/hl7/fhir/instance/model/valueset/" + "v3-codesystems.xml"), "UTF-8");
		} catch (IOException e) {
			throw new CommandFailureException(e.toString());
		}

		bundle = ctx.newXmlParser().parseResource(Bundle.class, vsContents);
		total = bundle.getEntry().size();
		count = 1;
		for (Bundle.BundleEntryComponent i : bundle.getEntry()) {
			ValueSet next = (ValueSet) i.getResource();
			next.setId(next.getIdElement().toUnqualifiedVersionless());

			ourLog.info("Uploading v3-codesystems ValueSet {}/{} : {}", new Object[]{count, total, next.getIdElement().getValue()});
			client.update().resource(next).execute();

			count++;
		}

		try {
			vsContents = IOUtils.toString(ValidationDataUploader.class.getResourceAsStream("/org/hl7/fhir/instance/model/valueset/" + "v2-tables.xml"), "UTF-8");
		} catch (IOException e) {
			throw new CommandFailureException(e.toString());
		}
		bundle = ctx.newXmlParser().parseResource(Bundle.class, vsContents);
		total = bundle.getEntry().size();
		count = 1;
		for (Bundle.BundleEntryComponent i : bundle.getEntry()) {
			ValueSet next = (ValueSet) i.getResource();
			next.setId(next.getIdElement().toUnqualifiedVersionless());

			ourLog.info("Uploading v2-tables ValueSet {}/{} : {}", new Object[]{count, total, next.getIdElement().getValue()});
			client.update().resource(next).execute();
			count++;
		}

		ourLog.info("Finished uploading ValueSets");

		ResourcePatternResolver patternResolver = new PathMatchingResourcePatternResolver();
		Resource[] mappingLocations;
		try {
			mappingLocations = patternResolver.getResources("classpath*:org/hl7/fhir/instance/model/profile/" + "*.profile.xml");
		} catch (IOException e) {
			throw new CommandFailureException(e.toString());
		}
		total = mappingLocations.length;
		count = 1;
		for (Resource i : mappingLocations) {
			StructureDefinition next;
			try {
				next = ctx.newXmlParser().parseResource(StructureDefinition.class, IOUtils.toString(i.getInputStream(), "UTF-8"));
			} catch (Exception e) {
				throw new CommandFailureException(e.toString());
			}
			next.setId(next.getIdElement().toUnqualifiedVersionless());

			ourLog.info("Uploading StructureDefinition {}/{} : {}", new Object[]{count, total, next.getIdElement().getValue()});
			try {
				client.update().resource(next).execute();
			} catch (Exception e) {
				ourLog.warn("Failed to upload {} - {}", next.getIdElement().getValue(), e.getMessage());
			}
			count++;
		}

		ourLog.info("Finished uploading ValueSets");

		long delay = System.currentTimeMillis() - start;

		ourLog.info("Finished uploading definitions to server (took {} ms)", delay);
	}

}
