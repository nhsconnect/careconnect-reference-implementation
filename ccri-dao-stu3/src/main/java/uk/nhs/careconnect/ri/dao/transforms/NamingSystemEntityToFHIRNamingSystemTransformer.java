package uk.nhs.careconnect.ri.dao.transforms;


import org.apache.commons.collections4.Transformer;
import org.hl7.fhir.dstu3.model.NamingSystem;
import org.hl7.fhir.dstu3.model.Period;
import org.hl7.fhir.dstu3.model.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import uk.nhs.careconnect.ri.database.entity.namingSystem.NamingSystemEntity;
import uk.nhs.careconnect.ri.database.entity.namingSystem.NamingSystemTelecom;
import uk.nhs.careconnect.ri.database.entity.namingSystem.NamingSystemUniqueId;


@Component
public class NamingSystemEntityToFHIRNamingSystemTransformer implements Transformer<NamingSystemEntity, NamingSystem> {

    private static final Logger log = LoggerFactory.getLogger(NamingSystemEntityToFHIRNamingSystemTransformer.class);

    @Override
    public NamingSystem transform(final NamingSystemEntity namingSystemEntity) {
        final NamingSystem namingSystem = new NamingSystem();


        namingSystem.setId(namingSystemEntity.getId().toString());

        namingSystem.setStatus(namingSystemEntity.getStatus());



        if (namingSystemEntity.getName() != null) {
            namingSystem.setName(namingSystemEntity.getName());
        }

        namingSystem.setStatus(namingSystemEntity.getStatus());


        if (namingSystemEntity.getKind() != null) {
            namingSystem.setKind(namingSystemEntity.getKind());
        }
        if (namingSystemEntity.getChangedDate() != null) {
            namingSystem.setDate(namingSystemEntity.getChangedDate());
        }
        if (namingSystemEntity.getPublisher() != null) {
            namingSystem.setPublisher(namingSystemEntity.getPublisher());
        }
        if (namingSystemEntity.getDescription()!= null) {
            namingSystem.setDescription(namingSystemEntity.getDescription());
        }
        if (namingSystemEntity.getUsage() != null) {
            namingSystem.setUsage(namingSystemEntity.getUsage());
        }
        if (namingSystemEntity.get_type() != null) {
            namingSystem.getType()
                    .addCoding()
                        .setDisplay(namingSystemEntity.get_type().getDisplay())
                        .setSystem(namingSystemEntity.get_type().getSystem())
                        .setCode(namingSystemEntity.get_type().getCode());
        }


        if (namingSystemEntity.getReplacedBy() != null) {
            namingSystem.setReplacedBy(new Reference("NamingSystem/"+namingSystemEntity.getReplacedBy().getId().toString()));
        }


        // Hard coded to not attempt to retrieve SNOMED!


        for (NamingSystemTelecom telecom : namingSystemEntity.getContacts()) {
            namingSystem.addContact()
                    .addTelecom()
                    .setUse(telecom.getTelecomUse())
                    .setValue(telecom.getValue())
                    .setSystem(telecom.getSystem());
        }


        for (NamingSystemUniqueId uniqueId : namingSystemEntity.getNamingSystemUniqueIds()) {
            NamingSystem.NamingSystemUniqueIdComponent uniqueIdComponent = namingSystem.addUniqueId();
            if (uniqueId.getIdentifierType() != null) {
                uniqueIdComponent.setType(uniqueId.getIdentifierType());
            }
            if (uniqueId.get_value() != null) {
                uniqueIdComponent.setValue(uniqueId.get_value());
            }
            if (uniqueId.getPreferred() != null) {
                uniqueIdComponent.setPreferred(uniqueId.getPreferred());
            }
            if (uniqueId.getComment() != null) {
                uniqueIdComponent.setComment(uniqueId.getComment());
            }
            if (uniqueId.getPeriodStart() != null || uniqueId.getPeriodEnd() != null) {
                Period period = new Period();
                if (uniqueId.getPeriodStart() != null ) {
                    period.setStart(uniqueId.getPeriodStart());
                }
                if (uniqueId.getPeriodEnd() != null) {
                    period.setEnd(uniqueId.getPeriodEnd());
                }
                uniqueIdComponent.setPeriod(period);
            }
        }

        return namingSystem;


    }
}
