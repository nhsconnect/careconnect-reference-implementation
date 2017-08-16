package uk.org.hl7.fhir.validation.stu3;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.IGenericClient;
import ca.uhn.fhir.rest.client.ServerValidationModeEnum;
import ca.uhn.fhir.rest.server.exceptions.BaseServerResponseException;
import org.hl7.fhir.instance.hapi.validation.IValidationSupport;
import org.hl7.fhir.instance.model.OperationOutcome;
import org.hl7.fhir.instance.model.ValueSet;
import org.hl7.fhir.instance.model.api.IBaseResource;

import java.net.HttpURLConnection;
import java.net.URL;

public class CareConnectValidation implements IValidationSupport {

    private FhirContext myCtx = FhirContext.forDstu3();

    private FhirContext myTermCtx = FhirContext.forDstu3();

    private static final org.slf4j.Logger ourLog = org.slf4j.LoggerFactory.getLogger(CareConnectValidation.class);
    @Override
    public ValueSet.ValueSetExpansionComponent expandValueSet(FhirContext theContext, ValueSet.ConceptSetComponent theInclude) {
        System.out.println("CareConnectValidator-expandValueSet "+theInclude.getSystem());
       // return null;
        return new ValueSet.ValueSetExpansionComponent();
    }

    @Override
    public ValueSet fetchCodeSystem(FhirContext theContext, String theSystem) {
        System.out.println("CareConnectValidator-CodeSystem "+theSystem);

        if (theSystem.startsWith("https://fhir-test.hl7.org.uk/") || theSystem.startsWith("https://fhir.hl7.org.uk/")) {
            theSystem = theSystem.replace("fhir.hl7.org.uk", "fhir-test.hl7.org.uk");

            return null;

        } else {

            return null;
        }
    }

    @Override
    public <T extends IBaseResource> T fetchResource(FhirContext theContext, Class<T> theClass, String theUri) {

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
            return null;
        }
    }

    @Override
    public boolean isCodeSystemSupported(FhirContext theContext, String theSystem) {

        if (theSystem.startsWith("http://hl7.org/fhir")) {
            return true;
        }
        switch (theSystem)
        {
            case "https://fhir.hl7.org.uk/CareConnect-SDSJobRoleName-1":
            case "http://snomed.info/sct" :
            case "http://loinc.org":
                return true;
            default:
                System.out.println("CareConnectValidator-isCodeSystemSupported "+theSystem);
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
