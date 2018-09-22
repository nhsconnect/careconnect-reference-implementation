package uk.nhs.careconnect.ri.database.daointerface;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.annotation.ConditionalUrlParam;
import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.OptionalParam;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenParam;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.Medication;
import org.hl7.fhir.dstu3.model.MedicationDispense;
import org.hl7.fhir.dstu3.model.MedicationRequest;
import uk.nhs.careconnect.fhir.OperationOutcomeException;
import uk.nhs.careconnect.ri.database.entity.medicationRequest.MedicationEntity;

import java.util.List;

public interface MedicationRepository extends BaseRepository<MedicationEntity, Medication> {

    Medication read(FhirContext ctx, IdType theId);

    MedicationEntity readEntity(FhirContext ctx, IdType theId);

    Medication create(FhirContext ctx, Medication medication, @IdParam IdType theId, @ConditionalUrlParam String theConditional) throws OperationOutcomeException;

    MedicationEntity createEntity(FhirContext ctx, Medication medication, @IdParam IdType theId, @ConditionalUrlParam String theConditional) throws OperationOutcomeException;

    List<Medication> search(FhirContext ctx
            , @OptionalParam(name = Medication.SP_CODE) TokenParam code
            ,@OptionalParam(name= Medication.SP_RES_ID) StringParam id

    );

    List<MedicationEntity> searchEntity(FhirContext ctx
            , @OptionalParam(name = Medication.SP_CODE) TokenParam code
            ,@OptionalParam(name= Medication.SP_RES_ID) StringParam id

    );
}
