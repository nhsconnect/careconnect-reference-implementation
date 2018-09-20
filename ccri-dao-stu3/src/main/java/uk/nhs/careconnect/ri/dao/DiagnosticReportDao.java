package uk.nhs.careconnect.ri.dao;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenParam;
import org.hl7.fhir.dstu3.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Repository;
import uk.nhs.careconnect.fhir.OperationOutcomeException;
import uk.nhs.careconnect.ri.database.daointerface.*;
import uk.nhs.careconnect.ri.dao.transforms.DiagnosticReportEntityToFHIRDiagnosticReportTransformer;
import uk.nhs.careconnect.ri.database.entity.Terminology.ConceptEntity;
import uk.nhs.careconnect.ri.database.entity.condition.ConditionEntity;
import uk.nhs.careconnect.ri.database.entity.diagnosticReport.DiagnosticReportEntity;
import uk.nhs.careconnect.ri.database.entity.diagnosticReport.DiagnosticReportIdentifier;
import uk.nhs.careconnect.ri.database.entity.diagnosticReport.DiagnosticReportResult;
import uk.nhs.careconnect.ri.database.entity.observation.ObservationEntity;
import uk.nhs.careconnect.ri.database.entity.patient.PatientEntity;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import javax.transaction.Transactional;
import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

@Repository
@Transactional
public class DiagnosticReportDao implements DiagnosticReportRepository {

    @PersistenceContext
    EntityManager em;

    @Autowired
    @Lazy
    ConceptRepository conceptDao;

    @Autowired
    PatientRepository patientDao;

    @Autowired
    PractitionerRepository practitionerDao;

    @Autowired
    @Lazy
    EncounterRepository encounterDao;

    @Autowired
    ObservationRepository observationDao;

    @Autowired
    OrganisationRepository organisationDao;

    @Autowired
    private CodeSystemRepository codeSystemSvc;

    @Autowired
    DiagnosticReportEntityToFHIRDiagnosticReportTransformer diagnosticReportEntityToFHIRDiagnosticReportTransformer;


    private static final Logger log = LoggerFactory.getLogger(DiagnosticReportDao.class);
    @Override
    public void save(FhirContext ctx, DiagnosticReportEntity diagnosticReport) {

    }

    @Override
    public DiagnosticReport read(FhirContext ctx, IdType theId) {
        if (daoutils.isNumeric(theId.getIdPart())) {
            DiagnosticReportEntity diagnosticReport = em.find(DiagnosticReportEntity.class, Long.parseLong(theId.getIdPart()));

            return diagnosticReport != null ? diagnosticReportEntityToFHIRDiagnosticReportTransformer.transform(diagnosticReport) : null;
        } else {
            return null;
        }
    }

    @Override
    public Long count() {
        CriteriaBuilder qb = em.getCriteriaBuilder();
        CriteriaQuery<Long> cq = qb.createQuery(Long.class);
        cq.select(qb.count(cq.from(ConditionEntity.class)));
        //cq.where(/*your stuff*/);
        return em.createQuery(cq).getSingleResult();
    }

    @Override
    public DiagnosticReportEntity readEntity(FhirContext ctx, IdType theId) {
        if (daoutils.isNumeric(theId.getIdPart())) {
            DiagnosticReportEntity diagnosticReport = em.find(DiagnosticReportEntity.class, Long.parseLong(theId.getIdPart()));

            return diagnosticReport;
        } else {
            return null;
        }
    }

