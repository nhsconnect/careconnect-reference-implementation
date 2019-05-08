package uk.nhs.careconnect.ri.stu3.dao;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.api.Include;
import ca.uhn.fhir.rest.annotation.ConditionalUrlParam;
import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.rest.server.exceptions.InternalErrorException;
import org.hl7.fhir.dstu3.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Repository;
import uk.nhs.careconnect.fhir.OperationOutcomeException;
import uk.nhs.careconnect.ri.database.daointerface.*;
import uk.nhs.careconnect.ri.database.entity.codeSystem.ConceptEntity;
import uk.nhs.careconnect.ri.database.entity.carePlan.*;
import uk.nhs.careconnect.ri.database.entity.clinicialImpression.ClinicalImpressionEntity;
import uk.nhs.careconnect.ri.database.entity.condition.ConditionEntity;
import uk.nhs.careconnect.ri.database.entity.consent.ConsentEntity;
import uk.nhs.careconnect.ri.database.entity.encounter.EncounterEntity;
import uk.nhs.careconnect.ri.database.entity.episode.EpisodeOfCareEntity;
import uk.nhs.careconnect.ri.database.entity.flag.FlagEntity;
import uk.nhs.careconnect.ri.database.entity.list.ListEntity;
import uk.nhs.careconnect.ri.database.entity.observation.ObservationEntity;
import uk.nhs.careconnect.ri.database.entity.patient.PatientEntity;
import uk.nhs.careconnect.ri.database.entity.practitioner.PractitionerEntity;
import uk.nhs.careconnect.ri.database.entity.procedure.ProcedureEntity;
import uk.nhs.careconnect.ri.database.entity.questionnaire.QuestionnaireEntity;
import uk.nhs.careconnect.ri.database.entity.questionnaire.QuestionnaireIdentifier;
import uk.nhs.careconnect.ri.database.entity.questionnaireResponse.QuestionnaireResponseEntity;
import uk.nhs.careconnect.ri.database.entity.questionnaireResponse.QuestionnaireResponseIdentifier;
import uk.nhs.careconnect.ri.database.entity.questionnaireResponse.QuestionnaireResponseItem;
import uk.nhs.careconnect.ri.database.entity.questionnaireResponse.QuestionnaireResponseItemAnswer;
import uk.nhs.careconnect.ri.database.entity.relatedPerson.RelatedPersonEntity;
import uk.nhs.careconnect.ri.stu3.dao.transforms.*;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.*;
import javax.transaction.Transactional;
import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

@Repository
@Transactional
public class QuestionnaireResponseDao implements QuestionnaireResponseRepository {


    @PersistenceContext
    EntityManager em;

    @Autowired
    PatientRepository patientDao;

    @Autowired
    PractitionerRepository practitionerDao;

    @Autowired
    @Lazy
    CarePlanRepository carePlanDao;

    @Autowired
    @Lazy
    EncounterRepository encounterDao;

    @Autowired
    EpisodeOfCareRepository episodeDao;

    @Autowired
    QuestionnaireRepository questionnaireDao;

    @Autowired
    @Lazy
    ConceptRepository conceptDao;

    @Autowired
    @Lazy
    ConditionRepository conditionDao;

    @Autowired
    @Lazy
    ObservationRepository observationDao;

    @Autowired
    @Lazy
    FlagRepository flagDao;

    @Autowired
    @Lazy
    ClinicalImpressionRepository clinicalImpressionDao;

    @Autowired
    @Lazy
    ConsentRepository consentDao;

    @Autowired
    @Lazy
    RelatedPersonRepository personDao;

    @Autowired
    @Lazy
    ProcedureRepository procedureDao;

    @Autowired
    @Lazy
    ListRepository listDao;

    @Autowired
    private LibDao libDao;

    @Autowired
    private QuestionnaireResponseEntityToFHIRQuestionnaireResponseTransformer formEntityToFHIRQuestionnaireResponseTransformer;

    @Autowired
    private PatientEntityToFHIRPatientTransformer patientEntityToFHIRPatientTransformer;

    @Autowired
    private ListEntityToFHIRListResourceTransformer listEntityToFHIRListResourceTransformer;

