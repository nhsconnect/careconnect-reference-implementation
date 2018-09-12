package uk.nhs.careconnect.ri.dao.transforms;

import org.apache.commons.collections4.Transformer;
import org.hl7.fhir.dstu3.model.*;
import org.springframework.stereotype.Component;
import uk.nhs.careconnect.ri.database.entity.consent.*;
import uk.nhs.careconnect.ri.database.entity.consent.*;

@Component
public class ConsentEntityToFHIRConsentTransformer implements Transformer<ConsentEntity, Consent> {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ConsentEntity.class);


    @Override
    public Consent transform(final ConsentEntity consentEntity) {
        final Consent consent = new Consent();

        Meta meta = new Meta();

        if (consentEntity.getUpdated() != null) {
            meta.setLastUpdated(consentEntity.getUpdated());
        }
        else {
            if (consentEntity.getCreated() != null) {
                meta.setLastUpdated(consentEntity.getCreated());
            }
        }
        consent.setMeta(meta);

        consent.setId(consentEntity.getId().toString());

        if (consentEntity.getStatus() != null) {
            consent.setStatus(consentEntity.getStatus());
        }


        
        if (consentEntity.getPatient() != null) {
            consent
                    .setPatient(new Reference("Patient/"+consentEntity.getPatient().getId())
                    .setDisplay(consentEntity.getPatient().getNames().get(0).getDisplayName()));
        }


        if (consentEntity.getPeriodEndDateTime() != null) {
            Period period = new Period();
            if (consentEntity.getPeriodStartDateTime() != null) {
                period.setStart(consentEntity.getPeriodStartDateTime());
            }
            period.setEnd(consentEntity.getPeriodEndDateTime());
            consent.setPeriod(period);
        } else  if (consentEntity.getPeriodStartDateTime() != null) {
            Period period = new Period();
            period.setStart(consentEntity.getPeriodStartDateTime());
            consent.setPeriod(period);
        }

        if (consentEntity.getDateTime() != null) {
            consent.setDateTime(consentEntity.getDateTime());
        }


        for (ConsentIdentifier identifier : consentEntity.getIdentifiers()) {
            consent.getIdentifier()
                .setSystem(identifier.getSystem().getUri())
                .setValue(identifier.getValue());
        }

        for (ConsentActor consentActor : consentEntity.getActors()) {
            Consent.ConsentActorComponent consentActorComponent = consent.addActor();
            if (consentActor.getRoleCode() != null) {
                consentActorComponent.getRole().addCoding()
                        .setCode(consentActor.getRoleCode().getCode())
                        .setSystem(consentActor.getRoleCode().getSystem())
                        .setDisplay(consentActor.getRoleCode().getDisplay());
            }
            if (consentActor.getReferenceCareTeam() != null) {
                consentActorComponent.setReference(new Reference("CareTeam/"+consentActor.getReferenceCareTeam().getId())
                        .setDisplay(consentActor.getReferenceCareTeam().getName()));
            }
            if (consentActor.getReferenceOrganisation() != null) {
                consentActorComponent.setReference(new Reference("Organization/"+consentActor.getReferenceOrganisation().getId())
                        .setDisplay(consentActor.getReferenceOrganisation().getName()));
            }
            if (consentActor.getReferencePatient() != null) {
                consentActorComponent.setReference(new Reference("Patient/"+consentActor.getReferencePatient().getId())
                        .setDisplay(consentActor.getReferencePatient().getNames().get(0).getDisplayName()));
            }
            if (consentActor.getReferencePerson() != null) {
                consentActorComponent.setReference(new Reference("RelatedPerson/"+consentActor.getReferencePerson().getId())
                        .setDisplay(consentActor.getReferencePerson().getNames().get(0).getDisplayName()));
            }
            if (consentActor.getReferencePractitioner() != null) {
                consentActorComponent.setReference(new Reference("Practitioner/"+consentActor.getReferencePractitioner().getId())
                        .setDisplay(consentActor.getReferencePractitioner().getNames().get(0).getDisplayName()));
            }
        }

        for (ConsentParty consentParty : consentEntity.getParties()) {

            if (consentParty.getReferenceOrganisation() != null) {
                consent.addConsentingParty(new Reference("Organization/"+consentParty.getReferenceOrganisation().getId()));
            }
            if (consentParty.getReferencePatient() != null) {
                consent.addConsentingParty(new Reference("Patient/"+consentParty.getReferencePatient().getId()));
            }
            if (consentParty.getReferencePerson() != null) {
                consent.addConsentingParty(new Reference("RelatedPerson/"+consentParty.getReferencePerson().getId()));
            }
            if (consentParty.getReferencePractitioner() != null) {
                consent.addConsentingParty(new Reference("Practitioner/"+consentParty.getReferencePractitioner().getId()));
            }
        }
        for (ConsentOrganisation consentOrganisation : consentEntity.getOrganisations()) {
            consent.addOrganization(new Reference("Organization/"+consentOrganisation.getOrganisation().getId()));
        }

        for (ConsentPolicy consentPolicy : consentEntity.getPolicies()) {
            Consent.ConsentPolicyComponent consentPolicyComponent = consent.addPolicy();
            if (consentPolicy.getAuthority() != null) {
                consentPolicyComponent.setAuthority(consentPolicy.getAuthority());
            }
            if (consentPolicy.getPolicyUri() != null) {
                consentPolicyComponent.setUri(consentPolicy.getPolicyUri());
            }
        }
        for (ConsentPurpose consentPurpose : consentEntity.getPurposes()) {
            if (consentPurpose.getPurpose() != null) {
                consent.addPurpose()
                        .setCode(consentPurpose.getPurpose().getCode())
                        .setDisplay(consentPurpose.getPurpose().getDisplay())
                        .setSystem(consentPurpose.getPurpose().getSystem());
            }
        }

        return consent;

    }
}
