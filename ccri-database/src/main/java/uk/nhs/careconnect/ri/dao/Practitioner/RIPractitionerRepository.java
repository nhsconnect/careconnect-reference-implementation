package uk.nhs.careconnect.ri.dao.Practitioner;

import ca.uhn.fhir.rest.annotation.OptionalParam;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenParam;
import org.hl7.fhir.instance.model.IdType;
import org.hl7.fhir.instance.model.Location;
import org.hl7.fhir.instance.model.Practitioner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import uk.nhs.careconnect.ri.entity.practitioner.PractitionerEntity;
import uk.nhs.careconnect.ri.entity.practitioner.PractitionerIdentifier;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.*;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

@Repository
@Transactional
public class RIPractitionerRepository implements PractitionerRepository {

    @PersistenceContext
    EntityManager em;
    @Autowired
    private PractitionerEntityToFHIRPractitionerTransformer practitionerEntityToFHIRPractitionerTransformer;

    public void save(PractitionerEntity practitioner)
    {
        em.persist(practitioner);
    }

    public Practitioner read(IdType theId) {

        PractitionerEntity practitionerEntity = (PractitionerEntity) em.find(PractitionerEntity.class,Long.parseLong(theId.getIdPart()));

        return practitionerEntity == null
                ? null
                : practitionerEntityToFHIRPractitionerTransformer.transform(practitionerEntity);

    }
    public List<Practitioner> searchPractitioner (
            @OptionalParam(name = Practitioner.SP_IDENTIFIER) TokenParam identifier,
            @OptionalParam(name = Location.SP_NAME) StringParam name
    )
    {
        List<PractitionerEntity> qryResults = null;

        CriteriaBuilder builder = em.getCriteriaBuilder();

        CriteriaQuery<PractitionerEntity> criteria = builder.createQuery(PractitionerEntity.class);
        Root<PractitionerEntity> root = criteria.from(PractitionerEntity.class);


        List<Predicate> predList = new LinkedList<Predicate>();
        List<Practitioner> results = new ArrayList<Practitioner>();

        if (identifier !=null)
        {

            Join<PractitionerEntity, PractitionerIdentifier> join = root.join("identifiers", JoinType.LEFT);

            Predicate p = builder.equal(join.get("value"),identifier.getValue());
            predList.add(p);
            // TODO predList.add(builder.equal(join.get("system"),identifier.getSystem()));

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

        for (PractitionerEntity practitionerEntity : qryResults)
        {
           // log.trace("HAPI Custom = "+doc.getId());
            Practitioner practitioner = practitionerEntityToFHIRPractitionerTransformer.transform(practitionerEntity);
            results.add(practitioner);
        }

        return results;
    }


}
