package uk.org.hl7.fhir.validation.dstu2;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.IGenericClient;
import ca.uhn.fhir.rest.client.ServerValidationModeEnum;
import ca.uhn.fhir.rest.server.exceptions.BaseServerResponseException;
import ca.uhn.fhir.rest.server.exceptions.InternalErrorException;
import org.hl7.fhir.instance.hapi.validation.DefaultProfileValidationSupport;
import org.hl7.fhir.instance.hapi.validation.IValidationSupport;
import org.hl7.fhir.instance.model.Bundle;
import org.hl7.fhir.instance.model.OperationOutcome;
import org.hl7.fhir.instance.model.ValueSet;
import org.hl7.fhir.instance.model.api.IBaseResource;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

public class CareConnectValidation implements IValidationSupport {

    private Map<String, ValueSet> myDefaultValueSets;
    private Map<String, ValueSet> myCodeSystems;

    //private FhirContext myCtx = FhirContext.forDstu2Hl7Org();

    private FhirContext myTermCtx = FhirContext.forDstu3();

    private static final org.slf4j.Logger ourLog = org.slf4j.LoggerFactory.getLogger(CareConnectValidation.class);

    private List<IValidationSupport> myChain;

    public CareConnectValidation() {
        myChain = new ArrayList<IValidationSupport>();
    }

    /**
     * Constructor
     */
    public CareConnectValidation(IValidationSupport... theValidationSupportModules) {
        this();
        for (IValidationSupport next : theValidationSupportModules) {
            System.out.println("Adding Validation Chaing to CareConnect = "+next.getClass());
            if (next != null) {
                myChain.add(next);
            }
        }
    }

    public void addValidationSupport(IValidationSupport theValidationSupport) {
        myChain.add(theValidationSupport);
    }

    @Override
    public ValueSet.ValueSetExpansionComponent expandValueSet(FhirContext theCtx, ValueSet.ConceptSetComponent theInclude) {
        System.out.println("CareConnectValidator-expandValueSet "+theInclude.getSystem());

        // TODO Need to revisit - need to ensure core codesystems and valuesets are being validated
        for (IValidationSupport next : myChain) {
            System.out.println("*** Looking for CodeSystem "+theInclude.getSystem()+" in "+next.getClass());
            ValueSet.ValueSetExpansionComponent component = next.expandValueSet(theCtx, theInclude);
            if (component != null) {

                System.out.println("Found the class");
                return component;
            }
            else {
                System.out.println("Failed to expand = " + theInclude.getSystem());
            }
        }
        return myChain.get(0).expandValueSet(theCtx, theInclude);
    }


