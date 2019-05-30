package uk.nhs.careconnect.ri.stu3.dao;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.annotation.OptionalParam;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import org.hl7.fhir.dstu3.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Repository;
import uk.nhs.careconnect.fhir.OperationOutcomeException;
import uk.nhs.careconnect.ri.database.daointerface.*;
import uk.nhs.careconnect.ri.database.entity.claim.ClaimEntity;
import uk.nhs.careconnect.ri.database.entity.encounter.EncounterEntity;
import uk.nhs.careconnect.ri.database.entity.healthcareService.HealthcareServiceEntity;
import uk.nhs.careconnect.ri.database.entity.task.*;
import uk.nhs.careconnect.ri.database.entity.codeSystem.ConceptEntity;
import uk.nhs.careconnect.ri.database.entity.organization.OrganisationEntity;
import uk.nhs.careconnect.ri.database.entity.patient.PatientEntity;
import uk.nhs.careconnect.ri.database.entity.practitioner.PractitionerEntity;
import uk.nhs.careconnect.ri.stu3.dao.transforms.TaskEntityToFHIRTask;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

@Repository
@Transactional
public class TaskDao implements TaskRepository {

    @PersistenceContext
    EntityManager em;

    @Autowired
    TaskEntityToFHIRTask taskEntityToFHIRTask;

    @Autowired
    @Lazy
    ConceptRepository conceptDao;

    @Autowired
    PatientRepository patientDao;

    @Autowired
    ClaimRepository claimDao;

    @Autowired
    OrganisationRepository organisationDao;

    @Autowired
    PractitionerRepository practitionerDao;

    @Autowired
    ConditionRepository conditionDao;


    @Autowired
    private LibDao libDao;

    private static final Logger log = LoggerFactory.getLogger(TaskDao.class);


    @Override
    public void save(FhirContext ctx, TaskEntity task) throws OperationOutcomeException {

    }

    @Override
    public Task read(FhirContext ctx, IdType theId) {
        if (daoutils.isNumeric(theId.getIdPart())) {
            TaskEntity task = (TaskEntity) em.find(TaskEntity.class, Long.parseLong(theId.getIdPart()));
            return taskEntityToFHIRTask.transform(task, ctx);
        }
        return null;
    }

    @Override
    public TaskEntity readEntity(FhirContext ctx, IdType theId) {
        if (daoutils.isNumeric(theId.getIdPart())) {
            TaskEntity task = (TaskEntity) em.find(TaskEntity.class, Long.parseLong(theId.getIdPart()));
            return task;
        }
        return null;
    }

