package uk.nhs.careconnect.ri.fhirserver.provider;


import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.annotation.*;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.DateParam;
import ca.uhn.fhir.rest.param.TokenOrListParam;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.OperationOutcome;
import org.hl7.fhir.dstu3.model.Slot;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.nhs.careconnect.ri.database.daointerface.SlotRepository;
import uk.nhs.careconnect.ri.lib.server.OperationOutcomeFactory;
import uk.nhs.careconnect.ri.lib.server.ProviderResponseLibrary;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Component
public class SlotProvider implements ICCResourceProvider {


    @Autowired
    private SlotRepository slotDao;

    @Override
    public Class<? extends IBaseResource> getResourceType() {
        return Slot.class;
    }

    @Autowired
    FhirContext ctx;

    private static final Logger log = LoggerFactory.getLogger(PatientProvider.class);

    @Override
    public Long count() {
        return slotDao.count();
    }

    @Update
    public MethodOutcome updateSchedule(HttpServletRequest theRequest, @ResourceParam Slot slot, @IdParam IdType theId, @ConditionalUrlParam String theConditional, RequestDetails theRequestDetails) {


        MethodOutcome method = new MethodOutcome();
        method.setCreated(true);
        OperationOutcome opOutcome = new OperationOutcome();

        method.setOperationOutcome(opOutcome);

        try {
            Slot existingSchedule = slotDao.create(ctx, slot, theId, theConditional);
            method.setId(existingSchedule.getIdElement());
            method.setResource(existingSchedule);
        } catch (Exception ex) {

            ProviderResponseLibrary.handleException(method,ex);
        }


        return method;
    }

    @Create
    public MethodOutcome createSlot(HttpServletRequest theRequest, @ResourceParam Slot slot) {


        MethodOutcome method = new MethodOutcome();
        method.setCreated(true);
        OperationOutcome opOutcome = new OperationOutcome();

        method.setOperationOutcome(opOutcome);

        try {
            Slot newSlot = slotDao.create(ctx, slot,null,null);
            method.setId(newSlot.getIdElement());
            method.setResource(newSlot);
        } catch (Exception ex) {

            ProviderResponseLibrary.handleException(method,ex);
        }


        return method;
    }

/*
    @Search
    public List<Slot> searchSlot(HttpServletRequest theRequest,
                                                           @OptionalParam(name = Slot.SP_IDENTIFIER) TokenParam identifier,
                                                           //@OptionalParam(name = Slot.SP_ACTOR) StringParam name,
                                                           @OptionalParam(name= Slot.SP_START) StringParam start
                                                           //@OptionalParam(name = Slot.SP_STATUS) StringParam status
    ) {
        return slotDao.searchSlot(ctx, identifier,start);
    }
*/

    @Read()
    public Slot getSlot(@IdParam IdType slotId) {

        Slot slot = slotDao.read(ctx,slotId);

        if ( slot == null) {
            throw OperationOutcomeFactory.buildOperationOutcomeException(
                    new ResourceNotFoundException("No Schedule/ " + slotId.getIdPart()),
                    OperationOutcome.IssueSeverity.ERROR, OperationOutcome.IssueType.NOTFOUND);
        }

        return slot;
    }


}
