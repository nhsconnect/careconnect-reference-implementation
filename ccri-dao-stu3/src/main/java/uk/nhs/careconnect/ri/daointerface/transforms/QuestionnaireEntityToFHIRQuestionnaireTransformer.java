package uk.nhs.careconnect.ri.daointerface.transforms;

import org.apache.commons.collections4.Transformer;
import org.hl7.fhir.dstu3.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.nhs.careconnect.ri.entity.BaseAddress;
import uk.nhs.careconnect.ri.entity.Terminology.ConceptEntity;
import uk.nhs.careconnect.ri.entity.questionnaire.QuestionnaireEntity;
import uk.nhs.careconnect.ri.entity.questionnaire.QuestionnaireIdentifier;

import javax.persistence.*;
import java.util.Date;


@Component
public class QuestionnaireEntityToFHIRQuestionnaireTransformer implements Transformer<QuestionnaireEntity
        , Questionnaire> {

    private final Transformer<BaseAddress, Address> addressTransformer;

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(QuestionnaireEntityToFHIRQuestionnaireTransformer.class);


    public QuestionnaireEntityToFHIRQuestionnaireTransformer(@Autowired Transformer<BaseAddress, Address> addressTransformer) {
        this.addressTransformer = addressTransformer;
    }

    @Override
    public Questionnaire transform(final QuestionnaireEntity questionnaireEntity) {
        final Questionnaire questionnaire = new Questionnaire();

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
            questionnaire.addIdentifier()
                    .setSystem(identifier.getSystem().getUri())
                    .setValue(identifier.getValue());
        }


        questionnaire.setId(questionnaireEntity.getId().toString());

        if (questionnaireEntity.getName() != null) {
            questionnaire.setName(questionnaireEntity.getName());
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

            CodeType type = new CodeType();
            type.setValue(questionnaireEntity.getSubjectType().toString());
            questionnaire.getSubjectType().add(type);
        }


        return questionnaire;

    }
}