    @Override
    public Task create(FhirContext ctx, Task task, IdType theId, String theConditional) throws OperationOutcomeException {
        TaskEntity taskEntity = null;

        if (task.hasId()) taskEntity = readEntity(ctx, task.getIdElement());


        if (taskEntity == null) taskEntity = new TaskEntity();

        ClaimEntity claimEntity = null;
        if (task.hasFocus() ) {
            if (task.getFocus().hasReference()) {
                log.trace(task.getFocus().getReference());
                claimEntity = claimDao.readEntity(ctx, new IdType(task.getFocus().getReference()));

            }
            if (task.getFocus().hasIdentifier()) {
                // This copes with reference.identifier param (a short cut?)
                log.trace(task.getFocus().getIdentifier().getSystem() + " " + task.getFocus().getIdentifier().getValue());
                claimEntity = claimDao.readEntity(ctx, new TokenParam().setSystem(task.getFocus().getIdentifier().getSystem()).setValue(task.getFocus().getIdentifier().getValue()));
            }
            if (claimEntity != null ) {
                taskEntity.setFocusClaim(claimEntity);
            } else {
                throw new ResourceNotFoundException("Focus reference was not found");
            }
        }
        PatientEntity patientEntity = null;
        if (task.hasFor() ) {
            if (task.getFor().hasReference()) {
                log.trace(task.getFor().getReference());
                patientEntity = patientDao.readEntity(ctx, new IdType(task.getFor().getReference()));

            }
            if (task.getFor().hasIdentifier()) {
                // This copes with reference.identifier param (a short cut?)
                log.trace(task.getFor().getIdentifier().getSystem() + " " + task.getFor().getIdentifier().getValue());
                patientEntity = patientDao.readEntity(ctx, new TokenParam().setSystem(task.getFor().getIdentifier().getSystem()).setValue(task.getFor().getIdentifier().getValue()));
            }
            if (patientEntity != null ) {
                taskEntity.setForPatient(patientEntity);
            } else {
                throw new ResourceNotFoundException("For reference was not found");
            }
        }
        OrganisationEntity organisationEntity = null;
        PractitionerEntity practitionerEntity =  null;
        patientEntity = null;
        if (task.hasRequester() ) {
            Reference ref = task.getRequester().getAgent();
            if (ref.hasReference()) {

                organisationEntity = organisationDao.readEntity(ctx, new IdType(ref.getReference()));

                if (organisationEntity == null) {
                    practitionerEntity = practitionerDao.readEntity(ctx, new TokenParam().setSystem(ref.getIdentifier().getSystem()).setValue(ref.getIdentifier().getValue()));
                }
                if (organisationEntity == null && practitionerEntity == null) {
                    patientEntity = patientDao.readEntity(ctx, new TokenParam().setSystem(ref.getIdentifier().getSystem()).setValue(ref.getIdentifier().getValue()));
                }
            }
            if (task.getRequester().getAgent().hasIdentifier()) {
                organisationEntity = organisationDao.readEntity(ctx, new TokenParam().setSystem(ref.getIdentifier().getSystem()).setValue(ref.getIdentifier().getValue()));
                if (organisationEntity == null) {
                    practitionerEntity = practitionerDao.readEntity(ctx, new TokenParam().setSystem(ref.getIdentifier().getSystem()).setValue(ref.getIdentifier().getValue()));
                }
                if (organisationEntity == null && practitionerEntity == null) {
                    patientEntity = patientDao.readEntity(ctx, new TokenParam().setSystem(ref.getIdentifier().getSystem()).setValue(ref.getIdentifier().getValue()));
                }
            }

            if (organisationEntity != null ) {
                taskEntity.setRequesterOrganisation(organisationEntity);
            } else if (practitionerEntity != null ) {
                taskEntity.setRequesterPractitioner(practitionerEntity);
            } else if (practitionerEntity != null ) {
                taskEntity.setRequesterPatient(patientEntity);
            } else {
                throw new ResourceNotFoundException("Requester reference was not found");
            }
        }
        organisationEntity = null;
        practitionerEntity =  null;
        patientEntity = null;
        HealthcareServiceEntity serviceEntity = null;
        if (task.hasOwner() ) {
            Reference ref = task.getOwner();
            if (ref.hasReference()) {

                organisationEntity = organisationDao.readEntity(ctx, new IdType(ref.getReference()));

                if (organisationEntity == null) {
                    practitionerEntity = practitionerDao.readEntity(ctx, new TokenParam().setSystem(ref.getIdentifier().getSystem()).setValue(ref.getIdentifier().getValue()));
                }
                if (organisationEntity == null && practitionerEntity == null) {
                    patientEntity = patientDao.readEntity(ctx, new TokenParam().setSystem(ref.getIdentifier().getSystem()).setValue(ref.getIdentifier().getValue()));
                }
                // TODO Add Service
            }
            if (task.getRequester().getAgent().hasIdentifier()) {
                organisationEntity = organisationDao.readEntity(ctx, new TokenParam().setSystem(ref.getIdentifier().getSystem()).setValue(ref.getIdentifier().getValue()));
                if (organisationEntity == null) {
                    practitionerEntity = practitionerDao.readEntity(ctx, new TokenParam().setSystem(ref.getIdentifier().getSystem()).setValue(ref.getIdentifier().getValue()));
                }
                if (organisationEntity == null && practitionerEntity == null) {
                    patientEntity = patientDao.readEntity(ctx, new TokenParam().setSystem(ref.getIdentifier().getSystem()).setValue(ref.getIdentifier().getValue()));
                }
                // TODO Add Service
            }

            if (organisationEntity != null ) {
                taskEntity.setOwnerOrganisation(organisationEntity);
            } else if (practitionerEntity != null ) {
                taskEntity.setOwnerPractitioner(practitionerEntity);
            } else if (practitionerEntity != null ) {
                taskEntity.setOwnerPatient(patientEntity);
            } else {
                throw new ResourceNotFoundException("Owner reference was not found");
            }
        }

        if (task.hasStatus()) {
            taskEntity.setStatus(task.getStatus());
        }

        if (task.hasExecutionPeriod()) {
            if (task.getExecutionPeriod().hasStart()) {
                taskEntity.setPeriodStart(task.getExecutionPeriod().getStart());
            }
            if (task.getExecutionPeriod().hasEnd()) {
                taskEntity.setPeriodEnd(task.getExecutionPeriod().getEnd());
            }
        }

        taskEntity.setPriority(task.getPriority());

        if (task.hasCode()) {
            TaskCode taskCode = taskEntity.getCode();
            if (taskCode == null) {
                taskCode = new TaskCode();
                log.info("Claim Id = "+taskEntity.getId());
            }
            taskCode.setConceptCode(null);
            if (task.getCode().hasCoding()) {
                ConceptEntity code = conceptDao.findCode(task.getCode().getCoding().get(0));
                if (code != null) {
                    taskCode.setConceptCode(code);
                } else {

                    throw new IllegalArgumentException("Missing System/Code = " + task.getCode().getCoding().get(0).getSystem() + " code = " + task.getCode().getCoding().get(0).getCode());
                }
            }
            if (task.getCode().hasText()) {
                taskCode.setConceptText(task.getCode().getText());
            }
            em.persist(taskCode);
            taskEntity.setCode(taskCode);
        }

        if (task.hasAuthoredOn()) {
            taskEntity.setAuthored(task.getAuthoredOn());
        }


        if (task.hasPriority()) {
            task.setPriority(task.getPriority());
        }

        String resource = ctx.newJsonParser().encodeResourceToString(task);
        taskEntity.setResource(resource);

        em.persist(taskEntity);

        for (Identifier identifier : task.getIdentifier()) {
            TaskIdentifier taskIdentifier = null;

            for (TaskIdentifier orgSearch : taskEntity.getIdentifiers()) {
                if (identifier.getSystem().equals(orgSearch.getSystemUri()) && identifier.getValue().equals(orgSearch.getValue())) {
                    taskIdentifier = orgSearch;
                    break;
                }
            }
            if (taskIdentifier == null)  taskIdentifier = new TaskIdentifier();

            taskIdentifier= (TaskIdentifier) libDao.setIdentifier(identifier, taskIdentifier );
            taskIdentifier.setTask(taskEntity);
            em.persist(taskIdentifier);
        }


        log.debug("Task.saveCategory");

        task.setId(taskEntity.getId().toString());

        return task;
    }

