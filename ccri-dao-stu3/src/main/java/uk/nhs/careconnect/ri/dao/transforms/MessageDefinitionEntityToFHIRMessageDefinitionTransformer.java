package uk.nhs.careconnect.ri.dao.transforms;


import org.apache.commons.collections4.Transformer;
import org.hl7.fhir.dstu3.model.MessageDefinition;
import org.hl7.fhir.dstu3.model.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import uk.nhs.careconnect.ri.database.entity.messageDefinition.*;



@Component
public class MessageDefinitionEntityToFHIRMessageDefinitionTransformer implements Transformer<MessageDefinitionEntity, MessageDefinition> {

    private static final Logger log = LoggerFactory.getLogger(MessageDefinitionEntityToFHIRMessageDefinitionTransformer.class);

    @Override
    public MessageDefinition transform(final MessageDefinitionEntity messageDefinitionEntity) {
        final MessageDefinition messageDefinition = new MessageDefinition();


        messageDefinition.setId(messageDefinitionEntity.getId().toString());

        messageDefinition.setUrl(messageDefinitionEntity.getUrl());

        if (messageDefinitionEntity.getVersion() != null) {
            messageDefinition.setVersion(messageDefinitionEntity.getVersion());
        }

        messageDefinition.setName(messageDefinitionEntity.getName());

        if (messageDefinitionEntity.getTitle() != null) {
            messageDefinition.setTitle(messageDefinitionEntity.getTitle());
            if (messageDefinition.getName().contains("null")) messageDefinition.setName(messageDefinitionEntity.getTitle());
        }

        messageDefinition.setStatus(messageDefinitionEntity.getStatus());

        if (messageDefinitionEntity.getExperimental() != null) {
            messageDefinition.setExperimental(messageDefinitionEntity.getExperimental());
        }

        if (messageDefinitionEntity.getChangedDate() != null) {
            messageDefinition.setDate(messageDefinitionEntity.getChangedDate());
        }
        if (messageDefinitionEntity.getPublisher() != null) {
            messageDefinition.setPublisher(messageDefinitionEntity.getPublisher());
        }

        messageDefinition.setDescription(messageDefinitionEntity.getDescription());

        if (messageDefinitionEntity.getPurpose() != null) {
            messageDefinition.setPurpose(messageDefinitionEntity.getPurpose());
        }

        if (messageDefinitionEntity.getCopyright() != null) {
            messageDefinition.setCopyright(messageDefinitionEntity.getCopyright());
        }

        if (messageDefinitionEntity.getBaseMessageDefinition()!=null) {
            messageDefinition.setBase(new Reference("MessageDefinition/"+messageDefinitionEntity.getBaseMessageDefinition().getId()));
        }

        if (messageDefinitionEntity.getEventCode() != null) {
            messageDefinition.getEvent()
                    .setSystem(messageDefinitionEntity.getEventCode().getSystem())
                    .setCode(messageDefinitionEntity.getEventCode().getCode())
                    .setDisplay(messageDefinitionEntity.getEventCode().getDisplay());
        }

        if (messageDefinitionEntity.getCategory() != null) {
            messageDefinition.setCategory(messageDefinitionEntity.getCategory());
        }

        if (messageDefinitionEntity.getResponseRequired() != null) {
            switch (messageDefinitionEntity.getResponseRequired()) {
                case NEVER:
                    messageDefinition.setResponseRequired(false);
                    break;
                case ALWAYS:
                case ONERROR:
                case ONSUCCESS:
                    messageDefinition.setResponseRequired(true);
                    break;
            }
        }


        for (MessageDefinitionIdentifier identifier : messageDefinitionEntity.getIdentifiers()) {
            messageDefinition.getIdentifier()
                    .setSystem(identifier.getSystem().getUri())
                    .setValue(identifier.getValue());
        }

        for (MessageDefinitionTelecom telecom : messageDefinitionEntity.getContacts()) {
            messageDefinition.addContact()
                    .addTelecom()
                    .setUse(telecom.getTelecomUse())
                    .setValue(telecom.getValue())
                    .setSystem(telecom.getSystem());
        }

        for (MessageDefinitionFocus focus : messageDefinitionEntity.getFoci()) {
            MessageDefinition.MessageDefinitionFocusComponent focusComponent = messageDefinition.addFocus();

            if (focus.getMaximum() != null) {
                focusComponent.setMax(focus.getMaximum());
            }
            if (focus.getMinimum() != null) {
                focusComponent.setMin(focus.getMinimum());
            }
            if (focus.getProfile() != null) {
                focusComponent.setProfile(new Reference(focus.getProfile()));
            }
            if (focus.getResourceType() != null) {
                focusComponent.setCode(focus.getResourceType().name());
            }
        }
        //System.out.println("Checking AllowedResponse");
        for (MessageDefinitionAllowedResponse allowedResponse : messageDefinitionEntity.getAllowedResponses()) {
          //  System.out.println("AllowedResponse present");
            MessageDefinition.MessageDefinitionAllowedResponseComponent allowedComponent = messageDefinition.addAllowedResponse();
            if (allowedResponse.getSituation() != null) {
                allowedComponent.setSituation(allowedResponse.getSituation());
            }
            if (allowedResponse.getResponseMessageDefinition() != null) {
           //     System.out.println("Response present");
                allowedComponent.setMessage(new Reference("MessageDefinition/"+allowedResponse.getResponseMessageDefinition().getId()).setDisplay(allowedResponse.getResponseMessageDefinition().getUrl()));
            }
        }

        for (MessageDefinitionParent parent : messageDefinitionEntity.getParents()) {
            if (parent.getParentMessageDefinition() != null) {
                messageDefinition.addParent(new Reference("MessageDefinition/"+parent.getParentMessageDefinition().getId()).setDisplay(parent.getParentMessageDefinition().getUrl()));
            }
        }

        for (MessageDefinitionReplaces replaces : messageDefinitionEntity.getReplaces()) {
            if (replaces.getReplacesMessageDefinition() != null) {
                messageDefinition.addReplaces(new Reference("MessageDefinition/"+replaces.getReplacesMessageDefinition().getId()).setDisplay(replaces.getReplacesMessageDefinition().getUrl()));
            }
        }

        return messageDefinition;




    }

}
