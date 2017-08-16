package uk.gov.hscic.patient;

import ca.uhn.fhir.model.dstu2.valueset.IssueTypeEnum;
import ca.uhn.fhir.rest.server.exceptions.InvalidRequestException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hscic.OperationOutcomeFactory;
import uk.gov.hscic.SystemCode;
import uk.gov.hscic.medications.MedicationHtmlRepository;
import uk.gov.hscic.medications.PatientMedicationHtmlEntity;
import uk.gov.hscic.model.patient.AdminItemData;
import uk.gov.hscic.model.patient.ClinicalItemData;
import uk.gov.hscic.model.patient.EncounterData;
import uk.gov.hscic.patient.adminitems.AdminItemSearch;
import uk.gov.hscic.patient.allergies.AllergyEntity;
import uk.gov.hscic.patient.allergies.AllergyRepository;
import uk.gov.hscic.patient.clinicalitems.ClinicalItemSearch;
import uk.gov.hscic.patient.encounters.EncounterSearch;
import uk.gov.hscic.patient.html.PageSection;
import uk.gov.hscic.patient.html.Table;
import uk.gov.hscic.patient.immunisations.ImmunisationEntity;
import uk.gov.hscic.patient.immunisations.ImmunisationRepository;
import uk.gov.hscic.patient.investigations.InvestigationEntity;
import uk.gov.hscic.patient.investigations.InvestigationRepository;
import uk.gov.hscic.patient.observations.ObservationEntity;
import uk.gov.hscic.patient.observations.ObservationRepository;
import uk.gov.hscic.patient.problems.ProblemEntity;
import uk.gov.hscic.patient.problems.ProblemRepository;
import uk.gov.hscic.patient.referrals.ReferralEntity;
import uk.gov.hscic.patient.referrals.ReferralSearch;

@Component
public class PageSectionFactory {

    @Autowired
    private ProblemRepository problemRepository;

    @Autowired
    private EncounterSearch encounterSearch;

    @Autowired
    private AllergyRepository allergyRepository;

    @Autowired
    private ClinicalItemSearch clinicalItemsSearch;

    @Autowired
    private MedicationHtmlRepository medicationHtmlRepository;

    @Autowired
    private ReferralSearch referralSearch;

    @Autowired
    private ObservationRepository observationRepository;

    @Autowired
    private InvestigationRepository investigationRepository;

    @Autowired
    private ImmunisationRepository immunisationRepository;

    @Autowired
    private AdminItemSearch adminItemSearch;

    public PageSection getPRBActivePageSection(String nhsNumber, Date requestedFromDate, Date requestedToDate) {
        List<List<Object>> problemActiveRows = new ArrayList<>();

        for (ProblemEntity problem : problemRepository.findBynhsNumber(nhsNumber)) {
            if ("Active".equals(problem.getActiveOrInactive())) {
                problemActiveRows.add(Arrays.asList(problem.getStartDate(), problem.getEntry(), problem.getSignificance(), problem.getDetails()));
            }
        }

        return new PageSection("Active Problems and Issues",
                new Table(Arrays.asList("Start Date", "Entry", "Significance", "Details"), problemActiveRows),
                requestedFromDate, requestedToDate);
    }

    public PageSection getPRBInctivePageSection(String nhsNumber, Date requestedFromDate, Date requestedToDate) {
        List<List<Object>> problemInactiveRows = new ArrayList<>();

        for (ProblemEntity problem : problemRepository.findBynhsNumber(nhsNumber)) {
            if (!"Active".equals(problem.getActiveOrInactive())) {
                problemInactiveRows.add(Arrays.asList(problem.getStartDate(), problem.getEndDate(), problem.getEntry(), problem.getSignificance(), problem.getDetails()));
            }
        }

        return new PageSection("Inactive Problems and Issues",
                new Table(Arrays.asList("Start Date", "End Date", "Entry", "Significance", "Details"), problemInactiveRows),
                requestedFromDate, requestedToDate);
    }

    public PageSection getENCPageSection(String header, String nhsNumber, Date fromDate, Date toDate, Date requestedFromDate, Date requestedToDate, int limit) {
        List<List<Object>> encounterRows = new ArrayList<>();

        for (EncounterData encounter : encounterSearch.findEncounterData(nhsNumber, fromDate, toDate, limit)) {
            encounterRows.add(Arrays.asList(encounter.getEncounterDate(), encounter.getTitle(), encounter.getDetails()));
        }

        return new PageSection(header,
                new Table(Arrays.asList("Date", "Title", "Details"), encounterRows),
                requestedFromDate, requestedToDate);
    }

