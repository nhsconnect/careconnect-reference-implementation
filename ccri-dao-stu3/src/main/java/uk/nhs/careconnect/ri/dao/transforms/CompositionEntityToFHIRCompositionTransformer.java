package uk.nhs.careconnect.ri.dao.transforms;

import org.apache.commons.collections4.Transformer;
import org.hl7.fhir.dstu3.model.*;
import org.springframework.stereotype.Component;
import uk.nhs.careconnect.ri.database.entity.composition.CompositionEntity;
import uk.nhs.careconnect.ri.database.entity.composition.CompositionIdentifier;
import uk.nhs.careconnect.ri.database.entity.composition.CompositionSection;

@Component
public class CompositionEntityToFHIRCompositionTransformer implements Transformer<CompositionEntity, Composition> {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CompositionEntity.class);


    @Override
    public Composition transform(final CompositionEntity compositionEntity) {
        final Composition composition = new Composition();

        Meta meta = new Meta();
                //.addProfile(CareConnectProfile.Condition_1);

        if (compositionEntity.getUpdated() != null) {
            meta.setLastUpdated(compositionEntity.getUpdated());
        }
        else {
            if (compositionEntity.getCreated() != null) {
                meta.setLastUpdated(compositionEntity.getCreated());
            }
        }
        composition.setMeta(meta);

        composition.setId(compositionEntity.getId().toString());

        if (compositionEntity.getDate() != null) {
            composition.setDate(compositionEntity.getDate());
        }
        if (compositionEntity.getType() != null) {
            composition.getType().addCoding()
                    .setCode(compositionEntity.getType().getCode())
                    .setDisplay(compositionEntity.getType().getDisplay())
                    .setSystem(compositionEntity.getType().getSystem());
        }
        if (compositionEntity.getClass_() != null)
        {
            composition.getClass_().addCoding()
                    .setCode(compositionEntity.getClass_().getCode())
                    .setDisplay(compositionEntity.getClass_().getDisplay())
                    .setSystem(compositionEntity.getClass_().getSystem());
        }
        if (compositionEntity.getTitle() != null) {
            composition.setTitle(compositionEntity.getTitle());
        }

        if (compositionEntity.getStatus() != null) {
            composition.setStatus(compositionEntity.getStatus());
        }
        if (compositionEntity.getConfidentiality() != null) {
            composition.setConfidentiality(compositionEntity.getConfidentiality());
        }

        if (compositionEntity.getPatient() != null) {
            composition
                    .setSubject(new Reference("Patient/"+compositionEntity.getPatient().getId())
                    .setDisplay(compositionEntity.getPatient().getNames().get(0).getDisplayName()));
        }

        if (compositionEntity.getAuthorPractitioner() != null) {
            composition.addAuthor(
                    new Reference("Practitioner/"+compositionEntity.getAuthorPractitioner().getId())
                    .setDisplay(compositionEntity.getAuthorPractitioner().getNames().get(0).getDisplayName())
            );
        }

        if (compositionEntity.getAuthorPerson() != null) {
            composition.addAuthor(
                    new Reference("RelatedPerson/"+compositionEntity.getAuthorPerson().getId())
                            .setDisplay(compositionEntity.getAuthorPerson().getNames().get(0).getDisplayName())
            );
        }

        if (compositionEntity.getCustodianOrganisation() != null) {
            composition.setCustodian(new Reference("Organization/" + compositionEntity.getCustodianOrganisation().getId()));
        }

        if (compositionEntity.getEncounter() != null) {
            composition.setEncounter(new Reference("Encounter/"+compositionEntity.getEncounter().getId()));
        }

        for (CompositionSection section : compositionEntity.getSections()) {
            Composition.SectionComponent component = composition.addSection();
            if (section.getCode() != null) {
                component.getCode().addCoding()
                        .setCode(section.getCode().getCode())
                        .setDisplay(section.getCode().getDisplay())
                        .setSystem(section.getCode().getSystem());
            }
            if (section.getTitle() != null) {
                component.setTitle(section.getTitle());
            }
            if (section.getNarrative() != null) {
                Narrative text = new Narrative();
                text.setDivAsString(section.getNarrative());
                if (section.getNarrativeStatus()!=null) {
                    text.setStatus(section.getNarrativeStatus());
                }
                component.setText(text);
            }
            if (section.getOrderBy() !=null) {
                component.getOrderedBy().addCoding()
                        .setCode(section.getOrderBy().getCode())
                        .setDisplay(section.getOrderBy().getDisplay())
                        .setSystem(section.getOrderBy().getSystem());
            }
        }

        for (CompositionIdentifier identifier : compositionEntity.getIdentifiers()) {
            composition.getIdentifier()
                    .setSystem(identifier.getSystem().getUri())
                    .setValue(identifier.getValue());
        }

        return composition;

    }
}
