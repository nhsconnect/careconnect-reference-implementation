package uk.nhs.careconnect.ri.r4.dao.transform;

import ca.uhn.fhir.context.FhirContext;


import org.apache.commons.collections4.Transformer;
import org.hl7.fhir.r4.model.ObservationDefinition;
import org.hl7.fhir.r4.model.Reference;
import org.springframework.stereotype.Component;
import uk.nhs.careconnect.ri.database.entity.observationDefinition.ObservationDefinitionEntity;

@Component
public class ObservationDefinitionEntityToFHIRObservationDefinitionTransformer implements Transformer<ObservationDefinitionEntity, ObservationDefinition> {


    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ObservationDefinitionEntityToFHIRObservationDefinitionTransformer.class);


    public ObservationDefinition transform(final ObservationDefinitionEntity observationDefinitionEntity, FhirContext ctx) {
        final ObservationDefinition observationDefinition = (ObservationDefinition) ctx.newJsonParser().parseResource(observationDefinitionEntity.getResource());

        observationDefinition.setId(observationDefinitionEntity.getId().toString());

        if (observationDefinitionEntity.getAbnormalValueSet() != null) {
            observationDefinition.setAbnormalCodedValueSet(
                    new Reference().setReference(observationDefinitionEntity.getAbnormalValueSet().getUrl())
                    .setDisplay(observationDefinitionEntity.getAbnormalValueSet().getName())
            );
        }

        if (observationDefinitionEntity.getNormalValueSet() != null) {
            observationDefinition.setNormalCodedValueSet(
                    new Reference().setReference(observationDefinitionEntity.getNormalValueSet().getUrl())
                            .setDisplay(observationDefinitionEntity.getNormalValueSet().getName())
            );
        }

        if (observationDefinitionEntity.getValidValueSet() != null) {
            observationDefinition.setValidCodedValueSet(
                    new Reference().setReference(observationDefinitionEntity.getValidValueSet().getUrl())
                            .setDisplay(observationDefinitionEntity.getValidValueSet().getName())
            );
        }

        if (observationDefinitionEntity.getCriticalValueSet() != null) {
            observationDefinition.setCriticalCodedValueSet(
                    new Reference().setReference(observationDefinitionEntity.getCriticalValueSet().getUrl())
                            .setDisplay(observationDefinitionEntity.getCriticalValueSet().getName())
            );
        }

        return observationDefinition;

    }

	
    @Override
    public ObservationDefinition transform(final ObservationDefinitionEntity observationDefinitionEntity) {
    	
    	return null;
    }

}
