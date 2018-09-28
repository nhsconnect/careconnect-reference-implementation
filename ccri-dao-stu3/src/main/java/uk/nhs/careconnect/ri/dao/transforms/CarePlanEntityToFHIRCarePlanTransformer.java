package uk.nhs.careconnect.ri.dao.transforms;

import org.apache.commons.collections4.Transformer;
import org.hl7.fhir.dstu3.model.*;
import org.springframework.stereotype.Component;

import uk.nhs.careconnect.ri.database.entity.carePlan.*;


@Component
public class CarePlanEntityToFHIRCarePlanTransformer implements Transformer<CarePlanEntity, CarePlan> {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CarePlanEntity.class);


    @Override
    public CarePlan transform(final CarePlanEntity carePlanEntity) {
        final CarePlan carePlan = new CarePlan();

        Meta meta = new Meta();

        if (carePlanEntity.getUpdated() != null) {
            meta.setLastUpdated(carePlanEntity.getUpdated());
        }
        else {
            if (carePlanEntity.getCreated() != null) {
                meta.setLastUpdated(carePlanEntity.getCreated());
            }
        }
        carePlan.setMeta(meta);

        carePlan.setId(carePlanEntity.getId().toString());

        
        if (carePlanEntity.getPatient() != null) {
            carePlan
                    .setSubject(new Reference("Patient/"+carePlanEntity.getPatient().getId())
                    .setDisplay(carePlanEntity.getPatient().getNames().get(0).getDisplayName()));
        }

        if (carePlanEntity.getStatus() != null) {
            carePlan.setStatus(carePlanEntity.getStatus());
        }

        if (carePlanEntity.getIntent() != null) {
            carePlan.setIntent(carePlanEntity.getIntent());
        }
        
        
        for (CarePlanCategory categoryEntity : carePlanEntity.getCategories()) {
            CodeableConcept concept = carePlan.addCategory();
            concept.addCoding()
                    .setSystem(categoryEntity.getCategory().getSystem())
                    .setCode(categoryEntity.getCategory().getCode())
                    .setDisplay(categoryEntity.getCategory().getDisplay());
        }

        if (carePlanEntity.getContextEncounter()!=null) {
            carePlan.setContext(new Reference("Encounter/"+carePlanEntity.getContextEncounter().getId()));
        }
        if (carePlanEntity.getContextEpisodeOfCare()!=null) {
            carePlan.setContext(new Reference("EpisodeOfCare/"+carePlanEntity.getContextEpisodeOfCare().getId()));
        }
        Period period = carePlan.getPeriod();
        if (carePlanEntity.getPeriodStartDateTime() != null) {
            period.setStart(carePlanEntity.getPeriodStartDateTime());
        }
        if (carePlanEntity.getPeriodEndDateTime() != null) {
            period.setEnd(carePlanEntity.getPeriodEndDateTime());
        }

        if (carePlanEntity.getTitle() != null) {
            carePlan.setTitle(carePlan.getTitle());
        }
        if (carePlanEntity.getDescription() != null) {
            carePlan.setDescription(carePlan.getDescription());
        }

        for (CarePlanCondition condition : carePlanEntity.getAddresses()) {
            carePlan.addAddresses(new Reference("Condition/"+condition.getCondition().getId())
                    .setDisplay(condition.getCondition().getCode().getDisplay()));
        }

        for (CarePlanActivity activity : carePlanEntity.getActivities()) {
            CarePlan.CarePlanActivityComponent activityComponent = carePlan.addActivity();
            for (CarePlanActivityDetail carePlanActivityDetail : activity.getDetails()) {
                CarePlan.CarePlanActivityDetailComponent activityDetailComponent = activityComponent.getDetail();
                if (carePlanActivityDetail.getCode() != null) {
                    activityDetailComponent.getCode().addCoding()
                            .setCode(carePlanActivityDetail.getCode().getCode())
                            .setDisplay(carePlanActivityDetail.getCode().getDisplay())
                            .setSystem(carePlanActivityDetail.getCode().getSystem());
                }
                if (carePlanActivityDetail.getStatus() != null) {
                    activityDetailComponent.setStatus(carePlanActivityDetail.getStatus());
                }
                if (carePlanActivityDetail.getDescription() != null) {
                    activityDetailComponent.setDescription(carePlanActivityDetail.getDescription());
                }
                if (carePlanActivityDetail.getCategory() != null) {
                    activityDetailComponent.getCategory().addCoding()
                            .setCode(carePlanActivityDetail.getCategory().getCode())
                            .setDisplay(carePlanActivityDetail.getCategory().getDisplay())
                            .setSystem(carePlanActivityDetail.getCategory().getSystem());
                }

            }
        }

        for (CarePlanIdentifier identifier : carePlanEntity.getIdentifiers()) {
            carePlan.addIdentifier()
                .setSystem(identifier.getSystem().getUri())
                .setValue(identifier.getValue());
        }

        for (CarePlanAuthor author : carePlanEntity.getAuthors()) {
            if (author.getOrganisation() != null) {
                carePlan.addAuthor(new Reference("Organization/" + author.getOrganisation().getId())
                .setDisplay(author.getOrganisation().getName()));
            }
            if (author.getPractitioner() != null)
                carePlan.addAuthor(new Reference("Practitioner/"+author.getPractitioner().getId())
                .setDisplay(author.getPractitioner().getNames().get(0).getDisplayName()));
        }

        for (CarePlanSupportingInformation supportingInformation : carePlanEntity.getSupportingInformation()) {
            /*if (supportingInformation.getCarePlan() != null) {
                carePlan.addSupportingInfo(new Reference("CarePlan/"+supportingInformation.getCarePlan().getId()));
            }*/
            if (supportingInformation.getReferenceCondition() != null) {
                carePlan.addSupportingInfo(new Reference("Condition/"+supportingInformation.getReferenceCondition().getId()));
            }
            if (supportingInformation.getReferenceDocumentReference() != null) {
                carePlan.addSupportingInfo(new Reference("DocumentReference/"+supportingInformation.getReferenceDocumentReference().getId()));
            }
            if (supportingInformation.getReferenceForm() != null) {
                carePlan.addSupportingInfo(new Reference("QuestionnaireResponse/"+supportingInformation.getReferenceForm().getId()));
            }
            if (supportingInformation.getReferenceListResource() != null) {
                carePlan.addSupportingInfo(new Reference("List/"+supportingInformation.getReferenceListResource().getId()));
            }
            if (supportingInformation.getReferenceObservation() != null) {
                carePlan.addSupportingInfo(new Reference("Observation/"+supportingInformation.getReferenceObservation().getId()));
            }
            if (supportingInformation.getReferencePatient() != null) {
                carePlan.addSupportingInfo(new Reference("Patient/"+supportingInformation.getReferencePatient().getId()));
            }
            if (supportingInformation.getReferenceRisk() != null) {
                carePlan.addSupportingInfo(new Reference("RiskAssessment/"+supportingInformation.getReferenceRisk().getId()));
            }
            if (supportingInformation.getReferenceClinicalImpression() != null) {
                carePlan.addSupportingInfo(new Reference("ClinicalImpression/"+supportingInformation.getReferenceClinicalImpression().getId()));
            }
            if (supportingInformation.getReferenceConsent() != null) {
                carePlan.addSupportingInfo(new Reference("Consent/"+supportingInformation.getReferenceConsent().getId()));
            }
        }
        for (CarePlanTeam team : carePlanEntity.getTeams()) {
            if (team.getTeam() != null) {
                carePlan.addCareTeam().setReference("CareTeam/" + team.getTeam().getId()).setDisplay(team.getTeam().getName());
            }
        }



        return carePlan;

    }
}
