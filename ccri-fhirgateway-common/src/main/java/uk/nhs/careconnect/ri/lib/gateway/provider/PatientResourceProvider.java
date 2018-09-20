package uk.nhs.careconnect.ri.lib.gateway.provider;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.api.Include;
import ca.uhn.fhir.model.valueset.BundleTypeEnum;
import ca.uhn.fhir.rest.annotation.*;
import ca.uhn.fhir.rest.param.DateParam;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenOrListParam;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.exceptions.InternalErrorException;
import org.apache.camel.*;
import org.hl7.fhir.dstu3.model.*;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.nhs.careconnect.ri.lib.server.ProviderResponseLibrary;

import javax.servlet.http.HttpServletRequest;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Component
public class PatientResourceProvider implements IResourceProvider {

    @Autowired
    CamelContext context;

    @Autowired
    FhirContext ctx;

    @Autowired
    PractitionerResourceProvider practitionerProvider;

    @Autowired
    OrganisationResourceProvider organistionProvider;

    @Autowired
    ConditionResourceProvider conditionResourceProvider;

    @Autowired
    ProcedureResourceProvider procedureResourceProvider;

    @Autowired
    ObservationResourceProvider observationResourceProvider;

    @Autowired
    AllergyIntoleranceResourceProvider allergyIntoleranceResourceProvider;

    @Autowired
    EncounterResourceProvider encounterResourceProvider;

    @Autowired
    ImmunizationResourceProvider immunizationResourceProvider;

    @Autowired
    MedicationRequestResourceProvider medicationRequestResourceProvider;

    @Autowired
    MedicationStatementResourceProvider medicationStatementResourceProvider;

    @Autowired
    LocationResourceProvider locationProvider;

    @Autowired
    ResourceTestProvider resourceTestProvider;


    private static final Logger log = LoggerFactory.getLogger(PatientResourceProvider.class);

    @Override
    public Class<Patient> getResourceType() {
        return Patient.class;
    }

    @Operation(name = "$getrecord3", idempotent = true, bundleType= BundleTypeEnum.COLLECTION)
    public Parameters getGetRecord3(
            @OperationParam(name="patientNHSnumber") TokenParam
                    nhsNumber,
            @OperationParam(name="recordType") TokenParam
                    recordType,
            @OperationParam(name="recordSection") TokenOrListParam
                    recordSection
    ) throws Exception {


        ProducerTemplate template = context.createProducerTemplate();

        String queryString = "";
        for (TokenParam token : recordSection.getValuesAsQueryTokens()) {
            if (token.getValue().equals("all")) {
                TokenOrListParam newList = new TokenOrListParam();
                newList.add(new TokenParam().setValue("consent"));
                newList.add(new TokenParam().setValue("prognosis"));
                newList.add(new TokenParam().setValue("preferences"));
                newList.add(new TokenParam().setValue("advancedtreatmentpreferences"));
                recordSection = newList;
                queryString = "consent,prognosis,preferences,advancedtreatmentpreferences";
            } else {
                if (queryString.equals("")) {
                    queryString = token.getValue();
                } else {
                    queryString = queryString+","+token.getValue();
                }
            }

        }

        String recordQuery = queryString;

        InputStream inputStream = null;
        // https://purple.testlab.nhs.uk/careconnect-ri/STU3/Encounter/804/$document?_count=50
        Exchange exchange = template.send("direct:FHIRPatientOperation",ExchangePattern.InOut, new Processor() {
            public void process(Exchange exchange) throws Exception {
                exchange.getIn().setHeader(Exchange.HTTP_QUERY, "patientNHSnumber="+nhsNumber.getValue()+"&recordType="+recordType.getValue()+"&recordSection="+recordQuery);
                exchange.getIn().setHeader(Exchange.CONTENT_TYPE, "application/fhir+xml");
                exchange.getIn().setHeader(Exchange.HTTP_METHOD, "GET");
                exchange.getIn().setHeader(Exchange.HTTP_PATH, "Patient/$getrecord3");
            }
        });
        inputStream = (InputStream) exchange.getIn().getBody();

        Bundle bundle = null;

        IBaseResource resource = null;
        try {
            String contents = org.apache.commons.io.IOUtils.toString(inputStream);
            resource = ca.uhn.fhir.rest.api.EncodingEnum.detectEncodingNoDefault(contents).newParser(ctx).parseResource(contents);
        } catch(Exception ex) {
            log.error("XML Parse failed " + ex.getMessage());
            throw new InternalErrorException(ex.getMessage());
        }
        if (resource instanceof Parameters) {
            Parameters param = (Parameters) resource;
          /*  for (Bundle.BundleEntryComponent entry : bundle.getEntry()) {
                entry.getResource().setId(entry.getFullUrl().replace("urn:uuid:",""));
            } */

            return param;

        } else {
            ProviderResponseLibrary.createException(ctx,resource);
        }

        return null;
    }

