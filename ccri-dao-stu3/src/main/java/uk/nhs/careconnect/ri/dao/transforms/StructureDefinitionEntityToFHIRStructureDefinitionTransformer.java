package uk.nhs.careconnect.ri.dao.transforms;

import ca.uhn.fhir.context.FhirContext;
import org.apache.commons.collections4.Transformer;
import org.hl7.fhir.dstu3.model.StructureDefinition;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import uk.nhs.careconnect.ri.database.entity.structureDefinition.StructureDefinitionEntity;

@Component
public class StructureDefinitionEntityToFHIRStructureDefinitionTransformer implements Transformer<StructureDefinitionEntity, StructureDefinition>  {


    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(StructureDefinitionEntityToFHIRStructureDefinitionTransformer.class);


    public StructureDefinition transform(final StructureDefinitionEntity structureDefinitionEntity, FhirContext ctx) {
        final StructureDefinition structureDefinition = (StructureDefinition) ctx.newJsonParser().parseResource(structureDefinitionEntity.getResource());

        structureDefinition.setId(structureDefinitionEntity.getId().toString());

        return structureDefinition;

    }

	
    @Override
    public StructureDefinition transform(final StructureDefinitionEntity structureDefinitionEntity) {
    	
    	return null;
    }

}
