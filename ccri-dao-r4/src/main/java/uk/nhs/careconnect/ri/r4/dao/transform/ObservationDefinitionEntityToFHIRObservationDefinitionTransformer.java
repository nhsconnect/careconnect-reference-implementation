package uk.nhs.careconnect.ri.r4.dao.transform;

import ca.uhn.fhir.context.FhirContext;


import org.apache.commons.collections4.Transformer;
import org.hl7.fhir.r4.model.ObservationDefinition;
import org.springframework.stereotype.Component;
import uk.nhs.careconnect.ri.database.entity.observationDefinition.ObservationDefinitionEntity;

@Component
public class ObservationDefinitionEntityToFHIRObservationDefinitionTransformer implements Transformer<ObservationDefinitionEntity, ObservationDefinition> {


    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ObservationDefinitionEntityToFHIRObservationDefinitionTransformer.class);


    public ObservationDefinition transform(final ObservationDefinitionEntity observationDefinitionEntity, FhirContext ctx) {
        final ObservationDefinition observationDefinition = (ObservationDefinition) ctx.newJsonParser().parseResource(observationDefinitionEntity.getResource());

        observationDefinition.setId(observationDefinitionEntity.getId().toString());

        return observationDefinition;

    }

	
    @Override
    public ObservationDefinition transform(final ObservationDefinitionEntity observationDefinitionEntity) {
    	
    	return null;
    }

}
