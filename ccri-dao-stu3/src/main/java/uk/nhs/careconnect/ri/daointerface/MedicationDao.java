package uk.nhs.careconnect.ri.daointerface;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.param.DateParam;
import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.param.TokenParam;
import org.hl7.fhir.dstu3.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import uk.nhs.careconnect.ri.daointerface.transforms.MedicationRequestEntityToFHIRMedicationRequestTransformer;
import uk.nhs.careconnect.ri.entity.Terminology.ConceptEntity;
import uk.nhs.careconnect.ri.entity.encounter.EncounterEntity;
import uk.nhs.careconnect.ri.entity.episode.EpisodeOfCareEntity;
import uk.nhs.careconnect.ri.entity.medication.MedicationEntity;
import uk.nhs.careconnect.ri.entity.medication.MedicationRequestDosage;
import uk.nhs.careconnect.ri.entity.medication.MedicationRequestEntity;
import uk.nhs.careconnect.ri.entity.medication.MedicationRequestIdentifier;
import uk.nhs.careconnect.ri.entity.organization.OrganisationEntity;
import uk.nhs.careconnect.ri.entity.patient.PatientEntity;
import uk.nhs.careconnect.ri.entity.practitioner.PractitionerEntity;
import uk.org.hl7.fhir.core.Stu3.CareConnectExtension;
import uk.org.hl7.fhir.core.Stu3.CareConnectProfile;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TemporalType;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import javax.transaction.Transactional;
import java.net.URI;
import java.util.*;

import static uk.nhs.careconnect.ri.daointerface.daoutils.MAXROWS;

@Repository
@Transactional
public class MedicationDao implements MedicationRepository {

    @PersistenceContext
    EntityManager em;



    private static final Logger log = LoggerFactory.getLogger(MedicationDao.class);



    @Override
    public Long count() {

        CriteriaBuilder qb = em.getCriteriaBuilder();
        CriteriaQuery<Long> cq = qb.createQuery(Long.class);
        cq.select(qb.count(cq.from(MedicationEntity.class)));
        //cq.where(/*your stuff*/);
        return em.createQuery(cq).getSingleResult();
    }


    @Override
    public Medication read(FhirContext ctx, IdType theId) {
        if (daoutils.isNumeric(theId.getIdPart())) {
            MedicationEntity medicationEntity = em.find(MedicationEntity.class, Long.parseLong(theId.getIdPart()));

            if (medicationEntity == null) return null;

            Medication medication = new Medication();

            Meta meta = new Meta().addProfile(CareConnectProfile.Medication_1);

            if (medicationEntity.getUpdated() != null) {
                meta.setLastUpdated(medicationEntity.getUpdated());
            }
            else {
                if (medicationEntity.getCreated() != null) {
                    meta.setLastUpdated(medicationEntity.getCreated());
                }
            }
            medication.setMeta(meta);

            medication.setId(medicationEntity.getId().toString());
            medication.getCode()
                    .addCoding()
                        .setCode(medicationEntity.getMedicationCode().getCode())
                        .setSystem(medicationEntity.getMedicationCode().getSystem())
                        .setDisplay(medicationEntity.getMedicationCode().getDisplay());
            return medication;
        } else {
            return null;
        }
    }

    @Override
    public MedicationEntity readEntity(FhirContext ctx, IdType theId) {
        return null;
    }

    @Override
    public void save(FhirContext ctx, MedicationEntity resource) {

    }
}