    @Override
    public List<Resource> search(FhirContext ctx, ReferenceParam patient, TokenParam identifier, StringParam id
            , @OptionalParam(name = Task.SP_OWNER) ReferenceParam owner
            , @OptionalParam(name = Task.SP_REQUESTER) ReferenceParam requester
            , @OptionalParam(name = Task.SP_STATUS) TokenParam status
            , @OptionalParam(name = Task.SP_CODE) TokenParam code
    ) {

        List<TaskEntity> qryResults =  searchEntity(ctx,patient, identifier,id, owner, requester, status,code);
        List<Resource> results = new ArrayList<>();

        for (TaskEntity taskIntoleranceEntity : qryResults)
        {
            // log.trace("HAPI Custom = "+doc.getId());
            Task task = taskEntityToFHIRTask.transform(taskIntoleranceEntity, ctx);
            results.add(task);
        }

        return results;
    }

    @Override
    public List<TaskEntity> searchEntity(FhirContext ctx, ReferenceParam patient, TokenParam identifier, StringParam resid
            , @OptionalParam(name = Task.SP_OWNER) ReferenceParam owner
            , @OptionalParam(name = Task.SP_REQUESTER) ReferenceParam requester
            , @OptionalParam(name = Task.SP_STATUS) TokenParam status
            , @OptionalParam(name = Task.SP_CODE) TokenParam code
                                         ) {
        List<TaskEntity> qryResults = null;

        CriteriaBuilder builder = em.getCriteriaBuilder();

        CriteriaQuery<TaskEntity> criteria = builder.createQuery(TaskEntity.class);
        Root<TaskEntity> root = criteria.from(TaskEntity.class);

        List<Predicate> predList = new LinkedList<Predicate>();
        List<Task> results = new ArrayList<Task>();

        if (patient != null) {
            // KGM 4/1/2018 only search on patient id
            if (daoutils.isNumeric(patient.getIdPart())) {
                Join<TaskEntity, PatientEntity> join = root.join("forPatient", JoinType.LEFT);

                Predicate p = builder.equal(join.get("id"), patient.getIdPart());
                predList.add(p);
            } else {
                Join<TaskEntity, PatientEntity> join = root.join("forPatient", JoinType.LEFT);

                Predicate p = builder.equal(join.get("id"), -1);
                predList.add(p);
            }
        }
        if (resid != null) {
            Predicate p = builder.equal(root.get("id"),resid.getValue());
            predList.add(p);
        }
        if (identifier !=null)
        {
            Join<TaskEntity, TaskIdentifier> join = root.join("identifiers", JoinType.LEFT);

            Predicate p = builder.equal(join.get("value"),identifier.getValue());
            predList.add(p);
            // TODO predList.add(builder.equal(join.get("system"),identifier.getSystem()));

        }

        if (owner !=null)
        {

            if (owner.getBaseUrl().contains("Patient")) {
                if (daoutils.isNumeric(owner.getIdPart())) {
                    Join<TaskEntity, PatientEntity> join = root.join("ownerPatient", JoinType.LEFT);

                    Predicate p = builder.equal(join.get("id"), owner.getIdPart());
                    predList.add(p);
                } else {
                    Join<TaskEntity, PatientEntity> join = root.join("ownerPatient", JoinType.LEFT);

                    Predicate p = builder.equal(join.get("id"), -1);
                    predList.add(p);
                }
            } else if (owner.getBaseUrl().contains("Practitioner")) {
                if (daoutils.isNumeric(owner.getIdPart())) {
                    Join<TaskEntity, PractitionerEntity> join = root.join("ownerPractitioner", JoinType.LEFT);

                    Predicate p = builder.equal(join.get("id"), owner.getIdPart());
                    predList.add(p);
                } else {
                    Join<TaskEntity, PractitionerEntity> join = root.join("ownerPractitioner", JoinType.LEFT);

                    Predicate p = builder.equal(join.get("id"), -1);
                    predList.add(p);
                }
            } else if (owner.getBaseUrl().contains("Organization")) {
                if (daoutils.isNumeric(owner.getIdPart())) {
                    Join<TaskEntity, OrganisationEntity> join = root.join("ownerOrganisation", JoinType.LEFT);

                    Predicate p = builder.equal(join.get("id"), owner.getIdPart());
                    predList.add(p);
                } else {
                    Join<TaskEntity, OrganisationEntity> join = root.join("ownerOrganisation", JoinType.LEFT);

                    Predicate p = builder.equal(join.get("id"), -1);
                    predList.add(p);
                }
            }

        }

        if (requester !=null)
        {

            if (requester.getBaseUrl().contains("Patient")) {
                if (daoutils.isNumeric(requester.getIdPart())) {
                    Join<TaskEntity, PatientEntity> join = root.join("requesterPatient", JoinType.LEFT);

                    Predicate p = builder.equal(join.get("id"), requester.getIdPart());
                    predList.add(p);
                } else {
                    Join<TaskEntity, PatientEntity> join = root.join("requesterPatient", JoinType.LEFT);

                    Predicate p = builder.equal(join.get("id"), -1);
                    predList.add(p);
                }
            } else if (requester.getBaseUrl().contains("Practitioner")) {
                if (daoutils.isNumeric(requester.getIdPart())) {
                    Join<TaskEntity, PractitionerEntity> join = root.join("requesterPractitioner", JoinType.LEFT);

                    Predicate p = builder.equal(join.get("id"), requester.getIdPart());
                    predList.add(p);
                } else {
                    Join<TaskEntity, PractitionerEntity> join = root.join("requesterPractitioner", JoinType.LEFT);

                    Predicate p = builder.equal(join.get("id"), -1);
                    predList.add(p);
                }
            } else if (requester.getBaseUrl().contains("Organization")) {
                if (daoutils.isNumeric(requester.getIdPart())) {
                    Join<TaskEntity, OrganisationEntity> join = root.join("requesterOrganisation", JoinType.LEFT);

                    Predicate p = builder.equal(join.get("id"), requester.getIdPart());
                    predList.add(p);
                } else {
                    Join<TaskEntity, OrganisationEntity> join = root.join("requesterOrganisation", JoinType.LEFT);

                    Predicate p = builder.equal(join.get("id"), -1);
                    predList.add(p);
                }
            }

        }

        if (status != null) {
            Integer taskstatus = null;
            switch (status.getValue().toLowerCase()) {
                case "draft":
                    taskstatus = 0;
                    break;
                case "requested":
                    taskstatus = 1;
                    break;
                case "received":
                    taskstatus = 2;
                    break;
                case "accepted":
                    taskstatus = 3;
                    break;

                default:
                    taskstatus=-1;
            }


            Predicate p = builder.equal(root.get("status"), taskstatus);
            predList.add(p);

        }

        if (code != null) {

            Join<TaskEntity, TaskCode> joinCode = root.join("code", JoinType.LEFT);
            Join<TaskCode, ConceptEntity> joinConcept = joinCode.join("conceptCode", JoinType.LEFT);
            Predicate p = builder.equal(joinConcept.get("code"),code.getValue());
            predList.add(p);
        }


        Predicate[] predArray = new Predicate[predList.size()];
        predList.toArray(predArray);
        if (predList.size()>0)
        {
            criteria.select(root).where(predArray);
        }
        else
        {
            criteria.select(root);
        }
        criteria.orderBy(builder.desc(root.get("created")));

        TypedQuery<TaskEntity> typedQuery = em.createQuery(criteria).setMaxResults(daoutils.MAXROWS);

        qryResults = typedQuery.getResultList();

        return qryResults;
    }

    @Override
    public Long count() {
        CriteriaBuilder qb = em.getCriteriaBuilder();
        CriteriaQuery<Long> cq = qb.createQuery(Long.class);
        cq.select(qb.count(cq.from(TaskEntity.class)));
        return em.createQuery(cq).getSingleResult();
    }
}