    public PageSection getALLCurrentPageSection(String nhsNumber, Date fromDate, Date toDate, Date requestedFromDate, Date requestedToDate) {
        if (toDate != null && fromDate != null) {
            throw OperationOutcomeFactory.buildOperationOutcomeException(
                    new InvalidRequestException("Date Ranges not allowed to be set"),
                    SystemCode.INVALID_PARAMETER, IssueTypeEnum.BUSINESS_RULE_VIOLATION);
        }

        List<List<Object>> currentAllergyRows = new ArrayList<>();

        for (AllergyEntity allergyEntity : allergyRepository.findByNhsNumber(nhsNumber)) {
            if ("Current".equals(allergyEntity.getCurrentOrHistoric())) {
                currentAllergyRows.add(Arrays.asList(allergyEntity.getStartDate(), allergyEntity.getDetails()));
            }
        }

        return new PageSection("Current Allergies and Adverse Reactions",
                new Table(Arrays.asList("Start Date", "Details"), currentAllergyRows),
                requestedFromDate, requestedToDate);
    }

    public PageSection getALLHistoricalPageSection(String nhsNumber, Date fromDate, Date toDate, Date requestedFromDate, Date requestedToDate) {
        if (toDate != null && fromDate != null) {
            throw OperationOutcomeFactory.buildOperationOutcomeException(
                    new InvalidRequestException("Date Ranges not allowed to be set"),
                    SystemCode.INVALID_PARAMETER, IssueTypeEnum.BUSINESS_RULE_VIOLATION);
        }

        List<List<Object>> historicalAllergyRows = new ArrayList<>();

        for (AllergyEntity allergyEntity : allergyRepository.findByNhsNumber(nhsNumber)) {
            if (!"Current".equals(allergyEntity.getCurrentOrHistoric())) {
                historicalAllergyRows.add(Arrays.asList(allergyEntity.getStartDate(), allergyEntity.getEndDate(), allergyEntity.getDetails()));
            }
        }

        return new PageSection("Historical Allergies and Adverse Reactions",
                new Table(Arrays.asList("Start Date", "End Date", "Details"), historicalAllergyRows),
                requestedFromDate, requestedToDate);
    }

    public PageSection getCLIPageSection(String nhsNumber, Date fromDate, Date toDate, Date requestedFromDate, Date requestedToDate) {
        List<ClinicalItemData> clinicalItemList = clinicalItemsSearch.findAllClinicalItemHTMLTables(nhsNumber, fromDate, toDate);
        List<List<Object>> clinicalItemsRows = new ArrayList<>();

        if (clinicalItemList != null) {
            for (ClinicalItemData clinicalItemData : clinicalItemList) {
                clinicalItemsRows.add(Arrays.asList(clinicalItemData.getDate(), clinicalItemData.getEntry(), clinicalItemData.getDetails()));
            }
        }

        return new PageSection("Clinical Items",
                new Table(Arrays.asList("Date", "Entry", "Details"), clinicalItemsRows),
                requestedFromDate, requestedToDate);
    }

    public PageSection getMEDCurrentPageSection(String nhsNumber, Date requestedFromDate, Date requestedToDate) {
        List<List<Object>> currentMedRows = new ArrayList<>();

        for (PatientMedicationHtmlEntity patientMedicationHtmlEntity : medicationHtmlRepository.findBynhsNumber(nhsNumber)) {
            if ("Current".equals(patientMedicationHtmlEntity.getCurrentRepeatPast())) {
                currentMedRows.add(Arrays.asList(
                        patientMedicationHtmlEntity.getStartDate(),
                        patientMedicationHtmlEntity.getMedicationItem(),
                        patientMedicationHtmlEntity.getTypeMed(),
                        patientMedicationHtmlEntity.getScheduledEnd(),
                        patientMedicationHtmlEntity.getDaysDuration(),
                        patientMedicationHtmlEntity.getDetails()));
            }
        }

        return new PageSection("Current Medication Issues",
                new Table(Arrays.asList("Start Date", "Medication Item", "Type", "Scheduled End", "Days Duration", "Details"), currentMedRows),
                requestedFromDate, requestedToDate);
    }

    public PageSection getMEDRepeatPageSection(String nhsNumber, Date requestedFromDate, Date requestedToDate) {
        List<List<Object>> repeatMedRows = new ArrayList<>();

        for (PatientMedicationHtmlEntity patientMedicationHtmlEntity : medicationHtmlRepository.findBynhsNumber(nhsNumber)) {
            if ("Repeat".equals(patientMedicationHtmlEntity.getCurrentRepeatPast())) {
                repeatMedRows.add(Arrays.asList(
                        patientMedicationHtmlEntity.getLastIssued(),
                        patientMedicationHtmlEntity.getMedicationItem(),
                        patientMedicationHtmlEntity.getStartDate(),
                        patientMedicationHtmlEntity.getReviewDate(),
                        patientMedicationHtmlEntity.getNumberIssued(),
                        patientMedicationHtmlEntity.getMaxIssues(),
                        patientMedicationHtmlEntity.getDetails()));
            }
        }

        return new PageSection("Current Repeat Medications",
                new Table(Arrays.asList("Last Issued", "Medication Item", "Start Date", "Review Date", "Number Issued", "Max Issues", "Details"), repeatMedRows),
                requestedFromDate, requestedToDate);
    }

