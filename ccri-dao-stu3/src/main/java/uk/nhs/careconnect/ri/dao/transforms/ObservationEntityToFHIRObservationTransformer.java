package uk.nhs.careconnect.ri.dao.transforms;

import org.apache.commons.collections4.Transformer;
import org.hl7.fhir.dstu3.model.*;
import org.springframework.stereotype.Component;
import uk.nhs.careconnect.ri.database.entity.observation.*;
import uk.nhs.careconnect.ri.database.entity.observation.*;


@Component
public class ObservationEntityToFHIRObservationTransformer implements Transformer<ObservationEntity, Observation> {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ObservationEntityToFHIRObservationTransformer.class);

    @Override
    public Observation transform(final ObservationEntity observationEntity) {

        // Bring back ^ASUM(nor,"X") and ^ASUM(nor,"N")

        Observation observation = new Observation();
        try {
            Meta meta = new Meta(); //.addProfile(CareConnectITKProfile.Observation_1);

            if (observationEntity.getUpdated() != null) {
                meta.setLastUpdated(observationEntity.getUpdated());
            } else {
                if (observationEntity.getCreated() != null) {
                    meta.setLastUpdated(observationEntity.getCreated());
                }
            }
            observation.setMeta(meta);

            observation.setId(observationEntity.getId().toString());

            if (observationEntity.getEffectiveDateTime() != null)
                observation.setEffective(new DateTimeType(observationEntity.getEffectiveDateTime()));

            if (observationEntity.getIssued() != null) observation.setIssued(observationEntity.getIssued());

            observation.setSubject(
                    new Reference("Patient/" + observationEntity.getPatient().getId())
                            .setDisplay(observationEntity.getPatient().getNames().get(0).getDisplayName()));

            if (observationEntity.getContext() != null) {
                observation.setContext(new Reference("Encounter/" + observationEntity.getContextEncounter().getId()));
            }

            if (observationEntity.getCode() != null) {
                observation.getCode().addCoding()
                        .setCode(observationEntity.getCode().getCode())
                        .setSystem(observationEntity.getCode().getSystem())
                        .setDisplay(observationEntity.getCode().getDisplay());
            }

            // Category

            for (ObservationCategory category : observationEntity.getCategories()) {
                if (category.getCategory() != null) {
                    CodeableConcept concept = new CodeableConcept();
                    concept.addCoding()
                            .setCode(category.getCategory().getCode())
                            .setSystem(category.getCategory().getSystem())
                            .setDisplay(category.getCategory().getDisplay());

                    observation.getCategory().add(concept);
                }
            }

            // Body Site

            if (observationEntity.getBodySite() != null) {
                observation.getBodySite().addCoding()
                        .setCode(observationEntity.getBodySite().getCode())
                        .setSystem(observationEntity.getBodySite().getSystem())
                        .setDisplay(observationEntity.getBodySite().getDisplay());
            }
            // Method

            if (observationEntity.getMethod() != null) {
                observation.getMethod().addCoding()
                        .setCode(observationEntity.getMethod().getCode())
                        .setSystem(observationEntity.getMethod().getSystem())
                        .setDisplay(observationEntity.getMethod().getDisplay());
            }

            // Interpretation

            if (observationEntity.getInterpretation() != null) {
                observation.getInterpretation().addCoding()
                        .setCode(observationEntity.getInterpretation().getCode())
                        .setSystem(observationEntity.getInterpretation().getSystem())
                        .setDisplay(observationEntity.getInterpretation().getDisplay());
            }

            if (observationEntity.getComments() != null) {
                observation.setComment(observationEntity.getComments());
            }
            // Performer

            for (ObservationPerformer performer : observationEntity.getPerformers()) {
                switch (performer.getPerformerType()) {
                    case Practitioner:
                        if (performer.getPerformerPractitioner() != null) {
                            observation.getPerformer().add(new Reference("Practitioner/" + performer.getPerformerPractitioner().getId())
                                    .setDisplay(performer.getPerformerPractitioner().getNames().get(0).getDisplayName()));
                        }
                        break;
                    case Patient:
                        if (performer.getPerformerPatient() != null) {
                            observation.getPerformer().add(new Reference("Patient/" + performer.getPerformerPatient().getId())
                                    .setDisplay(performer.getPerformerPatient().getNames().get(0).getDisplayName()));
                        }
                        break;
                    case Organisation:
                        if (performer.getPerformerOrganisation() != null)
                            observation.getPerformer().add(new Reference("Organization/" + performer.getPerformerOrganisation().getId())
                                    .setDisplay(performer.getPerformerOrganisation().getName()));
                        break;
                }
            }

            /// Referenece Range

            for (ObservationRange rangeEntity : observationEntity.getRanges()) {
                Observation.ObservationReferenceRangeComponent range = observation.addReferenceRange();

                if (rangeEntity.getLowQuantity() != null) {
                    SimpleQuantity qty = new SimpleQuantity();
                    qty.setValue(rangeEntity.getLowQuantity());
                    range.setLow(qty);
                }
                if (rangeEntity.getHighQuantity() != null) {
                    SimpleQuantity qty = new SimpleQuantity();
                    qty.setValue(rangeEntity.getHighQuantity());
                    range.setHigh(qty);
                }

                if (rangeEntity.getType() != null) {
                    CodeableConcept code = new CodeableConcept();
                    code.addCoding()
                            .setCode(rangeEntity.getType().getCode())
                            .setSystem(rangeEntity.getType().getSystem())
                            .setDisplay(rangeEntity.getType().getDisplay());

                    range.setType(code);
                }
                if (rangeEntity.getHighAgeRange() != null || rangeEntity.getLowAgeRange() != null) {
                    Range ageRange = range.getAge();
                    if (rangeEntity.getLowAgeRange() != null) {
                        SimpleQuantity qty = new SimpleQuantity();
                        qty.setValue(rangeEntity.getLowAgeRange());
                        ageRange.setLow(qty);
                    }
                    if (rangeEntity.getHighAgeRange() != null) {
                        SimpleQuantity qty = new SimpleQuantity();
                        qty.setValue(rangeEntity.getHighAgeRange());
                        ageRange.setHigh(qty);
                    }
                }
            }

            // Simple Value

            if (observationEntity.getValueQuantity() != null) {
                Quantity quantity = new Quantity();
                quantity.setValue(observationEntity.getValueQuantity());

                if (observationEntity.getValueUnitOfMeasure() != null) {
                    quantity.setCode(observationEntity.getValueUnitOfMeasure().getCode())
                            .setSystem(observationEntity.getValueUnitOfMeasure().getSystem())
                            .setUnit(observationEntity.getValueUnitOfMeasure().getCode());
                }
                observation.setValue(quantity);
            }

            // Components e.g. Blood Pressure
            // Plus ValueQuantity

            for (ObservationEntity componentEntity : observationEntity.getComponents()) {

                // Components only
                log.trace("OBS Component found");
                if (componentEntity.getObservationType() == null || componentEntity.getObservationType().equals(ObservationEntity.ObservationType.component)) {
                    Observation.ObservationComponentComponent component = new Observation.ObservationComponentComponent();
                    if (componentEntity.getCode() != null) {
                        log.trace("OBS Code found");
                        component.getCode().addCoding()
                                .setCode(componentEntity.getCode().getCode())
                                .setSystem(componentEntity.getCode().getSystem())
                                .setDisplay(componentEntity.getCode().getDisplay());
                    }

                    // Component Values simple

                    if (componentEntity.getValueQuantity() != null) {
                        log.trace("OBS Value found");
                        Quantity quantity = new Quantity();
                        quantity.setValue(componentEntity.getValueQuantity());

                        if (componentEntity.getValueUnitOfMeasure() != null) {
                                quantity.setCode(componentEntity.getValueUnitOfMeasure().getCode())
                                    .setSystem(componentEntity.getValueUnitOfMeasure().getSystem())
                                    .setUnit(componentEntity.getValueUnitOfMeasure().getCode());
                        }
                        component.setValue(quantity);
                    }

                    // Component Values CodeableConcept

                    for (ObservationEntity valueEntity : componentEntity.getComponents()) {
                        if (valueEntity.getObservationType().equals(ObservationEntity.ObservationType.valueQuantity)) {
                            CodeableConcept concept = new CodeableConcept();
                            concept.addCoding()
                                    .setDisplay(valueEntity.getCode().getDisplay())
                                    .setCode(valueEntity.getCode().getCode())
                                    .setSystem(valueEntity.getCode().getSystem());
                            component.setValue(concept);
                        }
                    }
                    log.trace("OBS addComponent");
                    observation.addComponent(component);
                }

                // Value Quantity

                if (componentEntity.getObservationType() != null && componentEntity.getObservationType().equals(ObservationEntity.ObservationType.valueQuantity)) {
                    log.trace("OBS valueQuantity");
                    CodeableConcept concept = new CodeableConcept();
                    concept.addCoding()
                            .setDisplay(componentEntity.getCode().getDisplay())
                            .setCode(componentEntity.getCode().getCode())
                            .setSystem(componentEntity.getCode().getSystem());
                    observation.setValue(concept);
                }
            }

            for (ObservationRelated relatedResource : observationEntity.getRelatedResources()) {
                Observation.ObservationRelatedComponent related = observation.addRelated();
                if (relatedResource.getType() != null) {
                    related.setType(relatedResource.getType());
                }
                if (relatedResource.getRelatedObservation() != null) {
                    related.setTarget(new Reference("Observation/"+relatedResource.getRelatedObservation().getId()));
                } else if (relatedResource.getRelatedForm() != null) {
                    related.setTarget(new Reference("QuestionnaireResponse/"+relatedResource.getRelatedForm().getId()));
                }
            }

            observation.setStatus(observationEntity.getStatus());

            for (ObservationIdentifier identifier : observationEntity.getIdentifiers()) {
                observation.addIdentifier()
                        .setSystem(identifier.getSystem().getUri())
                        .setValue(identifier.getValue());
            }
        }
        catch (Exception ex) {
            log.error("Transformation Error = "+ex.getMessage());
        }
        return observation;
    }
}
