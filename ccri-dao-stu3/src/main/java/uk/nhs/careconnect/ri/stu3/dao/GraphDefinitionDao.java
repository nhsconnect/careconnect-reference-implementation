package uk.nhs.careconnect.ri.stu3.dao;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.annotation.OptionalParam;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.UriParam;
import ca.uhn.fhir.rest.server.exceptions.ResourceVersionConflictException;
import org.hl7.fhir.dstu3.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Repository;
import uk.nhs.careconnect.fhir.OperationOutcomeException;
import uk.nhs.careconnect.ri.stu3.dao.transforms.GraphDefinitionEntityToFHIRGraphDefinitionTransformer;
import uk.nhs.careconnect.ri.database.daointerface.CodeSystemRepository;
import uk.nhs.careconnect.ri.database.daointerface.ConceptRepository;
import uk.nhs.careconnect.ri.database.daointerface.GraphDefinitionRepository;
import uk.nhs.careconnect.ri.database.entity.graphDefinition.*;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.*;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

@Repository
@Transactional
public class GraphDefinitionDao implements GraphDefinitionRepository {

    @PersistenceContext
    EntityManager em;

    @Autowired
    private GraphDefinitionEntityToFHIRGraphDefinitionTransformer graphEntityToFHIRValuesetTransformer;

    private static final Logger log = LoggerFactory.getLogger(GraphDefinitionDao.class);

    @Autowired
    CodeSystemRepository codeSystemDao;

    @Autowired
    @Lazy
    ConceptRepository conceptDao;

    public void save(FhirContext ctx, GraphDefinitionEntity graph)
    {
        em.persist(graph);
    }


    GraphDefinition graph;

    @Override
    public Long count() {
        CriteriaBuilder qb = em.getCriteriaBuilder();
        CriteriaQuery<Long> cq = qb.createQuery(Long.class);
        cq.select(qb.count(cq.from(GraphDefinitionEntity.class)));
        //cq.where(/*your stuff*/);
        return em.createQuery(cq).getSingleResult();
    }
    @Override
    public GraphDefinitionEntity readEntity(FhirContext ctx, IdType theId) {
        return null;
    }

    @Transactional
    @Override
    public GraphDefinition create(FhirContext ctx,  GraphDefinition graph) throws OperationOutcomeException {
        this.graph = graph;

        GraphDefinitionEntity graphEntity = null;

        if (graph.hasId()) {
            graphEntity = findGraphDefinitionEntity(graph.getIdElement());
        }

        List<GraphDefinitionEntity> entries = searchEntity(ctx, null, null, new UriParam().setValue(graph.getUrl()));
        for (GraphDefinitionEntity msg : entries) {
            if (graph.getId() == null) {
                throw new ResourceVersionConflictException("GraphDefinition Url "+ msg.getUrl()+ " is already present on the system "+ msg.getId());
            }

            if (!msg.getId().toString().equals(graph.getIdElement().getIdPart())) {
                throw new ResourceVersionConflictException("GraphDefinition url "+msg.getUrl()+ " is already present on the system "+ msg.getId());
            }
        }

        if (graphEntity == null)
        {
            graphEntity = new GraphDefinitionEntity();
        }

        graphEntity.setResource(null);


        if (graph.hasUrl())
        {
            graphEntity.setUrl(graph.getUrl());
        }
        if (graph.hasVersion()) {
            graphEntity.setVersion(graph.getVersion());
        }
        if (graph.hasName())
        {
            graphEntity.setName(graph.getName());
        }

        if (graph.hasStatus())
        {
            graphEntity.setStatus(graph.getStatus());
        }
        if (graph.hasExperimental()) {
            graphEntity.setExperimental(graph.getExperimental());
        }
        if (graph.hasDate()) {
            graphEntity.setDateTime(graph.getDate());
        }
        if (graph.hasPublisher()) {
            graphEntity.setPublisher(graph.getPublisher());
        }

        if (graph.hasDescription())
        {
            graphEntity.setDescription(graph.getDescription());
        }

        if (graph.hasStart())
        {
            graphEntity.setStart(graph.getStart());
        }

        if (graph.hasProfile()) {
            graphEntity.setProfile(graph.getProfile());
        }


        log.trace("Call em.persist GraphDefinitionEntity");
        em.persist(graphEntity);



        //Created the GraphDefinition so add the sub concepts

        for (GraphDefinitionTelecom telcom : graphEntity.getContacts()) {
            em.remove(telcom);
        }

        for (ContactDetail contact : graph.getContact()) {
            for (ContactPoint contactPoint : contact.getTelecom()) {
                GraphDefinitionTelecom telecom = new GraphDefinitionTelecom();
                telecom.setGraphDefinition(graphEntity);
                if (contactPoint.hasSystem()) {
                    telecom.setSystem(contactPoint.getSystem());
                }
                if (contactPoint.hasValue()) {
                    telecom.setValue(contactPoint.getValue());
                }
                if (contactPoint.hasUse()) {
                    telecom.setTelecomUse(contactPoint.getUse());
                }
                em.persist(telecom);
            }
        }

        for (GraphDefinitionLink link : graphEntity.getLinks()) {
            removeLinks(link);
        }

        for (GraphDefinition.GraphDefinitionLinkComponent component :graph.getLink()) {
            GraphDefinitionLink link = new GraphDefinitionLink();
            link.setGraph(graphEntity);
            buildLinks(link, component);
        }

        log.trace("GraphDefinition = "+graph.getUrl());


        log.debug("Called PERSIST id="+graphEntity.getId().toString());
        graph.setId(graphEntity.getId().toString());

        GraphDefinition newGraphDefinition = null;
        if (graphEntity != null) {
            newGraphDefinition = graphEntityToFHIRValuesetTransformer.transform(graphEntity);
            String resource = ctx.newJsonParser().encodeResourceToString(newGraphDefinition);
            if (resource.length() < 10000) {
              // TODO  graphEntity.setResource(resource);
              // TODO   em.persist(graphEntity);
            }

        }
        return newGraphDefinition;
    }