    @Override
    public DiagnosticReport create(FhirContext ctx, DiagnosticReport diagnosticReport, IdType theId, String theConditional) throws OperationOutcomeException {
        log.debug("DiagnosticReport.save");

        DiagnosticReportEntity diagnosticReportEntity = null;

        if (diagnosticReport.hasId()) diagnosticReportEntity = readEntity(ctx, diagnosticReport.getIdElement());

        if (theConditional != null) {
            try {


                if (theConditional.contains("fhir.leedsth.nhs.uk/Id/diagnosticReport")) {
                    URI uri = new URI(theConditional);

                    String scheme = uri.getScheme();
                    String host = uri.getHost();
                    String query = uri.getRawQuery();
                    log.debug(query);
                    String[] spiltStr = query.split("%7C");
                    log.debug(spiltStr[1]);

                    List<DiagnosticReportEntity> results = searchEntity(ctx, null, new TokenParam().setValue(spiltStr[1]).setSystem("https://fhir.leedsth.nhs.uk/Id/diagnosticReport"),null);
                    for (DiagnosticReportEntity con : results) {
                        diagnosticReportEntity = con;
                        break;
                    }
                } else {
                    log.info("NOT SUPPORTED: Conditional Url = "+theConditional);
                }

            } catch (Exception ex) {

            }
        }

        if (diagnosticReportEntity == null) {
            diagnosticReportEntity = new DiagnosticReportEntity();
        }



        if (diagnosticReport.hasStatus()) {
            diagnosticReportEntity.setStatus(diagnosticReport.getStatus());
        }

        if (diagnosticReport.hasType()) {
            ConceptEntity code = conceptDao.findAddCode(diagnosticReport.getCategory().getCoding().get(0));
            if (code != null) { diagnosticReportEntity.setCategory(code); }
            else {
                log.info("Type: Missing System/Code = "+ diagnosticReport.getCategory().getCoding().get(0).getSystem() +" code = "+diagnosticReport.getCategory().getCoding().get(0).getCode());

                throw new IllegalArgumentException("Missing System/Code = "+ diagnosticReport.getCategory().getCoding().get(0).getSystem()
                        +" code = "+diagnosticReport.getCategory().getCoding().get(0).getCode());
            }
        }
        if (diagnosticReport.hasCode()) {
            ConceptEntity code = conceptDao.findAddCode(diagnosticReport.getCode().getCoding().get(0));
            if (code != null) { diagnosticReportEntity.setCode(code); }
            else {
                log.info("Class: Missing System/Code = "+ diagnosticReport.getCode().getCoding().get(0).getSystem()
                        +" code = "+diagnosticReport.getCode().getCoding().get(0).getCode());

                throw new IllegalArgumentException("Missing System/Code = "+ diagnosticReport.getCode().getCoding().get(0).getSystem()
                        +" code = "+diagnosticReport.getCode().getCoding().get(0).getCode());
            }
        }

        PatientEntity patientEntity = null;
        if (diagnosticReport.hasSubject()) {
            log.trace(diagnosticReport.getSubject().getReference());
            patientEntity = patientDao.readEntity(ctx, new IdType(diagnosticReport.getSubject().getReference()));
            diagnosticReportEntity.setPatient(patientEntity);
        }



        em.persist(diagnosticReportEntity);


        for (Identifier identifier : diagnosticReport.getIdentifier()) {
            DiagnosticReportIdentifier diagnosticReportIdentifier = null;

            for (DiagnosticReportIdentifier orgSearch : diagnosticReportEntity.getIdentifiers()) {
                if (identifier.getSystem().equals(orgSearch.getSystemUri()) && identifier.getValue().equals(orgSearch.getValue())) {
                    diagnosticReportIdentifier = orgSearch;
                    break;
                }
            }
            if (diagnosticReportIdentifier == null)  diagnosticReportIdentifier = new DiagnosticReportIdentifier();

            diagnosticReportIdentifier.setValue(identifier.getValue());
            diagnosticReportIdentifier.setSystem(codeSystemSvc.findSystem(identifier.getSystem()));
            diagnosticReportIdentifier.setDiagnosticReport(diagnosticReportEntity);
            em.persist(diagnosticReportIdentifier);
        }
        for (Reference reference : diagnosticReport.getResult()) {
            DiagnosticReportResult diagnosticReportResult = null;

            for (DiagnosticReportResult resultSearch : diagnosticReportEntity.getResults()) {
                log.info("Diag = "+reference.getIdElement().getValue());
                if (resultSearch.getObservation().getId().equals(reference.getIdElement().getValue())) {
                    diagnosticReportResult = resultSearch;
                    break;
                }
            }
            if (diagnosticReportResult == null) {
                diagnosticReportResult = new DiagnosticReportResult();
                diagnosticReportResult.setDiagnosticReport(diagnosticReportEntity);
                ObservationEntity observation = observationDao.readEntity(ctx,new IdType(reference.getReference()));
                if (observation!=null) {
                    diagnosticReportResult.setObservation(observation);
                    em.persist(diagnosticReportResult);
                }
            }

        }



        return diagnosticReportEntityToFHIRDiagnosticReportTransformer.transform(diagnosticReportEntity);
    }

    @Override
    public List<DiagnosticReport> search(FhirContext ctx, ReferenceParam patient, TokenParam identifier, StringParam id) {
        List<DiagnosticReportEntity> qryResults = searchEntity(ctx,patient, identifier, id);
        List<DiagnosticReport> results = new ArrayList<>();

        for (DiagnosticReportEntity diagnosticReportEntity : qryResults)
        {
            DiagnosticReport diagnosticReport = diagnosticReportEntityToFHIRDiagnosticReportTransformer.transform(diagnosticReportEntity);
            results.add(diagnosticReport);
        }

        return results;
    }

    @Override
    public List<DiagnosticReportEntity> searchEntity(FhirContext ctx, ReferenceParam patient, TokenParam identifier, StringParam id) {
        List<DiagnosticReportEntity> qryResults = null;

        CriteriaBuilder builder = em.getCriteriaBuilder();

        CriteriaQuery<DiagnosticReportEntity> criteria = builder.createQuery(DiagnosticReportEntity.class);
        Root<DiagnosticReportEntity> root = criteria.from(DiagnosticReportEntity.class);

        List<Predicate> predList = new LinkedList<Predicate>();
        List<Condition> results = new ArrayList<Condition>();

        if (patient != null) {
            // KGM 4/1/2018 only search on patient id
            if (daoutils.isNumeric(patient.getIdPart())) {
                Join<DiagnosticReportEntity, PatientEntity> join = root.join("patient", JoinType.LEFT);

                Predicate p = builder.equal(join.get("id"),patient.getIdPart());
                predList.add(p);
            } else {
                Join<DiagnosticReportEntity, PatientEntity> join = root.join("patient", JoinType.LEFT);

                Predicate p = builder.equal(join.get("id"),-1);
                predList.add(p);
            }

        }
        if (id != null) {
            Predicate p = builder.equal(root.get("id"),id.getValue());
            predList.add(p);
        }
        if (identifier !=null)
        {
            Join<DiagnosticReportEntity, DiagnosticReportIdentifier> join = root.join("identifiers", JoinType.LEFT);

            Predicate p = builder.equal(join.get("value"),identifier.getValue());
            predList.add(p);
            // TODO predList.add(builder.equal(join.get("system"),identifier.getSystem()));

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

        TypedQuery<DiagnosticReportEntity> typedQuery = em.createQuery(criteria).setMaxResults(daoutils.MAXROWS);

        qryResults = typedQuery.getResultList();
        return qryResults;
    }
}
