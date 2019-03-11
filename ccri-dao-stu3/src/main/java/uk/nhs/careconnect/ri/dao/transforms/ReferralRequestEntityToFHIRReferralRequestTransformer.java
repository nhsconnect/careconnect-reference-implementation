package uk.nhs.careconnect.ri.dao.transforms;

import org.apache.commons.collections4.Transformer;
import org.hl7.fhir.dstu3.model.*;
import org.springframework.stereotype.Component;
import uk.nhs.careconnect.ri.dao.daoutils;
import uk.nhs.careconnect.ri.database.entity.referral.*;
import uk.nhs.careconnect.ri.database.entity.referral.*;


@Component
public class ReferralRequestEntityToFHIRReferralRequestTransformer implements Transformer<ReferralRequestEntity, ReferralRequest> {


    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ReferralRequestEntityToFHIRReferralRequestTransformer.class);


    @Override
    public ReferralRequest transform(final ReferralRequestEntity referralRequestEntity) {
        final ReferralRequest referral = new ReferralRequest();

        Meta meta = new Meta(); //.addProfile(CareConnectProfile.Location_1);

        if (referralRequestEntity.getUpdated() != null) {
            meta.setLastUpdated(referralRequestEntity.getUpdated());
        }
        else {
            if (referralRequestEntity.getCreated() != null) {
                meta.setLastUpdated(referralRequestEntity.getCreated());
            }
        }
        referral.setMeta(meta);

        referral.setId(referralRequestEntity.getId().toString());

        for(ReferralRequestIdentifier identifier : referralRequestEntity.getIdentifiers())
        {
            Identifier ident = referral.addIdentifier();
            ident = daoutils.getIdentifier(identifier, ident);
        }


        if (referralRequestEntity.getIntent() != null) {
            referral.setIntent(referralRequestEntity.getIntent());
        }
        if (referralRequestEntity.getPriority() !=null) {
            referral.setPriority(referralRequestEntity.getPriority());
        }
        if (referralRequestEntity.getStatus() != null) {
            referral.setStatus(referralRequestEntity.getStatus());
        }
        for (ReferralRequestServiceRequested service : referralRequestEntity.getServices()) {
            referral.addServiceRequested()
                    .addCoding()
                    .setSystem(service.getService().getSystem())
                    .setCode(service.getService().getCode())
                    .setDisplay(service.getService().getDisplay());
        }
        if (referralRequestEntity.getPatient() != null) {
            referral.setSubject(new Reference("Patient/" + referralRequestEntity.getPatient().getId())
                    .setDisplay(referralRequestEntity.getPatient().getNames().get(0).getDisplayName()));
        }
        if (referralRequestEntity.getType() != null) {
            referral.getType()
                    .addCoding()
                    .setDisplay(referralRequestEntity.getType().getDisplay())
                    .setSystem(referralRequestEntity.getType().getSystem())
                    .setCode(referralRequestEntity.getType().getCode());
        }
        if (referralRequestEntity.getSpecialty() != null) {
            referral.getSpecialty()
                    .addCoding()
                    .setDisplay(referralRequestEntity.getSpecialty().getDisplay())
                    .setSystem(referralRequestEntity.getSpecialty().getSystem())
                    .setCode(referralRequestEntity.getSpecialty().getCode());
        }
        if (referralRequestEntity.getContextEncounter() != null) {
            referral.setContext(new Reference("Encounter/" + referralRequestEntity.getContextEncounter().getId()));
        }
        if (referralRequestEntity.getAuthoredOn() != null) {
            referral.setAuthoredOn(referralRequestEntity.getAuthoredOn());
        }
        if (referralRequestEntity.getRequesterOrganisation() != null) {
            referral.getRequester().setAgent(new Reference("Organization/" + referralRequestEntity.getRequesterOrganisation().getId()).setDisplay(referralRequestEntity.getRequesterOrganisation().getName()));
        }
        if (referralRequestEntity.getRequesterPractitioner() != null) {
            referral.getRequester().setAgent(new Reference("Practitioner/" + referralRequestEntity.getRequesterPractitioner().getId()).setDisplay(referralRequestEntity.getRequesterPractitioner().getNames().get(0).getDisplayName()));
        }
        if (referralRequestEntity.getRequesterPatient() != null) {
            referral.getRequester().setAgent(new Reference("Patient/" + referralRequestEntity.getRequesterPatient().getId()));
        }
        if (referralRequestEntity.getOnBehalfOrganisation() != null) {
            referral.getRequester().setOnBehalfOf(new Reference("Organization/" + referralRequestEntity.getOnBehalfOrganisation().getId()).setDisplay(referralRequestEntity.getOnBehalfOrganisation().getName()));
        }
        for (ReferralRequestReason reason : referralRequestEntity.getReasons()) {
            referral.addReasonCode()
                    .addCoding()
                    .setSystem(reason.getReason().getSystem())
                    .setCode(reason.getReason().getCode())
                    .setDisplay(reason.getReason().getDisplay());
        }
        for (ReferralRequestReasonReference reason : referralRequestEntity.getReasonReferences()) {
            if (reason.getCondition() != null) {
                referral.addReasonReference(new Reference("Condition/"+reason.getCondition().getId()).setDisplay(reason.getCondition().getCode().getDisplay()));
            }
            if (reason.getObservation() != null) {
                referral.addReasonReference(new Reference("Observation/"+reason.getObservation().getId()).setDisplay(reason.getObservation().getCode().getDisplay()));
            }
        }
        for (ReferralRequestRecipient recipient : referralRequestEntity.getRecipients()) {
            if (recipient.getOrganisation() != null) {
                referral.addRecipient(new Reference("Organization/" + recipient.getOrganisation().getId()).setDisplay(recipient.getOrganisation().getName()));
            }
            if (recipient.getPractitioner() != null) {
                referral.addRecipient(new Reference("Practitioner/" + recipient.getPractitioner().getId()).setDisplay(recipient.getPractitioner().getNames().get(0).getDisplayName()));
            }
            if (recipient.getService() != null) {
                referral.addRecipient(new Reference("HealthcareService/" + recipient.getService().getId()).setDisplay(recipient.getService().getName()));
            }
        }

        return referral;

    }
}
