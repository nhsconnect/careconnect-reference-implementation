package uk.nhs.careconnect.ri.dao.Location;

import ca.uhn.fhir.rest.annotation.OptionalParam;
import ca.uhn.fhir.rest.param.TokenParam;
import org.hl7.fhir.instance.model.IdType;
import org.hl7.fhir.instance.model.Location;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import uk.nhs.careconnect.ri.entity.location.LocationEntity;
import uk.nhs.careconnect.ri.entity.location.LocationIdentifier;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.criteria.*;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

@Repository
@Transactional
public class RILocationRepository implements LocationRepository {

    @Autowired
    EntityManagerFactory entityManagerFactory;

    @Autowired
    private LocationEntityToFHIRLocationTransformer locationEntityToFHIRLocationTransformer;

    public void save(LocationEntity location)
    {
        EntityManager em = entityManagerFactory.createEntityManager();
        em.persist(location);
    }

    public Location read(IdType theId) {
        EntityManager em = entityManagerFactory.createEntityManager();

        LocationEntity locationEntity = (LocationEntity) em.find(LocationEntity.class,Long.parseLong(theId.getIdPart()));

        return locationEntity == null
                ? null
                : locationEntityToFHIRLocationTransformer.transform(locationEntity);

    }
    public List<Location> searchLocation (

            @OptionalParam(name = Location.SP_IDENTIFIER) TokenParam identifier
    )
    {
        EntityManager em = entityManagerFactory.createEntityManager();
        List<LocationEntity> qryResults = null;

        CriteriaBuilder builder = em.getCriteriaBuilder();

        CriteriaQuery<LocationEntity> criteria = builder.createQuery(LocationEntity.class);
        Root<LocationEntity> root = criteria.from(LocationEntity.class);
        Join<LocationEntity, LocationIdentifier> join = root.join("identifiers", JoinType.LEFT);

        List<Predicate> predList = new LinkedList<Predicate>();
        List<Location> results = new ArrayList<Location>();

        if (identifier !=null)
        {
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

        for (LocationEntity locationEntity : qryResults)
        {
           // log.trace("HAPI Custom = "+doc.getId());
            Location location = locationEntityToFHIRLocationTransformer.transform(locationEntity);
            results.add(location);
        }

        return results;
    }


}
