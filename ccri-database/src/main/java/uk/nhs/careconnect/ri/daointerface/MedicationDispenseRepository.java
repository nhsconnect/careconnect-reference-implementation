package uk.nhs.careconnect.ri.daointerface;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.annotation.ConditionalUrlParam;
import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.OptionalParam;
import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.param.TokenParam;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.MedicationDispense;
import org.hl7.fhir.dstu3.model.MedicationRequest;
import uk.nhs.careconnect.fhir.OperationOutcomeException;
import uk.nhs.careconnect.ri.entity.medicationDispense.MedicationDispenseEntity;

import java.util.List;

public interface MedicationDispenseRepository extends BaseRepository<MedicationDispenseEntity,MedicationDispense> {
    void save(FhirContext ctx, MedicationDispenseEntity statement) throws OperationOutcomeException;

    MedicationDispense read(FhirContext ctx, IdType theId);

    MedicationDispense create(FhirContext ctx, MedicationDispense statement, @IdParam IdType theId, @ConditionalUrlParam String theConditional) throws OperationOutcomeException;


    List<MedicationDispense> search(FhirContext ctx,

                                     @OptionalParam(name = MedicationDispense.SP_PATIENT) ReferenceParam patient
            , @OptionalParam(name = MedicationDispense.SP_STATUS) TokenParam status
            , @OptionalParam(name = MedicationDispense.SP_RES_ID) TokenParam id
            , @OptionalParam(name = MedicationDispense.SP_IDENTIFIER) TokenParam identifier
            , @OptionalParam(name = MedicationDispense.SP_CODE) TokenParam code
            , @OptionalParam(name= MedicationDispense.SP_MEDICATION) ReferenceParam medication

    );

    List<MedicationDispenseEntity> searchEntity(FhirContext ctx,
                                                 @OptionalParam(name = MedicationDispense.SP_PATIENT) ReferenceParam patient
            , @OptionalParam(name = MedicationDispense.SP_STATUS) TokenParam status
            , @OptionalParam(name = MedicationDispense.SP_RES_ID) TokenParam id
            , @OptionalParam(name = MedicationDispense.SP_IDENTIFIER) TokenParam identifier
            , @OptionalParam(name = MedicationDispense.SP_CODE) TokenParam code
            , @OptionalParam(name= MedicationDispense.SP_MEDICATION) ReferenceParam medication
    );
}
