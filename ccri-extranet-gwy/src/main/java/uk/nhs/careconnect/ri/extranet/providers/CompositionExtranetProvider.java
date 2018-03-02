package uk.nhs.careconnect.ri.extranet.providers;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.valueset.BundleTypeEnum;
import ca.uhn.fhir.rest.annotation.*;

import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.rest.server.IResourceProvider;


import org.hl7.fhir.dstu3.model.*;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

import java.util.List;

@Component
public class CompositionExtranetProvider implements IResourceProvider {

    @Autowired
    FhirContext ctx;

    @Autowired
    IComposition compositionDao;


    public Class<? extends IBaseResource> getResourceType() {
        return Composition.class;
    }


    @Operation(name = "document", idempotent = true, bundleType= BundleTypeEnum.DOCUMENT)
    public Bundle compositionDocumentOperation(
            @IdParam IdType internalId,
            @OperationParam(name="persist") TokenParam persist
    ) {
        HttpServletRequest request =  null;

        return compositionDao.readDocument(ctx,internalId);

    }

    @Read
    public Composition getCompositionById(HttpServletRequest request, @IdParam IdType internalId) {

        Composition composition = compositionDao.read(ctx,internalId);

        return composition;
    }



    @Search
    public List<Resource> searchComposition(HttpServletRequest theRequest
            , @OptionalParam(name = Composition.SP_RES_ID) TokenParam resid
            , @OptionalParam(name = Composition.SP_PATIENT) ReferenceParam patient
     //       , @OptionalParam(name = Composition.SP_DATE) DateRangeParam date
     //       , @OptionalParam(name = Composition.SP_TYPE) TokenParam type
     //       , @OptionalParam(name = Composition.SP_CLASS) TokenParam _class
    ) {

        List<Resource> results = compositionDao.search(ctx,resid,patient);


        return results;

    }
}
