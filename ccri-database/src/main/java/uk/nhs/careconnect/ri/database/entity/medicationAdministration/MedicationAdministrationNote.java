package uk.nhs.careconnect.ri.database.entity.medicationAdministration;


import uk.nhs.careconnect.ri.database.entity.medicationAdministration.MedicationAdministrationEntity;
import uk.nhs.careconnect.ri.database.entity.patient.PatientEntity;
import uk.nhs.careconnect.ri.database.entity.practitioner.PractitionerEntity;

import javax.persistence.*;
import java.util.Date;


@Entity
@Table(name="MedicationAdministrationNote", uniqueConstraints= @UniqueConstraint(name="PK_ADMINISTRATION_NOTE", columnNames={"ADMINISTRATION_NOTE_ID"})
		)
public class MedicationAdministrationNote {

	public MedicationAdministrationNote() {

	}

	public MedicationAdministrationNote(MedicationAdministrationEntity administration) {
		this.administration = administration;
	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name= "ADMINISTRATION_NOTE_ID")
	private Long noteId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn (name = "ADMINISTRATION_ID",foreignKey= @ForeignKey(name="FK_ADMINISTRATION_ADMINISTRATION_NOTE"))
	private MedicationAdministrationEntity administration;

	@Column(name= "noteText")
	private String noteText;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn (name = "PATIENT_ID",foreignKey= @ForeignKey(name="FK_PATIENT_ADMINISTRATION_NOTE"))
	private PatientEntity notePatient;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn (name = "PRACTITIONER_ID",foreignKey= @ForeignKey(name="FK_PRACTITIONER_ADMINISTRATION_NOTE"))
	private PractitionerEntity notePractitioner;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "noteDate")
	private Date noteDate;

    public Long getNoteId() { return noteId; }
	public void setNoteId(Long noteId) { this.noteId = noteId; }

	public MedicationAdministrationEntity getMedicationAdministration() {
	        return this.administration;
	}

	public void setMedicationAdministration(MedicationAdministrationEntity administration) {
	        this.administration = administration;
	}

	public MedicationAdministrationEntity getPrescription() {
		return administration;
	}

	public void setPrescription(MedicationAdministrationEntity administration) {
		this.administration = administration;
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
