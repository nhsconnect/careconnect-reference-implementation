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
import org.apache.commons.io.Charsets;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.BOMInputStream;
import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.dstu3.model.*;
import org.hl7.fhir.instance.model.api.IIdType;
import uk.org.hl7.fhir.core.Stu3.CareConnectProfile;
import uk.org.hl7.fhir.core.Stu3.CareConnectSystem;

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

    private ArrayList<Practitioner> docs = new ArrayList<>();

    private ArrayList<Location> locs = new ArrayList<>();

    private Map<String,Organization> orgMap = new HashMap<>();

    private Map<String,Practitioner> docMap = new HashMap<>();

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


            IRecordHandler handler = new OrgHandler("930621000000104","National Health Service Trust");
            uploadODSDstu2(handler, targetServer, ctx, ',', QuoteMode.NON_NUMERIC, "etr.zip", "etr.csv","https://digital.nhs.uk/media/352/etr/zip/etr");
            uploadOrganisation();

            handler = new OrgHandler("394747008","Health Authority");
            uploadODSDstu2(handler, targetServer, ctx, ',', QuoteMode.NON_NUMERIC, "eccg.zip", "eccg.csv", "https://digital.nhs.uk/media/354/eccg/zip/eccg");
            uploadOrganisation();

            handler = new OrgHandler("394745000","General practice");
            uploadODSDstu2(handler, targetServer, ctx, ',', QuoteMode.NON_NUMERIC, "epraccur.zip", "epraccur.csv", "https://digital.nhs.uk/media/372/epraccur/zip/epraccur");
            uploadOrganisation();

            handler = new LocationHandler("930631000000102", "National Health Service Trust site");
            uploadODSDstu2(handler, targetServer, ctx, ',', QuoteMode.NON_NUMERIC, "ets.zip", "ets.csv", "https://digital.nhs.uk/media/351/ets/zip/ets");
            uploadLocation();

            handler = new LocationHandler("394761003", "GP practice site");
            uploadODSDstu2(handler, targetServer, ctx, ',', QuoteMode.NON_NUMERIC, "ebranchs.zip", "ebranchs.csv", "https://digital.nhs.uk/media/393/ebranchs/zip/ebranchs");
            uploadLocation();


            handler = new PractitionerHandler();
            uploadODSDstu2(handler, targetServer, ctx, ',', QuoteMode.NON_NUMERIC, "egpcur.zip", "egpcur.csv","https://digital.nhs.uk/media/370/egpcur/zip/egpcur");
            uploadPractitioner();

            // uploadODSDstu2(handler, targetServer, ctx, ',', QuoteMode.NON_NUMERIC, "econcur.zip", "econcur.csv","https://digital.nhs.uk/media/450/econcur/zip/econcur");
            // uploadPractitioner();

		}

	}

	private void uploadOrganisation() {
	    for (Organization organization : orgs) {
            MethodOutcome outcome = client.update().resource(organization)
                    .conditionalByUrl("Organization?identifier=" + organization.getIdentifier().get(0).getSystem() + "%7C" +organization.getIdentifier().get(0).getValue())
                    .execute();
           // System.out.println(outcome.getId());
            if (outcome.getId() != null ) {
                organization.setId(outcome.getId().getIdPart());
                orgMap.put(organization.getIdentifier().get(0).getValue(), organization);
            }
        }
        orgs.clear();
    }
    private void uploadLocation() {
        for (Location location : locs) {
            MethodOutcome outcome = client.update().resource(location)
                    .conditionalByUrl("Location?identifier=" + location.getIdentifier().get(0).getSystem() + "%7C" +location.getIdentifier().get(0).getValue())
                    .execute();

            if (outcome.getId() != null ) {
                location.setId(outcome.getId().getIdPart());
            }
        }
        locs.clear();
    }
    private void uploadPractitioner() {
        for (Practitioner practitioner : docs) {

         //   System.out.println(ctx.newXmlParser().setPrettyPrint(true).encodeResourceToString(practitioner));
            MethodOutcome outcome = client.update().resource(practitioner)
                    .conditionalByUrl("Practitioner?identifier=" + practitioner.getIdentifier().get(0).getSystem() + "%7C" +practitioner.getIdentifier().get(0).getValue())
                    .execute();
       //     System.out.println(outcome.getId());
            if (outcome.getId() != null ) {
                practitioner.setId(outcome.getId().getIdPart());
                docMap.put(practitioner.getIdentifier().get(0).getValue(), practitioner);
            }
        }
        orgs.clear();
    }


	private void uploadODSDstu2(IRecordHandler handler, String targetServer, FhirContext ctx, char theDelimiter, QuoteMode theQuoteMode, String fileName, String fileNamePart, String webSite) throws CommandFailureException {


	    Boolean found = false;
	    try {

            URL website = new URL(webSite);
            ReadableByteChannel rbc = Channels.newChannel(website.openStream());

            FileOutputStream fos = new FileOutputStream(fileName);
            fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
            List<byte[]> theZipBytes = new ArrayList<>();
            byte[] nextData = IOUtils.toByteArray(new FileInputStream(fileName));
            theZipBytes.add(nextData);



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

    public class LocationHandler implements IRecordHandler {

	    private String typeSncCT = "";

        private String typeDisplay = "";
	    LocationHandler(String typeSncCT, String typeDisplay) {
	        this.typeSncCT = typeSncCT;
	        this.typeDisplay = typeDisplay;
        }

        public void setType(String type) {
            this.typeSncCT = type;
        }

        @Override
        public void accept(CSVRecord theRecord) {
            Location location = new Location();
            location.setId("dummy");
            location.setMeta(new Meta().addProfile(CareConnectProfile.Location_1));

            location.addIdentifier()
                    .setSystem(CareConnectSystem.ODSSiteCode)
                    .setValue(theRecord.get("OrganisationCode"));

            location.setName(Inicaps(theRecord.get("Name")));

            if (!theRecord.get("ContactTelephoneNumber").isEmpty()) {
                location.addTelecom()
                        .setUse(ContactPoint.ContactPointUse.WORK)
                        .setValue(theRecord.get("ContactTelephoneNumber"))
                        .setSystem(ContactPoint.ContactPointSystem.PHONE);
            }
            location.setStatus(Location.LocationStatus.ACTIVE);
            if (!theRecord.get("CloseDate").isEmpty()) {
                location.setStatus(Location.LocationStatus.ACTIVE);
            }
            location.getAddress()
                    .setUse(Address.AddressUse.WORK)
                    .addLine(Inicaps(theRecord.get("AddressLine_1")))
                    .addLine(Inicaps(theRecord.get("AddressLine_2")))
                    .addLine(Inicaps(theRecord.get("AddressLine_3")))
                    .setCity(Inicaps(theRecord.get("AddressLine_4")))
                    .setDistrict(Inicaps(theRecord.get("AddressLine_5")))
                    .setPostalCode(theRecord.get("Postcode"));


            if (typeSncCT!=null) {
                location.getType()
                        .addCoding()
                        .setSystem(CareConnectSystem.SNOMEDCT)
                        .setCode(typeSncCT)
                        .setDisplay(typeDisplay);

            }

            if (!theRecord.get("Commissioner").isEmpty()) {

                Organization parentOrg = orgMap.get(theRecord.get("Commissioner"));

                if (parentOrg != null) {
                   // System.out.println("Org Id = "+parentOrg.getId());
                    location.setManagingOrganization(new Reference("Organization/"+parentOrg.getId()).setDisplay(parentOrg.getName()));
                }
            }

         //   System.out.println(ctx.newXmlParser().setPrettyPrint(true).encodeResourceToString(location));

            locs.add(location);
        }

    }

    public class PractitionerHandler implements IRecordHandler {
        @Override
        public void accept(CSVRecord theRecord) {
           // System.out.println(theRecord.toString());
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
            /* STU3
            Practitioner.PractitionerPractitionerRoleComponent role = practitioner.addPractitionerRole();

            if (!theRecord.get("Commissioner").isEmpty()) {
             //   System.out.println("Commissioner="+theRecord.get("Commissioner"));
                Organization parentOrg = orgMap.get(theRecord.get("Commissioner"));

                if (parentOrg != null) {
                  //  System.out.println("Org Id = "+parentOrg.getId());
                   role.setManagingOrganization(new Reference("Organization/"+parentOrg.getId()).setDisplay(parentOrg.getName()));
                }
            }
            if (!theRecord.get("OrganisationSubTypeCode").isEmpty()) {
                switch (theRecord.get("OrganisationSubTypeCode")) {
                    case "O":
                    case "P":
                        role.getRole().addCoding()
                                .setSystem(CareConnectSystem.SDSJobRoleName)
                                .setCode("R0260")
                                .setDisplay("General Medical Practitioner");
                }
            }
            */
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




            docs.add(practitioner);
        }

    }
    public class OrgHandler implements IRecordHandler {

        private String typeSncCT = "";

        private String typeDisplay = "";
        OrgHandler(String typeSncCT, String typeDisplay) {
            this.typeSncCT = typeSncCT;
            this.typeDisplay = typeDisplay;
        }

        public void setType(String type) {
            this.typeSncCT = type;
        }

        @Override
        public void accept(CSVRecord theRecord) {
              //v  System.out.println(theRecord.toString());
                Organization organization = new Organization();



                organization.setId("dummy");
                organization.setMeta(new Meta().addProfile(CareConnectProfile.Organization_1));


                organization.addIdentifier()
                        .setSystem(CareConnectSystem.ODSOrganisationCode )
                        .setValue(theRecord.get("OrganisationCode"));




                organization.setName(Inicaps(theRecord.get("Name")));

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
                organization.setActive(true);
                if (!theRecord.get("CloseDate").isEmpty()) {
                    organization.setActive(false);
                }
                if (typeSncCT!=null)
                {
                    organization.addType().addCoding().setDisplay(typeDisplay)
                        .setSystem(CareConnectSystem.SNOMEDCT)
                        .setCode(typeSncCT);


                } else {
                    organization.addType().addCoding()
                            .setSystem(CareConnectSystem.OrganisationType)
                            .setCode("prov")
                            .setDisplay("Healthcare Provider");
                }

                organization.addAddress()
                        .setUse(Address.AddressUse.WORK)
                        .addLine(Inicaps(theRecord.get("AddressLine_1")))
                        .addLine(Inicaps(theRecord.get("AddressLine_2")))
                        .addLine(Inicaps(theRecord.get("AddressLine_3")))
                        .setCity(Inicaps(theRecord.get("AddressLine_4")))
                        .setDistrict(Inicaps(theRecord.get("AddressLine_5")))
                        .setPostalCode(theRecord.get("Postcode"));
                orgs.add(organization);
            //System.out.println(ctx.newXmlParser().setPrettyPrint(true).encodeResourceToString(organization));


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

