package uk.nhs.careconnect.ri.database.entity.medicationRequest;


import uk.nhs.careconnect.ri.database.entity.patient.PatientEntity;
import uk.nhs.careconnect.ri.database.entity.practitioner.PractitionerEntity;

import javax.persistence.*;
import java.util.Date;


@Entity
@Table(name="MedicationRequestNote", uniqueConstraints= @UniqueConstraint(name="PK_PRESCRIPTION_NOTE", columnNames={"PRESCRIPTION_NOTE_ID"})
		)
public class MedicationRequestNote {

	public MedicationRequestNote() {

	}

	public MedicationRequestNote(MedicationRequestEntity prescription) {
		this.prescription = prescription;
	}
	private static final int MAX_DESC_LENGTH = 1024;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name= "PRESCRIPTION_NOTE_ID")
	private Long noteId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn (name = "PRESCRIPTION_ID",foreignKey= @ForeignKey(name="FK_PRESCRIPTION_PRESCRIPTION_NOTE"))
	private MedicationRequestEntity prescription;

	@Column(name= "noteText", length = MAX_DESC_LENGTH)
	private String noteText;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn (name = "PATIENT_ID",foreignKey= @ForeignKey(name="FK_PATIENT_PRESCRIPTION_NOTE"))
	private PatientEntity notePatient;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn (name = "PRACTITIONER_ID",foreignKey= @ForeignKey(name="FK_PRACTITIONER_PRESCRIPTION_NOTE"))
	private PractitionerEntity notePractitioner;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "noteDate")
	private Date noteDate;

    public Long getNoteId() { return noteId; }
	public void setNoteId(Long noteId) { this.noteId = noteId; }

	public MedicationRequestEntity getMedicationRequest() {
	        return this.prescription;
	}

	public void setMedicationRequest(MedicationRequestEntity prescription) {
	        this.prescription = prescription;
	}

	public MedicationRequestEntity getPrescription() {
		return prescription;
	}

	public void setPrescription(MedicationRequestEntity prescription) {
		this.prescription = prescription;
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
