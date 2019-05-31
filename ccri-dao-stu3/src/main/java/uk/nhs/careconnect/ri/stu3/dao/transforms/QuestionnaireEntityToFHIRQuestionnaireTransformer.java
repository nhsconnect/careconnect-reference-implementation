package uk.nhs.careconnect.ri.stu3.dao.transforms;

import org.apache.commons.collections4.Transformer;
import org.hl7.fhir.dstu3.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.nhs.careconnect.ri.database.entity.questionnaire.*;
import uk.nhs.careconnect.ri.stu3.dao.daoutils;
import uk.nhs.careconnect.ri.database.entity.BaseAddress;


@Component
public class QuestionnaireEntityToFHIRQuestionnaireTransformer implements Transformer<QuestionnaireEntity
        , Questionnaire> {

    private final Transformer<BaseAddress, Address> addressTransformer;

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(QuestionnaireEntityToFHIRQuestionnaireTransformer.class);


    public QuestionnaireEntityToFHIRQuestionnaireTransformer(@Autowired Transformer<BaseAddress, Address> addressTransformer) {
        this.addressTransformer = addressTransformer;
    }

    private Questionnaire questionnaire;

    @Override
    public Questionnaire transform(final QuestionnaireEntity questionnaireEntity) {
        questionnaire = new Questionnaire();

        Meta meta = new Meta();

        if (questionnaireEntity.getUpdated() != null) {
            meta.setLastUpdated(questionnaireEntity.getUpdated());
        }
        else {
            if (questionnaireEntity.getCreated() != null) {
                meta.setLastUpdated(questionnaireEntity.getCreated());
            }
        }
        questionnaire.setMeta(meta);

        for(QuestionnaireIdentifier identifier : questionnaireEntity.getIdentifiers())
        {
            Identifier ident = questionnaire.addIdentifier();
            ident = daoutils.getIdentifier(identifier, ident);
        }


        questionnaire.setId(questionnaireEntity.getId().toString());

        if (questionnaireEntity.getUrl() != null) {
            questionnaire.setUrl(questionnaireEntity.getUrl());
        }
        if (questionnaireEntity.getName() != null) {
            questionnaire.setName(questionnaireEntity.getName());
        }

        if (questionnaireEntity.getPublisher() != null) {
            questionnaire.setPublisher(questionnaireEntity.getPublisher());
        }
        if (questionnaireEntity.getCopyright() != null) {
            questionnaire.setCopyright(questionnaireEntity.getCopyright());
        }

        if (questionnaireEntity.getPurpose() != null) {
            questionnaire.setPurpose(questionnaireEntity.getPurpose());
        }

        if (questionnaireEntity.getStatus() != null){
            questionnaire.setStatus(questionnaireEntity.getStatus());
        }

        if (questionnaireEntity.getVersion() != null) {
            questionnaire.setVersion(questionnaireEntity.getVersion());
        }

        if (questionnaireEntity.getTitle() != null) {
            questionnaire.setTitle(questionnaireEntity.getTitle());
        }

        if (questionnaireEntity.getDescription() != null) {
            questionnaire.setDescription(questionnaireEntity.getDescription());
        }

        if (questionnaireEntity.getDateTime() != null) {
            questionnaire.setDate(questionnaireEntity.getDateTime());
        }

        if (questionnaireEntity.getApprovalDateTime() != null) {
            questionnaire.setApprovalDate(questionnaireEntity.getApprovalDateTime());
        }

        if (questionnaireEntity.getLastReviewDateTime() != null) {
            questionnaire.setLastReviewDate(questionnaireEntity.getLastReviewDateTime());
        }

        if (questionnaireEntity.getQuestionnaireCode() != null){
            questionnaire.getCode().add(
                    new Coding()
                            .setCode(questionnaireEntity.getQuestionnaireCode().getCode())
                            .setDisplay(questionnaireEntity.getQuestionnaireCode().getDisplay())
                            .setSystem(questionnaireEntity.getQuestionnaireCode().getSystem()));
        }

        if (questionnaireEntity.getSubjectType() != null) {
            questionnaire.addSubjectType(questionnaireEntity.getSubjectType());
        }

        for (QuestionnaireItem item : questionnaireEntity.getItems()) {
            Questionnaire.QuestionnaireItemComponent itemComponent = questionnaire.addItem();
            getItemComponent(item, itemComponent);
        }

        return questionnaire;

    }

    private Questionnaire.QuestionnaireItemComponent getItemComponent(QuestionnaireItem item, Questionnaire.QuestionnaireItemComponent itemComponent) {
        if (item.getLinkId() != null) {
            itemComponent.setLinkId(item.getLinkId());
        }
        if (item.getItemCode() != null){
            itemComponent.addCode()
                    .setCode(item.getItemCode().getCode())
                    .setDisplay(item.getItemCode().getDisplay())
                    .setSystem(item.getItemCode().getSystem());
        }

        if (item.getPrefix() != null) {
            itemComponent.setPrefix(item.getPrefix());
        }

        if (item.getItemText() != null) {
            itemComponent.setText(item.getItemText());
        }

        if (item.getItemType() != null) {
            itemComponent.setType(item.getItemType());
        }



        if (item.getRequired() != null) {
            itemComponent.setRequired(item.getRequired());
        }
        if (item.getReadOnly() != null) {
            itemComponent.setReadOnly(item.getReadOnly());
        }
        if (item.getRepeats() != null) {
            itemComponent.setRepeats(item.getRepeats());
        }

        if (item.getAllowedResource() != null && item.getItemType().equals(Questionnaire.QuestionnaireItemType.REFERENCE)) {
            Extension referenceType = itemComponent.addExtension();
            referenceType.setUrl("http://hl7.org/fhir/StructureDefinition/questionnaire-allowedResource");
            referenceType.setValue(new CodeType().setValue(item.getAllowedResource()));
        }
        if (item.getAllowedProfile() != null) {
            itemComponent.addExtension()
                    .setUrl("http://hl7.org/fhir/StructureDefinition/questionnaire-allowedProfile")
                    .setValue(new Reference(item.getAllowedProfile()));
        }

        if (item.getValueSetOptions() != null) {
            itemComponent.setOptions(new Reference(item.getValueSetOptions()));
        }

        if (item.getDefinitionUri() != null) {
            itemComponent.setDefinition(item.getDefinitionUri());
        }

        if (item.getDesignNote() != null ) {
            Extension referenceType = itemComponent.addExtension();
            referenceType.setUrl("http://hl7.org/fhir/StructureDefinition/designNote");
            referenceType.setValue(new MarkdownType().setValue(item.getDesignNote()));
        }

        if (item.getChildItems() != null) {
            for (QuestionnaireItem childItem : item.getChildItems()) {
                Questionnaire.QuestionnaireItemComponent childItemComponent = itemComponent.addItem();
                getItemComponent(childItem, childItemComponent);
            }
        }

        for (QuestionnaireItemOptions option : item.getOptions()) {
            if (option.getValueCode() != null) {
                itemComponent.addOption( new Questionnaire.QuestionnaireItemOptionComponent()
                        .setValue(new Coding()
                                .setCode(option.getValueCode().getCode())
                                .setSystem(option.getValueCode().getSystem())
                                .setDisplay(option.getValueCode().getDisplay())));
            }
            if (option.getValueString() != null) {
                itemComponent.addOption( new Questionnaire.QuestionnaireItemOptionComponent()
                        .setValue(new StringType().setValue(option.getValueString())));
            }
        }

        for (QuestionnaireItemEnable enable : item.getEnabled()) {
            Questionnaire.QuestionnaireItemEnableWhenComponent when = new Questionnaire.QuestionnaireItemEnableWhenComponent();
            if (enable.getAnswerCode() != null) {
                when.setAnswer(new Coding()
                                .setCode(enable.getAnswerCode().getCode())
                                .setSystem(enable.getAnswerCode().getSystem())
                                .setDisplay(enable.getAnswerCode().getDisplay()));
            }
            if (enable.getAnswerString() != null) {
                when.setAnswer(new StringType().setValue(enable.getAnswerString()));
            }

            /*
            if (enable.getAnswerInteger() != null) {
                when.setAnswer(new IntegerType().setValue(enable.getAnswerInteger().));
            }

             */

            if (enable.getHasAnswer() != null) {
                when.setHasAnswer(enable.getHasAnswer());
            }
            if (enable.getQuestion() != null ) {
                when.setQuestion(enable.getQuestion());
            }
            itemComponent.addEnableWhen(when);
        }

        return itemComponent;
    }

}
