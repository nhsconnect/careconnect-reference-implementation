package uk.nhs.careconnect.ri.stu3.dao.transforms;

import ca.uhn.fhir.context.FhirContext;
import org.apache.commons.collections4.Transformer;
import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.Task;
import org.hl7.fhir.dstu3.model.Reference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.nhs.careconnect.ri.database.entity.careTeam.CareTeamEntity;
import uk.nhs.careconnect.ri.database.entity.task.TaskEntity;
import uk.nhs.careconnect.ri.stu3.dao.LibDao;

@Component
public class TaskEntityToFHIRTask implements Transformer<TaskEntity, Task> {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CareTeamEntity.class);


    @Autowired
    private LibDao libDao;

    @Override
    public Task transform(TaskEntity taskEntity) {
        return null;
    }

    public Task transform(TaskEntity taskEntity, FhirContext ctx) {

        final Task task = (Task) ctx.newJsonParser().parseResource(taskEntity.getResource());

        task.setId(taskEntity.getId().toString());

        task.setFor(null);
        if (taskEntity.getForPatient() != null) {
            task
                    .setFor(new Reference("Patient/"+taskEntity.getForPatient().getId())
                            .setDisplay(taskEntity.getForPatient().getNames().get(0).getDisplayName()));
        }

        task.setFocus(null);
        if (taskEntity.getFocusClaim() != null) {
            task
                    .setFocus(new Reference("Claim/"+taskEntity.getFocusClaim().getId())
                            .setDisplay(taskEntity.getFocusClaim().getType().getConceptText()));
        }

        // Requester

        task.setRequester(null);

        if (taskEntity.getRequesterPatient() != null) {
            Reference patRef = new Reference("Patient/"+taskEntity.getRequesterPatient().getId())
                    .setDisplay(taskEntity.getRequesterPatient().getNames().get(0).getDisplayName());
            task.setRequester(new Task.TaskRequesterComponent().setAgent(patRef));
        }
        if (taskEntity.getRequesterPractitioner() != null) {
            Reference ref = new Reference("Practitioner/"+taskEntity.getRequesterPractitioner().getId())
                    .setDisplay(taskEntity.getRequesterPractitioner().getNames().get(0).getDisplayName());
            task.setRequester(new Task.TaskRequesterComponent().setAgent(ref));
        }
        if (taskEntity.getRequesterOrganisation() != null) {
            Reference ref = new Reference("Organization/"+taskEntity.getRequesterOrganisation().getId())
                    .setDisplay(taskEntity.getRequesterOrganisation().getName());
            task.setRequester(new Task.TaskRequesterComponent().setAgent(ref));
        }

        // Owner

        task.setOwner(null);

        if (taskEntity.getOwnerPatient() != null) {
            Reference patRef = new Reference("Patient/"+taskEntity.getOwnerPatient().getId())
                    .setDisplay(taskEntity.getOwnerPatient().getNames().get(0).getDisplayName());
            task.setOwner(patRef);
        }
        if (taskEntity.getOwnerPractitioner() != null) {
            Reference ref = new Reference("Practitioner/"+taskEntity.getOwnerPractitioner().getId())
                    .setDisplay(taskEntity.getOwnerPractitioner().getNames().get(0).getDisplayName());
            task.setOwner(ref);
        }
        if (taskEntity.getOwnerOrganisation() != null) {
            Reference ref = new Reference("Organization/"+taskEntity.getOwnerOrganisation().getId())
                    .setDisplay(taskEntity.getOwnerOrganisation().getName());
            task.setOwner(ref);
        }

        task.setCode(null);
        if (taskEntity.getCode() != null) {
            CodeableConcept concept = new CodeableConcept();

            if (taskEntity.getCode().getConceptText() != null) {
                concept.setText(taskEntity.getCode().getConceptText());
            }
            if (taskEntity.getCode().getConceptCode() != null) {
                concept.addCoding()
                        .setCode(taskEntity.getCode().getConceptCode().getCode())
                        .setDisplay(taskEntity.getCode().getConceptCode().getDisplay())
                        .setSystem(taskEntity.getCode().getConceptCode().getSystem());
            }
            task.setCode(concept);
        }

        task.setAuthoredOn(taskEntity.getAuthored());
        task.setLastModified(task.getLastModified());

        return task;
    }
}
