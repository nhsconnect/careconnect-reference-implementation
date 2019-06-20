package uk.nhs.careconnect.ccri.fhirserver.stu3.provider;


import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.annotation.*;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.api.ValidationModeEnum;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.Immunization;
import org.hl7.fhir.dstu3.model.ListResource;
import org.hl7.fhir.dstu3.model.OperationOutcome;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.nhs.careconnect.ccri.fhirserver.support.OperationOutcomeFactory;
import uk.nhs.careconnect.ccri.fhirserver.support.ProviderResponseLibrary;
import uk.nhs.careconnect.ri.database.daointerface.ListRepository;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Component
public class ListProvider implements ICCResourceProvider {


    @Autowired
    private ListRepository listDao;

    @Override
    public Class<? extends IBaseResource> getResourceType() {
        return ListResource.class;
    }

    @Autowired
    FhirContext ctx;
    
    @Autowired
    private ResourceTestProvider resourceTestProvider;

    @Autowired
    private ResourcePermissionProvider resourcePermissionProvider;
    
    private static final Logger log = LoggerFactory.getLogger(ListProvider.class);

    @Override
    public Long count() {
        return listDao.count();
    }

    @Update
    public MethodOutcome updateList(HttpServletRequest theRequest, @ResourceParam ListResource list, @IdParam IdType theId, @ConditionalUrlParam String theConditional, RequestDetails theRequestDetails) {

    	resourcePermissionProvider.checkPermission("update");
        MethodOutcome method = new MethodOutcome();
        method.setCreated(true);
        OperationOutcome opOutcome = new OperationOutcome();

        method.setOperationOutcome(opOutcome);

    try {
        log.info(theId.getId());
        ListResource newList = listDao.create(ctx, list, theId, theConditional);
        method.setId(newList.getIdElement());
        method.setResource(newList);

    } catch (Exception ex) {

        ProviderResponseLibrary.handleException(method,ex);
    }


        return method;
    }

    @Create
    public MethodOutcome createList(HttpServletRequest theRequest, @ResourceParam ListResource list) {

    	resourcePermissionProvider.checkPermission("create");
        MethodOutcome method = new MethodOutcome();
        method.setCreated(true);
        OperationOutcome opOutcome = new OperationOutcome();

        method.setOperationOutcome(opOutcome);
        try {
            ListResource newList = listDao.create(ctx, list,null,null);
            method.setId(newList.getIdElement());
            method.setResource(newList);
        } catch (Exception ex) {

            ProviderResponseLibrary.handleException(method,ex);
        }

        return method;
    }

   

    @Read()
    public ListResource getList(@IdParam IdType listId) {
    	resourcePermissionProvider.checkPermission("read");
        ListResource form = listDao.read(ctx,listId);

        if ( form == null) {
            throw OperationOutcomeFactory.buildOperationOutcomeException(
                    new ResourceNotFoundException("No List/ " + listId.getIdPart()),
                    OperationOutcome.IssueSeverity.ERROR, OperationOutcome.IssueType.NOTFOUND);
        }

        return form;
    }
    @Search
    public List<ListResource> searchQuestionnaire(HttpServletRequest theRequest,
                                                           @OptionalParam(name = ListResource.SP_IDENTIFIER) TokenParam identifier,
                                                           @OptionalParam(name= ListResource.SP_RES_ID) StringParam id,
                                                           @OptionalParam(name = ListResource.SP_PATIENT) ReferenceParam patient
    ) {
        return listDao.searchListResource(ctx, identifier,id,patient);
    }

    @Validate
    public MethodOutcome testResource(@ResourceParam Immunization resource,
                                  @Validate.Mode ValidationModeEnum theMode,
                                  @Validate.Profile String theProfile) {
        return resourceTestProvider.testResource(resource,theMode,theProfile);
    }
    
}
