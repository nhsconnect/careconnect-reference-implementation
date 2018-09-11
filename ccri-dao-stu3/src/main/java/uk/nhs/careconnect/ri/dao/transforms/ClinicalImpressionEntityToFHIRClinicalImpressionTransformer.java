package uk.nhs.careconnect.ri.dao.transforms;

import org.apache.commons.collections4.Transformer;
import org.hl7.fhir.dstu3.model.*;
import org.springframework.stereotype.Component;
import uk.nhs.careconnect.ri.database.entity.clinicialImpression.ClinicalImpressionEntity;
import uk.nhs.careconnect.ri.database.entity.clinicialImpression.ClinicalImpressionFinding;
import uk.nhs.careconnect.ri.database.entity.clinicialImpression.ClinicalImpressionIdentifier;
import uk.nhs.careconnect.ri.database.entity.clinicialImpression.ClinicalImpressionPrognosis;

@Component
public class ClinicalImpressionEntityToFHIRClinicalImpressionTransformer implements Transformer<ClinicalImpressionEntity, ClinicalImpression> {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ClinicalImpressionEntity.class);


    @Override
    public ClinicalImpression transform(final ClinicalImpressionEntity impressionAssessmentEntity) {
        final ClinicalImpression impressionAssessment = new ClinicalImpression();

        Meta meta = new Meta();

        if (impressionAssessmentEntity.getUpdated() != null) {
            meta.setLastUpdated(impressionAssessmentEntity.getUpdated());
        }
        else {
            if (impressionAssessmentEntity.getCreated() != null) {
                meta.setLastUpdated(impressionAssessmentEntity.getCreated());
            }
        }
        impressionAssessment.setMeta(meta);

        impressionAssessment.setId(impressionAssessmentEntity.getId().toString());

        if (impressionAssessmentEntity.getStatus() != null) {
            impressionAssessment.setStatus(impressionAssessmentEntity.getStatus());
        }

        if (impressionAssessmentEntity.getRiskCode() != null) {
            impressionAssessment.getCode().addCoding()
                    .setCode(impressionAssessmentEntity.getRiskCode().getCode())
                    .setSystem(impressionAssessmentEntity.getRiskCode().getSystem())
                    .setDisplay(impressionAssessmentEntity.getRiskCode().getDisplay());
        }
        
        if (impressionAssessmentEntity.getPatient() != null) {
            impressionAssessment
                    .setSubject(new Reference("Patient/"+impressionAssessmentEntity.getPatient().getId())
                    .setDisplay(impressionAssessmentEntity.getPatient().getNames().get(0).getDisplayName()));
        }

        if (impressionAssessmentEntity.getDescription() != null) {
            impressionAssessment.setDescription(impressionAssessmentEntity.getDescription());
        }
        if (impressionAssessmentEntity.getContextEncounter()!=null) {
            impressionAssessment.setContext(new Reference("Encounter/"+impressionAssessmentEntity.getContextEncounter().getId()));
        }

        if (impressionAssessmentEntity.getEffectiveEndDateTime() != null) {
            Period period = new Period();
            if (impressionAssessmentEntity.getEffectiveStartDateTime() != null) {
                period.setStart(impressionAssessmentEntity.getEffectiveStartDateTime());
            }
            period.setEnd(impressionAssessmentEntity.getEffectiveEndDateTime());
            impressionAssessment.setEffective(period);
        } else  if (impressionAssessmentEntity.getEffectiveStartDateTime() != null) {
            impressionAssessment.setEffective(new DateTimeType(impressionAssessmentEntity.getEffectiveStartDateTime()));
        }

        if (impressionAssessmentEntity.getImpressionDateTime() != null) {
            impressionAssessment.setDate(impressionAssessmentEntity.getImpressionDateTime());
        }

        if (impressionAssessmentEntity.getAssessorPractitioner() != null) {
            impressionAssessment.setAssessor(new Reference(("Practitioner/"+impressionAssessmentEntity.getAssessorPractitioner().getId())));
        }

        if (impressionAssessmentEntity.getProblemAllergy() != null) {
            impressionAssessment.addProblem(new Reference(("AllergyIntolerance/"+impressionAssessmentEntity.getProblemAllergy().getId())));
        }
        if (impressionAssessmentEntity.getProblemCondition() != null) {
            impressionAssessment.addProblem(new Reference(("Condition/"+impressionAssessmentEntity.getProblemCondition().getId())));
        }
        if (impressionAssessmentEntity.getSummary() != null) {
            impressionAssessment.setSummary(impressionAssessmentEntity.getSummary());
        }

        for (ClinicalImpressionIdentifier identifier : impressionAssessmentEntity.getIdentifiers()) {
            impressionAssessment.addIdentifier()
                .setSystem(identifier.getSystem().getUri())
                .setValue(identifier.getValue());
        }

        for (ClinicalImpressionFinding findingEntity : impressionAssessmentEntity.getFindings()) {
            ClinicalImpression.ClinicalImpressionFindingComponent finding = impressionAssessment.addFinding();
            if (findingEntity.getBasis()!=null) {
                finding.setBasis(findingEntity.getBasis());
            }
            if (findingEntity.getItemCondition() != null) {
                finding.setItem(new Reference("Condition/"+findingEntity.getItemCondition().getId()));
            } else if (findingEntity.getItemObservation() != null) {
                finding.setItem(new Reference("Observation/"+findingEntity.getItemObservation().getId()));
            } else if (findingEntity.getItemCode() != null) {
                finding.setItem(new CodeableConcept()
                    .addCoding()
                        .setSystem(findingEntity.getItemCode().getSystem())
                        .setDisplay(findingEntity.getItemCode().getDisplay())
                        .setCode(findingEntity.getItemCode().getCode())
                );
            }

        }
        for (ClinicalImpressionPrognosis prognosisEntity : impressionAssessmentEntity.getPrognosis()) {
            if (prognosisEntity.getPrognosisCode() != null) {
                impressionAssessment.addPrognosisCodeableConcept()
                        .addCoding()
                            .setCode(prognosisEntity.getPrognosisCode().getCode())
                        .setSystem(prognosisEntity.getPrognosisCode().getSystem())
                        .setDisplay(prognosisEntity.getPrognosisCode().getDisplay());
            }
            if (prognosisEntity.getPrognosisRisk() != null) {
                impressionAssessment.addPrognosisReference(new Reference("RiskAssessment/"+prognosisEntity.getPrognosisRisk().getId()));
            }
         }


        return impressionAssessment;

    }
}