    private void removeLinks(GraphDefinitionLink link) {
        for (GraphDefinitionLinkTarget target : link.getTargets()) {
            for (GraphDefinitionLinkTargetCompartment compartment :target.getCompartments()) {
                em.remove(compartment);
            }
            for (GraphDefinitionLink sublink :target.getLinks()) {
                removeLinks(sublink);
            }
            em.remove(target);
        }
        em.remove(link);
    }

    private void buildLinks(GraphDefinitionLink link, GraphDefinition.GraphDefinitionLinkComponent component) {
        if (component.hasPath()) {
            link.setPath(component.getPath());
        }
        if (component.hasSliceName()) {
            link.setSlice(component.getSliceName());
        }
        if (component.hasMax()) {
            link.setMaximum(component.getMax());
        }
        if (component.hasMin()) {
            link.setMinimum(component.getMin());
        }
        if (component.hasDescription()) {
            link.setDescription(component.getDescription());
        }

        link.setSourceId(null);
        for (Extension extension : component.getExtensionsByUrl("https://fhir.mayfield-is.co.uk/extension-GraphDefinition.sourceLinkId")) {
            StringType sourceId = (StringType) extension.getValue();
            link.setSourceId(sourceId.getValue());
        }

        em.persist(link);



       for (GraphDefinition.GraphDefinitionLinkTargetComponent targetComponent : component.getTarget()) {
           GraphDefinitionLinkTarget target = new GraphDefinitionLinkTarget();
           target.setGraphDefinitionLink(link);

           if (targetComponent.hasType()) {
               target.setType(targetComponent.getType());
           }
           /* R4
           if (targetComponent.hasParams)
             */

           for (Extension extension : targetComponent.getExtensionsByUrl("http://hl7.org/fhir/4.0/StructureDefinition/extension-GraphDefinition.link.target.params")) {
               StringType param = (StringType) extension.getValue();
               target.setParams(param.getValue());
           }
            target.setTargetId(null);
           for (Extension extension : targetComponent.getExtensionsByUrl("https://fhir.mayfield-is.co.uk/extension-GraphDefinition.targetLinkId")) {
               StringType param = (StringType) extension.getValue();
               target.setTargetId(param.getValue());
           }

           if (targetComponent.hasProfile()) {
               target.setProfile(targetComponent.getProfile());
           }
           em.persist(target);

           for (GraphDefinition.GraphDefinitionLinkTargetCompartmentComponent compartmentComponent : targetComponent.getCompartment()) {
               GraphDefinitionLinkTargetCompartment compartment = new GraphDefinitionLinkTargetCompartment();
               compartment.setGraphDefinitionLinkTarget(target);

               if (compartmentComponent.hasCode()) {
                   compartment.setCode(compartmentComponent.getCode());
               }
               if (compartmentComponent.hasDescription()) {
                   compartment.setDescription(compartmentComponent.getDescription());
               }
               if (compartmentComponent.hasRule()) {
                   compartment.setRule(compartmentComponent.getRule());
               }
               if (compartmentComponent.hasExpression()) {
                   compartment.setExpression(compartmentComponent.getExpression());
               }
               em.persist(compartment);

           }
           // recursion
           for (GraphDefinition.GraphDefinitionLinkComponent linkComponent : targetComponent.getLink()) {
               GraphDefinitionLink sublink = new GraphDefinitionLink();
               sublink.setGraphDefinitionLinkTarget(target);
               buildLinks(sublink, linkComponent);
           }
       }


    }


