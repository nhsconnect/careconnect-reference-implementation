package uk.nhs.careconnect.ri.daointerface;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.annotation.OptionalParam;
import ca.uhn.fhir.rest.param.TokenParam;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.Medication;
import org.hl7.fhir.dstu3.model.MedicationRequest;
import uk.nhs.careconnect.ri.entity.medicationRequest.MedicationEntity;

import java.util.List;

public interface MedicationRepository extends BaseRepository<MedicationEntity, Medication> {

    Medication read(FhirContext ctx, IdType theId);

    List<Medication> search(FhirContext ctx
            , @OptionalParam(name = MedicationRequest.SP_CODE) TokenParam code
            ,@OptionalParam(name= MedicationRequest.SP_RES_ID) TokenParam id

    );
}
