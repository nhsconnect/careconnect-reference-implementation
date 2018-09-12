package uk.nhs.careconnect.ri.dao.transforms;

import org.apache.commons.collections4.Transformer;
import org.hl7.fhir.dstu3.model.Goal;
import org.hl7.fhir.dstu3.model.Meta;
import org.hl7.fhir.dstu3.model.Reference;
import org.springframework.stereotype.Component;
import uk.nhs.careconnect.ri.database.entity.goal.GoalEntity;
import uk.nhs.careconnect.ri.database.entity.goal.GoalIdentifier;

@Component
public class GoalEntityToFHIRGoalTransformer implements Transformer<GoalEntity, Goal> {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(GoalEntity.class);


    @Override
    public Goal transform(final GoalEntity goalEntity) {
        final Goal goal = new Goal();

        Meta meta = new Meta();

        if (goalEntity.getUpdated() != null) {
            meta.setLastUpdated(goalEntity.getUpdated());
        }
        else {
            if (goalEntity.getCreated() != null) {
                meta.setLastUpdated(goalEntity.getCreated());
            }
        }
        goal.setMeta(meta);

        goal.setId(goalEntity.getId().toString());

        
        if (goalEntity.getPatient() != null) {
            goal
                    .setSubject(new Reference("Patient/"+goalEntity.getPatient().getId())
                    .setDisplay(goalEntity.getPatient().getNames().get(0).getDisplayName()));
        }

        if (goalEntity.getStatus() != null) {
            goal.setStatus(goalEntity.getStatus());
        }




        for (GoalIdentifier identifier : goalEntity.getIdentifiers()) {
            goal.addIdentifier()
                .setSystem(identifier.getSystem().getUri())
                .setValue(identifier.getValue());
        }




        return goal;

    }
}