    public PageSection getMEDPastPageSection(String nhsNumber, Date requestedFromDate, Date requestedToDate) {
        List<List<Object>> pastMedRows = new ArrayList<>();

        for (PatientMedicationHtmlEntity patientMedicationHtmlEntity : medicationHtmlRepository.findBynhsNumber(nhsNumber)) {
            if ("Past".equals(patientMedicationHtmlEntity.getCurrentRepeatPast())) {
                pastMedRows.add(Arrays.asList(
                        patientMedicationHtmlEntity.getStartDate(),
                        patientMedicationHtmlEntity.getMedicationItem(),
                        patientMedicationHtmlEntity.getTypeMed(),
                        patientMedicationHtmlEntity.getLastIssued(),
                        patientMedicationHtmlEntity.getReviewDate(),
                        patientMedicationHtmlEntity.getNumberIssued(),
                        patientMedicationHtmlEntity.getMaxIssues(),
                        patientMedicationHtmlEntity.getDetails()));
            }
        }

        return new PageSection("Past Medications",
                new Table(Arrays.asList("Start Date", "Medication Item", "Type", "Last Issued", "Review Date", "Number Issued", "Max Issues", "Details"), pastMedRows),
                requestedFromDate, requestedToDate);
    }

    public PageSection getREFPageSection(String nhsNumber, Date fromDate, Date toDate, Date requestedFromDate, Date requestedToDate) {
        List<List<Object>> referralRows = new ArrayList<>();

        for (ReferralEntity referralEntity : referralSearch.findReferrals(nhsNumber, fromDate, toDate)) {
            referralRows.add(Arrays.asList(
                    referralEntity.getSectionDate(),
                    referralEntity.getFrom(),
                    referralEntity.getTo(),
                    referralEntity.getPriority(),
                    referralEntity.getDetails()));
        }

        return new PageSection("Referrals",
                new Table(Arrays.asList("Date", "From", "To", "Priority", "Details"), referralRows),
                requestedFromDate, requestedToDate);
    }

    public PageSection getOBSPageSection(String nhsNumber, Date requestedFromDate, Date requestedToDate) {
        List<List<Object>> observationRows = new ArrayList<>();

        for (ObservationEntity observationEntity : observationRepository.findBynhsNumber(nhsNumber)) {
            observationRows.add(Arrays.asList(
                    observationEntity.getObservationDate(),
                    observationEntity.getEntry(),
                    observationEntity.getValue(),
                    observationEntity.getValue()));
        }

        return new PageSection("Observations",
                new Table(Arrays.asList("Date", "Entry", "Value", "Details"), observationRows),
                requestedFromDate, requestedToDate);
    }

    public PageSection getINVPageSection(String nhsNumber, Date requestedFromDate, Date requestedToDate) {
        List<List<Object>> investigationRows = new ArrayList<>();

        for (InvestigationEntity investigationEntity : investigationRepository.findByNhsNumber(nhsNumber)) {
            investigationRows.add(Arrays.asList(
                    investigationEntity.getDate(),
                    investigationEntity.getTitle(),
                    investigationEntity.getDetails()));
        }

        return new PageSection("Investigations",
                new Table(Arrays.asList("Date", "Title", "Details"), investigationRows),
                requestedFromDate, requestedToDate);
    }

    public PageSection getIMMPageSection(String nhsNumber, Date fromDate, Date toDate, Date requestedFromDate, Date requestedToDate) {
        if (toDate != null && fromDate != null) {
            throw OperationOutcomeFactory.buildOperationOutcomeException(
                    new InvalidRequestException("Date Ranges not allowed to be set"),
                    SystemCode.INVALID_PARAMETER, IssueTypeEnum.BUSINESS_RULE_VIOLATION);
        }

        List<List<Object>> immunisationRows = new ArrayList<>();

        for (ImmunisationEntity immunisationEntity : immunisationRepository.findByNhsNumber(nhsNumber)) {
            immunisationRows.add(Arrays.asList(
                    immunisationEntity.getDateOfVac(),
                    immunisationEntity.getVaccination(),
                    immunisationEntity.getPart(),
                    immunisationEntity.getContents(),
                    immunisationEntity.getDetails()));
        }

        return new PageSection("Immunisations",
                new Table(Arrays.asList("Date", "Vaccination", "Part", "Contents", "Details"), immunisationRows),
                requestedFromDate, requestedToDate);
    }

    public PageSection getADMPageSection(String nhsNumber, Date fromDate, Date toDate, Date requestedFromDate, Date requestedToDate) {
        List<List<Object>> adminItemsRows = new ArrayList<>();
        List<AdminItemData> adminItemList = adminItemSearch.findAllAdminItemHTMLTables(nhsNumber, fromDate, toDate);

        if (adminItemList != null) {
            for (AdminItemData adminItemData : adminItemList) {
                adminItemsRows.add(Arrays.asList(adminItemData.getAdminDate(), adminItemData.getEntry(), adminItemData.getDetails()));
            }
        }

        return new PageSection("Administrative Items",
                new Table(Arrays.asList("Date", "Entry", "Details"), adminItemsRows),
                requestedFromDate, requestedToDate);
    }
}
