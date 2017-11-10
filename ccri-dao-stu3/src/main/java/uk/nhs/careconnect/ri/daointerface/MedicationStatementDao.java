package uk.nhs.careconnect.ri.daointerface;

import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.param.TokenParam;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.MedicationStatement;
import org.springframework.stereotype.Repository;
import uk.nhs.careconnect.ri.entity.medication.MedicationStatementEntity;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.util.List;

@Repository
@Transactional
public class MedicationStatementDao implements MedicationStatementRepository {

    @PersistenceContext
    EntityManager em;

    @Override
    public void save(MedicationStatementEntity statement) {

    }

    @Override
    public MedicationStatement read(IdType theId) {
        return null;
    }

    @Override
    public MedicationStatement create(MedicationStatement statement, IdType theId, String theConditional) {
        return null;
    }

    @Override
    public List<MedicationStatement> search(ReferenceParam patient, DateRangeParam effectiveDate, TokenParam status) {
        return null;
    }

    @Override
    public List<MedicationStatementEntity> searchEntity(ReferenceParam patient, DateRangeParam effectiveDate, TokenParam status) {
        return null;
    }
}
