package uk.nhs.careconnect.ri.daointerface.transforms;

import org.apache.commons.collections4.Transformer;
import org.hl7.fhir.dstu3.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.nhs.careconnect.ri.entity.BaseAddress;
import uk.nhs.careconnect.ri.entity.questionnaire.QuestionnaireEntity;
import uk.nhs.careconnect.ri.entity.questionnaire.QuestionnaireIdentifier;
import uk.nhs.careconnect.ri.entity.questionnaire.QuestionnaireItem;
import uk.nhs.careconnect.ri.entity.questionnaireResponse.QuestionnaireResponseEntity;
import uk.nhs.careconnect.ri.entity.questionnaireResponse.QuestionnaireResponseIdentifier;


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

        for(QuestionnaireResponseIdentifier identifier : formEntity.getIdentifiers())
        {
            form.getIdentifier()
                    .setSystem(identifier.getSystem().getUri())
                    .setValue(identifier.getValue());
        }


        form.setId(formEntity.getId().toString());


        if (formEntity.getStatus() != null){
            form.setStatus(formEntity.getStatus());
        }


        return form;

    }

}
