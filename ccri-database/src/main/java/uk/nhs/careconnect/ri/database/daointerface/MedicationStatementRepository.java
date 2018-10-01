package uk.nhs.careconnect.ri.database.daointerface;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.annotation.ConditionalUrlParam;
import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.OptionalParam;
import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenParam;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.MedicationStatement;
import uk.nhs.careconnect.fhir.OperationOutcomeException;
import uk.nhs.careconnect.ri.database.entity.medicationStatement.MedicationStatementEntity;

import java.util.List;

public interface MedicationStatementRepository  extends BaseRepository<MedicationStatementEntity,MedicationStatement> {
    void save(FhirContext ctx,MedicationStatementEntity statement) throws OperationOutcomeException;

    MedicationStatement read(FhirContext ctx, IdType theId);

    MedicationStatement create(FhirContext ctx,MedicationStatement statement, @IdParam IdType theId, @ConditionalUrlParam String theConditional) throws OperationOutcomeException;


    List<MedicationStatement> search(FhirContext ctx,

            @OptionalParam(name = MedicationStatement.SP_PATIENT) ReferenceParam patient
            , @OptionalParam(name = MedicationStatement.SP_EFFECTIVE) DateRangeParam effectiveDate
            , @OptionalParam(name = MedicationStatement.SP_STATUS) TokenParam status
            ,@OptionalParam(name= MedicationStatement.SP_RES_ID) StringParam id
            , @OptionalParam(name = MedicationStatement.SP_IDENTIFIER) TokenParam identifier

    );

    List<MedicationStatementEntity> searchEntity(FhirContext ctx,
            @OptionalParam(name = MedicationStatement.SP_PATIENT) ReferenceParam patient
            , @OptionalParam(name = MedicationStatement.SP_EFFECTIVE) DateRangeParam effectiveDate
            , @OptionalParam(name = MedicationStatement.SP_STATUS) TokenParam status
            ,@OptionalParam(name= MedicationStatement.SP_RES_ID) StringParam id
            , @OptionalParam(name = MedicationStatement.SP_IDENTIFIER) TokenParam identifier

    );
}
