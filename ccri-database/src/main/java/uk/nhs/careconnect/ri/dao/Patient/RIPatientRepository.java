package uk.nhs.careconnect.ri.dao.Patient;

import ca.uhn.fhir.rest.annotation.OptionalParam;
import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenParam;
import org.hl7.fhir.instance.model.IdType;
import org.hl7.fhir.instance.model.Patient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import uk.nhs.careconnect.ri.entity.patient.PatientEntity;
import uk.nhs.careconnect.ri.entity.patient.PatientIdentifier;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.*;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

@Repository
@Transactional
public class RIPatientRepository implements PatientRepository {


    @PersistenceContext
    EntityManager em;

    @Autowired
    private PatientEntityToFHIRPatientTransformer patientEntityToFHIRPatientTransformer;

    @Transactional
    @Override
    public void save(PatientEntity patient)
    {
        em.persist(patient);
    }

    public Patient read(IdType theId) {

        PatientEntity patientEntity = (PatientEntity) em.find(PatientEntity.class,Long.parseLong(theId.getIdPart()));

        return patientEntity == null
                ? null
                : patientEntityToFHIRPatientTransformer.transform(patientEntity);

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
        List<PatientEntity> qryResults = null;

        CriteriaBuilder builder = em.getCriteriaBuilder();

        CriteriaQuery<PatientEntity> criteria = builder.createQuery(PatientEntity.class);
        Root<PatientEntity> root = criteria.from(PatientEntity.class);


        List<Predicate> predList = new LinkedList<Predicate>();
        List<Patient> results = new ArrayList<Patient>();

        if (identifier !=null)
        {
            Join<PatientEntity, PatientIdentifier> join = root.join("identifiers", JoinType.LEFT);

            Predicate p = builder.equal(join.get("value"),identifier.getValue());
            predList.add(p);
            // TODO predList.add(builder.equal(join.get("system"),identifier.getSystem()));

        }

        if (familyName != null)
        {

            Predicate p = builder.equal(root.get("familyName"),familyName.getValue());
            predList.add(p);
        }
        if (givenName != null)
        {
            Predicate p = builder.equal(root.get("givenName"),givenName.getValue());
            predList.add(p);
        }

        if (gender != null)
        {
            Predicate p = builder.equal(root.get("gender"),gender.getValue());
            predList.add(p);
        }

        if (name != null)
        {
            Predicate pgiven = builder.like(root.get("givenName"),"%"+name.getValue()+"%");
            Predicate pfamily = builder.like(root.get("familyName"),"%"+name.getValue()+"%");
            Predicate p = builder.or(pfamily,pgiven);
            predList.add(p);
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
        for (PatientEntity patientEntity : qryResults)
        {
            Patient patient = patientEntityToFHIRPatientTransformer.transform(patientEntity);
            results.add(patient);
        }

        return results;
    }


}