    private GraphDefinitionEntity findGraphDefinitionEntity(IdType theId) {

    	System.out.println(" the graph id is " + theId.getIdPart());
        GraphDefinitionEntity graphEntity = null;
        // Only look up if the id is numeric else need to do a search
        if (daoutils.isNumeric(theId.getIdPart())) {
            graphEntity = em.find(GraphDefinitionEntity.class,  Long.parseLong(theId.getIdPart()));
        }

        // if null try a search on strId
        /*
        if (graphEntity == null)
        {
            CriteriaBuilder builder = em.getCriteriaBuilder();

            CriteriaQuery<GraphDefinitionEntity> criteria = builder.createQuery(GraphDefinitionEntity.class);
            Root<GraphDefinitionEntity> root = criteria.from(GraphDefinitionEntity.class);
            List<Predicate> predList = new LinkedList<Predicate>();
            Predicate p = builder.equal(root.get("strId"),theId.getValue());
            predList.add(p);
            Predicate[] predArray = new Predicate[predList.size()];
            predList.toArray(predArray);
            if (predList.size()>0)
            {
                criteria.select(root).where(predArray);

                List<GraphDefinitionEntity> qryResults = em.createQuery(criteria).setMaxResults(30).getResultList();

                for (GraphDefinitionEntity cme : qryResults)
                {
                    graphEntity = cme;
                    break;
                }
            }
        }*/
        return graphEntity;
    }


    public GraphDefinition read(FhirContext ctx, IdType theId) {

        log.trace("Retrieving GraphDefinition = " + theId.getValue());

        GraphDefinitionEntity graphEntity = findGraphDefinitionEntity(theId);

        if (graphEntity == null) return null;

        GraphDefinition graph = graphEntityToFHIRValuesetTransformer.transform(graphEntity);

        if (graphEntity.getResource() == null) {
            String resource = ctx.newJsonParser().encodeResourceToString(graph);
            if (resource.length() < 10000) {
                graphEntity.setResource(resource);
                em.persist(graphEntity);
            }
        }
        return graph;

    }

    public List<GraphDefinition> search (FhirContext ctx,
                                  @OptionalParam(name = GraphDefinition.SP_NAME) StringParam name,
                                  @OptionalParam(name = GraphDefinition.SP_PUBLISHER) StringParam publisher,
                                  @OptionalParam(name = GraphDefinition.SP_URL) UriParam url

    ) {
        List<GraphDefinition> results = new ArrayList<>();
        List<GraphDefinitionEntity> qryResults = searchEntity(ctx, name, publisher, url);

        for (GraphDefinitionEntity graphEntity : qryResults) {
            if (graphEntity.getResource() != null) {
                results.add((GraphDefinition) ctx.newJsonParser().parseResource(graphEntity.getResource()));
            } else {

                GraphDefinition graph = graphEntityToFHIRValuesetTransformer.transform(graphEntity);
                String resource = ctx.newJsonParser().encodeResourceToString(graph);
                if (resource.length() < 10000) {
                    graphEntity.setResource(resource);
                    em.persist(graphEntity);
                }
                results.add(graph);
            }
        }
        return results;
    }

    public List<GraphDefinitionEntity> searchEntity (FhirContext ctx,
                                                     @OptionalParam(name = GraphDefinition.SP_NAME) StringParam name,
                                                     @OptionalParam(name = GraphDefinition.SP_PUBLISHER) StringParam publisher,
                                                     @OptionalParam(name = GraphDefinition.SP_URL) UriParam url
    )
    {


        CriteriaBuilder builder = em.getCriteriaBuilder();

        CriteriaQuery<GraphDefinitionEntity> criteria = builder.createQuery(GraphDefinitionEntity.class);
        Root<GraphDefinitionEntity> root = criteria.from(GraphDefinitionEntity.class);
       

        List<Predicate> predList = new LinkedList<Predicate>();


        if (name !=null)
        {

            Predicate p =
                    builder.like(
                            builder.upper(root.get("name").as(String.class)),
                            builder.upper(builder.literal("%" + name.getValue() + "%"))
                    );

            predList.add(p);
        }
        if (publisher !=null)
        {

            Predicate p =
                    builder.like(
                            builder.upper(root.get("publisher").as(String.class)),
                            builder.upper(builder.literal( publisher.getValue() + "%"))
                    );

            predList.add(p);
        }
        if (url !=null)
        {

            Predicate p =
                    builder.like(
                            builder.upper(root.get("url").as(String.class)),
                            builder.upper(builder.literal( url.getValue()))
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

        return em.createQuery(criteria).setMaxResults(100).getResultList();


    }


}
