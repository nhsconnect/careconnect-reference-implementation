package uk.nhs.careconnect.cli;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.FhirVersionEnum;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.IOUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.TextProgressMonitor;
import org.hl7.fhir.dstu3.model.*;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.instance.model.api.IIdType;


import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.apache.commons.lang3.StringUtils.isBlank;

public class GitDownloader extends BaseCommand {

	private static final org.slf4j.Logger ourLog = org.slf4j.LoggerFactory.getLogger(GitDownloader.class);

	private ArrayList<IIdType> myExcludes = new ArrayList<>();

	private ArrayList<Organization> orgs = new ArrayList<>();

    private ArrayList<Practitioner> docs = new ArrayList<>();

    private ArrayList<Location> locs = new ArrayList<>();

    private Map<String,Organization> orgMap = new HashMap<>();

    private Map<String,Practitioner> docMap = new HashMap<>();

    private Map<String,String> resources = new HashMap<>();

    FhirContext ctx ;

    IGenericClient client;

	Bundle valueSet;
	Bundle structuredDefinition;

    /* Intelij Programme arguments
-d
/Development/NHSD/careconnect-reference-implementation/cc-fhir-validation-resources-stu3/src/main/resources/uk/org/hl7/fhir/stu3/model
*/
	@Override
	public String getCommandDescription() {
		return "Download STU3 profiles and valuesets from GitHub. INTERNAL - Used to populate cc-fhir-validation-resources";
	}

	@Override
	public String getCommandName() {
		return "download-profiles";
	}

	@Override
	public Options getOptions() {
		Options options = new Options();
		Option opt;

		addFhirVersionOption(options);

        opt = new Option("d", "data", true, "Destination folder of processed resources");
        opt.setRequired(false);
        options.addOption(opt);

		return options;
	}
	static void rm(File f) {
		if (f.isDirectory())
			for (File c : f.listFiles())
				rm(c);
		f.delete();
	}

	@Override
	public void run(CommandLine theCommandLine) throws ParseException {
		String targetServer = theCommandLine.getOptionValue("d");
		if (isBlank(targetServer)) {
			throw new ParseException("No destination folder (-d) specified");
		}
    	valueSet = new Bundle();
     	structuredDefinition = new Bundle();
		ctx = getSpecVersionContext(theCommandLine);


		if (ctx.getVersion().getVersion() == FhirVersionEnum.DSTU3) {

			try {
                callGits("https://github.com/nhsconnect/CareConnect-profiles-STU3.git", "develop");
				callGits("https://github.com/nhsconnect/CareConnect-profiles-STU3.git", "draftprofilesrelease1");
                callGits("https://github.com/nhsconnect/CareConnect-profiles-STU3.git", "draftprofilesrelease2");
				callGits("https://github.com/nhsconnect/STU3-FHIR-Assets.git", "develop");
                callGits("https://github.com/nhsconnect/STU3-FHIR-Assets.git", "release/Transfer_of_care_2.2.0-Alpha");

				File file = new File(targetServer + "/profile/profiles-resources.xml");

				FileOutputStream profileFS = new FileOutputStream(file);
				if (!file.exists()) {
					file.createNewFile();
				}
				Writer writer = new OutputStreamWriter(profileFS);
				ctx.newXmlParser().encodeResourceToWriter(structuredDefinition, writer);
				profileFS.flush();
				profileFS.close();

				file = new File(targetServer + "/valueset/valuesets.xml");

				profileFS = new FileOutputStream(file);
				if (!file.exists()) {
					file.createNewFile();
				}
				writer = new OutputStreamWriter(profileFS);
				ctx.newXmlParser().encodeResourceToWriter(valueSet, writer);
				profileFS.flush();
				profileFS.close();

					/*
					System.out.println("KGM="+r.getRepository().getDirectory().getName());
					for (Ref f : r.branchList().setListMode(ListBranchCommand.ListMode.ALL).call()) {
						r.checkout().setName(f.getName()).call();
						System.out.println("checked out branch " + f.getName()
								+ ". HEAD: " + r.getRepository().getRef("HEAD"));
					}
					// try to checkout branches by specifying abbreviated names
					r.checkout().setName("master").call();
					r.checkout().setName("feature/stu3").call();
					try {
						r.checkout().setName("test").call();
					} catch (RefNotFoundException e) {
						System.err.println("couldn't checkout 'test'. Got exception: "
								+ e.toString() + ". HEAD: "
								+ r.getRepository().getRef("HEAD"));
					}
					*/

			} catch (Exception ex) {
				System.out.println(ex.getMessage());
			}
		}
	}
	private void callGits(String repo, String branch) {
		try {
			File tmpDir;
			tmpDir = new File(System.getProperty("java.io.tmpdir"), "tmp"
					+ System.currentTimeMillis());
			tmpDir.mkdirs();
			try {
				Git r = Git.cloneRepository().setDirectory(tmpDir)
						.setURI(repo)
						.setBranch(branch)
						.setProgressMonitor(new TextProgressMonitor())
						.call();

				r.checkout().setName(branch).call();
				for (File directory : tmpDir.listFiles(File::isDirectory)) {
					System.out.println(directory.getName());
					if (!directory.getName().equals(".git")) {
						for (File file : directory.listFiles()) {
							//   System.out.println("-"+file.getName());
							FileInputStream inputStream = new FileInputStream(file);
							Reader reader = new InputStreamReader(inputStream);
							IBaseResource resource = null;
							try {
								String sResource = IOUtils.toString(inputStream,  "UTF-8");
								sResource = sResource.replaceAll("[^\\x20-\\x7e]", "");
								resource = ctx.newXmlParser().parseResource(sResource);
							} catch (Exception ex) {
								try {
									System.out.println(ex.getMessage());
									System.out.println("WARNING - XML Parse error " + file.getName());
									resource = ctx.newJsonParser().parseResource(reader);
								} catch (Exception ec1) {
									System.out.println("XML and JSON failed - " + file);
									resource = null;
								}
							}
							if (resource instanceof ValueSet) {

								ValueSet valueSetR = (ValueSet) resource;
                                if (!resources.containsKey(file.getName())) {
                                    valueSet.addEntry().setResource(valueSetR);
                                    resources.put(file.getName(), file.getName()+repo + " branch "+ branch);
                                } else {
                                    System.out.println("Duplicate CodeSystem ** " + " Using: "+resources.get(file.getName()));
                                }
							}
							if (resource instanceof CodeSystem) {

								CodeSystem codeSystem= (CodeSystem) resource;
                                if (!resources.containsKey(file.getName())) {
                                    valueSet.addEntry().setResource(codeSystem);
                                    resources.put(file.getName(), repo + " branch "+ branch);
                                } else {
                                    System.out.println("Duplicate CodeSystem ** "+file.getName() + " Using: "+resources.get(file.getName()));
                                }
							}
							if (resource instanceof StructureDefinition) {
								StructureDefinition structureDefinition = (StructureDefinition) resource;
                                if (!resources.containsKey(file.getName())) {
                                    structuredDefinition.addEntry().setResource(structureDefinition);
                                    resources.put(file.getName(), repo + " branch "+ branch);
                                } else {
                                    System.out.println("Duplicate StrutureDefinition ** "+file.getName() + " Using: "+resources.get(file.getName()));
                                }
							}
						}
					}
				}
			} finally {
				rm(tmpDir);
			}
		}
		catch (Exception ex) {
			System.out.println(ex.getMessage());
		}
	}
}

