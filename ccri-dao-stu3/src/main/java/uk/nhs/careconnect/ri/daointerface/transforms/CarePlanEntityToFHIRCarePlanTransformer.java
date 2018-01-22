package uk.nhs.careconnect.ri.daointerface.transforms;

import org.apache.commons.collections4.Transformer;
import org.hl7.fhir.dstu3.model.CarePlan;
import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.Meta;
import org.hl7.fhir.dstu3.model.Reference;
import org.springframework.stereotype.Component;

import uk.nhs.careconnect.ri.entity.carePlan.CarePlanCategory;
import uk.nhs.careconnect.ri.entity.carePlan.CarePlanEntity;
import uk.nhs.careconnect.ri.entity.carePlan.CarePlanIdentifier;

@Component
public class CarePlanEntityToFHIRCarePlanTransformer implements Transformer<CarePlanEntity, CarePlan> {



    @Override
    public CarePlan transform(final CarePlanEntity carePlanEntity) {
        final CarePlan carePlan = new CarePlan();

        Meta meta = new Meta();

        if (carePlanEntity.getUpdated() != null) {
            meta.setLastUpdated(carePlanEntity.getUpdated());
        }
        else {
            if (carePlanEntity.getCreated() != null) {
                meta.setLastUpdated(carePlanEntity.getCreated());
            }
        }
        carePlan.setMeta(meta);

        carePlan.setId(carePlanEntity.getId().toString());

        
        if (carePlanEntity.getPatient() != null) {
            carePlan
                    .setSubject(new Reference("Patient/"+carePlanEntity.getPatient().getId())
                    .setDisplay(carePlanEntity.getPatient().getNames().get(0).getDisplayName()));
        }
        
        
        for (CarePlanCategory categoryEntity : carePlanEntity.getCategories()) {
            CodeableConcept concept = carePlan.addCategory();
            concept.addCoding()
                    .setSystem(categoryEntity.getCategory().getSystem())
                    .setCode(categoryEntity.getCategory().getCode())
                    .setDisplay(categoryEntity.getCategory().getDisplay());
            carePlan.addCategory(concept);
        }

        for (CarePlanIdentifier identifier : carePlanEntity.getIdentifiers()) {
            carePlan.addIdentifier()
                .setSystem(identifier.getSystem().getUri())
                .setValue(identifier.getValue());
        }


        return carePlan;

    }
}
