package uk.nhs.careconnect.ri.dao.Patient;

import ca.uhn.fhir.rest.annotation.OptionalParam;
import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenParam;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.instance.model.Patient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import uk.nhs.careconnect.ri.entity.patient.PatientEntity;
import uk.nhs.careconnect.ri.entity.patient.PatientIdentifier;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.criteria.*;
import javax.transaction.Transactional;
import java.util.LinkedList;
import java.util.List;

@Repository
@Transactional
public class PatientDao {

    @Autowired
    EntityManagerFactory entityManagerFactory;


    public void save(PatientEntity patient)
    {
        EntityManager em = entityManagerFactory.createEntityManager();
        em.persist(patient);
    }

    public Patient read(IdType theId) {
        EntityManager em = entityManagerFactory.createEntityManager();

        em.find(PatientEntity.class,Integer.parseInt(theId.getIdPart()));

        return null;
    }
    public List<Patient> searchPatient (
            @OptionalParam(name= Patient.SP_BIRTHDATE) DateRangeParam birthDate,
            @OptionalParam(name = Patient.SP_FAMILY) StringParam familyName,
            @OptionalParam(name= Patient.SP_GENDER) StringParam gender ,
            @OptionalParam(name= Patient.SP_GIVEN) StringParam givenName ,
            @OptionalParam(name = Patient.SP_IDENTIFIER) TokenParam identifier,
            @OptionalParam(name= Patient.SP_NAME) StringParam name
    )
    {
        EntityManager em = entityManagerFactory.createEntityManager();
        List<PatientEntity> qryResults = null;

        CriteriaBuilder builder = em.getCriteriaBuilder();

        CriteriaQuery<PatientEntity> criteria = builder.createQuery(PatientEntity.class);
        Root<PatientEntity> root = criteria.from(PatientEntity.class);
        Join<PatientEntity, PatientIdentifier> join = root.join("identifiers", JoinType.LEFT);

        List<Predicate> predList = new LinkedList<Predicate>();

        if (identifier !=null)
        {
            Predicate p = builder.equal(join.get("value"),identifier.getValue());
            predList.add(p);
            // predList.add(builder.equal(join.get("system"),identifier.getSystem()));

        }

        Predicate[] predArray = new Predicate[predList.size()];
        predList.toArray(predArray);
        if (predList.size()>0)
        {
            criteria.select(root).where(predArray);
        }
        else
        {
            criteria.select(root);
        }

        qryResults = em.createQuery(criteria).getResultList();

        return null;
    }


}
