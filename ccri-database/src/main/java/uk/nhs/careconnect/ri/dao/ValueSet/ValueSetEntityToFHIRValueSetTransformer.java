package uk.nhs.careconnect.ri.dao.ValueSet;


import org.apache.commons.collections4.Transformer;
import org.hl7.fhir.instance.model.ValueSet;
import org.springframework.stereotype.Component;
import uk.nhs.careconnect.ri.entity.Terminology.ConceptEntity;
import uk.nhs.careconnect.ri.entity.Terminology.ValueSetEntity;


@Component
public class ValueSetEntityToFHIRValueSetTransformer implements Transformer<ValueSetEntity, ValueSet> {

    @Override
    public ValueSet transform(final ValueSetEntity valueSetEntity) {
        final ValueSet valueSet = new ValueSet();

        valueSet.setDescription(valueSetEntity.getDescription());
        valueSet.setName(valueSetEntity.getName());
        if (valueSetEntity.getStrId() != null) {
            valueSet.setId(valueSetEntity.getStrId());
        }
        else {
            valueSet.setId(valueSetEntity.getId().toString());
        }
        valueSet.setUrl(valueSetEntity.getUrl());

        valueSet.setStatus(valueSetEntity.getStatus());

        ValueSet.ValueSetCodeSystemComponent valueSetCodeSystemComponent = new ValueSet.ValueSetCodeSystemComponent();
        valueSetCodeSystemComponent.setSystem(valueSetEntity.getCodeSystem().getCodeSystemUri());

        for (ConceptEntity concept : valueSetEntity.getCodeSystem().getContents()) {
            valueSetCodeSystemComponent.addConcept()
                    .setCode(concept.getCode())
                    .setDisplay(concept.getDisplay());
        }
        valueSet.setCodeSystem(valueSetCodeSystemComponent);


        return valueSet;
    }
}
