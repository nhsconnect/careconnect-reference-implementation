package uk.nhs.careconnect.ri.dao.transforms;

import org.apache.commons.collections4.Transformer;
import org.hl7.fhir.dstu3.model.DateTimeType;
import org.hl7.fhir.dstu3.model.RiskAssessment;
import org.hl7.fhir.dstu3.model.Meta;

import org.hl7.fhir.dstu3.model.Reference;
import org.springframework.stereotype.Component;
import uk.nhs.careconnect.ri.database.entity.riskAssessment.RiskAssessmentEntity;
import uk.nhs.careconnect.ri.database.entity.riskAssessment.RiskAssessmentIdentifier;
import uk.nhs.careconnect.ri.database.entity.riskAssessment.RiskAssessmentPrediction;

@Component
public class RiskAssessmentEntityToFHIRRiskAssessmentTransformer implements Transformer<RiskAssessmentEntity, RiskAssessment> {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(RiskAssessmentEntity.class);


    @Override
    public RiskAssessment transform(final RiskAssessmentEntity riskAssessmentEntity) {
        final RiskAssessment riskAssessment = new RiskAssessment();

        Meta meta = new Meta();

        if (riskAssessmentEntity.getUpdated() != null) {
            meta.setLastUpdated(riskAssessmentEntity.getUpdated());
        }
        else {
            if (riskAssessmentEntity.getCreated() != null) {
                meta.setLastUpdated(riskAssessmentEntity.getCreated());
            }
        }
        riskAssessment.setMeta(meta);

        riskAssessment.setId(riskAssessmentEntity.getId().toString());

        if (riskAssessmentEntity.getRiskCode() != null) {
            riskAssessment.getCode().addCoding()
                    .setCode(riskAssessmentEntity.getRiskCode().getCode())
                    .setSystem(riskAssessmentEntity.getRiskCode().getSystem())
                    .setDisplay(riskAssessmentEntity.getRiskCode().getDisplay());
        }
        
        if (riskAssessmentEntity.getPatient() != null) {
            riskAssessment
                    .setSubject(new Reference("Patient/"+riskAssessmentEntity.getPatient().getId())
                    .setDisplay(riskAssessmentEntity.getPatient().getNames().get(0).getDisplayName()));
        }

        if (riskAssessmentEntity.getStatus() != null) {
            riskAssessment.setStatus(riskAssessmentEntity.getStatus());
        }

        if (riskAssessmentEntity.getContextEncounter()!=null) {
            riskAssessment.setContext(new Reference("Encounter/"+riskAssessmentEntity.getContextEncounter().getId()));
        }

        if (riskAssessmentEntity.getOccurrenceEndDateTime() != null) {
            riskAssessment.setOccurrence(new DateTimeType().setValue(riskAssessmentEntity.getOccurrenceEndDateTime()));
        }

        if (riskAssessmentEntity.getCondition() != null) {
            riskAssessment.setCondition(new Reference("Condition/"+riskAssessmentEntity.getCondition().getId()));
        }

        if (riskAssessmentEntity.getMitigation() != null) {
            riskAssessment.setMitigation(riskAssessmentEntity.getMitigation());
        }


        for (RiskAssessmentIdentifier identifier : riskAssessmentEntity.getIdentifiers()) {
            riskAssessment.getIdentifier()
                .setSystem(identifier.getSystem().getUri())
                .setValue(identifier.getValue());
        }

        for (RiskAssessmentPrediction prediction : riskAssessmentEntity.getPredictions()) {
            RiskAssessment.RiskAssessmentPredictionComponent component = riskAssessment.addPrediction();

            if (prediction.getOutcome() != null) {
                component.getOutcome().addCoding()
                        .setCode(prediction.getOutcome().getCode())
                        .setSystem(prediction.getOutcome().getSystem())
                        .setDisplay(prediction.getOutcome().getDisplay());
            }
            if (prediction.getQualitiveRiskConcept() != null) {
                component.getQualitativeRisk().addCoding()
                        .setCode(prediction.getQualitiveRiskConcept().getCode())
                        .setSystem(prediction.getQualitiveRiskConcept().getSystem())
                        .setDisplay(prediction.getQualitiveRiskConcept().getDisplay());
            }
        }

        return riskAssessment;

    }
}
