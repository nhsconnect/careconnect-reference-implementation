package uk.nhs.careconnect.ri.stu3.dao.transforms;

import org.apache.commons.collections4.Transformer;
import org.hl7.fhir.dstu3.model.*;
import org.springframework.stereotype.Component;
import uk.nhs.careconnect.ri.stu3.dao.daoutils;
import uk.nhs.careconnect.ri.database.entity.flag.FlagEntity;
import uk.nhs.careconnect.ri.database.entity.flag.FlagIdentifier;

@Component
public class FlagEntityToFHIRFlagTransformer implements Transformer<FlagEntity
        , Flag> {



    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(FlagEntityToFHIRFlagTransformer.class);


    private Flag flag;

    @Override
    public Flag transform(final FlagEntity flagEntity) {
        flag = new Flag();

        Meta meta = new Meta();

        if (flagEntity.getUpdated() != null) {
            meta.setLastUpdated(flagEntity.getUpdated());
        }
        else {
            if (flagEntity.getCreated() != null) {
                meta.setLastUpdated(flagEntity.getCreated());
            }
        }
        meta.addProfile("https://fhir.hl7.org.uk/STU3/StructureDefinition/CareConnect-Flag-1");

        flag.setMeta(meta);

        flag.setId(flagEntity.getId().toString());

        for(FlagIdentifier identifier : flagEntity.getIdentifiers())
        {
            Identifier ident =flag.addIdentifier();
            ident = daoutils.getIdentifier(identifier, ident);
        }

        if (flagEntity.getStatus() != null){
            flag.setStatus(flagEntity.getStatus());
        }


        if (flagEntity.getCode() != null) {
            flag.getCode().addCoding()
                    .setSystem(flagEntity.getCode().getSystem())
                    .setCode(flagEntity.getCode().getCode())
                    .setDisplay(flagEntity.getCode().getDisplay());
        }

        if (flagEntity.getCategory() != null) {
            flag.getCategory().addCoding()
                    .setSystem(flagEntity.getCategory().getSystem())
                    .setCode(flagEntity.getCategory().getCode())
                    .setDisplay(flagEntity.getCategory().getDisplay());
        }


        if (flagEntity.getPatient() != null) {
            flag.setSubject(new Reference("Patient/"+flagEntity.getPatient().getId()).setDisplay(flagEntity.getPatient().getNames().get(0).getDisplayName()));
        }
        if (flagEntity.getEncounter() != null) {
            flag.setEncounter(new Reference(("Encounter/"+flagEntity.getEncounter().getId())));
        }

        if (flagEntity.getStartDateTime() != null) {
            flag.getPeriod().setStart(flagEntity.getStartDateTime());
        }
        if (flagEntity.getEndDateTime() != null) {
            flag.getPeriod().setEnd(flagEntity.getEndDateTime());
        }


        if (flagEntity.getAuthorPractitioner() != null) {
            flag.setAuthor(new Reference("Practitioner/"+flagEntity.getAuthorPractitioner().getId()).setDisplay(flagEntity.getAuthorPractitioner().getNames().get(0).getDisplayName()));
        }

        if (flagEntity.getAuthorOrganisation() != null) {
            flag.setAuthor(new Reference("Organization/"+flagEntity.getAuthorOrganisation().getId()).setDisplay(flagEntity.getAuthorOrganisation().getName()));
        }


        return flag;

    }


}
