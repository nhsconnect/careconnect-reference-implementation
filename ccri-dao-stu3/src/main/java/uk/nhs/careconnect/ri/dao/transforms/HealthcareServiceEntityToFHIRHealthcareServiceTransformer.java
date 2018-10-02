package uk.nhs.careconnect.ri.dao.transforms;

import org.apache.commons.collections4.Transformer;
import org.hl7.fhir.dstu3.model.Meta;
import org.hl7.fhir.dstu3.model.HealthcareService;
import org.hl7.fhir.dstu3.model.Reference;
import org.springframework.stereotype.Component;
import uk.nhs.careconnect.ri.database.entity.healthcareService.*;
import uk.nhs.careconnect.ri.database.entity.healthcareService.*;


@Component
public class HealthcareServiceEntityToFHIRHealthcareServiceTransformer implements Transformer<HealthcareServiceEntity, HealthcareService> {

    
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(HealthcareServiceEntityToFHIRHealthcareServiceTransformer.class);
    

    @Override
    public HealthcareService transform(final HealthcareServiceEntity serviceEntity) {
        final HealthcareService service = new HealthcareService();

        Meta meta = new Meta(); //.addProfile(CareConnectProfile.Location_1);

        if (serviceEntity.getUpdated() != null) {
            meta.setLastUpdated(serviceEntity.getUpdated());
        }
        else {
            if (serviceEntity.getCreated() != null) {
                meta.setLastUpdated(serviceEntity.getCreated());
            }
        }
        service.setMeta(meta);

        service.setId(serviceEntity.getId().toString());

        for(HealthcareServiceIdentifier identifier : serviceEntity.getIdentifiers())
        {
            service.addIdentifier()
                    .setSystem(identifier.getSystem().getUri())
                    .setValue(identifier.getValue());
        }

        if (serviceEntity.getActive() != null) {
            service.setActive(serviceEntity.getActive());
        }
        if (serviceEntity.getName() != null) {
            service.setName(serviceEntity.getName());
        }

        if (serviceEntity.getComment() != null) {
            service.setComment(serviceEntity.getComment());
        }

        if (serviceEntity.getCategory() != null) {
            service.getCategory()
                    .addCoding()
                    .setDisplay(serviceEntity.getCategory().getDisplay())
                    .setSystem(serviceEntity.getCategory().getSystem())
                    .setCode(serviceEntity.getCategory().getCode());
        }

        System.out.println("Provided By: " + serviceEntity.getProvidedBy().getId());
        if (serviceEntity.getProvidedBy() != null) {
            service.setProvidedBy(new Reference("Organization/"+serviceEntity.getProvidedBy().getId()));
        }
        for (HealthcareServiceSpecialty serviceSpecialty : serviceEntity.getSpecialties()) {
            service.addSpecialty()
                    .addCoding()
                        .setCode(serviceSpecialty.getSpecialty().getCode())
                        .setSystem(serviceSpecialty.getSpecialty().getSystem())
                        .setDisplay(serviceSpecialty.getSpecialty().getDisplay());
        }
        for (HealthcareServiceLocation serviceLocation : serviceEntity.getLocations()) {
            service.addLocation(new Reference("Location/"+serviceLocation.getLocation().getId()));
        }

        for (HealthcareServiceTelecom serviceTelecom : serviceEntity.getTelecoms()) {
            service.addTelecom()
                    .setSystem(serviceTelecom.getSystem())
                    .setValue(serviceTelecom.getValue())
                    .setUse(serviceTelecom.getTelecomUse());

        }
/*        for (HealthcareServiceType serviceType : serviceEntity.getTypes()) {
            service.addType()
                    .addCoding()
                    .setCode(serviceType.getType_().getCode())
                    .setSystem(serviceType.getType_().getSystem())
                    .setDisplay(serviceType.getType_().getDisplay());
        }*/


        return service;

    }
}