    @Operation(name = "$getrecord4", idempotent = true, bundleType= BundleTypeEnum.DOCUMENT)
    public Parameters getCareRecord4(
            @OperationParam(name="patientNHSnumber") TokenParam
                    nhsNumber,
            @OperationParam(name="recordType") TokenParam
                    recordType,
            @OperationParam(name="recordSection") TokenOrListParam
                    recordSection
    ) throws Exception {
        ProducerTemplate template = context.createProducerTemplate();
        String queryString = "";
        for (TokenParam token : recordSection.getValuesAsQueryTokens()) {
            if (token.getValue().equals("all")) {
                TokenOrListParam newList = new TokenOrListParam();
                newList.add(new TokenParam().setValue("consent"));
                newList.add(new TokenParam().setValue("prognosis"));
                newList.add(new TokenParam().setValue("preferences"));
                newList.add(new TokenParam().setValue("advancedtreatmentpreferences"));
                recordSection = newList;
                queryString = "consent,prognosis,preferences,advancedtreatmentpreferences";
            } else {
                if (queryString.equals("")) {
                    queryString = token.getValue();
                } else {
                    queryString = queryString+","+token.getValue();
                }
            }

        }

        String recordQuery = queryString;

        InputStream inputStream = null;
        // https://purple.testlab.nhs.uk/careconnect-ri/STU3/Encounter/804/$document?_count=50
        Exchange exchange = template.send("direct:FHIRPatientOperation",ExchangePattern.InOut, new Processor() {
            public void process(Exchange exchange) throws Exception {
                exchange.getIn().setHeader(Exchange.HTTP_QUERY, "patientNHSnumber="+nhsNumber.getValue()+"&recordType="+recordType.getValue()+"&recordSection="+recordQuery);
                exchange.getIn().setHeader(Exchange.CONTENT_TYPE, "application/fhir+xml");
                exchange.getIn().setHeader(Exchange.HTTP_METHOD, "GET");
                exchange.getIn().setHeader(Exchange.HTTP_PATH, "Patient/$getrecord4");
            }
        });
        inputStream = (InputStream) exchange.getIn().getBody();

        Bundle bundle = null;

        IBaseResource resource = null;
        try {
            String contents = org.apache.commons.io.IOUtils.toString(inputStream);
            resource = ca.uhn.fhir.rest.api.EncodingEnum.detectEncodingNoDefault(contents).newParser(ctx).parseResource(contents);
        } catch(Exception ex) {
            log.error("XML Parse failed " + ex.getMessage());
            throw new InternalErrorException(ex.getMessage());
        }
        if (resource instanceof Parameters) {
            Parameters parameters = (Parameters) resource;


            return parameters;

        } else {
            ProviderResponseLibrary.createException(ctx,resource);
        }

        return null;

    }


    @Read
    public Patient getPatientById(HttpServletRequest request, @IdParam IdType internalId) throws Exception {

        ProducerTemplate template = context.createProducerTemplate();

        Patient patient = null;
        IBaseResource resource = null;
         try {
            InputStream inputStream = null;
            if (request != null) {
                inputStream = (InputStream) template.sendBody("direct:FHIRPatient",
                        ExchangePattern.InOut, request);
            } else {
                Exchange exchange = template.send("direct:FHIRPatient",ExchangePattern.InOut, new Processor() {
                    public void process(Exchange exchange) throws Exception {
                        exchange.getIn().setHeader(Exchange.HTTP_QUERY, null);
                        exchange.getIn().setHeader(Exchange.HTTP_METHOD, "GET");
                        exchange.getIn().setHeader(Exchange.HTTP_PATH, "/"+internalId.getValue());
                    }
                });
                inputStream = (InputStream) exchange.getIn().getBody();
            }
            Reader reader = new InputStreamReader(inputStream);
            resource = ctx.newJsonParser().parseResource(reader);
        } catch(Exception ex) {
            log.error("JSON Parse failed " + ex.getMessage());
            throw new InternalErrorException(ex.getMessage());
        }
        if (resource instanceof Patient) {
             patient = (Patient) resource;
        } else {
            ProviderResponseLibrary.createException(ctx,resource);
        }

        return patient;
    }



    @Search
    public List<Resource> searchPatient(HttpServletRequest request,

                                       @OptionalParam(name= Patient.SP_ADDRESS_POSTALCODE) StringParam addressPostcode,
                                       @OptionalParam(name= Patient.SP_BIRTHDATE) DateParam birthDate,
                                       @OptionalParam(name= Patient.SP_EMAIL) StringParam email,
                                       @OptionalParam(name = Patient.SP_FAMILY) StringParam familyName,
                                       @OptionalParam(name= Patient.SP_GENDER) StringParam gender ,
                                       @OptionalParam(name= Patient.SP_GIVEN) StringParam givenName ,
                                       @OptionalParam(name = Patient.SP_IDENTIFIER) TokenParam identifier,
                                       @OptionalParam(name= Patient.SP_NAME) StringParam name,
                                       @OptionalParam(name= Patient.SP_PHONE) StringParam phone
                                        , @OptionalParam(name = Patient.SP_RES_ID) StringParam resid
                                     //   ,@IncludeParam(reverse=true, allow = {"*"}) Set<Include> reverseIncludes
                                     ,@IncludeParam(allow= {
                                        "Patient:general-practitioner"
                                        ,"Patient:organization"
                                        , "*"}) Set<Include> includes
                                       ) throws Exception {

        List<Resource> results = new ArrayList<>();

        ProducerTemplate template = context.createProducerTemplate();

        InputStream inputStream = (InputStream) template.sendBody("direct:FHIRPatient",
                ExchangePattern.InOut,request);

        Bundle bundle = null;

        Reader reader = new InputStreamReader(inputStream);
        IBaseResource resource = null;
        try {
            resource = ctx.newJsonParser().parseResource(reader);
        } catch(Exception ex) {
                log.error("JSON Parse failed " + ex.getMessage());
                throw new InternalErrorException(ex.getMessage());
        }
        if (resource instanceof Bundle) {
            bundle = (Bundle) resource;
            log.trace("Found Entries = " + bundle.getEntry().size());
            for (Bundle.BundleEntryComponent entry : bundle.getEntry()) {
                //Patient patient = (Patient) ;
                results.add(entry.getResource());
            }
        } else {
            ProviderResponseLibrary.createException(ctx,resource);
        }
        return results;

    }

}
