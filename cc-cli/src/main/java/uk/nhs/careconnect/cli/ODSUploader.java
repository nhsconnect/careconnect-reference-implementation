package uk.nhs.careconnect.cli;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.FhirVersionEnum;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.client.IGenericClient;
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
import org.apache.commons.io.input.BOMInputStream;
import org.hl7.fhir.instance.model.*;
import org.hl7.fhir.instance.model.api.IIdType;

import java.io.*;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

public class ODSUploader extends BaseCommand {

	private static final org.slf4j.Logger ourLog = org.slf4j.LoggerFactory.getLogger(ODSUploader.class);

	private ArrayList<IIdType> myExcludes = new ArrayList<>();

	private ArrayList<Organization> orgs = new ArrayList<>();

	private Map<String,Organization> orgMap = new HashMap<>();

    FhirContext ctx ;

    IGenericClient client;

	@Override
	public String getCommandDescription() {
		return "Uploads the ods/sds resources from NHS Digital.";
	}

	@Override
	public String getCommandName() {
		return "upload-ods";
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

		if (ctx.getVersion().getVersion() == FhirVersionEnum.DSTU2_HL7ORG) {
            client = ctx.newRestfulGenericClient(targetServer);

            uploadODSDstu2(targetServer, ctx, ',', QuoteMode.NON_NUMERIC, "etr.zip", "etr.csv","https://digital.nhs.uk/media/352/etr/zip/etr");
            uploadOrganisation();

            uploadODSDstu2(targetServer, ctx, ',', QuoteMode.NON_NUMERIC, "eccg.zip", "eccg.csv", "https://digital.nhs.uk/media/354/eccg/zip/eccg");
            uploadOrganisation();

            uploadODSDstu2(targetServer, ctx, ',', QuoteMode.NON_NUMERIC, "epraccur.zip", "epraccur.csv", "https://digital.nhs.uk/media/372/epraccur/zip/epraccur");
            uploadOrganisation();

		}

	}

	private void uploadOrganisation() {
	    for (Organization organization : orgs) {
            MethodOutcome outcome = client.update().resource(organization)
                    .conditionalByUrl("Organization?identifier=" + organization.getIdentifier().get(0).getSystem() + "%7C" +organization.getIdentifier().get(0).getValue())
                    .execute();
            System.out.println(outcome.getId());
            if (outcome.getId() != null ) {
                organization.setId(outcome.getId().getIdPart());
                orgMap.put(organization.getIdentifier().get(0).getValue(), organization);
            }
        }
        orgs.clear();
    }


