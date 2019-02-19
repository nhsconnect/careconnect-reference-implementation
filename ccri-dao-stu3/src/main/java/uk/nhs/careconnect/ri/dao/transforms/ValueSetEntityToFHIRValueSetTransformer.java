package uk.nhs.careconnect.ri.dao.transforms;


import org.apache.commons.collections4.Transformer;
import org.hl7.fhir.dstu3.model.ValueSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import uk.nhs.careconnect.ri.database.entity.condition.ConditionIdentifier;
import uk.nhs.careconnect.ri.database.entity.valueSet.*;


@Component
public class ValueSetEntityToFHIRValueSetTransformer implements Transformer<ValueSetEntity, ValueSet> {

    private static final Logger log = LoggerFactory.getLogger(ValueSetEntityToFHIRValueSetTransformer.class);

    @Override
    public ValueSet transform(final ValueSetEntity valueSetEntity) {
        final ValueSet valueSet = new ValueSet();



        if (valueSetEntity.getStrId() != null) {
            valueSet.setId(valueSetEntity.getStrId());
        }
        else {
            valueSet.setId(valueSetEntity.getId().toString());
        }
        valueSet.setUrl(valueSetEntity.getUrl());

        if (valueSetEntity.getVersion() != null) {
            valueSet.setVersion(valueSetEntity.getVersion());
        }

        valueSet.setName(valueSetEntity.getName());

        if (valueSetEntity.getTitle() != null) {
            valueSet.setTitle(valueSetEntity.getTitle());
        }

        valueSet.setStatus(valueSetEntity.getStatus());

        if (valueSetEntity.getExperimental() != null) {
            valueSet.setExperimental(valueSetEntity.getExperimental());
        }

        if (valueSetEntity.getChangeDateTime() != null) {
            valueSet.setDate(valueSetEntity.getChangeDateTime());
        }
        if (valueSetEntity.getPublisher() != null) {
            valueSet.setPublisher(valueSetEntity.getPublisher());
        }

        valueSet.setDescription(valueSetEntity.getDescription());

        if (valueSetEntity.getImmutable() != null) {
            valueSet.setImmutable(valueSetEntity.getImmutable());
        }

        if (valueSetEntity.getPurpose() != null) {
            valueSet.setPurpose(valueSetEntity.getPurpose());
        }

        if (valueSetEntity.getCopyright() != null) {
            valueSet.setCopyright(valueSetEntity.getCopyright());
        }

        if (valueSetEntity.getExtensible() != null) {
            valueSet.setExtensible(valueSetEntity.getExtensible());
        }

        log.trace("ValueSetEntity name ="+valueSetEntity.getName());

        if (valueSetEntity.getCodeSystem() != null) {
            log.trace("CodeSystem Id = "+valueSetEntity.getCodeSystem().getId());
        }
        // Hard coded to not attempt to retrieve SNOMED!

        for (ValueSetIdentifier identifier : valueSetEntity.getIdentifiers()) {
            valueSet.addIdentifier()
                    .setSystem(identifier.getSystem().getUri())
                    .setValue(identifier.getValue());
        }

        for (ValueSetTelecom telecom : valueSetEntity.getContacts()) {
            valueSet.addContact()
                    .addTelecom()
                    .setUse(telecom.getTelecomUse())
                    .setValue(telecom.getValue())
                    .setSystem(telecom.getSystem());
        }

        for (ValueSetInclude includeEntity : valueSetEntity.getIncludes()) {
            log.trace("Compose CodeSystem : "+includeEntity.getSystem());
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
                        .setValue(filterEntity.getValueCode())
                        .setProperty(filterEntity.getPropertyCode());

            }
        }



        return valueSet;



        // STU 3 Removed
        /*
        if ((valueSetEntity.getCodeSystem() != null) && (valueSetEntity.getCodeSystem().getId() != 9)) {
            // ONly for DSTU2 - This should be in the CodeSystemEntity for STU3
         //   ValueSet.ValueSetCodeSystemComponent valueSetCodeSystemComponent = new ValueSet.ValueSetCodeSystemComponent();
          //  valueSetCodeSystemComponent.setSystem(valueSetEntity.getCodeSystem().getCodeSystemUri());

         //   valueSet.setCodeSystem(valueSetCodeSystemComponent);
            log.info("Internal CodeSystem: "+valueSetEntity.getCodeSystem().getCodeSystemUri()+" size="+valueSetEntity.getCodeSystem().getConcepts().size());
            if (valueSetEntity.getCodeSystem().getConcepts().size() < 1000) {
                for (ConceptEntity concept : valueSetEntity.getCodeSystem().getConcepts()) {

                        ValueSet.ConceptDefinitionComponent CSconcept = valueSetCodeSystemComponent.addConcept()
                                .setCode(concept.getCode())
                                .setDisplay(concept.getDisplay());
                        if (concept.getDescription() != null) {
                            CSconcept.setDefinition(concept.getDescription());
                        }

                        getChildCodes(concept, CSconcept);
                    }
                }
            }
        }
        */
    }
    /* Not required for STU3
    private void getChildCodes(ConceptEntity conceptEntity, ValueSet.ConceptReferenceComponent CSconcept ) {

        for (ConceptParentChildLink childConcepts : conceptEntity.getChildren()) {

            ConceptEntity conceptSub = childConcepts.getChild();
            ValueSet.ConceptReferenceComponent CSconceptSub = CSconcept.addConcept()
                    .setCode(conceptSub.getCode())
                    .setDisplay(conceptSub.getDisplay());
            if (conceptSub.getDescription() != null) {
                CSconceptSub.setDefinition(conceptEntity.getDescription());
            }

        }
    }
    */
}
