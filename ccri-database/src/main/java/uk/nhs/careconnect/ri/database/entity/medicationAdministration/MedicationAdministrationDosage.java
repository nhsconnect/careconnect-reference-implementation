package uk.nhs.careconnect.ri.database.entity.medicationAdministration;

import uk.nhs.careconnect.ri.database.entity.BaseResource;
import uk.nhs.careconnect.ri.database.entity.Terminology.ConceptEntity;
import uk.nhs.careconnect.ri.database.entity.medicationAdministration.MedicationAdministrationEntity;

import javax.persistence.*;
import java.math.BigDecimal;


@Entity
@Table(name="MedicationAdministrationDosage")
public class MedicationAdministrationDosage extends BaseResource {

	public MedicationAdministrationDosage() {

	}

	public MedicationAdministrationDosage(MedicationAdministrationEntity administration) {
		this.administration = administration;
	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name= "ADMINISTRATION_DOSAGE_ID")
	private Long dosageId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn (name = "ADMINISTRATION_ID",foreignKey= @ForeignKey(name="FK_ADMINISTRATION_ADMINISTRATION_DOSAGE"))
	private MedicationAdministrationEntity administration;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ADDITIONAL_INSTRUCTION_CONCEPT",foreignKey= @ForeignKey(name="FK_ADMINISTRATION_DOSE_ADDITIONAL_INSTRUCTION_CONCEPT"))

    ConceptEntity additionalInstructionCode;

	@Column(name="sequence")
	Integer sequence;

	@Column(name="patientInstruction")
	String patientInstruction;



	@Column(name="otherText")
    String dosageText;

	// TODO TIMING

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ASNEEDED_CONCEPT",foreignKey= @ForeignKey(name="FK_ADMINISTRATION_DOSE_AS_NEEDED_CONCEPT"))

	ConceptEntity asNeededCode;

