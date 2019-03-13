package uk.nhs.careconnect.ri.dao.transforms;

import org.apache.commons.collections4.Transformer;
import org.hl7.fhir.dstu3.model.Address;
import org.hl7.fhir.dstu3.model.StructureDefinition;
import org.hl7.fhir.dstu3.model.Reference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.nhs.careconnect.ri.database.entity.BaseAddress;
import uk.nhs.careconnect.ri.database.entity.structureDefinition.*;
import uk.nhs.careconnect.ri.database.entity.structureDefinition.StructureDefinitionEntity;

@Component
public class StructureDefinitionEntityToFHIRStructureDefinitionTransformer implements Transformer<StructureDefinitionEntity, StructureDefinition>  {

	private final Transformer<BaseAddress, Address> addressTransformer;

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(StructureDefinitionEntityToFHIRStructureDefinitionTransformer.class);

    public StructureDefinitionEntityToFHIRStructureDefinitionTransformer(@Autowired Transformer<BaseAddress, Address> addressTransformer) {
        this.addressTransformer = addressTransformer;
    }


	
    @Override
    public StructureDefinition transform(final StructureDefinitionEntity structureDefinitionEntity) {
    	
    	final StructureDefinition structureDefinition = new StructureDefinition();

    	structureDefinition.setDescription(structureDefinitionEntity.getDescription());
    	structureDefinition.setName(structureDefinitionEntity.getName());
        if (structureDefinitionEntity.getId() != null) {
            structureDefinition.setId(structureDefinitionEntity.getId().toString());
        }
        else {
        	structureDefinition.setId(structureDefinitionEntity.getId().toString());
        }
        structureDefinition.setUrl(structureDefinitionEntity.getUrl());

        structureDefinition.setStatus(structureDefinitionEntity.getStatus());

        log.trace("ValueSetEntity name ="+structureDefinitionEntity.getName());

        if (structureDefinitionEntity.getPublisher() != null) {
            structureDefinition.setPublisher(structureDefinitionEntity.getPublisher());
        }
        if (structureDefinitionEntity.getVersion() != null) {
            structureDefinition.setVersion(structureDefinitionEntity.getVersion());
        }
        if (structureDefinitionEntity.getCopyright() != null) {
            structureDefinition.setCopyright(structureDefinitionEntity.getCopyright());
        }
       

        return structureDefinition;

    }

}
