package uk.nhs.careconnect.ri.lib.gateway.provider;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.valueset.BundleTypeEnum;
import ca.uhn.fhir.rest.annotation.*;

import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.rest.server.IResourceProvider;
import org.apache.camel.*;
import org.hl7.fhir.dstu3.model.*;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.activation.UnsupportedDataTypeException;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.*;

@Component
public class CompositionResourceProvider implements IResourceProvider {



    @Autowired
    CamelContext context;

    @Autowired
    FhirContext ctx;

    @Autowired
    ResourceTestProvider resourceTestProvider;

    private static final Logger log = LoggerFactory.getLogger(CompositionResourceProvider.class);

    private ClassLoader getContextClassLoader() {
        return Thread.currentThread().getContextClassLoader();
    }


    @Override
    public Class<Composition> getResourceType() {
        return Composition.class;
    }
/*
    @Validate
    public MethodOutcome testResource(@ResourceParam Composition resource,
                                  @Validate.Mode ValidationModeEnum theMode,
                                  @Validate.Profile String theProfile) {
        return resourceTestProvider.testResource(resource,theMode,theProfile);
    }
*/
    @Operation(name = "document", idempotent = true, bundleType= BundleTypeEnum.DOCUMENT)
    public Bundle compositionDocumentOperation(
            @OptionalParam(name = Composition.SP_RES_ID) StringParam resid,
            @OperationParam(name="persist") TokenParam persist
    ) {
        HttpServletRequest request =  null;

        Bundle bundle = null;
        bundle.setType(Bundle.BundleType.DOCUMENT);

        return bundle;
    }

    @Operation(name = "getcarerecord", idempotent = true, bundleType= BundleTypeEnum.DOCUMENT)
    public Bundle getCareRecord(
          //  @OperationParam(name="code") TokenParam code,
            @OperationParam(name="patient") ReferenceParam patient
    ) throws UnsupportedDataTypeException {

        // Build requested section
       Bundle bundle = new Bundle();

       return bundle;
    }

    @Operation(name = "getencounterrecord", idempotent = true, bundleType= BundleTypeEnum.DOCUMENT)
    public Bundle getEncounterRecord(
            //  @OperationParam(name="code") TokenParam code,
            @OperationParam(name="encounter") ReferenceParam encounter
    ) throws UnsupportedDataTypeException {

       // https://nhsconnect.github.io/ITK-FHIR-Outpatient-Letter/explore_document_profiles.html

        ClassLoader classLoader = getContextClassLoader();
        File file = new File(classLoader.getResource("sampleDocuments/sampleOutpatientLetter.xml").getFile());


        Bundle bundle = new Bundle();

        try {
            String contents = org.apache.commons.io.IOUtils.toString(new InputStreamReader(new FileInputStream(file), "UTF-8"));
            IBaseResource document = ca.uhn.fhir.rest.api.EncodingEnum.detectEncodingNoDefault(contents).newParser(ctx).parseResource(contents);


            if (document instanceof Bundle) {
                bundle = (Bundle) document;
            }
        } catch (Exception ex)  {
            log.error(ex.getMessage());
        }


        return bundle;
    }

    @Operation(name = "getepisoderecord", idempotent = true, bundleType= BundleTypeEnum.DOCUMENT)
    public Bundle getEpisodeRecord(
            //  @OperationParam(name="code") TokenParam code,
            @OperationParam(name="episode") ReferenceParam episode
    ) throws UnsupportedDataTypeException {

        ClassLoader classLoader = getContextClassLoader();
        File file = new File(classLoader.getResource("sampleDocuments/sampleDischarge.xml").getFile());


        Bundle bundle = new Bundle();

        try {
            String contents = org.apache.commons.io.IOUtils.toString(new InputStreamReader(new FileInputStream(file), "UTF-8"));
            IBaseResource document = ca.uhn.fhir.rest.api.EncodingEnum.detectEncodingNoDefault(contents).newParser(ctx).parseResource(contents);


            if (document instanceof Bundle) {
                bundle = (Bundle) document;
            }
        } catch (Exception ex)  {
            log.error(ex.getMessage());
        }


        return bundle;
    }


    @Read
    public Composition getCompositionById(HttpServletRequest theRequest, @IdParam IdType internalId) {

        ProducerTemplate template = context.createProducerTemplate();

        Composition composition = null;

        // Dummy action

        return composition;
    }

    @Search
    public List<Composition> searchComposition(HttpServletRequest theRequest
            , @OptionalParam(name = Composition.SP_RES_ID) StringParam resid
            , @OptionalParam(name = Composition.SP_PATIENT) ReferenceParam patient
            , @OptionalParam(name = Composition.SP_IDENTIFIER) TokenParam identifier
            , @OptionalParam(name = Composition.SP_PERIOD) DateRangeParam date
            , @OptionalParam(name = Composition.SP_TYPE) TokenParam type

    ) {

        List<Composition> results = new ArrayList<Composition>();


        return results;

    }




}
