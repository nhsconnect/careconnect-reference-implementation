package uk.nhs.careconnect.ri.dao.transforms;

import org.apache.commons.collections4.Transformer;
import org.hl7.fhir.dstu3.model.*;
import org.springframework.stereotype.Component;
import uk.nhs.careconnect.ri.database.entity.list.ListEntity;
import uk.nhs.careconnect.ri.database.entity.list.ListIdentifier;
import uk.nhs.careconnect.ri.database.entity.list.ListItem;

@Component
public class ListEntityToFHIRListResourceTransformer implements Transformer<ListEntity
        , ListResource> {



    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ListEntityToFHIRListResourceTransformer.class);



    private ListResource list;

    @Override
    public ListResource transform(final ListEntity listEntity) {
        list = new ListResource();

        Meta meta = new Meta();

        if (listEntity.getUpdated() != null) {
            meta.setLastUpdated(listEntity.getUpdated());
        }
        else {
            if (listEntity.getCreated() != null) {
                meta.setLastUpdated(listEntity.getCreated());
            }
        }
        list.setMeta(meta);

        list.setId(listEntity.getId().toString());

        for(ListIdentifier identifier : listEntity.getIdentifiers())
        {
            list.addIdentifier()
                    .setSystem(identifier.getSystem().getUri())
                    .setValue(identifier.getValue());
        }

        if (listEntity.getStatus() != null){
            list.setStatus(listEntity.getStatus());
        }

        if (listEntity.getMode() != null) {
            list.setMode(listEntity.getMode());
        }

        if (listEntity.getTitle() != null) {
            list.setTitle(listEntity.getTitle());
        }

        if (listEntity.getCode() != null) {
            list.getCode().addCoding()
                    .setSystem(listEntity.getCode().getSystem())
                    .setCode(listEntity.getCode().getCode())
                    .setDisplay(listEntity.getCode().getDisplay());
        }

        if (listEntity.getPatient() != null) {
            list.setSubject(new Reference("Patient/"+listEntity.getPatient().getId()).setDisplay(listEntity.getPatient().getNames().get(0).getDisplayName()));
        }
        if (listEntity.getContextEncounter() != null) {
            list.setEncounter(new Reference(("Encounter/"+listEntity.getContextEncounter().getId())));
        }

        if (listEntity.getDateTime() != null) {
            list.setDate(listEntity.getDateTime());
        }

        if (listEntity.getSourcePatient() != null) {
            list.setSource(new Reference("Patient/"+listEntity.getSourcePatient().getId()).setDisplay(listEntity.getSourcePatient().getNames().get(0).getDisplayName()));
        }
        if (listEntity.getSourcePractitioner() != null) {
            list.setSource(new Reference("Practitioner/"+listEntity.getSourcePractitioner().getId()).setDisplay(listEntity.getSourcePractitioner().getNames().get(0).getDisplayName()));
        }

        if (listEntity.getNote() != null) {
            list.addNote(new Annotation().setText(listEntity.getNote()));
        }

        for (ListItem itemEntity : listEntity.getItems()) {
           ListResource.ListEntryComponent item = list.addEntry();
           getItem(itemEntity,item);
        }

        return list;

    }

    private void getItem(ListItem itemEntity,ListResource.ListEntryComponent item ) {

        if (itemEntity.getFlag() != null) {
            item.getFlag().addCoding()
                    .setCode(itemEntity.getFlag().getCode())
                    .setDisplay(itemEntity.getFlag().getDisplay())
                    .setSystem(itemEntity.getFlag().getSystem());
        }

        if (itemEntity.getItemDeleted() != null) {
            item.setDeleted(itemEntity.getItemDeleted());
        }

         if (itemEntity.getReferenceCondition() != null) {
                item.setItem(new Reference("Condition/"+itemEntity.getReferenceCondition().getId())
                );
            }
        else if (itemEntity.getReferenceObservation() != null) {
            item.setItem(new Reference("Observation/"+itemEntity.getReferenceObservation().getId())
            );
        }

    }

}
