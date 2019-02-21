package uk.nhs.careconnect.ri.dao;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.annotation.OptionalParam;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.rest.param.UriParam;
import ca.uhn.fhir.rest.server.exceptions.ResourceVersionConflictException;
import org.hl7.fhir.dstu3.model.*;
import org.hl7.fhir.dstu3.model.codesystems.MessageheaderResponseRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Repository;
import uk.nhs.careconnect.fhir.OperationOutcomeException;
import uk.nhs.careconnect.ri.dao.transforms.MessageDefinitionEntityToFHIRMessageDefinitionTransformer;
import uk.nhs.careconnect.ri.database.daointerface.CodeSystemRepository;
import uk.nhs.careconnect.ri.database.daointerface.ConceptRepository;
import uk.nhs.careconnect.ri.database.daointerface.MessageDefinitionRepository;
import uk.nhs.careconnect.ri.database.entity.codeSystem.CodeSystemEntity;
import uk.nhs.careconnect.ri.database.entity.codeSystem.ConceptEntity;
import uk.nhs.careconnect.ri.database.entity.messageDefinition.*;
import uk.nhs.careconnect.ri.database.entity.namingSystem.NamingSystemEntity;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.*;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

@Repository
@Transactional
public class MessageDefinitionDao implements MessageDefinitionRepository {

    @PersistenceContext
    EntityManager em;

    @Autowired
    private MessageDefinitionEntityToFHIRMessageDefinitionTransformer messageDefinitionEntityToFHIRValuesetTransformer;

    private static final Logger log = LoggerFactory.getLogger(MessageDefinitionDao.class);

    @Autowired
    CodeSystemRepository codeSystemDao;

    @Autowired
    @Lazy
    ConceptRepository conceptDao;

    public void save(FhirContext ctx, MessageDefinitionEntity messageDefinition)
    {
        em.persist(messageDefinition);
    }


    MessageDefinition messageDefinition;

