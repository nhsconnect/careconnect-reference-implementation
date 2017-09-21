package uk.nhs.careconnect.ri.dao.Location;

import ca.uhn.fhir.rest.annotation.ConditionalUrlParam;
import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.OptionalParam;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenParam;
import org.hl7.fhir.instance.model.IdType;
import org.hl7.fhir.instance.model.Location;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import uk.nhs.careconnect.ri.entity.location.LocationEntity;
import uk.nhs.careconnect.ri.entity.location.LocationIdentifier;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.*;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

@Repository
@Transactional
public class RILocationRepository implements LocationRepository {

    @PersistenceContext
    EntityManager em;

    @Autowired
    private LocationEntityToFHIRLocationTransformer locationEntityToFHIRLocationTransformer;

    public void save(LocationEntity location)
    {
        em.persist(location);
    }

    public Location read(IdType theId) {

        LocationEntity locationEntity = (LocationEntity) em.find(LocationEntity.class,Long.parseLong(theId.getIdPart()));

        return locationEntity == null
                ? null
                : locationEntityToFHIRLocationTransformer.transform(locationEntity);

    }

    @Override
    public Location create(Location location, @IdParam IdType theId, @ConditionalUrlParam String theConditional) {


        return location;
    }


    public List<Location> searchLocation (

            @OptionalParam(name = Location.SP_IDENTIFIER) TokenParam identifier,
            @OptionalParam(name = Location.SP_NAME) StringParam name
    )
    {
         List<LocationEntity> qryResults = null;

        CriteriaBuilder builder = em.getCriteriaBuilder();

        CriteriaQuery<LocationEntity> criteria = builder.createQuery(LocationEntity.class);
        Root<LocationEntity> root = criteria.from(LocationEntity.class);

        List<Predicate> predList = new LinkedList<Predicate>();
        List<Location> results = new ArrayList<Location>();

        if (identifier !=null)
        {
            Join<LocationEntity, LocationIdentifier> join = root.join("identifiers", JoinType.LEFT);

            Predicate p = builder.equal(join.get("value"),identifier.getValue());
            predList.add(p);
            // TODO predList.add(builder.equal(join.get("system"),identifier.getSystem()));

        }
        if (name !=null)
        {

            Predicate p =
                    builder.like(
                            builder.upper(root.get("name").as(String.class)),
                            builder.upper(builder.literal("%"+name.getValue()+"%"))
                    );

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

        for (LocationEntity locationEntity : qryResults)
        {
           // log.trace("HAPI Custom = "+doc.getId());
            Location location = locationEntityToFHIRLocationTransformer.transform(locationEntity);
            results.add(location);
        }

        return results;
    }


}
