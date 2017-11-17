package uk.nhs.careconnect.ri.daointerface;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.param.TokenParam;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.MedicationRequest;
import org.springframework.stereotype.Repository;
import uk.nhs.careconnect.ri.entity.medication.MedicationRequestEntity;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.util.List;

@Repository
@Transactional
public class MedicationRequestDao implements MedicationRequestRepository {

    @PersistenceContext
    EntityManager em;

    @Override
    public void save(FhirContext ctx, MedicationRequestEntity prescription) {

    }

    @Override
    public MedicationRequest read(FhirContext ctx,IdType theId) {
        return null;
    }

    @Override
    public MedicationRequest create(FhirContext ctx,MedicationRequest prescription, IdType theId, String theConditional) {
        return null;
    }

    @Override
    public List<MedicationRequest> search(FhirContext ctx,ReferenceParam patient, TokenParam code, DateRangeParam dateWritten, TokenParam status) {
        return null;
    }

    @Override
    public List<MedicationRequestEntity> searchEntity(FhirContext ctx,ReferenceParam patient, TokenParam code, DateRangeParam dateWritten, TokenParam status) {
        return null;
    }
}
