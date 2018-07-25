package uk.nhs.careconnect.ri.daointerface;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.param.TokenParam;
import org.hl7.fhir.dstu3.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import uk.nhs.careconnect.ri.daointerface.transforms.MedicationRequestEntityToFHIRMedicationTransformer;
import uk.nhs.careconnect.ri.entity.medicationRequest.MedicationEntity;
import uk.nhs.careconnect.ri.entity.medicationRequest.MedicationRequestEntity;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.*;
import javax.transaction.Transactional;
import java.util.*;

@Repository
@Transactional
public class MedicationDao implements MedicationRepository {

    @PersistenceContext
    EntityManager em;

    @Autowired
    MedicationRequestRepository prescriptionDao;

    @Autowired
    MedicationRequestEntityToFHIRMedicationTransformer medicationRequestEntityToFHIRMedicationTransformer;

    private static final Logger log = LoggerFactory.getLogger(MedicationDao.class);



    @Override
    public Long count() {

        CriteriaBuilder qb = em.getCriteriaBuilder();
        CriteriaQuery<Long> cq = qb.createQuery(Long.class);
        // TODO This is going to MedicationRequest
        cq.select(qb.count(cq.from(MedicationRequestEntity.class)));
        //cq.where(/*your stuff*/);
        return em.createQuery(cq).getSingleResult();
    }

    @Override
    public List<Medication> search(FhirContext ctx, TokenParam code, TokenParam id) {
        List<MedicationRequestEntity> results = prescriptionDao.searchEntity(ctx,null,code,null,null,null,id,null);
        List<Medication> res = new ArrayList<>();

        for (MedicationRequestEntity medicationRequest :results) {
            res.add(medicationRequestEntityToFHIRMedicationTransformer.transform(medicationRequest));
        }
        return res;
    }



    @Override
    public Medication read(FhirContext ctx, IdType theId) {
        Medication medication = null;
        // Uses the MedicationRequest table.
        if (daoutils.isNumeric(theId.getIdPart())) {
            MedicationRequestEntity medicationRequest = prescriptionDao.readEntity(ctx, theId);
            if (medicationRequest != null) {
                medication = medicationRequestEntityToFHIRMedicationTransformer.transform(medicationRequest);
            }
        }
        return medication;
    }


            /* 28/2/2018 KGM Disable for now future use

            MedicationEntity medicationEntity = em.find(MedicationEntity.class, Long.parseLong(theId.getIdPart()));

            if (medicationEntity == null) return null;

            Medication medicationRequest = new Medication();

            Meta meta = new Meta().addProfile(CareConnectProfile.Medication_1);

            if (medicationEntity.getUpdated() != null) {
                meta.setLastUpdated(medicationEntity.getUpdated());
            }
            else {
                if (medicationEntity.getCreated() != null) {
                    meta.setLastUpdated(medicationEntity.getCreated());
                }
            }
            medicationRequest.setMeta(meta);

            medicationRequest.setId(medicationEntity.getId().toString());
            medicationRequest.getCode()
                    .addCoding()
                        .setCode(medicationEntity.getMedicationCode().getCode())
                        .setSystem(medicationEntity.getMedicationCode().getSystem())
                        .setDisplay(medicationEntity.getMedicationCode().getDisplay());
            return medicationRequest;
            */


    @Override
    public MedicationEntity readEntity(FhirContext ctx, IdType theId) {
        return null;
    }

    @Override
    public void save(FhirContext ctx, MedicationEntity resource) {

    }
}
