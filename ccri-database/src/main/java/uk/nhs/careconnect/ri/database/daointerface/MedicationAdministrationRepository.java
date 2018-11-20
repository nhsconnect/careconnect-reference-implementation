package uk.nhs.careconnect.ri.database.daointerface;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.annotation.ConditionalUrlParam;
import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.OptionalParam;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenParam;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.MedicationAdministration;
import uk.nhs.careconnect.fhir.OperationOutcomeException;
import uk.nhs.careconnect.ri.database.entity.medicationAdministration.MedicationAdministrationEntity;

import java.util.List;

public interface MedicationAdministrationRepository extends BaseRepository<MedicationAdministrationEntity,MedicationAdministration> {
    void save(FhirContext ctx, MedicationAdministrationEntity administration) throws OperationOutcomeException;

    MedicationAdministration read(FhirContext ctx, IdType theId);

    MedicationAdministration create(FhirContext ctx, MedicationAdministration administration, @IdParam IdType theId, @ConditionalUrlParam String theConditional) throws OperationOutcomeException;


    List<MedicationAdministration> search(FhirContext ctx,

                                    @OptionalParam(name = MedicationAdministration.SP_PATIENT) ReferenceParam patient
            , @OptionalParam(name = MedicationAdministration.SP_STATUS) TokenParam status
            , @OptionalParam(name = MedicationAdministration.SP_RES_ID) StringParam id
            , @OptionalParam(name = MedicationAdministration.SP_IDENTIFIER) TokenParam identifier
            , @OptionalParam(name = MedicationAdministration.SP_CODE) TokenParam code
            , @OptionalParam(name = MedicationAdministration.SP_MEDICATION) ReferenceParam medication

    );

    List<MedicationAdministrationEntity> searchEntity(FhirContext ctx,
                                                @OptionalParam(name = MedicationAdministration.SP_PATIENT) ReferenceParam patient
            , @OptionalParam(name = MedicationAdministration.SP_STATUS) TokenParam status
            , @OptionalParam(name = MedicationAdministration.SP_RES_ID) StringParam id
            , @OptionalParam(name = MedicationAdministration.SP_IDENTIFIER) TokenParam identifier
            , @OptionalParam(name = MedicationAdministration.SP_CODE) TokenParam code
            , @OptionalParam(name = MedicationAdministration.SP_MEDICATION) ReferenceParam medication
    );
}
