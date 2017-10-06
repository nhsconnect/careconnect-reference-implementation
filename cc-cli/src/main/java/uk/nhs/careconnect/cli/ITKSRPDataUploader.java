package uk.nhs.careconnect.cli;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.FhirVersionEnum;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.dstu3.model.ValueSet;
import org.hl7.fhir.instance.model.api.IIdType;
import uk.nhs.careconnect.itksrp.Vocabulary;
import uk.nhs.careconnect.itksrp.VocabularyToFHIRValueSet;
import uk.nhs.careconnect.itksrp.index.VocabularyIndex;
import uk.nhs.careconnect.itksrp.index.vocabulary;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

public class ITKSRPDataUploader extends BaseCommand {

	private static final org.slf4j.Logger ourLog = org.slf4j.LoggerFactory.getLogger(ITKSRPDataUploader.class);

	private ArrayList<IIdType> myExcludes = new ArrayList<>();

	private ArrayList<ValueSet> valueSets = new ArrayList<>();

    private FhirContext ctx;

	@Override
	public String getCommandDescription() {
		return "Uploads the conformance resources (StructureDefinition and ValueSet) from the official FHIR definitions.";
	}

	@Override
	public String getCommandName() {
		return "upload-itksrp";
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

        opt = new Option("d", "data", true, "Local folder containing ITK SRP download to use to upload");
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

		callLoadFolders(theCommandLine.getOptionValue("d") +"/Vocabulary/HL7v2/","HL7v2.XML","itk-hl7v2-");
        callLoadFolders(theCommandLine.getOptionValue("d") +"/Vocabulary/HL7v3/","HL7v3.XML","itk-hl7v3-");
        callLoadFolders(theCommandLine.getOptionValue("d")+"/Vocabulary/SNOMEDCT/","SNOMEDCT.XML","itk-snct-");

		if (ctx.getVersion().getVersion() == FhirVersionEnum.DSTU2_HL7ORG) {
			uploadValueSetsStu3(targetServer);
		}

	}
	private void callLoadFolders(String path, String indexFile, String prefix) {
        try {
            File index = new File(path+indexFile);
            FileInputStream fis = new FileInputStream(index);
            JAXBContext jc = JAXBContext.newInstance(VocabularyIndex.class);
            Unmarshaller unmarshaller = jc.createUnmarshaller();
            VocabularyIndex vocabIndex = (VocabularyIndex) unmarshaller.unmarshal(fis);

          //  System.out.println(vocabIndex.getVocabularyName());
            File folder = new File(path+"XML");
            VocabularyToFHIRValueSet converter = new VocabularyToFHIRValueSet(ctx);

            for (final File fileEntry : folder.listFiles()) {
                if (!fileEntry.isDirectory()) {

                    System.out.println(fileEntry.getName());
                    try {

                        FileInputStream fisVocab = new FileInputStream(fileEntry);
                        JAXBContext jcvocab = JAXBContext.newInstance(Vocabulary.class);
                        Unmarshaller unmarshallerVocab = jcvocab.createUnmarshaller();
                        Vocabulary vocab = (Vocabulary) unmarshallerVocab.unmarshal(fisVocab);

                        String name = null;
                        for (vocabulary voc : vocabIndex.getVocabulary()) {
                            if (voc.getId().equals(vocab.getId())) {
                                name = voc.getName()+"-"+voc.getVersion().replace(".","-");
                                name = name.replace(" ","");
                                name = StringUtils.lowerCase(name).replace("/","-");
                                name = name.replace("'","");

                                break;
                            }
                        }
                        System.out.println(name);
                        if ((vocab.getStatus().contains("active") || vocab.getStatus().contains("created") ) && name!=null) {

                            ValueSet valueSet = converter.process(vocab, name, prefix);
                            if (valueSet !=null) {
                                valueSets.add(valueSet);
                            }
                        }
                    } catch (Exception ex) {
                        System.out.println(ex.getMessage());
                    }

                }
            }
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }

	}

	private void uploadValueSetsStu3(String targetServer) throws CommandFailureException {
		IGenericClient client = newClient(ctx, targetServer);
		ourLog.info("Uploading ValueSets to server: " + targetServer);

		long start = System.currentTimeMillis();

		String vsContents;


		int count = 1;
		/*
		for (ValueSet next : valueSets) {
		    System.out.println(next.getCodeSystem().getSystem());
            client.update().resource(next).execute();

			count++;
		}
		*/

		try {
			vsContents = IOUtils.toString(ITKSRPDataUploader.class.getResourceAsStream("/org/hl7/fhir/instance/model/valueset/" + "v3-codesystems.xml"), "UTF-8");
		} catch (IOException e) {
			throw new CommandFailureException(e.toString());
		}

		ourLog.info("Finished uploading ValueSets");

	}

}
