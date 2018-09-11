package uk.nhs.careconnect.ri.dao.transforms;

import org.apache.commons.collections4.Transformer;
import org.hl7.fhir.dstu3.model.*;
import org.springframework.stereotype.Component;
import uk.nhs.careconnect.ri.database.entity.immunisation.ImmunisationEntity;
import uk.nhs.careconnect.ri.database.entity.immunisation.ImmunisationIdentifier;
import uk.org.hl7.fhir.core.Stu3.CareConnectExtension;
import uk.org.hl7.fhir.core.Stu3.CareConnectProfile;

@Component
public class ImmunisationEntityToFHIRImmunizationTransformer implements Transformer<ImmunisationEntity, Immunization> {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ImmunisationEntityToFHIRImmunizationTransformer.class);


    @Override
    public Immunization transform(final ImmunisationEntity immunisationEntity) {
        final Immunization immunisation = new Immunization();

        Meta meta = new Meta().addProfile(CareConnectProfile.Immunization_1);

        if (immunisationEntity.getUpdated() != null) {
            meta.setLastUpdated(immunisationEntity.getUpdated());
        }
        else {
            if (immunisationEntity.getCreated() != null) {
                meta.setLastUpdated(immunisationEntity.getCreated());
            }
        }
        immunisation.setMeta(meta);

        immunisation.setId(immunisationEntity.getId().toString());


        if (immunisationEntity.getPatient() != null) {
            immunisation
                    .setPatient(new Reference("Patient/"+immunisationEntity.getPatient().getId())
                    .setDisplay(immunisationEntity.getPatient().getNames().get(0).getDisplayName()));
        }
        if (immunisationEntity.getNotGiven() !=null) {
            immunisation.setNotGiven(immunisationEntity.getNotGiven());
        }
        if (immunisationEntity.getAdministrationDate() != null) {
            immunisation.setDate(immunisationEntity.getAdministrationDate());
        }
        if (immunisationEntity.getExpirationDate() != null) {
            immunisation.setExpirationDate(immunisationEntity.getExpirationDate());
        }
        if (immunisationEntity.getEncounter() != null) {
            immunisation.setEncounter(new Reference("Encounter/"+immunisationEntity.getEncounter().getId()));
        }
        if (immunisationEntity.getLocation() != null) {
            immunisation
                    .setLocation(new Reference("Location/"+immunisationEntity.getLocation().getId()))
                    .getLocation()
                    .setDisplay(immunisationEntity.getLocation().getName());
        }
        if (immunisationEntity.getLotNumber() != null) {
            immunisation.setLotNumber(immunisationEntity.getLotNumber());
        }
        if (immunisationEntity.getNote() != null) {
            // TODO immunisation.setNote()
        }

        if (immunisationEntity.getOrganisation() != null) {
            immunisation
                    .setManufacturer(new Reference("Organization/"+immunisationEntity.getOrganisation().getId()))
                    .getManufacturer().setDisplay(immunisationEntity.getOrganisation().getName());
        }
        if (immunisationEntity.getStatus() != null) {
            immunisation.setStatus(immunisationEntity.getStatus());
        }
        if (immunisationEntity.getValueQuantity() != null) {
            SimpleQuantity qty = new SimpleQuantity();
            qty.setValue(immunisationEntity.getValueQuantity());

            if (immunisationEntity.getValueUnitOfMeasure() != null) {
                qty.setUnit(immunisationEntity.getValueUnitOfMeasure().getCode());
            }
            immunisation.setDoseQuantity(qty);
        }
        if (immunisationEntity.getPrimarySource() != null) {
            immunisation.setPrimarySource(immunisationEntity.getPrimarySource());
        }
        if (immunisationEntity.getPractitioner() != null) {
            immunisation
                    .addPractitioner()
                        .setActor(new Reference("Practitioner/"+immunisationEntity.getPractitioner().getId()))
                    .getActor().setDisplay(immunisationEntity.getPractitioner().getNames().get(0).getDisplayName());
        }

        if (immunisationEntity.getExplanationReasonGiven() != null) {
            immunisation.getExplanation().addReason()
                    .addCoding()
                    .setCode(immunisationEntity.getExplanationReasonGiven().getCode())
                    .setSystem(immunisationEntity.getExplanationReasonGiven().getSystem())
                    .setDisplay(immunisationEntity.getExplanationReasonGiven().getDisplay());
        } else {
            if (immunisationEntity.getExplanationReasonNotGiven() != null) {
                immunisation.getExplanation().addReasonNotGiven()
                        .addCoding()
                        .setCode(immunisationEntity.getExplanationReasonNotGiven().getCode())
                        .setSystem(immunisationEntity.getExplanationReasonNotGiven().getSystem())
                        .setDisplay(immunisationEntity.getExplanationReasonNotGiven().getDisplay());
            }
        }


        for (ImmunisationIdentifier identifier : immunisationEntity.getIdentifiers()) {
            immunisation.addIdentifier()
                    .setSystem(identifier.getSystem().getUri())
                    .setValue(identifier.getValue());
        }
        if (immunisationEntity.getReportOrigin() != null) {
            immunisation.getReportOrigin().addCoding()
                    .setCode(immunisationEntity.getReportOrigin().getCode())
                    .setSystem(immunisationEntity.getReportOrigin().getSystem())
                    .setDisplay(immunisationEntity.getReportOrigin().getDisplay());
        }
        if (immunisationEntity.getSite() != null) {
            immunisation.getSite().addCoding()
                    .setCode(immunisationEntity.getSite().getCode())
                    .setSystem(immunisationEntity.getSite().getSystem())
                    .setDisplay(immunisationEntity.getSite().getDisplay());
        }
        if (immunisationEntity.getRoute() != null) {
            immunisation.getRoute().addCoding()
                    .setCode(immunisationEntity.getRoute().getCode())
                    .setSystem(immunisationEntity.getRoute().getSystem())
                    .setDisplay(immunisationEntity.getRoute().getDisplay());
        }
        if (immunisationEntity.getVacinationCode() != null) {
            immunisation.getVaccineCode().addCoding()
                    .setCode(immunisationEntity.getVacinationCode().getCode())
                    .setSystem(immunisationEntity.getVacinationCode().getSystem())
                    .setDisplay(immunisationEntity.getVacinationCode().getDisplay());
        }

        // UK Care Connect Extensions
        if (immunisationEntity.getRecordedDate() != null) {
            immunisation.addExtension()
                    .setUrl(CareConnectExtension.UrlImmunizationDateRecorded)
                    .setValue(new DateTimeType().setValue(immunisationEntity.getRecordedDate()));
        }
        if (immunisationEntity.getParentPresent() != null) {
            immunisation.addExtension()
                    .setUrl(CareConnectExtension.UrlImmunizationParentPresent)
                    .setValue(new BooleanType().setValue(immunisationEntity.getParentPresent()));
        }

        return immunisation;

    }
}
