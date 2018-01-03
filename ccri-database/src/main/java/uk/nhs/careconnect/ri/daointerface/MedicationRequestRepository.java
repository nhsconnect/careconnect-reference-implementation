package uk.nhs.careconnect.ri.daointerface;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.annotation.ConditionalUrlParam;
import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.OptionalParam;
import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.param.TokenParam;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.MedicationRequest;
import org.hl7.fhir.dstu3.model.Patient;
import uk.nhs.careconnect.ri.entity.medication.MedicationRequestEntity;

import java.util.List;

public interface MedicationRequestRepository extends BaseDao<MedicationRequestEntity, MedicationRequest> {
    void save(FhirContext ctx,MedicationRequestEntity prescription);

    MedicationRequest read(FhirContext ctx, IdType theId);

    MedicationRequest create(FhirContext ctx,MedicationRequest prescription, @IdParam IdType theId, @ConditionalUrlParam String theConditional);



    List<MedicationRequest> search(FhirContext ctx,

            @OptionalParam(name = MedicationRequest.SP_PATIENT) ReferenceParam patient
            , @OptionalParam(name = MedicationRequest.SP_CODE) TokenParam code
            , @OptionalParam(name = MedicationRequest.SP_AUTHOREDON) DateRangeParam dateWritten
            , @OptionalParam(name = MedicationRequest.SP_STATUS) TokenParam status
            , @OptionalParam(name = MedicationRequest.SP_IDENTIFIER) TokenParam identifier
            ,@OptionalParam(name= MedicationRequest.SP_RES_ID) TokenParam id

    );

    List<MedicationRequestEntity> searchEntity(FhirContext ctx,
            @OptionalParam(name = MedicationRequest.SP_PATIENT) ReferenceParam patient
            , @OptionalParam(name = MedicationRequest.SP_CODE) TokenParam code
            , @OptionalParam(name = MedicationRequest.SP_AUTHOREDON) DateRangeParam dateWritten
            , @OptionalParam(name = MedicationRequest.SP_STATUS) TokenParam status
            , @OptionalParam(name = MedicationRequest.SP_IDENTIFIER) TokenParam identifier
            ,@OptionalParam(name= MedicationRequest.SP_RES_ID) TokenParam id
    );
}
