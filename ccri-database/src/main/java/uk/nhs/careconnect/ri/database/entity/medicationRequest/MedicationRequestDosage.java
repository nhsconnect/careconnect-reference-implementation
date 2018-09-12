package uk.nhs.careconnect.ri.database.entity.medicationRequest;

import uk.nhs.careconnect.ri.database.entity.BaseResource;
import uk.nhs.careconnect.ri.database.entity.Terminology.ConceptEntity;

import javax.persistence.*;
import java.math.BigDecimal;


@Entity
@Table(name="MedicationRequestDosage")
public class MedicationRequestDosage extends BaseResource {

	public MedicationRequestDosage() {

	}

	public MedicationRequestDosage(MedicationRequestEntity prescription) {
		this.prescription = prescription;
	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name= "PRESCRIPTION_DOSAGE_ID")
	private Long dosageId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn (name = "PRESCRIPTION_ID",foreignKey= @ForeignKey(name="FK_PRESCRIPTION_PRESCRIPTION_DOSAGE"))
	private MedicationRequestEntity prescription;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ADDITIONAL_INSTRUCTION_CONCEPT",foreignKey= @ForeignKey(name="FK_PRESCRIPTION_DOSE_ADDITIONAL_INSTRUCTION_CONCEPT"))

    ConceptEntity additionalInstructionCode;

	@Column(name="sequence")
	Integer sequence;

	@Column(name="patientInstruction")
	String patientInstruction;



	@Column(name="otherText")
    String otherText;

	// TODO TIMING

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ASNEEDED_CONCEPT",foreignKey= @ForeignKey(name="FK_PRESCRIPTION_DOSE_AS_NEEDED_CONCEPT"))

	ConceptEntity asNeededCode;

	@Column(name="asNeededBoolean")
	Boolean asNeededBoolean;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "SITE_CONCEPT",foreignKey= @ForeignKey(name="FK_PRESCRIPTION_DOSE_SITE_CONCEPT"))

	ConceptEntity siteCode;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ROUTE_CONCEPT",foreignKey= @ForeignKey(name="FK_PRESCRIPTION_DOSE_ROUTE_CONCEPT"))

	ConceptEntity routeCode;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "METHOD_CONCEPT",foreignKey= @ForeignKey(name="FK_PRESCRIPTION_DOSE_METHOD_CONCEPT"))

	ConceptEntity methodCode;

	@Column(name="doseRangeLow")
	private BigDecimal doseRangeLow;

	@Column(name="doseRangeHigh")
	private BigDecimal doseRangeHigh;

	@Column(name="doseQuantity")
	private BigDecimal doseQuantity;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name="DOSE_UNITS_CONCEPT",foreignKey= @ForeignKey(name="FK_PRESCRIPTION_DOSE_UNITS_CONCEPT"))

	private ConceptEntity doseUnitOfMeasure;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name="DOSE_LOW_UNITS_CONCEPT",foreignKey= @ForeignKey(name="FK_PRESCRIPTION_DOSE_LOW_UNITS_CONCEPT"))

	private ConceptEntity doseLowUnitOfMeasure;



	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name="DOSE_HIGH_UNITS_CONCEPT",foreignKey= @ForeignKey(name="FK_PRESCRIPTION_DOSE_HIGH_UNITS_CONCEPT"))

	private ConceptEntity doseHighUnitOfMeasure;



	public MedicationRequestEntity getMedicationRequest() {
	        return this.prescription;
	}

	public MedicationRequestDosage setMedicationRequest(MedicationRequestEntity prescription) {
	        this.prescription = prescription;
        return this;
	}

	public Long getDosageId() {
		return dosageId;
	}

	public MedicationRequestDosage setDosageId(Long dosageId) {
		this.dosageId = dosageId;
        return this;
	}

	public MedicationRequestEntity getPrescription() {
		return prescription;
	}

	public MedicationRequestDosage setPrescription(MedicationRequestEntity prescription) {
		this.prescription = prescription;
		return this;
	}

	public ConceptEntity getAdditionalInstructionCode() {
		return additionalInstructionCode;
	}

	public MedicationRequestDosage setAdditionalInstructionCode(ConceptEntity additionalInstructionCode) {
		this.additionalInstructionCode = additionalInstructionCode;
        return this;
	}

	public Integer getSequence() {
		return sequence;
	}

	public MedicationRequestDosage setSequence(Integer sequence) {
		this.sequence = sequence;
        return this;
	}

	public String getPatientInstruction() {
		return patientInstruction;
	}

	public MedicationRequestDosage setPatientInstruction(String patientInstruction) {
		this.patientInstruction = patientInstruction;
        return this;
	}

	public ConceptEntity getAsNeededCode() {
		return asNeededCode;
	}

	public MedicationRequestDosage setAsNeededCode(ConceptEntity asNeededCode) {
		this.asNeededCode = asNeededCode;
        return this;
	}

	public Boolean getAsNeededBoolean() {
		return asNeededBoolean;
	}

	public MedicationRequestDosage setAsNeededBoolean(Boolean asNeededBoolean) {
		this.asNeededBoolean = asNeededBoolean;
        return this;
	}

	public ConceptEntity getSiteCode() {
		return siteCode;
	}

	public MedicationRequestDosage setSiteCode(ConceptEntity siteCode) {
		this.siteCode = siteCode;
        return this;
	}

	public ConceptEntity getRouteCode() {
		return routeCode;
	}

	public MedicationRequestDosage setRouteCode(ConceptEntity routeCode) {
		this.routeCode = routeCode;
        return this;
	}

	public ConceptEntity getMethodCode() {
		return methodCode;
	}

	public MedicationRequestDosage setMethodCode(ConceptEntity methodCode) {
		this.methodCode = methodCode;
        return this;
	}

	public BigDecimal getDoseRangeLow() {
		return doseRangeLow;
	}

	public MedicationRequestDosage setDoseRangeLow(BigDecimal doseRangeLow) {
		this.doseRangeLow = doseRangeLow;
        return this;
	}

	public BigDecimal getDoseRangeHigh() {
		return doseRangeHigh;
	}

	public MedicationRequestDosage setDoseRangeHigh(BigDecimal doseRangeHigh) {
		this.doseRangeHigh = doseRangeHigh;
        return this;
	}

	public BigDecimal getDoseQuantity() {
		return doseQuantity;
	}

	public MedicationRequestDosage setDoseQuantity(BigDecimal doseQuantity) {
		this.doseQuantity = doseQuantity;
        return this;
	}

	public ConceptEntity getDoseUnitOfMeasure() {
		return doseUnitOfMeasure;
	}

	public MedicationRequestDosage setDoseUnitOfMeasure(ConceptEntity doseUnitOfMeasure) {
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
