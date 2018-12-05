package uk.nhs.careconnect.ri.database.entity.medicationDispense;


import uk.nhs.careconnect.ri.database.entity.medicationDispense.MedicationDispenseEntity;
import uk.nhs.careconnect.ri.database.entity.patient.PatientEntity;
import uk.nhs.careconnect.ri.database.entity.practitioner.PractitionerEntity;

import javax.persistence.*;
import java.util.Date;


@Entity
@Table(name="MedicationDispenseNote", uniqueConstraints= @UniqueConstraint(name="PK_DISPENSE_NOTE", columnNames={"DISPENSE_NOTE_ID"})
		)
public class MedicationDispenseNote {

	public MedicationDispenseNote() {

	}

	public MedicationDispenseNote(MedicationDispenseEntity dispense) {
		this.dispense = dispense;
	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name= "DISPENSE_NOTE_ID")
	private Long noteId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn (name = "DISPENSE_ID",foreignKey= @ForeignKey(name="FK_DISPENSE_DISPENSE_NOTE"))
	private MedicationDispenseEntity dispense;

	@Column(name= "noteText")
	private String noteText;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn (name = "PATIENT_ID",foreignKey= @ForeignKey(name="FK_PATIENT_DISPENSE_NOTE"))
	private PatientEntity notePatient;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn (name = "PRACTITIONER_ID",foreignKey= @ForeignKey(name="FK_PRACTITIONER_DISPENSE_NOTE"))
	private PractitionerEntity notePractitioner;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "noteDate")
	private Date noteDate;

    public Long getNoteId() { return noteId; }
	public void setNoteId(Long noteId) { this.noteId = noteId; }

	public MedicationDispenseEntity getMedicationDispense() {
	        return this.dispense;
	}

	public void setMedicationDispense(MedicationDispenseEntity dispense) {
	        this.dispense = dispense;
	}

	public MedicationDispenseEntity getPrescription() {
		return dispense;
	}

	public void setPrescription(MedicationDispenseEntity dispense) {
		this.dispense = dispense;
	}

	public String getNoteText() {
		return noteText;
	}

	public void setNoteText(String noteText) {
		this.noteText = noteText;
	}

	public PatientEntity getNotePatient() {
		return notePatient;
	}

	public void setNotePatient(PatientEntity notePatient) {
		this.notePatient = notePatient;
	}

	public PractitionerEntity getNotePractitioner() {
		return notePractitioner;
	}

	public void setNotePractitioner(PractitionerEntity notePractitioner) {
		this.notePractitioner = notePractitioner;
	}

	public Date getNoteDate() {
		return noteDate;
	}

	public void setNoteDate(Date noteDate) {
		this.noteDate = noteDate;
	}
}
