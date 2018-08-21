package uk.nhs.careconnect.ri.daointerface.transforms;

import org.apache.commons.collections4.Transformer;
import org.hl7.fhir.dstu3.model.*;
import org.springframework.stereotype.Component;
import uk.nhs.careconnect.ri.entity.consent.ConsentEntity;
import uk.nhs.careconnect.ri.entity.consent.ConsentIdentifier;

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



        return consent;

    }
}
