package uk.nhs.careconnect.ri.dao.transforms;

import org.apache.commons.collections4.Transformer;
import org.hl7.fhir.dstu3.model.Address;
import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.ConceptMap;
import org.hl7.fhir.dstu3.model.Extension;
import org.hl7.fhir.dstu3.model.HumanName;
import org.hl7.fhir.dstu3.model.Identifier;
import org.hl7.fhir.dstu3.model.Meta;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.dstu3.model.Reference;
import org.hl7.fhir.dstu3.model.StringType;
import org.hl7.fhir.dstu3.model.ValueSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import uk.nhs.careconnect.ri.dao.daoutils;
import uk.nhs.careconnect.ri.database.entity.BaseAddress;
import uk.nhs.careconnect.ri.database.entity.conceptMap.ConceptMapEntity;
import uk.nhs.careconnect.ri.database.entity.patient.PatientAddress;
import uk.nhs.careconnect.ri.database.entity.patient.PatientEntity;
import uk.nhs.careconnect.ri.database.entity.patient.PatientIdentifier;
import uk.nhs.careconnect.ri.database.entity.patient.PatientName;
import uk.nhs.careconnect.ri.database.entity.patient.PatientTelecom;
import uk.nhs.careconnect.ri.database.entity.practitioner.PractitionerName;
import uk.nhs.careconnect.ri.database.entity.valueSet.ValueSetInclude;
import uk.nhs.careconnect.ri.database.entity.valueSet.ValueSetIncludeConcept;
import uk.nhs.careconnect.ri.database.entity.valueSet.ValueSetIncludeFilter;
import uk.org.hl7.fhir.core.Stu3.CareConnectExtension;
import uk.org.hl7.fhir.core.Stu3.CareConnectProfile;
import uk.org.hl7.fhir.core.Stu3.CareConnectSystem;

@Component
public class ConceptMapEntityToFHIRConceptMapTransformer implements Transformer<ConceptMapEntity, ConceptMap>  {
	
	private final Transformer<BaseAddress, Address> addressTransformer;

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ConceptMapEntityToFHIRConceptMapTransformer.class);

    public ConceptMapEntityToFHIRConceptMapTransformer(@Autowired Transformer<BaseAddress, Address> addressTransformer) {
        this.addressTransformer = addressTransformer;
    }
	
    @Override
    public ConceptMap transform(final ConceptMapEntity conceptMapEntity) {
    	
    	final ConceptMap conceptMap = new ConceptMap();

    	conceptMap.setDescription(conceptMapEntity.getDescription());
    	conceptMap.setName(conceptMapEntity.getName());
        if (conceptMapEntity.getId() != null) {
            conceptMap.setId(conceptMapEntity.getId().toString());
        }
        else {
        	conceptMap.setId(conceptMapEntity.getId().toString());
        }
        conceptMap.setUrl(conceptMapEntity.getUrl());

        conceptMap.setStatus(conceptMapEntity.getStatus());

        log.trace("ValueSetEntity name ="+conceptMapEntity.getName());

        /*if (conceptMapEntity.getCodeSystem() != null) {
            log.trace("CodeSystem Id = "+conceptMapEntity.getCodeSystem().getId());
        }
        // Hard coded to not attempt to retrieve SNOMED!

/*
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
                        .setValue(filterEntity.getValue().getCode());
            }
        }

*/

        return conceptMap;

    }

}
