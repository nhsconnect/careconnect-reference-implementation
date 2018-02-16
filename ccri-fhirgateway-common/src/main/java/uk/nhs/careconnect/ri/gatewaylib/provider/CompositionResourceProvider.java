package uk.nhs.careconnect.ri.gatewaylib.provider;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.valueset.BundleTypeEnum;
import ca.uhn.fhir.rest.annotation.*;

import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.exceptions.InvalidRequestException;
import ca.uhn.fhir.rest.server.exceptions.UnprocessableEntityException;
import org.apache.camel.*;
import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.dstu3.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.nhs.careconnect.ri.lib.OperationOutcomeFactory;

import javax.activation.UnsupportedDataTypeException;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class CompositionResourceProvider implements IResourceProvider {



    @Autowired
    CamelContext context;

    @Autowired
    FhirContext ctx;

    private static final Logger log = LoggerFactory.getLogger(CompositionResourceProvider.class);



    @Override
    public Class<Composition> getResourceType() {
        return Composition.class;
    }


    @Operation(name = "document", idempotent = true, bundleType= BundleTypeEnum.DOCUMENT)
    public Bundle compositionDocumentOperation(
            @OptionalParam(name = Composition.SP_RES_ID) TokenParam resid,
            @OperationParam(name="persist") TokenParam persist
    ) {
        HttpServletRequest request =  null;

        Bundle bundle = null;
        bundle.setType(Bundle.BundleType.DOCUMENT);

        return bundle;
    }

    @Operation(name = "getcarerecord", idempotent = true, bundleType= BundleTypeEnum.DOCUMENT)
    public Bundle getCareRecord(
            @OperationParam(name="code") TokenParam code,
            @OperationParam(name="patient") TokenParam patient
    ) throws UnsupportedDataTypeException {

        // Build requested section
       Bundle bundle = new Bundle();

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
            , @OptionalParam(name = Composition.SP_RES_ID) TokenParam resid
            , @OptionalParam(name = Composition.SP_PATIENT) ReferenceParam patient
            , @OptionalParam(name = Composition.SP_DATE) DateRangeParam date
            , @OptionalParam(name = Composition.SP_TYPE) TokenParam type
            , @OptionalParam(name = Composition.SP_CLASS) TokenParam _class
    ) {

        List<Composition> results = new ArrayList<Composition>();


        return results;

    }




}