    private void loadCodeSystems(FhirContext theContext, Map<String, ValueSet> codeSystems, String file) {
        InputStream valuesetText = DefaultProfileValidationSupport.class.getResourceAsStream(file);
        if (valuesetText != null) {
            InputStreamReader reader;
            try {
                reader = new InputStreamReader(valuesetText, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                // Shouldn't happen!
                throw new InternalErrorException("UTF-8 encoding not supported on this platform", e);
            }

            //FhirContext ctx = FhirInstanceValidator.class(theContext);
            Bundle bundle = theContext.newXmlParser().parseResource(Bundle.class, reader);
            for (Bundle.BundleEntryComponent next : bundle.getEntry()) {
                ValueSet nextValueSet = (ValueSet) next.getResource();
                String system = nextValueSet.getCodeSystem().getSystem();
                if (isNotBlank(system)) {
                    codeSystems.put(system, nextValueSet);
                }
            }
        }
    }

    @Override
    public ValueSet fetchCodeSystem(FhirContext theCtx, String theSystem) {
        System.out.println("CareConnectValidator-CodeSystem "+theSystem);

        for (IValidationSupport next : myChain) {
            System.out.println("Searching Chain: "+myChain.getClass());
            ValueSet retVal = next.fetchCodeSystem(theCtx, theSystem);
            if (retVal != null) {
                System.out.println("CareConnectValidator-CodeSystem "+theSystem);
                return retVal;
            }
        }
        if (theSystem.startsWith("https://fhir-test.hl7.org.uk/") || theSystem.startsWith("https://fhir.hl7.org.uk/")) {
            theSystem = theSystem.replace("fhir.hl7.org.uk", "fhir-test.hl7.org.uk");

            return null;

        } else {

            return null;
        }
    }

    @Override
    public <T extends IBaseResource> T fetchResource(FhirContext myCtx, Class<T> theClass, String theUri) {

        if (theUri.startsWith("https://fhir-test.hl7.org.uk/") || theUri.startsWith("https://fhir.hl7.org.uk/")) {


            if (theUri.contains("/StructureDefinition/") && !theUri.contains("/StructureDefinition/Ext")){
                System.out.println("CareConnectValidator-Resource DISABLED due to slicing issue. fetch Resource - " + theUri);

                return null;
            }

         //   System.out.println("CareConnectValidator fetch Resource-" + theUri);
            theUri = theUri.replace("fhir.hl7.org.uk", "fhir-test.hl7.org.uk");

            String resName = myCtx.getResourceDefinition(theClass).getName();
            ourLog.info("Attempting to fetch {} at URL: {}", resName, theUri);

            myCtx.getRestfulClientFactory().setServerValidationMode(ServerValidationModeEnum.NEVER);
            IGenericClient client = myCtx.newRestfulGenericClient("https://fhir-test.hl7.org.uk");

            T result = null;
            try {
                result = client.read(theClass, theUri);
            } catch (BaseServerResponseException e) {
                ourLog.error("FAILURE: Received HTTP " + e.getStatusCode() + ": " + e.getMessage());
            }
            ourLog.info("Successfully loaded resource");
            return result;

        }
        else {
           // System.out.println("Fetch Resource : "+ theUri);
            for (IValidationSupport next : myChain) {
                //System.out.println("*** Support class: "+next.getClass());
                return next.fetchResource(myCtx,theClass,theUri);
            }
            return null;
        }
    }

    @Override
    public boolean isCodeSystemSupported(FhirContext theContext, String theSystem) {

        if (theSystem.startsWith("http://hl7.org/fhir")) {
            // These classes don't validate core codes
            return false;
        }
        // TODO need to validate only care connect CodeSystems
        switch (theSystem)
        {
            case "https://fhir.hl7.org.uk/CareConnect-SDSJobRoleName-1":
            case "http://snomed.info/sct" :
            case "http://loinc.org":
                return true;
            default:
                System.out.println("Fail - CareConnectValidator-isCodeSystemSupported "+theSystem);
                return false;

        }

    }

    @Override
    public CodeValidationResult validateCode(FhirContext theContext, String theCodeSystem, String theCode, String theDisplay) {
        CodeValidationResult result = null;


        switch(theCodeSystem) {
            case "https://fhir.hl7.org.uk/CareConnect-SDSJobRoleName-1":

                System.out.println("CareConnectValidator-validateCode System="+theCodeSystem + " Code="+theCode);
                break;
            default:
                String urlString = "http://test.fhir.org/r3/CodeSystem/$lookup?system="+theCodeSystem+"&code="+theCode;
                try {

                    URL url = new URL(urlString);
                    HttpURLConnection con = (HttpURLConnection) url.openConnection();

                    con.setRequestMethod("GET");
                    int responseCode = con.getResponseCode();

                    // will fail on UK SNOMED codes
                    if ((responseCode != 200) && theCodeSystem.startsWith("http://snomed.info/sct")) {
                        // Assume UK code so check response code from snomedbrowser
                        try {
                            System.out.println("CareConnectValidator-validateCode SNOMED UK Check System="+theCodeSystem + " Code="+theCode);
                            url = new URL("http://www.snomedbrowser.com/Codes/Details/" + theCode);
                            con = (HttpURLConnection) url.openConnection();

                            con.setRequestMethod("GET");
                            responseCode = con.getResponseCode();
                        } catch (Exception ex1) {

                        }
                    }
                    if (responseCode != 200) {


                        result = new CodeValidationResult(OperationOutcome.IssueSeverity.ERROR,"Not Found "+theCodeSystem+ " code "+theCode);
                    }

                }
                catch (Exception ex)
                {

                }
                break;

        }
        return result;
    }
}
