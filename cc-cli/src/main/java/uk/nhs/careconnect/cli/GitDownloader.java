package uk.nhs.careconnect.cli;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.FhirVersionEnum;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
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

    FhirContext ctx ;

    IGenericClient client;

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
        Bundle valueSet = new Bundle();
        Bundle structuredDefinition = new Bundle();
		ctx = getSpecVersionContext(theCommandLine);


		if (ctx.getVersion().getVersion() == FhirVersionEnum.DSTU3) {
			try {
				File tmpDir = new File(System.getProperty("java.io.tmpdir"), "tmp"
						+ System.currentTimeMillis());
				tmpDir.mkdirs();
				try {
					Git r = Git.cloneRepository().setDirectory(tmpDir)
							.setURI("https://github.com/nhsconnect/CareConnect-profiles.git")
                            .setBranch("feature/stu3")
							.setProgressMonitor(new TextProgressMonitor())
                            .call();

					r.checkout(). setName("feature/stu3").call();
                    for (File directory : tmpDir.listFiles(File::isDirectory)) {
                        System.out.println(directory.getName());
					    if (!directory.getName().equals(".git")) {
					        for (File file : directory.listFiles()) {
					         //   System.out.println("-"+file.getName());
					            FileInputStream inputStream = new FileInputStream(file);
					            Reader reader = new InputStreamReader(inputStream);
					            IBaseResource resource = ctx.newXmlParser().parseResource(reader);
					            if (resource instanceof ValueSet) {
					            //    System.out.println("ValueSet");
					                valueSet.addEntry().setResource((ValueSet) resource);
                                }
                                if (resource instanceof CodeSystem) {
                                    //    System.out.println("ValueSet");
                                    valueSet.addEntry().setResource((CodeSystem) resource);
                                }
                                if (resource instanceof StructureDefinition) {
                                 //   System.out.println("StructuredDefinition");
                                    structuredDefinition.addEntry().setResource((StructureDefinition) resource);
                                }
                            }
                        }
                    }

                    File file = new File(targetServer+"/profile/profiles-resources.xml");

                    FileOutputStream profileFS = new FileOutputStream(file);
                    if (!file.exists()) {
                        file.createNewFile();
                    }
                    Writer writer = new OutputStreamWriter(profileFS);
                    ctx.newXmlParser().encodeResourceToWriter(structuredDefinition,writer);
                    profileFS.flush();
                    profileFS.close();

                    file = new File(targetServer+"/valueset/valuesets.xml");

                    profileFS = new FileOutputStream(file);
                    if (!file.exists()) {
                        file.createNewFile();
                    }
                    writer = new OutputStreamWriter(profileFS);
                    ctx.newXmlParser().encodeResourceToWriter(valueSet,writer);
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
				} finally {
					rm(tmpDir);
				}
			} catch (Exception ex) {
                System.out.println(ex.getMessage());
			}
		}


	}




}