	private void uploadODSDstu2(String targetServer, FhirContext ctx, char theDelimiter, QuoteMode theQuoteMode, String fileName, String fileNamePart, String webSite) throws CommandFailureException {


	    Boolean found = false;
	    try {

            URL website = new URL(webSite);
            ReadableByteChannel rbc = Channels.newChannel(website.openStream());

            FileOutputStream fos = new FileOutputStream(fileName);
            fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
            List<byte[]> theZipBytes = new ArrayList<>();
            byte[] nextData = IOUtils.toByteArray(new FileInputStream(fileName));
            theZipBytes.add(nextData);

            IRecordHandler handler = new OrgHandler();

            for (byte[] nextZipBytes : theZipBytes) {
                ZipInputStream zis = new ZipInputStream(new BufferedInputStream(new ByteArrayInputStream(nextZipBytes)));
                try {
                    for (ZipEntry nextEntry; (nextEntry = zis.getNextEntry()) != null; ) {
                        ZippedFileInputStream inputStream = new ZippedFileInputStream(zis);

                        String nextFilename = nextEntry.getName();
                        if (nextFilename.contains(fileNamePart)) {

                            ourLog.info("Processing file {}", nextFilename);
                            found = true;

                            Reader reader = null;
                            CSVParser parsed = null;
                            try {
                                reader = new InputStreamReader(new BOMInputStream(zis), Charsets.UTF_8);
                                CSVFormat format = CSVFormat
                                        .newFormat(theDelimiter)
                                        .withAllowMissingColumnNames()
                                        .withHeader("OrganisationCode"
                                                ,"Name","NationalGrouping"
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
                                                ,"Fld14"
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
                               // int logIncrement = LOG_INCREMENT;
                                int nextLoggedCount = 0;
                                while (iter.hasNext()) {
                                    CSVRecord nextRecord = iter.next();
                                    handler.accept(nextRecord);
                                    count++;
                                    if (count >= nextLoggedCount) {
                                        ourLog.info(" * Processed {} records in {}", count, nextFilename);
                                     //   nextLoggedCount += logIncrement;
                                    }
                                }

                            } catch (IOException e) {
                                throw new InternalErrorException(e);
                            }
                        }
                    }
                } catch (Exception ex) {
                    System.out.println("Ooops 2 :"+ex.getMessage());
                }

            }
        } catch(Exception ex){
            System.out.println("Ooops 3 :"+ex.getMessage());
        }

	}

    private interface IRecordHandler {
        void accept(CSVRecord theRecord);
    }

    public class OrgHandler implements IRecordHandler {


        @Override
        public void accept(CSVRecord theRecord) {
              //v  System.out.println(theRecord.toString());
                Organization organization = new Organization();

                // List<IdDt> profiles = new ArrayList<IdDt>();
                // profiles.add(new IdDt(CareConnectSystem.ProfileOrganization));
                /// ResourceMetadataKeyEnum.PROFILES.put(organization, profiles);

                organization.setId("dummy");
                organization.setMeta(new Meta().addProfile("https://fhir.hl7.org.uk/StructureDefinition/CareConnect-Organization-1" /* CareConnectProfile.Organization_1 */));


                organization.addIdentifier()
                        .setSystem("https://fhir.nhs.uk/Id/ods-organization-code" /* CareConnectSystem.ODSOrganisationCode */ )
                        .setValue(theRecord.get("OrganisationCode"));

               // switch (type) {
               //     case "prov":
                        organization.getType().addCoding()
                                .setSystem("http://hl7.org/fhir/organization-type" /* CareConnectSystem.OrganisationType */)
                                .setCode("prov")
                                .setDisplay("Healthcare Provider");
                //        break;
                //}
                organization.setName(theRecord.get("Name"));

                if (!theRecord.get("ContactTelephoneNumber").isEmpty()) {
                    organization.addTelecom()
                            .setUse(ContactPoint.ContactPointUse.WORK)
                            .setValue(theRecord.get("ContactTelephoneNumber"))
                            .setSystem(ContactPoint.ContactPointSystem.PHONE);
                }
                if (!theRecord.get("Commissioner").isEmpty()) {
                    Organization parentOrg = orgMap.get(theRecord.get("Commissioner"));
                    if (parentOrg != null) {
                        organization.setPartOf(new Reference("Organization/"+parentOrg.getId()).setDisplay(parentOrg.getName()));
                    }
                }
                organization.addAddress()
                        .setUse(Address.AddressUse.WORK)
                        .addLine(theRecord.get("AddressLine_1"))
                        .addLine(theRecord.get("AddressLine_2"))
                        .addLine(theRecord.get("AddressLine_3"))
                        .setCity(theRecord.get("AddressLine_4"))
                        .setDistrict(theRecord.get("AddressLine_5"))
                        .setPostalCode(theRecord.get("Postcode"));
                orgs.add(organization);
            //System.out.println(ctx.newXmlParser().setPrettyPrint(true).encodeResourceToString(organization));
                /*
                switch (type) {
                    case "CSC":
                        organization.getType().addCoding()
                                .setSystem("http://hl7.org/fhir/ValueSet/v3-ServiceDeliveryLocationRoleType")
                                .setCode(type)
                                .setDisplay("Community Service Centre");
                        break;
                }
                */
            /*
            String code = theRecord.get("LOINC_NUM");
            if (isNotBlank(code)) {
                String longCommonName = theRecord.get("LONG_COMMON_NAME");
                String shortName = theRecord.get("SHORTNAME");
                String consumerName = theRecord.get("CONSUMER_NAME");
                String display = firstNonBlank(longCommonName, shortName, consumerName);

                ConceptEntity concept = new ConceptEntity(myCodeSystemVersion, code);
                concept.setDisplay(display);

                Validate.isTrue(!myCode2Concept.containsKey(code));
                myCode2Concept.put(code, concept);
            }
            */
        }

    }

    private static class ZippedFileInputStream extends InputStream {

        private ZipInputStream is;

        public ZippedFileInputStream(ZipInputStream is) {
            this.is = is;
        }

        @Override
        public void close() throws IOException {
            is.closeEntry();
        }

        @Override
        public int read() throws IOException {
            return is.read();
        }
    }

}

