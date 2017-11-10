package uk.nhs.careconnect.ri.daointerface;

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
    public void save(MedicationRequestEntity prescription) {

    }

    @Override
    public MedicationRequest read(IdType theId) {
        return null;
    }

    @Override
    public MedicationRequest create(MedicationRequest prescription, IdType theId, String theConditional) {
        return null;
    }

    @Override
    public List<MedicationRequest> search(ReferenceParam patient, TokenParam code, DateRangeParam dateWritten, TokenParam status) {
        return null;
    }

    @Override
    public List<MedicationRequestEntity> searchEntity(ReferenceParam patient, TokenParam code, DateRangeParam dateWritten, TokenParam status) {
        return null;
    }
}