    @Override
    public MessageDefinition create(FhirContext ctx,  MessageDefinition messageDefinition) throws OperationOutcomeException {
        this.messageDefinition = messageDefinition;

        MessageDefinitionEntity messageDefinitionEntity = null;

        if (messageDefinition.hasId()) {
            messageDefinitionEntity = findEntity(messageDefinition.getIdElement());
        }


        List<MessageDefinitionEntity> entries = searchEntity(ctx, null, null, new UriParam().setValue(messageDefinition.getUrl()),null);
        for (MessageDefinitionEntity msg : entries) {
            if (messageDefinition.getId() == null) {
                throw new ResourceVersionConflictException("Url "+ msg.getUrl()+ " is already present on the system "+ msg.getId());
            }

            if (!msg.getId().toString().equals(messageDefinition.getIdElement().getIdPart())) {
                throw new ResourceVersionConflictException("Unique identifier "+msg.getUrl()+ " is already present on the system "+ msg.getId() + ". Supplied id = "+messageDefinition.getIdElement().getIdPart());
            }
        }


        if (messageDefinitionEntity == null)
        {
            messageDefinitionEntity = new MessageDefinitionEntity();
        }

        if (messageDefinition.hasUrl())
        {
            messageDefinitionEntity.setUrl(messageDefinition.getUrl());
        }
        if (messageDefinition.hasVersion()) {
            messageDefinitionEntity.setVersion(messageDefinition.getVersion());
        }
        if (messageDefinition.hasTitle()) {
            messageDefinitionEntity.setTitle(messageDefinition.getTitle());
            messageDefinitionEntity.setName(messageDefinition.getTitle());
        }

        if (messageDefinition.hasName() && messageDefinition.getName() != null) {
            System.out.println("**** name = "+ messageDefinition.getName());
            messageDefinitionEntity.setName(messageDefinition.getName());
        }

        if (messageDefinition.hasStatus())
        {
            messageDefinitionEntity.setStatus(messageDefinition.getStatus());
        }
        if (messageDefinition.hasExperimental()) {
            messageDefinitionEntity.setExperimental(messageDefinition.getExperimental());
        }
        if (messageDefinition.hasDate()) {
            messageDefinitionEntity.setChangedDate(messageDefinition.getDate());
        }
        if (messageDefinition.hasPublisher()) {
            messageDefinitionEntity.setPublisher(messageDefinition.getPublisher());
        }

        if (messageDefinition.hasDescription())
        {
            messageDefinitionEntity.setDescription(messageDefinition.getDescription());
        }

        if (messageDefinition.hasPurpose()) {
            messageDefinitionEntity.setPurpose(messageDefinition.getPurpose());
        }
        if (messageDefinition.hasCopyright()) {
            messageDefinitionEntity.setCopyright(messageDefinition.getCopyright());
        }

        if (messageDefinition.hasEvent()) {
            ConceptEntity event = conceptDao.findAddCode(messageDefinition.getEvent());
            if (event != null) {
                messageDefinitionEntity.setEventCode(event);
            }
        }

        if (messageDefinition.hasCategory()) {
            //System.out.println("*** has Category "+messageDefinition.getCategory());
            messageDefinitionEntity.setCategory(messageDefinition.getCategory());
        }

        if (messageDefinition.hasResponseRequired()) {
            if (messageDefinition.getResponseRequired()) {
                messageDefinitionEntity.setResponseRequired(MessageheaderResponseRequest.ALWAYS);
            } else {
                messageDefinitionEntity.setResponseRequired(MessageheaderResponseRequest.NULL);
            }
        }


        log.trace("Call em.persist MessageDefinitionEntity");
        em.persist(messageDefinitionEntity);

        for (MessageDefinitionReplaces replaces : messageDefinitionEntity.getReplaces()) {
            em.remove(replaces);
        }
        if (messageDefinition.hasReplaces()) {
            for (Reference reference : messageDefinition.getReplaces()) {
                MessageDefinitionReplaces messageDefinitionReplaces = new MessageDefinitionReplaces();
                messageDefinitionReplaces.setMessageDefinition(messageDefinitionEntity);
                MessageDefinitionEntity event = findEntity(new IdType(reference.getReference()));
                if (event== null) {
                    List<MessageDefinitionEntity> list = searchEntity(ctx,null,null, new UriParam(reference.getReference()),null);
                    if (list.size()>0) event = list.get(0);
                }
                if (event != null) {
                    messageDefinitionReplaces.setReplacesMessageDefinition(event);
                }
                em.persist(messageDefinitionReplaces);
            }

        }

        for (MessageDefinitionAllowedResponse allowedResponse: messageDefinitionEntity.getAllowedResponses()) {
            em.remove(allowedResponse);
        }
        if (messageDefinition.hasAllowedResponse()) {
            for (MessageDefinition.MessageDefinitionAllowedResponseComponent component : messageDefinition.getAllowedResponse()) {
                MessageDefinitionAllowedResponse allowedResponse = new MessageDefinitionAllowedResponse();
                allowedResponse.setMessageDefinition(messageDefinitionEntity);
                if (component.hasMessage()) {
                 //   System.out.println("******* Has a message");
                    MessageDefinitionEntity event = findEntity(new IdType(component.getMessage().getReference()));
                    if (event== null) {
                      //  System.out.println("******* searching for that message");
                        List<MessageDefinitionEntity> list = searchEntity(ctx,null,null, new UriParam(component.getMessage().getReference()),null);
                        if (list.size()>0) {
                           // System.out.println("******* found that message");
                            event = list.get(0);
                        }
                    }
                    if (event != null) {
                       log.debug("Saving allowed message with id "+event.getId());
                        allowedResponse.setResponseMessageDefinition(event);
                    }

                }
                if (component.hasSituation()) {
                    allowedResponse.setSituation(component.getSituation());
                }
                em.persist(allowedResponse);
            }

        }

        for (MessageDefinitionParent parent: messageDefinitionEntity.getParents()) {
            em.remove(parent);
        }
        if (messageDefinition.hasParent()) {
            for (Reference reference : messageDefinition.getParent()) {
                MessageDefinitionParent messageDefinitionParent = new MessageDefinitionParent();
                messageDefinitionParent.setMessageDefinition(messageDefinitionEntity);
                MessageDefinitionEntity event = findEntity(new IdType(reference.getReference()));
                if (event== null) {
                    List<MessageDefinitionEntity> list = searchEntity(ctx,null,null, new UriParam(reference.getReference()),null);
                    if (list.size()>0) event = list.get(0);
                }
                if (event != null) {
                    messageDefinitionParent.setParentMessageDefinition(event);
                }
                em.persist(messageDefinitionParent);
            }

        }

        if (messageDefinition.hasIdentifier()) {
            for (MessageDefinitionIdentifier identifier : messageDefinitionEntity.getIdentifiers()) {
                em.remove(identifier);
            }
            if (messageDefinition.hasIdentifier()) {
                Identifier identifier = messageDefinition.getIdentifier();
                MessageDefinitionIdentifier messageDefinitionIdentifier = new MessageDefinitionIdentifier();
                messageDefinitionIdentifier.setMessageDefinition(messageDefinitionEntity);
                messageDefinitionIdentifier.setSystem(codeSystemDao.findSystem(identifier.getSystem()));
                messageDefinitionIdentifier.setValue(identifier.getValue());
                em.persist(messageDefinitionIdentifier);
            }
        }


        //Created the MessageDefinition so add the sub concepts

        for (MessageDefinitionTelecom telcom : messageDefinitionEntity.getContacts()) {
            em.remove(telcom);
        }

        for (ContactDetail contact : messageDefinition.getContact()) {
            for (ContactPoint contactPoint : contact.getTelecom()) {
                MessageDefinitionTelecom telecom = new MessageDefinitionTelecom();
                telecom.setMessageDefinition(messageDefinitionEntity);
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

        for (MessageDefinitionFocus focus : messageDefinitionEntity.getFoci()) {
            em.remove(focus);
        }

        for (MessageDefinition.MessageDefinitionFocusComponent focusComponent : messageDefinition.getFocus()) {
            MessageDefinitionFocus focus = new MessageDefinitionFocus();
            focus.setMessageDefinition(messageDefinitionEntity);

            if (focusComponent.hasCode()) {
                log.info(focusComponent.getCode());

                focus.setResourceType(ResourceType.fromCode(focusComponent.getCode()));
            }
            if (focusComponent.hasMax()) {
                focus.setMaximum(focusComponent.getMax());
            }
            if (focusComponent.hasMin()) {
                focus.setMinimum(focus.getMinimum());
            }
            if (focusComponent.hasProfile()) {
                focus.setProfile(focusComponent.getProfile().getReference());
            }

            em.persist(focus);
        }



        log.debug("Called PERSIST id="+messageDefinitionEntity.getId().toString());
        messageDefinition.setId(messageDefinitionEntity.getId().toString());

        MessageDefinition newMessageDefinition = null;
        if (messageDefinitionEntity != null) {
            newMessageDefinition = messageDefinitionEntityToFHIRValuesetTransformer.transform(messageDefinitionEntity);
            String resource = ctx.newJsonParser().encodeResourceToString(newMessageDefinition);
            if (resource.length() < 10000) {
                messageDefinitionEntity.setResource(resource);
                em.persist(messageDefinitionEntity);
            }

        }
        return newMessageDefinition;
    }




    private MessageDefinitionEntity findEntity(IdType theId) {

    	System.out.println(" the messageDefinition id is " + theId.getIdPart());
        MessageDefinitionEntity messageDefinitionEntity = null;
        // Only look up if the id is numeric else need to do a search
        if (daoutils.isNumeric(theId.getIdPart())) {
            messageDefinitionEntity = em.find(MessageDefinitionEntity.class,  Long.parseLong(theId.getIdPart()));
        }

        return messageDefinitionEntity;
    }


    public MessageDefinition read(FhirContext ctx, IdType theId) {

        log.trace("Retrieving MessageDefinition = " + theId.getValue());

        MessageDefinitionEntity messageDefinitionEntity = findEntity(theId);

        if (messageDefinitionEntity == null) return null;

        MessageDefinition messageDefinition = messageDefinitionEntityToFHIRValuesetTransformer.transform(messageDefinitionEntity);

        if (messageDefinitionEntity.getResource() == null) {
            String resource = ctx.newJsonParser().encodeResourceToString(messageDefinition);
            if (resource.length() < 10000) {
                messageDefinitionEntity.setResource(resource);
                em.persist(messageDefinitionEntity);
            }
        }
        return messageDefinition;

    }

    public List<MessageDefinition> search (FhirContext ctx,
                                           @OptionalParam(name = MessageDefinition.SP_NAME) StringParam name,
                                           @OptionalParam(name = MessageDefinition.SP_PUBLISHER) StringParam publisher,
                                           @OptionalParam(name = MessageDefinition.SP_URL) UriParam url,
                                           @OptionalParam(name = MessageDefinition.SP_IDENTIFIER) TokenParam identifier
    ) {
        List<MessageDefinitionEntity> qryResults = searchEntity(ctx, name, publisher, url, identifier);
        List<MessageDefinition> results = new ArrayList<MessageDefinition>();

        for (MessageDefinitionEntity messageDefinitionEntity : qryResults)
        {
            if (messageDefinitionEntity.getResource() != null) {
                results.add((MessageDefinition) ctx.newJsonParser().parseResource(messageDefinitionEntity.getResource()));
            } else {

                MessageDefinition messageDefinition = messageDefinitionEntityToFHIRValuesetTransformer.transform(messageDefinitionEntity);
                String resource = ctx.newJsonParser().encodeResourceToString(messageDefinition);
                if (resource.length() < 10000) {
                    messageDefinitionEntity.setResource(resource);
                    em.persist(messageDefinitionEntity);
                }
                results.add(messageDefinition);
            }
        }
        return results;
    }

    public List<MessageDefinitionEntity> searchEntity (FhirContext ctx,
            @OptionalParam(name = MessageDefinition.SP_NAME) StringParam name,
            @OptionalParam(name = MessageDefinition.SP_PUBLISHER) StringParam publisher,
            @OptionalParam(name = MessageDefinition.SP_URL) UriParam url,
                                  @OptionalParam(name = MessageDefinition.SP_IDENTIFIER) TokenParam identifier
    )
    {
        List<MessageDefinitionEntity> qryResults = null;

        CriteriaBuilder builder = em.getCriteriaBuilder();

        CriteriaQuery<MessageDefinitionEntity> criteria = builder.createQuery(MessageDefinitionEntity.class);
        Root<MessageDefinitionEntity> root = criteria.from(MessageDefinitionEntity.class);
       

        List<Predicate> predList = new LinkedList<Predicate>();


        if (name !=null)
        {

            Predicate pname = builder.like(
                    builder.upper(root.get("name").as(String.class)),
                    builder.upper(builder.literal("%" + name.getValue() + "%"))
            );

            Predicate ptitle = builder.like(
                    builder.upper(root.get("title").as(String.class)),
                    builder.upper(builder.literal("%" + name.getValue() + "%"))
            );

            Predicate p = builder.or(pname, ptitle);
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
        if (identifier !=null)
        {
            Join<MessageDefinitionEntity, MessageDefinitionIdentifier> join = root.join("identifiers", JoinType.LEFT);

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

        return em.createQuery(criteria).setMaxResults(100).getResultList();

    }


}
