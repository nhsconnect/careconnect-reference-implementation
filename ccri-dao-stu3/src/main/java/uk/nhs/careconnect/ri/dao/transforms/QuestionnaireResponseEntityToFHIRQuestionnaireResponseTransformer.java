package uk.nhs.careconnect.ri.dao.transforms;

import org.apache.commons.collections4.Transformer;
import org.hl7.fhir.dstu3.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.nhs.careconnect.ri.database.entity.BaseAddress;
import uk.nhs.careconnect.ri.database.entity.questionnaireResponse.QuestionnaireResponseEntity;
import uk.nhs.careconnect.ri.database.entity.questionnaireResponse.QuestionnaireResponseIdentifier;
import uk.nhs.careconnect.ri.database.entity.questionnaireResponse.QuestionnaireResponseItem;
import uk.nhs.careconnect.ri.database.entity.questionnaireResponse.QuestionnaireResponseItemAnswer;


@Component
public class QuestionnaireResponseEntityToFHIRQuestionnaireResponseTransformer implements Transformer<QuestionnaireResponseEntity
        , QuestionnaireResponse> {

    private final Transformer<BaseAddress, Address> addressTransformer;

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(QuestionnaireResponseEntityToFHIRQuestionnaireResponseTransformer.class);


    public QuestionnaireResponseEntityToFHIRQuestionnaireResponseTransformer(@Autowired Transformer<BaseAddress, Address> addressTransformer) {
        this.addressTransformer = addressTransformer;
    }

    private QuestionnaireResponse form;

    @Override
    public QuestionnaireResponse transform(final QuestionnaireResponseEntity formEntity) {
        form = new QuestionnaireResponse();

        Meta meta = new Meta();

        if (formEntity.getUpdated() != null) {
            meta.setLastUpdated(formEntity.getUpdated());
        }
        else {
            if (formEntity.getCreated() != null) {
                meta.setLastUpdated(formEntity.getCreated());
            }
        }
        form.setMeta(meta);

        form.setId(formEntity.getId().toString());

        for(QuestionnaireResponseIdentifier identifier : formEntity.getIdentifiers())
        {
            form.getIdentifier()
                    .setSystem(identifier.getSystem().getUri())
                    .setValue(identifier.getValue());
        }

        if (formEntity.getAuthoredDateTime() != null) {
            form.setAuthored(formEntity.getAuthoredDateTime());
        }

        if (formEntity.getStatus() != null){
            form.setStatus(formEntity.getStatus());
        }

        if (formEntity.getAuthorPatient() != null) {
            form.setAuthor(new Reference("Patient/"+formEntity.getAuthorPatient().getId()).setDisplay(formEntity.getAuthorPatient().getNames().get(0).getDisplayName()));
        }
        if (formEntity.getAuthorPractitioner() != null) {
            form.setAuthor(new Reference("Practitioner/"+formEntity.getAuthorPractitioner().getId()).setDisplay(formEntity.getAuthorPractitioner().getNames().get(0).getDisplayName()));
        }

        if (formEntity.getCarePlan() != null) {
            form.getBasedOn().add(new Reference("CarePlan/"+formEntity.getCarePlan().getId()));
        }
        if (formEntity.getContextEncounter() != null) {
            form.setContext(new Reference("Encounter/"+formEntity.getContextEncounter().getId()));
        }
        if (formEntity.getContextEpisodeOfCare() != null) {
            form.setContext(new Reference("EpisodeOfCare/"+formEntity.getContextEpisodeOfCare().getId()));
        }
        if (formEntity.getPatient() != null) {
            form.setSubject(new Reference("Patient/"+formEntity.getPatient().getId()).setDisplay(formEntity.getPatient().getNames().get(0).getDisplayName()));
        }
        if (formEntity.getQuestionnaire() != null) {
            form.setQuestionnaire(new Reference("Questionnaire/"+formEntity.getQuestionnaire().getId()));
        }
        if (formEntity.getSourcePatient() != null) {
            form.setSource(new Reference("Patient/"+formEntity.getSourcePatient().getId()).setDisplay(formEntity.getSourcePatient().getNames().get(0).getDisplayName()));
        }
        if (formEntity.getSourcePractitioner() != null) {
            form.setSource(new Reference("Practitioner/"+formEntity.getSourcePractitioner().getId()).setDisplay(formEntity.getSourcePractitioner().getNames().get(0).getDisplayName()));
        }

        for (QuestionnaireResponseItem itemEntity : formEntity.getItems()) {
            QuestionnaireResponse.QuestionnaireResponseItemComponent item = form.addItem();
            getItem(itemEntity,item);
        }

        return form;

    }

    private void getItem(QuestionnaireResponseItem itemEntity,QuestionnaireResponse.QuestionnaireResponseItemComponent item ) {
        if (itemEntity.getLinkId() != null) {
            item.setLinkId(itemEntity.getLinkId());
        }
        if (itemEntity.getText() != null) {
            item.setText(itemEntity.getText());
        }
        if (itemEntity.getDefinition() != null) {
            item.setDefinition(itemEntity.getDefinition());
        }
        for (QuestionnaireResponseItem subItemEntity : itemEntity.getItems()) {
            QuestionnaireResponse.QuestionnaireResponseItemComponent subItem = item.addItem();
            getItem(subItemEntity,subItem);
        }
        for (QuestionnaireResponseItemAnswer answerEntity : itemEntity.getAnswers()) {
            QuestionnaireResponse.QuestionnaireResponseItemAnswerComponent answer =item.addAnswer();
            if (answerEntity.getValueBoolean() != null) {
                answer.setValue(new BooleanType(answerEntity.getValueBoolean()));
            } else if (answerEntity.getValueInteger() != null) {
                answer.setValue(new IntegerType(answerEntity.getValueInteger()));
            } else if (answerEntity.getValueString() != null) {
                answer.setValue(new StringType(answerEntity.getValueString()));
            } else if (answerEntity.getValueCoding() != null) {
                answer.setValue(new Coding()
                        .setCode(answerEntity.getValueCoding().getCode())
                        .setDisplay(answerEntity.getValueCoding().getDisplay())
                        .setSystem(answerEntity.getValueCoding().getSystem())
                );
            }
             else if (answerEntity.getReferenceCondition() != null) {
                    answer.setValue(new Reference("Condition/"+answerEntity.getReferenceCondition().getId())
                    );
                }
            else if (answerEntity.getReferenceObservation() != null) {
                answer.setValue(new Reference("Observation/"+answerEntity.getReferenceObservation().getId())
                );
            }
        }


    }

}
