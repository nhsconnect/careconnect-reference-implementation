package uk.nhs.careconnect.ri.database.entity.medicationDispense;

import uk.nhs.careconnect.ri.database.entity.Terminology.ConceptEntity;
import uk.nhs.careconnect.ri.database.entity.BaseResource;

import javax.persistence.*;
import java.math.BigDecimal;


@Entity
@Table(name="MedicationDispenseDosage")
public class MedicationDispenseDosage extends BaseResource {

	public MedicationDispenseDosage() {

	}

	public MedicationDispenseDosage(MedicationDispenseEntity dispense) {
		this.dispense = dispense;
	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name= "DISPENSE_DOSAGE_ID")
	private Long dosageId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn (name = "DISPENSE_ID",foreignKey= @ForeignKey(name="FK_DISPENSE_DISPENSE_DOSAGE"))
	private MedicationDispenseEntity dispense;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ADDITIONAL_INSTRUCTION_CONCEPT",foreignKey= @ForeignKey(name="FK_DISPENSE_DOSE_ADDITIONAL_INSTRUCTION_CONCEPT"))

    ConceptEntity additionalInstructionCode;

	@Column(name="sequence")
	Integer sequence;

	@Column(name="patientInstruction")
	String patientInstruction;



	@Column(name="otherText")
    String otherText;

	// TODO TIMING

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ASNEEDED_CONCEPT",foreignKey= @ForeignKey(name="FK_DISPENSE_DOSE_AS_NEEDED_CONCEPT"))

	ConceptEntity asNeededCode;

	@Column(name="asNeededBoolean")
	Boolean asNeededBoolean;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "SITE_CONCEPT",foreignKey= @ForeignKey(name="FK_DISPENSE_DOSE_SITE_CONCEPT"))

	ConceptEntity siteCode;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ROUTE_CONCEPT",foreignKey= @ForeignKey(name="FK_DISPENSE_DOSE_ROUTE_CONCEPT"))

	ConceptEntity routeCode;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "METHOD_CONCEPT",foreignKey= @ForeignKey(name="FK_DISPENSE_DOSE_METHOD_CONCEPT"))

	ConceptEntity methodCode;

	@Column(name="doseRangeLow")
	private BigDecimal doseRangeLow;

	@Column(name="doseRangeHigh")
	private BigDecimal doseRangeHigh;

	@Column(name="doseQuantity")
	private BigDecimal doseQuantity;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name="DOSE_UNITS_CONCEPT",foreignKey= @ForeignKey(name="FK_DISPENSE_DOSE_UNITS_CONCEPT"))

	private ConceptEntity doseUnitOfMeasure;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name="DOSE_LOW_UNITS_CONCEPT",foreignKey= @ForeignKey(name="FK_DISPENSE_DOSE_LOW_UNITS_CONCEPT"))

	private ConceptEntity doseLowUnitOfMeasure;



	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name="DOSE_HIGH_UNITS_CONCEPT",foreignKey= @ForeignKey(name="FK_DISPENSE_DOSE_HIGH_UNITS_CONCEPT"))

	private ConceptEntity doseHighUnitOfMeasure;



	public MedicationDispenseEntity getMedicationDispense() {
	        return this.dispense;
	}

	public MedicationDispenseDosage setMedicationDispense(MedicationDispenseEntity dispense) {
	        this.dispense = dispense;
        return this;
	}

	public Long getDosageId() {
		return dosageId;
	}

	public MedicationDispenseDosage setDosageId(Long dosageId) {
		this.dosageId = dosageId;
        return this;
	}

	public MedicationDispenseEntity getPrescription() {
		return dispense;
	}

	public MedicationDispenseDosage setPrescription(MedicationDispenseEntity dispense) {
		this.dispense = dispense;
		return this;
	}

	public ConceptEntity getAdditionalInstructionCode() {
		return additionalInstructionCode;
	}

	public MedicationDispenseDosage setAdditionalInstructionCode(ConceptEntity additionalInstructionCode) {
		this.additionalInstructionCode = additionalInstructionCode;
        return this;
	}

	public Integer getSequence() {
		return sequence;
	}

	public MedicationDispenseDosage setSequence(Integer sequence) {
		this.sequence = sequence;
        return this;
	}

	public String getPatientInstruction() {
		return patientInstruction;
	}

	public MedicationDispenseDosage setPatientInstruction(String patientInstruction) {
		this.patientInstruction = patientInstruction;
        return this;
	}

	public ConceptEntity getAsNeededCode() {
		return asNeededCode;
	}

	public MedicationDispenseDosage setAsNeededCode(ConceptEntity asNeededCode) {
		this.asNeededCode = asNeededCode;
        return this;
	}

	public Boolean getAsNeededBoolean() {
		return asNeededBoolean;
	}

	public MedicationDispenseDosage setAsNeededBoolean(Boolean asNeededBoolean) {
		this.asNeededBoolean = asNeededBoolean;
        return this;
	}

	public ConceptEntity getSiteCode() {
		return siteCode;
	}

	public MedicationDispenseDosage setSiteCode(ConceptEntity siteCode) {
		this.siteCode = siteCode;
        return this;
	}

	public ConceptEntity getRouteCode() {
		return routeCode;
	}

	public MedicationDispenseDosage setRouteCode(ConceptEntity routeCode) {
		this.routeCode = routeCode;
        return this;
	}

	public ConceptEntity getMethodCode() {
		return methodCode;
	}

	public MedicationDispenseDosage setMethodCode(ConceptEntity methodCode) {
		this.methodCode = methodCode;
        return this;
	}

	public BigDecimal getDoseRangeLow() {
		return doseRangeLow;
	}

	public MedicationDispenseDosage setDoseRangeLow(BigDecimal doseRangeLow) {
		this.doseRangeLow = doseRangeLow;
        return this;
	}

	public BigDecimal getDoseRangeHigh() {
		return doseRangeHigh;
	}

	public MedicationDispenseDosage setDoseRangeHigh(BigDecimal doseRangeHigh) {
		this.doseRangeHigh = doseRangeHigh;
        return this;
	}

	public BigDecimal getDoseQuantity() {
		return doseQuantity;
	}

	public MedicationDispenseDosage setDoseQuantity(BigDecimal doseQuantity) {
		this.doseQuantity = doseQuantity;
        return this;
	}

	public ConceptEntity getDoseUnitOfMeasure() {
		return doseUnitOfMeasure;
	}

	public MedicationDispenseDosage setDoseUnitOfMeasure(ConceptEntity doseUnitOfMeasure) {
		this.doseUnitOfMeasure = doseUnitOfMeasure;
        return this;
	}

	public String getOtherText() {
		return otherText;
	}

	public void setOtherText(String otherText) {
		this.otherText = otherText;
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
}
