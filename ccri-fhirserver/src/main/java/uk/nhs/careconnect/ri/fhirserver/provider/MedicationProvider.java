package uk.nhs.careconnect.ri.fhirserver.provider;


import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.annotation.*;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.rest.server.exceptions.InternalErrorException;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import org.hl7.fhir.dstu3.model.*;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.nhs.careconnect.ri.daointerface.MedicationRepository;
import uk.nhs.careconnect.ri.daointerface.MedicationRequestRepository;
import uk.nhs.careconnect.ri.lib.OperationOutcomeFactory;

import javax.servlet.http.HttpServletRequest;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

@Component
public class MedicationProvider implements ICCResourceProvider {


    @Autowired
    private MedicationRepository medicationDao;

    @Autowired
    FhirContext ctx;

    @Override
    public Long count() {
        return medicationDao.count();
    }

    @Override
    public Class<? extends IBaseResource> getResourceType() {
        return Medication.class;
    }



    @Read()
    public Medication get(@IdParam IdType prescriptionId) {

        Medication medication = medicationDao.read(ctx,prescriptionId);

        if ( medication == null) {
            throw OperationOutcomeFactory.buildOperationOutcomeException(
                    new ResourceNotFoundException("No Medication/ " + prescriptionId.getIdPart()),
                    OperationOutcome.IssueSeverity.ERROR, OperationOutcome.IssueType.NOTFOUND);
        }

        return medication;
    }

    @Search
    public List<Medication> searchMedication(HttpServletRequest httpRequest
            , @OptionalParam(name = Medication.SP_CODE) TokenParam code
            , @OptionalParam(name = Medication.SP_RES_ID) TokenParam resid
    ) {

        return medicationDao.search(ctx,code,resid);


    }



}