	@Column(name="asNeededBoolean")
	Boolean asNeededBoolean;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "SITE_CONCEPT",foreignKey= @ForeignKey(name="FK_ADMINISTRATION_DOSE_SITE_CONCEPT"))

	ConceptEntity siteCode;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ROUTE_CONCEPT",foreignKey= @ForeignKey(name="FK_ADMINISTRATION_DOSE_ROUTE_CONCEPT"))

	ConceptEntity routeCode;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "METHOD_CONCEPT",foreignKey= @ForeignKey(name="FK_ADMINISTRATION_DOSE_METHOD_CONCEPT"))

	ConceptEntity methodCode;

	@Column(name="doseRangeLow")
	private BigDecimal doseRangeLow;

	@Column(name="doseRangeHigh")
	private BigDecimal doseRangeHigh;

	@Column(name="doseQuantity")
	private BigDecimal doseQuantity;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name="DOSE_UNITS_CONCEPT",foreignKey= @ForeignKey(name="FK_ADMINISTRATION_DOSE_UNITS_CONCEPT"))

	private ConceptEntity doseUnitOfMeasure;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name="DOSE_LOW_UNITS_CONCEPT",foreignKey= @ForeignKey(name="FK_ADMINISTRATION_DOSE_LOW_UNITS_CONCEPT"))

	private ConceptEntity doseLowUnitOfMeasure;



	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name="DOSE_HIGH_UNITS_CONCEPT",foreignKey= @ForeignKey(name="FK_ADMINISTRATION_DOSE_HIGH_UNITS_CONCEPT"))

	private ConceptEntity doseHighUnitOfMeasure;



	public MedicationAdministrationEntity getMedicationAdministration() {
	        return this.administration;
	}

	public MedicationAdministrationDosage setMedicationAdministration(MedicationAdministrationEntity administration) {
	        this.administration = administration;
        return this;
	}

	public Long getDosageId() {
		return dosageId;
	}

	public MedicationAdministrationDosage setDosageId(Long dosageId) {
		this.dosageId = dosageId;
        return this;
	}

	public MedicationAdministrationEntity getPrescription() {
		return administration;
	}

	public MedicationAdministrationDosage setPrescription(MedicationAdministrationEntity administration) {
		this.administration = administration;
		return this;
	}

	public ConceptEntity getAdditionalInstructionCode() {
		return additionalInstructionCode;
	}

	public MedicationAdministrationDosage setAdditionalInstructionCode(ConceptEntity additionalInstructionCode) {
		this.additionalInstructionCode = additionalInstructionCode;
        return this;
	}

	public Integer getSequence() {
		return sequence;
	}

	public MedicationAdministrationDosage setSequence(Integer sequence) {
		this.sequence = sequence;
        return this;
	}

	public String getPatientInstruction() {
		return patientInstruction;
	}

	public MedicationAdministrationDosage setPatientInstruction(String patientInstruction) {
		this.patientInstruction = patientInstruction;
        return this;
	}

	public ConceptEntity getAsNeededCode() {
		return asNeededCode;
	}

	public MedicationAdministrationDosage setAsNeededCode(ConceptEntity asNeededCode) {
		this.asNeededCode = asNeededCode;
        return this;
	}

	public Boolean getAsNeededBoolean() {
		return asNeededBoolean;
	}

	public MedicationAdministrationDosage setAsNeededBoolean(Boolean asNeededBoolean) {
		this.asNeededBoolean = asNeededBoolean;
        return this;
	}

	public ConceptEntity getSiteCode() {
		return siteCode;
	}

	public MedicationAdministrationDosage setSiteCode(ConceptEntity siteCode) {
		this.siteCode = siteCode;
        return this;
	}

	public ConceptEntity getRouteCode() {
		return routeCode;
	}

	public MedicationAdministrationDosage setRouteCode(ConceptEntity routeCode) {
		this.routeCode = routeCode;
        return this;
	}

	public ConceptEntity getMethodCode() {
		return methodCode;
	}

	public MedicationAdministrationDosage setMethodCode(ConceptEntity methodCode) {
		this.methodCode = methodCode;
        return this;
	}

	public BigDecimal getDoseRangeLow() {
		return doseRangeLow;
	}

	public MedicationAdministrationDosage setDoseRangeLow(BigDecimal doseRangeLow) {
		this.doseRangeLow = doseRangeLow;
        return this;
	}

	public BigDecimal getDoseRangeHigh() {
		return doseRangeHigh;
	}

	public MedicationAdministrationDosage setDoseRangeHigh(BigDecimal doseRangeHigh) {
		this.doseRangeHigh = doseRangeHigh;
        return this;
	}

	public BigDecimal getDoseQuantity() {
		return doseQuantity;
	}

	public MedicationAdministrationDosage setDoseQuantity(BigDecimal doseQuantity) {
		this.doseQuantity = doseQuantity;
        return this;
	}

	public ConceptEntity getDoseUnitOfMeasure() {
		return doseUnitOfMeasure;
	}

	public MedicationAdministrationDosage setDoseUnitOfMeasure(ConceptEntity doseUnitOfMeasure) {
		this.doseUnitOfMeasure = doseUnitOfMeasure;
        return this;
	}

	public MedicationAdministrationEntity getAdministration() {
		return administration;
	}

	public void setAdministration(MedicationAdministrationEntity administration) {
		this.administration = administration;
	}

	public ConceptEntity getDoseLowUnitOfMeasure() {
		return doseLowUnitOfMeasure;
	}

	public void setDoseLowUnitOfMeasure(ConceptEntity doseLowUnitOfMeasure) {
		this.doseLowUnitOfMeasure = doseLowUnitOfMeasure;
	}

	public ConceptEntity getDoseHighUnitOfMeasure() {
		return doseHighUnitOfMeasure;
	}

	public void setDoseHighUnitOfMeasure(ConceptEntity doseHighUnitOfMeasure) {
		this.doseHighUnitOfMeasure = doseHighUnitOfMeasure;
	}

	@Override
	public Long getId() {
		return this.dosageId;
	}

	public String getDosageText() {
		return dosageText;
	}

	public void setDosageText(String dosageText) {
		this.dosageText = dosageText;
	}
}