    @Autowired
    private QuestionnaireResponseEntityToFHIRQuestionnaireResponseTransformer questionnaireResponseEntityToFHIRQuestionnaireResponseTransformer;

    @Autowired
    private PractitionerEntityToFHIRPractitionerTransformer practitionerEntityToFHIRPractitionerTransformer;

    @Autowired
    private OrganisationEntityToFHIROrganizationTransformer organisationEntityToFHIROrganizationTransformer;

    @Autowired
    private ConditionEntityToFHIRConditionTransformer conditionEntityToFHIRConditionTransformer;

    @Autowired
    private RiskAssessmentEntityToFHIRRiskAssessmentTransformer riskAssessmentEntityToFHIRRiskAssessmentTransformer;

    @Autowired
    private CareTeamEntityToFHIRCareTeamTransformer careTeamEntityToFHIRCareTeamTransformer;

    @Autowired
    private ClinicalImpressionEntityToFHIRClinicalImpressionTransformer clinicalImpressionEntityToFHIRClinicalImpressionTransformer;

    @Autowired
    private ConsentEntityToFHIRConsentTransformer consentEntityToFHIRConsentTransformer;

    @Autowired
    private GoalEntityToFHIRGoalTransformer goalEntityToFHIRGoalTransformer;

    @Autowired
    private RelatedPersonEntityToFHIRRelatedPersonTransformer personEntityToFHIRRelatedPersonTransformer;

    @Autowired
    private FlagEntityToFHIRFlagTransformer flagEntityToFHIRFlagTransformer;

    @Autowired
    private CarePlanEntityToFHIRCarePlanTransformer carePlanEntityToFHIRCarePlanTransformer;

    @Autowired
    private ObservationEntityToFHIRObservationTransformer observationEntityToFHIRObservationTransformer;

    @Autowired
    private ProcedureEntityToFHIRProcedureTransformer procedureEntityToFHIRProcedureTransformer;

    @Autowired
    private CodeSystemRepository codeSystemSvc;

    List<Resource> results = null;

    private static final Logger log = LoggerFactory.getLogger(QuestionnaireResponseDao.class);

    @Override
    public void save(FhirContext ctx, QuestionnaireResponseEntity
            form)
    {
        em.persist(form);
    }

    @Override
    public QuestionnaireResponse read(FhirContext ctx,IdType theId) {
        if (daoutils.isNumeric(theId.getIdPart())) {
            QuestionnaireResponseEntity formEntity = (QuestionnaireResponseEntity) em.find(QuestionnaireResponseEntity.class, Long.parseLong(theId.getIdPart()));

            return formEntity == null
                    ? null
                    : formEntityToFHIRQuestionnaireResponseTransformer.transform(formEntity);

        } else {
            return null;
        }
    }

    private void resultsAddIfNotPresent(Resource resource) {
        boolean found = false;
        for (Resource resource1 : results) {
            if (resource1.getId().equals(resource.getId()) && resource.getClass().getSimpleName().equals(resource1.getClass().getSimpleName())) found=true;
        }
        if (!found) results.add(resource);
    }

    @Override
    public List<Resource> searchQuestionnaireResponse(
            FhirContext ctx,
            TokenParam identifier,
            StringParam id,
            ReferenceParam questionnaire,
            ReferenceParam patient,
            Set<Include> includes
    ) {
        List<QuestionnaireResponseEntity> qryResults = searchQuestionnaireResponseEntity(ctx, identifier, id, questionnaire, patient, includes);

        results = new ArrayList<>();

        for (QuestionnaireResponseEntity form : qryResults)
        {
            // log.trace("HAPI Custom = "+doc.getId());
            QuestionnaireResponse questionnaireResponse = formEntityToFHIRQuestionnaireResponseTransformer.transform(form);
            results.add(questionnaireResponse);
        }
        if (includes!=null) {
            log.debug("Includes");
            for (QuestionnaireResponseEntity questionnaireResponse : qryResults) {
                if (includes !=null) {
                    for (Include include : includes) {
                        switch(include.getValue()) {
                            case "*":
                                PatientEntity patientEntity2 = questionnaireResponse.getPatient();
                                if (patientEntity2 !=null) resultsAddIfNotPresent(patientEntityToFHIRPatientTransformer.transform(patientEntity2));

                                if (questionnaireResponse.getAuthorPractitioner() != null) {
                                   resultsAddIfNotPresent(practitionerEntityToFHIRPractitionerTransformer.transform(questionnaireResponse.getAuthorPractitioner()));
                                }
                                for (QuestionnaireResponseItem item : questionnaireResponse.getItems()) {
                                    getReferencedResources(item);
                                }
                                break;
                        }
                    }
                }

            }
        }
        return results;
    }

