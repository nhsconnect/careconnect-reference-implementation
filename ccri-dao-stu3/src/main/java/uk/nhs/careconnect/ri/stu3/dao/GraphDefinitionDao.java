package uk.nhs.careconnect.ri.stu3.dao;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.annotation.OptionalParam;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.UriParam;
import ca.uhn.fhir.rest.server.exceptions.ResourceVersionConflictException;
import ca.uhn.fhir.rest.server.exceptions.UnprocessableEntityException;
import org.hl7.fhir.convertors.VersionConvertor_30_40;
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
import uk.nhs.careconnect.ri.stu3.dao.transforms.GraphDefinitionEntityToFHIRR4GraphDefinitionTransformer;

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
    private GraphDefinitionEntityToFHIRGraphDefinitionTransformer graphDefinitionEntityToFHIRGraphDefinitionTransformer;

    @Autowired
    private GraphDefinitionEntityToFHIRR4GraphDefinitionTransformer graphDefinitionEntityToFHIRR4GraphDefinitionTransformer;

    private static final Logger log = LoggerFactory.getLogger(GraphDefinitionDao.class);

    @Autowired
    CodeSystemRepository codeSystemDao;

    @Autowired
    @Lazy
    ConceptRepository conceptDao;

    //org.hl7.fhir.r4.model.GraphDefinition graph;

    @Override
    public void save(FhirContext ctx, GraphDefinitionEntity graph)
    {
        em.persist(graph);
    }





    @Override
    public Long count() {
        CriteriaBuilder qb = em.getCriteriaBuilder();
        CriteriaQuery<Long> cq = qb.createQuery(Long.class);
        cq.select(qb.count(cq.from(GraphDefinitionEntity.class)));
        //cq.where(/*your stuff*/);
        return em.createQuery(cq).getSingleResult();
    }



    @Override
    public GraphDefinition create(FhirContext ctxR3,FhirContext ctxR4, GraphDefinition graphDefinition) throws OperationOutcomeException {
        VersionConvertor_30_40 convertor = new VersionConvertor_30_40();
        org.hl7.fhir.r4.model.Resource graphDefinitionR4 = convertor.convertResource( graphDefinition, true);
        if (graphDefinitionR4 instanceof org.hl7.fhir.r4.model.GraphDefinition) {
            org.hl7.fhir.r4.model.GraphDefinition graph = create(ctxR3, ctxR4,(org.hl7.fhir.r4.model.GraphDefinition) graphDefinitionR4);
            IdType id = new IdType().setValue(graph.getIdElement().getValue());
            return read(ctxR3, id);
        }
        throw new UnprocessableEntityException("Unable to process STU3 GraphDefinition");
    }



    @Override
    public GraphDefinitionEntity readEntity(FhirContext ctx, IdType theId) {
        return null;
    }

    @Transactional
    @Override
    public org.hl7.fhir.r4.model.GraphDefinition create(FhirContext ctxR3,FhirContext ctxR4,  org.hl7.fhir.r4.model.GraphDefinition graph) throws OperationOutcomeException {


        GraphDefinitionEntity graphEntity = null;

        if (graph.hasId()) {
            graphEntity = findGraphDefinitionEntity(graph.getIdElement());
        }

        List<GraphDefinitionEntity> entries = searchEntity( null, null, new UriParam().setValue(graph.getUrl()));
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

        for (org.hl7.fhir.r4.model.ContactDetail contact : graph.getContact()) {
            for (org.hl7.fhir.r4.model.ContactPoint contactPoint : contact.getTelecom()) {
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

        for (org.hl7.fhir.r4.model.GraphDefinition.GraphDefinitionLinkComponent component :graph.getLink()) {
            GraphDefinitionLink link = new GraphDefinitionLink();
            link.setGraph(graphEntity);
            buildLinks(link, component);
        }

        log.trace("GraphDefinition = "+graph.getUrl());


        log.debug("Called PERSIST id="+graphEntity.getId().toString());
        graph.setId(graphEntity.getId().toString());

        org.hl7.fhir.r4.model.GraphDefinition newGraphDefinition = null;
        if (graphEntity != null) {
            newGraphDefinition = graphDefinitionEntityToFHIRR4GraphDefinitionTransformer.transform(graphEntity);
            String resource = ctxR4.newJsonParser().encodeResourceToString(newGraphDefinition);
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

    private void buildLinks(GraphDefinitionLink link, org.hl7.fhir.r4.model.GraphDefinition.GraphDefinitionLinkComponent component) {
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

        link.setSourceId(component.getId());
        /*
        for (Extension extension : component.getExtensionsByUrl("https://fhir.mayfield-is.co.uk/extension-GraphDefinition.sourceNodeId")) {
            StringType sourceId = (StringType) extension.getValue();
            link.setSourceId(sourceId.getValue());
        }

         */

        em.persist(link);



       for (org.hl7.fhir.r4.model.GraphDefinition.GraphDefinitionLinkTargetComponent targetComponent : component.getTarget()) {
           GraphDefinitionLinkTarget target = new GraphDefinitionLinkTarget();
           target.setGraphDefinitionLink(link);

           if (targetComponent.hasType()) {
               target.setType(targetComponent.getType());
           }

           if (targetComponent.hasParams()) {
               target.setParams(targetComponent.getParams());
           }
           /* This is the same action as above but a STU3 resource will be using an extension */
           for (org.hl7.fhir.r4.model.Extension extension : targetComponent.getExtensionsByUrl("http://hl7.org/fhir/4.0/StructureDefinition/extension-GraphDefinition.link.target.params")) {
               org.hl7.fhir.r4.model.StringType param = (org.hl7.fhir.r4.model.StringType) extension.getValue();
               target.setParams(param.getValue());
           }

           target.setTargetId(null);
           for (org.hl7.fhir.r4.model.Extension extension : targetComponent.getExtensionsByUrl("https://fhir.mayfield-is.co.uk/extension-GraphDefinition.targetLinkId")) {
               org.hl7.fhir.r4.model.StringType param = (org.hl7.fhir.r4.model.StringType) extension.getValue();
               target.setTargetId(param.getValue());
           }

           if (targetComponent.hasProfile()) {
               target.setProfile(targetComponent.getProfile());
           }
           em.persist(target);

           for (org.hl7.fhir.r4.model.GraphDefinition.GraphDefinitionLinkTargetCompartmentComponent compartmentComponent : targetComponent.getCompartment()) {
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
           for (org.hl7.fhir.r4.model.GraphDefinition.GraphDefinitionLinkComponent linkComponent : targetComponent.getLink()) {
               GraphDefinitionLink sublink = new GraphDefinitionLink();
               sublink.setGraphDefinitionLinkTarget(target);
               buildLinks(sublink, linkComponent);
           }
       }


    }


    private GraphDefinitionEntity findGraphDefinitionEntity(IdType theId) {

        GraphDefinitionEntity graphEntity = null;
        // Only look up if the id is numeric else need to do a search
        if (daoutils.isNumeric(theId.getIdPart())) {
            graphEntity = em.find(GraphDefinitionEntity.class,  Long.parseLong(theId.getIdPart()));
        }
        return graphEntity;
    }

    private GraphDefinitionEntity findGraphDefinitionEntity(org.hl7.fhir.r4.model.IdType theId) {


        GraphDefinitionEntity graphEntity = null;
        // Only look up if the id is numeric else need to do a search
        if (daoutils.isNumeric(theId.getIdPart())) {
            graphEntity = em.find(GraphDefinitionEntity.class,  Long.parseLong(theId.getIdPart()));
        }

        return graphEntity;
    }

    @Override
    public org.hl7.fhir.r4.model.GraphDefinition read(FhirContext ctxR4, org.hl7.fhir.r4.model.IdType theId) {
        log.trace("Retrieving GraphDefinition = " + theId.getValue());

        GraphDefinitionEntity graphEntity = findGraphDefinitionEntity(theId);

        if (graphEntity == null) return null;

        org.hl7.fhir.r4.model.GraphDefinition graph = graphDefinitionEntityToFHIRR4GraphDefinitionTransformer.transform(graphEntity);

        if (graphEntity.getResource() == null) {
            String resource = ctxR4.newJsonParser().encodeResourceToString(graph);
            if (resource.length() < 10000) {
                graphEntity.setResource(resource);
                em.persist(graphEntity);
            }
        }
        return graph;
    }


    @Override
    public GraphDefinition read(FhirContext ctxR3, IdType theId) {

        log.trace("Retrieving GraphDefinition = " + theId.getValue());

        GraphDefinitionEntity graphEntity = findGraphDefinitionEntity(theId);

        if (graphEntity == null) return null;

        GraphDefinition graph = graphDefinitionEntityToFHIRGraphDefinitionTransformer.transform(graphEntity);

        if (graphEntity.getResource() == null) {
            String resource = ctxR3.newJsonParser().encodeResourceToString(graph);
            if (resource.length() < 10000) {
                graphEntity.setResource(resource);
                em.persist(graphEntity);
            }
        }
        return graph;

    }

    @Override
    public List<org.hl7.fhir.r4.model.GraphDefinition> searchR4(FhirContext ctxR4, StringParam name, StringParam publisher, UriParam url) {
        List<org.hl7.fhir.r4.model.GraphDefinition> results = new ArrayList<>();
        List<GraphDefinitionEntity> qryResults = searchEntity(name, publisher, url);

        for (GraphDefinitionEntity graphEntity : qryResults) {


                org.hl7.fhir.r4.model.GraphDefinition graph = graphDefinitionEntityToFHIRR4GraphDefinitionTransformer.transform(graphEntity);
                String resource = ctxR4.newJsonParser().encodeResourceToString(graph);
                if (resource.length() < 10000) {
                    graphEntity.setResource(resource);
                    em.persist(graphEntity);
                }
                results.add(graph);

        }
        return results;
    }


    @Override
    public List<GraphDefinition> search (FhirContext ctxR3,
                                  @OptionalParam(name = GraphDefinition.SP_NAME) StringParam name,
                                  @OptionalParam(name = GraphDefinition.SP_PUBLISHER) StringParam publisher,
                                  @OptionalParam(name = GraphDefinition.SP_URL) UriParam url

    ) {
        List<GraphDefinition> results = new ArrayList<>();
        List<GraphDefinitionEntity> qryResults = searchEntity( name, publisher, url);

        for (GraphDefinitionEntity graphEntity : qryResults) {


                GraphDefinition graph = graphDefinitionEntityToFHIRGraphDefinitionTransformer.transform(graphEntity);
                String resource = ctxR3.newJsonParser().encodeResourceToString(graph);
                if (resource.length() < 10000) {
                    graphEntity.setResource(resource);
                    em.persist(graphEntity);
                }
                results.add(graph);

        }
        return results;
    }

    public List<GraphDefinitionEntity> searchEntity (
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
