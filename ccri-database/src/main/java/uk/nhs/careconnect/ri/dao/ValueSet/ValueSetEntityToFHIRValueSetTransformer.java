package uk.nhs.careconnect.ri.dao.ValueSet;


import org.apache.commons.collections4.Transformer;
import org.hl7.fhir.instance.model.ValueSet;
import org.springframework.stereotype.Component;
import uk.nhs.careconnect.ri.entity.Terminology.*;


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

        if (valueSetEntity.getCodeSystem() != null ) {
            // ONly for DSTU2 - This should be in the CodeSystemEntity for STU3
            ValueSet.ValueSetCodeSystemComponent valueSetCodeSystemComponent = new ValueSet.ValueSetCodeSystemComponent();
            valueSetCodeSystemComponent.setSystem(valueSetEntity.getCodeSystem().getCodeSystemUri());

            valueSet.setCodeSystem(valueSetCodeSystemComponent);
            for (ConceptEntity concept : valueSetEntity.getCodeSystem().getContents()) {
            // TODO Only process parent codes. This is a Full System scan
                if (concept.getParents().size() == 0) {
                    ValueSet.ConceptDefinitionComponent CSconcept = valueSetCodeSystemComponent.addConcept()
                            .setCode(concept.getCode())
                            .setDisplay(concept.getDisplay());
                    if (concept.getDescription() != null) {
                        CSconcept.setDefinition(concept.getDescription());
                    }
                    // TODO Refactor to throw except if codeSystem is too large
                    getChildCodes(concept, CSconcept);
                }
            }
        }

        for (ValueSetInclude includeEntity : valueSetEntity.getIncludes()) {

            ValueSet.ConceptSetComponent include = valueSet.getCompose().addInclude().setSystem(includeEntity.getSystem());

            for (ValueSetIncludeConcept
                    conceptEntity : includeEntity.getConcepts()) {
                include.addConcept()
                        .setCode(conceptEntity.getConcept().getCode())
                        .setDisplay(conceptEntity.getConcept().getDisplay());
            }
            for (ValueSetIncludeFilter filterEntity : includeEntity.getFilters()) {
                include.addFilter()
                        .setOp(filterEntity.getOperator())
                        .setValue(filterEntity.getValue().getCode());
            }
        }



        return valueSet;
    }
    private void getChildCodes(ConceptEntity conceptEntity, ValueSet.ConceptDefinitionComponent CSconcept ) {

        for (ConceptParentChildLink childConcepts : conceptEntity.getChildren()) {

            ConceptEntity conceptSub = childConcepts.getChild();
            ValueSet.ConceptDefinitionComponent CSconceptSub = CSconcept.addConcept()
                    .setCode(conceptSub.getCode())
                    .setDisplay(conceptSub.getDisplay());
            if (conceptSub.getDescription() != null) {
                CSconceptSub.setDefinition(conceptEntity.getDescription());
            }
            if (conceptSub.getChildren().size() > 0) {
                getChildCodes(conceptSub,CSconceptSub);
            }

        }
    }
}