    private void getReferencedResources(QuestionnaireResponseItem item) {
        for (QuestionnaireResponseItemAnswer answer : item.getAnswers()) {
            if (answer.getReferenceClinicalImpression() != null) {
                resultsAddIfNotPresent(clinicalImpressionEntityToFHIRClinicalImpressionTransformer.transform(answer.getReferenceClinicalImpression()));
            }
            if (answer.getReferenceFlag() != null) {
                resultsAddIfNotPresent(flagEntityToFHIRFlagTransformer.transform(answer.getReferenceFlag()));
            }
            if (answer.getReferenceCarePlan() != null) {
                resultsAddIfNotPresent(carePlanEntityToFHIRCarePlanTransformer.transform(answer.getReferenceCarePlan()));
            }
            if (answer.getReferenceCondition() != null) {
                resultsAddIfNotPresent(conditionEntityToFHIRConditionTransformer.transform(answer.getReferenceCondition()));
            }
            if (answer.getReferenceConsent() != null) {
                resultsAddIfNotPresent(consentEntityToFHIRConsentTransformer.transform(answer.getReferenceConsent()));
            }
            if (answer.getReferenceObservation() != null) {
                resultsAddIfNotPresent(observationEntityToFHIRObservationTransformer.transform(answer.getReferenceObservation()));
            }
            if (answer.getReferenceProcedure() != null) {
                resultsAddIfNotPresent(procedureEntityToFHIRProcedureTransformer.transform(answer.getReferenceProcedure()));
            }
            if (answer.getReferenceOrganisation() != null) {
                resultsAddIfNotPresent(organisationEntityToFHIROrganizationTransformer.transform(answer.getReferenceOrganisation()));
            }
            if (answer.getReferenceListResource() != null) {
                resultsAddIfNotPresent(listEntityToFHIRListResourceTransformer.transform(answer.getReferenceListResource()));
            }
            if (answer.getReferenceConsent() != null) {
                resultsAddIfNotPresent(consentEntityToFHIRConsentTransformer.transform(answer.getReferenceConsent()));
            }
            if (answer.getReferencePerson() != null) {
                resultsAddIfNotPresent(personEntityToFHIRRelatedPersonTransformer.transform(answer.getReferencePerson()));
            }
         }
        for (QuestionnaireResponseItem subItem : item.getItems()) {
            getReferencedResources(subItem);
        }
    }

