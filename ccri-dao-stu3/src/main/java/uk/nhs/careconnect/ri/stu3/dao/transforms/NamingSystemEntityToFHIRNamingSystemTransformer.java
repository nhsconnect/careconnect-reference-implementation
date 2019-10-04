package uk.nhs.careconnect.ri.stu3.dao.transforms;


import ca.uhn.fhir.context.FhirContext;
import org.apache.commons.collections4.Transformer;
import org.hl7.fhir.dstu3.model.NamingSystem;
import org.hl7.fhir.dstu3.model.Period;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import uk.nhs.careconnect.ri.database.entity.namingSystem.NamingSystemEntity;
import uk.nhs.careconnect.ri.database.entity.namingSystem.NamingSystemUniqueId;


@Component
public class NamingSystemEntityToFHIRNamingSystemTransformer implements Transformer
        <NamingSystemEntity, NamingSystem> {

    private static final Logger log = LoggerFactory.getLogger(NamingSystemEntityToFHIRNamingSystemTransformer.class);



    public NamingSystem transform(final NamingSystemEntity namingSystemEntity, FhirContext ctx) {
        final NamingSystem namingSystem = (NamingSystem) ctx.newJsonParser().parseResource(namingSystemEntity.getResource());

        namingSystem.setId(namingSystemEntity.getId().toString());

        return namingSystem;

    }

    @Override
    public NamingSystem transform(final NamingSystemEntity namingSystemEntity) {
        final NamingSystem namingSystem = new NamingSystem();


        namingSystem.setId(namingSystemEntity.getId().toString());

        namingSystem.setStatus(namingSystemEntity.getStatus());



        if (namingSystemEntity.getName() != null) {
            namingSystem.setName(namingSystemEntity.getName());
        }

        namingSystem.setStatus(namingSystemEntity.getStatus());



        for (NamingSystemUniqueId uniqueId : namingSystemEntity.getNamingSystemUniqueIds()) {
            NamingSystem.NamingSystemUniqueIdComponent uniqueIdComponent = namingSystem.addUniqueId();
            if (uniqueId.getIdentifierType() != null) {
                uniqueIdComponent.setType(uniqueId.getIdentifierType());
            }
            if (uniqueId.getValue() != null) {
                uniqueIdComponent.setValue(uniqueId.getValue());
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