    @Override
    public List<QuestionnaireResponseEntity> searchQuestionnaireResponseEntity(
            FhirContext ctx,
            TokenParam identifier,
            StringParam resid,
            ReferenceParam questionnaire,
            ReferenceParam patient,
            Set<Include> includes) {

            List<QuestionnaireResponseEntity> qryResults = null;

            CriteriaBuilder builder = em.getCriteriaBuilder();

            CriteriaQuery<QuestionnaireResponseEntity> criteria = builder.createQuery(QuestionnaireResponseEntity.class);

            Root<QuestionnaireResponseEntity> root = criteria.from(QuestionnaireResponseEntity.class);

            List<Predicate> predList = new LinkedList<Predicate>();
            List<Questionnaire> results = new ArrayList<Questionnaire>();

            if (identifier != null)
            {

                //if (identifier.getModifier().getValue().equals(":identifier"))
                Join<QuestionnaireEntity, QuestionnaireIdentifier> join = root.join("identifiers", JoinType.LEFT);

                Predicate p = builder.equal(join.get("value"),identifier.getValue());
                predList.add(p);
                // TODO predList.add(builder.equal(join.get("system"),identifier.getSystem()));

            }
            if (resid != null) {
                Predicate p = builder.equal(root.get("questionnaire"),resid.getValue());
                predList.add(p);
            }

            if (patient != null) {
                if (daoutils.isNumeric(patient.getIdPart())) {
                    Join<QuestionnaireResponseEntity, PatientEntity> join = root.join("patient", JoinType.LEFT);

                    Predicate p = builder.equal(join.get("id"), patient.getIdPart());
                    predList.add(p);
                } else {
                    Join<ObservationEntity, PatientEntity> join = root.join("patient", JoinType.LEFT);

                    Predicate p = builder.equal(join.get("id"), -1);
                    predList.add(p);
                }
            }

            if (questionnaire != null) {
                Predicate p = builder.equal(root.get("questionnaire"),questionnaire.getValue());
                predList.add(p);
                    /*
                if (questionnaire.getValue() != null) { log.info("value - " + questionnaire.getValue()); }
                if (questionnaire.getQueryParameterQualifier() != null) { log.info("QueryParam - " + questionnaire.getQueryParameterQualifier()); }
                //if (questionnaire.getChain() != null) { log.info("chain - " + questionnaire.getChain()); }

                if (questionnaire.getValue().contains("|")) {

                    String[] ident = questionnaire.getValue().split("\\|");
                    log.info("System = "+ident[0]);
                    log.info("Value = "+ident[1]);
                    Predicate p = builder.equal(root.get("questionnaireIdSystem"), ident[0]);
                    predList.add(p);
                    p = builder.equal(root.get("questionnaireIdValue"), ident[1]);
                    predList.add(p);
                } else {
                    if (daoutils.isNumeric(questionnaire.getIdPart())) {
                        Join<QuestionnaireResponseEntity, QuestionnaireEntity> join = root.join("questionnaire", JoinType.LEFT);

                        Predicate p = builder.equal(join.get("id"), questionnaire.getIdPart());
                        predList.add(p);
                    } else {
                        Join<QuestionnaireResponseEntity, PatientEntity> join = root.join("questionnaire", JoinType.LEFT);

                        Predicate p = builder.equal(join.get("id"), -1);
                        predList.add(p);
                    }
                }
                */
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

            qryResults = em.createQuery(criteria).setMaxResults(daoutils.MAXROWS).getResultList();
            return qryResults;
        }


    @Override
    public Long count() {

        CriteriaBuilder qb = em.getCriteriaBuilder();
        CriteriaQuery<Long> cq = qb.createQuery(Long.class);
        cq.select(qb.count(cq.from(QuestionnaireResponseEntity.class)));
        //cq.where(/*your stuff*/);
        return em.createQuery(cq).getSingleResult();
    }


    @Override
    public QuestionnaireResponseEntity readEntity(FhirContext ctx, IdType theId) {
        if (daoutils.isNumeric(theId.getIdPart())) {
            QuestionnaireResponseEntity formEntity = (QuestionnaireResponseEntity) em.find(QuestionnaireResponseEntity.class, Long.parseLong(theId.getIdPart()));

            return formEntity;

        } else {
            return null;
        }
    }

    @Override
    public QuestionnaireResponse create(FhirContext ctx,QuestionnaireResponse form, @IdParam IdType theId, @ConditionalUrlParam String theConditional) throws OperationOutcomeException {

        QuestionnaireResponseEntity formEntity = null;
        log.debug("Called QuestionnaireResponse Create Condition Url: "+theConditional);

        if (theConditional != null) {
            try {


                if (theConditional.contains("fhir.leedsth.nhs.uk/Id/form")) {
                    URI uri = new URI(theConditional);

                    String scheme = uri.getScheme();
                    String host = uri.getHost();
                    String query = uri.getRawQuery();
                    log.debug(query);
                    String[] spiltStr = query.split("%7C");
                    log.debug(spiltStr[1]);

                    List<QuestionnaireResponseEntity> results = searchQuestionnaireResponseEntity(ctx, new TokenParam().setValue(spiltStr[1]).setSystem("https://fhir.leedsth.nhs.uk/Id/risk"),null,null,null, null);
                    for (QuestionnaireResponseEntity con : results) {
                        formEntity = con;
                        break;
                    }
                } else {
                    log.info("NOT SUPPORTED: Conditional Url = "+theConditional);
                }

            } catch (Exception ex) {

            }
        }
        if ((theId != null)) {
            formEntity =  readEntity(ctx,theId);
        }

        if (formEntity == null) {
            formEntity = new QuestionnaireResponseEntity();
        }

        formEntity.setResource(null);
        formEntity.setStatus(form.getStatus());

        if (form.hasAuthored()) {
            formEntity.setAuthoredDateTime(form.getAuthored());
        }
        if (form.hasAuthor()) {
            if (form.getAuthor().getReference().contains("Patient")) {
                PatientEntity patientEntity = null;
                patientEntity = patientDao.readEntity(ctx, new IdType(form.getAuthor().getReference()));
                formEntity.setAuthorPatient(patientEntity);
            }
            if (form.getAuthor().getReference().contains("Practitioner")) {
                PractitionerEntity practitionerEntity = null;
                practitionerEntity = practitionerDao.readEntity(ctx, new IdType(form.getAuthor().getReference()));
                formEntity.setAuthorPractitioner(practitionerEntity);
            }
        }
        if (form.hasBasedOn()) {
            for (Reference reference : form.getBasedOn()) {
                if (form.getBasedOn().contains("CarePlan")) {
                    CarePlanEntity carePlanEntity = null;
                    carePlanEntity = carePlanDao.readEntity(ctx, new IdType(reference.getReference()));
                    formEntity.setCarePlan(carePlanEntity);
                }
            }
        }

        if (form.hasContext()) {
            if (form.getContext().getReference().contains("Encounter")) {
                EncounterEntity encounterEntity = encounterDao.readEntity(ctx, new IdType(form.getContext().getReference()));
                formEntity.setContextEncounter(encounterEntity);
            }
            if (form.getContext().getReference().contains("EpisodeOfCare")) {
                EpisodeOfCareEntity episodeOfCareEntity = episodeDao.readEntity(ctx, new IdType(form.getContext().getReference()));
                formEntity.setContextEpisodeOfCare(episodeOfCareEntity);
            }
        }

        if (form.hasSubject()) {
            if (form.getSubject().getReference().contains("Patient")) {
                PatientEntity patientEntity = null;
                patientEntity = patientDao.readEntity(ctx, new IdType(form.getSubject().getReference()));
                formEntity.setPatient(patientEntity);
            }
        }

        if (form.hasQuestionnaire()) {
            if (form.getQuestionnaire().hasReference()) {
                //QuestionnaireEntity questionnaireEntity = questionnaireDao.readEntity(ctx, new IdType(form.getQuestionnaire().getReference()));
                formEntity.setQuestionnaire(form.getQuestionnaire().getReference());
            }
            /*
            if (form.getQuestionnaire().hasIdentifier()) {
                formEntity.setQuestionnaireIdSystem(form.getQuestionnaire().getIdentifier().getSystem());
                formEntity.setQuestionnaireIdValue(form.getQuestionnaire().getIdentifier().getValue());
            }*/
        }
        if (form.hasSource()) {
            if (form.getSource().getReference().contains("Patient")) {
                PatientEntity patientEntity = null;
                patientEntity = patientDao.readEntity(ctx, new IdType(form.getSource().getReference()));
                formEntity.setSourcePatient(patientEntity);
            }
            if (form.getSource().getReference().contains("Practitioner")) {
                PractitionerEntity practitionerEntity = null;
                practitionerEntity = practitionerDao.readEntity(ctx, new IdType(form.getSource().getReference()));
                formEntity.setSourcePractitioner(practitionerEntity);
            }
        }

        em.persist(formEntity);


        if (form.hasIdentifier()) {

            Identifier ident = form.getIdentifier();

            log.debug("QuestionnaireResponse SDS = " + ident.getValue() + " System =" + ident.getSystem());
            QuestionnaireResponseIdentifier formIdentifier = null;

            for (QuestionnaireResponseIdentifier orgSearch : formEntity.getIdentifiers()) {
                if (ident.getSystem().equals(orgSearch.getSystemUri()) && ident.getValue().equals(orgSearch.getValue())) {
                    formIdentifier = orgSearch;
                    break;
                }
            }
            if (formIdentifier == null) {
                formIdentifier = new QuestionnaireResponseIdentifier();
                formIdentifier.setQuestionnaireResponse(formEntity);

            }

            formIdentifier= (QuestionnaireResponseIdentifier) libDao.setIdentifier(ident,  formIdentifier);
            log.debug("QuestionnaireResponse System Code: "+formIdentifier.getSystemUri());

            em.persist(formIdentifier);
        }
        for (QuestionnaireResponseItem itemSearch : formEntity.getItems()) {
           removeItem(itemSearch);
        }
        if (form.hasItem()) {
            for (QuestionnaireResponse.QuestionnaireResponseItemComponent item : form.getItem()) {
                QuestionnaireResponseItem itemEntity = null;
                /*
                if (item.hasLinkId()) {
                    for (QuestionnaireResponseItem itemSearch : formEntity.getItems()) {
                        if (itemSearch.getLinkId() != null && itemSearch.getLinkId().contains(item.getLinkId())) {
                            itemEntity = itemSearch;
                        }
                    }
                }*/
                if (itemEntity == null) { itemEntity = new QuestionnaireResponseItem(); }
                itemEntity.setForm(formEntity);
                buildItem(ctx, item, itemEntity);
            }
        }

       // log.info("Called PERSIST id="+formEntity.getId().toString());
        form.setId(formEntity.getId().toString());

        return form;
    }

    private void removeItem(QuestionnaireResponseItem item) {
        for (QuestionnaireResponseItemAnswer answer : item.getAnswers()) {
            for (QuestionnaireResponseItem itemSearch : answer.getItems()) {
                removeItem(itemSearch);
            }
            em.remove(answer);
        }
        for (QuestionnaireResponseItem itemSearch : item.getItems()) {
            removeItem(itemSearch);
        }
        em.remove(item);
    }

    private void buildItem(FhirContext ctx,QuestionnaireResponse.QuestionnaireResponseItemComponent item, QuestionnaireResponseItem itemEntity ) {

        if (item.hasLinkId()) {
            itemEntity.setLinkId(item.getLinkId());
        }
        if (item.hasDefinition()) {
            itemEntity.setDefinition(item.getDefinition());
        }
        if (item.hasText()) {
            itemEntity.setText(item.getText());
        }
        if (item.hasSubject()) {
            if (item.getSubject().getReference().contains("Patient")) {
                PatientEntity patientEntity = null;
                patientEntity = patientDao.readEntity(ctx, new IdType(item.getSubject().getReference()));
                itemEntity.setPatient(patientEntity);
            }
        }
        em.persist(itemEntity);

        if (item.hasItem()) {
            for (QuestionnaireResponse.QuestionnaireResponseItemComponent subitem : item.getItem()) {

                QuestionnaireResponseItem subItemEntity = null;
                /*
                if (subitem.hasLinkId()) {
                    for (QuestionnaireResponseItem itemSearch : itemEntity.getItems()) {
                        if (itemSearch.getLinkId() != null && itemSearch.getLinkId().contains(subitem.getLinkId())) {
                            itemEntity = itemSearch;
                        }
                    }
                }
                */
                if (subItemEntity == null) { subItemEntity = new QuestionnaireResponseItem(); }

                subItemEntity.setParentItem(itemEntity);
                buildItem(ctx, subitem, subItemEntity);
            }
        }

        if (item.hasAnswer()) {
            for (QuestionnaireResponse.QuestionnaireResponseItemAnswerComponent answer : item.getAnswer()) {
                QuestionnaireResponseItemAnswer answerEntity = new QuestionnaireResponseItemAnswer();
                answerEntity.setItem(itemEntity);
                try {
                    if (answer.hasValue()) {
                        if (answer.hasValueBooleanType()) {
                            answerEntity.setValueBoolean(answer.getValueBooleanType().booleanValue());
                        }
                        if (answer.hasValueStringType()) {
                            answerEntity.setValueString(answer.getValueStringType().asStringValue());
                        }
                        if (answer.hasValueIntegerType()) {
                            answerEntity.setValueInteger(answer.getValueIntegerType().getValue());
                        }
                        if (answer.hasValueDateTimeType()) {
                            answerEntity.setValueDate(answer.getValueDateTimeType().getValue());
                        }
                        if (answer.hasValueDateType()) {
                            answerEntity.setValueDate(answer.getValueDateType().getValue());
                        }
                        if (answer.hasValueCoding()) {
                            ConceptEntity concept = conceptDao.findAddCode(answer.getValueCoding());
                            if (concept != null) answerEntity.setValueCoding(concept);
                        }
                        if (answer.hasValueReference()) {
                            Reference reference = answer.getValueReference();
                            // log.info("QuestionnaireResponse answer reference = "+reference.getReference());
                            URI uri = new URI(reference.getReference());
                            if (uri.getPath() != null) {
                                String[] segments = uri.getPath().split("/");
                                String resourceType = "";
                                if (segments.length > 1) resourceType = segments[segments.length - 2];
                                // log.info("QuestionnaireResponse answer resourceType = "+resourceType);
                                switch (resourceType) {
                                    case "Condition":
                                        ConditionEntity conditionEntity = conditionDao.readEntity(ctx, new IdType(reference.getReference()));
                                        answerEntity.setReferenceCondition(conditionEntity);
                                        break;
                                    case "Observation":
                                        ObservationEntity observationEntity = observationDao.readEntity(ctx, new IdType(reference.getReference()));
                                        answerEntity.setReferenceObservation(observationEntity);
                                        break;
                                    case "Practitioner":
                                        PractitionerEntity practitionerEntity = practitionerDao.readEntity(ctx, new IdType(reference.getReference()));
                                        answerEntity.setReferencePractitioner(practitionerEntity);
                                        break;
                                    case "Flag":
                                        FlagEntity flagEntity = flagDao.readEntity(ctx, new IdType(reference.getReference()));
                                        answerEntity.setReferenceFlag(flagEntity);
                                        break;
                                    case "CarePlan":
                                        CarePlanEntity carePlanEntity = carePlanDao.readEntity(ctx, new IdType(reference.getReference()));
                                        answerEntity.setReferenceCarePlan(carePlanEntity);
                                        break;
                                    case "ClinicalImpression":
                                        ClinicalImpressionEntity clinicalImpressionEntity = clinicalImpressionDao.readEntity(ctx, new IdType(reference.getReference()));
                                        answerEntity.setReferenceClinicalImpression(clinicalImpressionEntity);
                                        break;
                                    case "List":
                                        ListEntity
                                                listEntity = listDao.readEntity(ctx, new IdType(reference.getReference()));
                                        answerEntity.setReferenceListResource(listEntity);
                                        break;
                                    case "Consent":
                                        ConsentEntity consentEntity = consentDao.readEntity(ctx, new IdType(reference.getReference()));
                                        answerEntity.setReferenceConsent(consentEntity);
                                        break;
                                    case "Procedure":
                                        ProcedureEntity procedureEntity = procedureDao.readEntity(ctx, new IdType(reference.getReference()));
                                        answerEntity.setReferenceProcedure(procedureEntity);
                                        break;
                                    case "RelatedPerson":
                                        RelatedPersonEntity relatedPersonEntity = personDao.readEntity(ctx, new IdType(reference.getReference()));
                                        answerEntity.setReferencePerson(relatedPersonEntity);
                                        break;
                                    default:
                                        log.error("Not supported: " + resourceType);
                                }
                            }
                        }
                    }
                } catch (Exception ex) {
                    log.error(item.getLinkId() + ex.getMessage());
                    throw new InternalErrorException(ex);
                }
                em.persist(answerEntity);
                if (answer.hasItem()) {
                    for (QuestionnaireResponse.QuestionnaireResponseItemComponent subitem : answer.getItem()) {
                        QuestionnaireResponseItem subItemEntity = new QuestionnaireResponseItem();
                        subItemEntity.setParentAnswer(answerEntity);
                        buildItem(ctx, subitem, subItemEntity);
                    }
                }
            }
        }
    }

}
