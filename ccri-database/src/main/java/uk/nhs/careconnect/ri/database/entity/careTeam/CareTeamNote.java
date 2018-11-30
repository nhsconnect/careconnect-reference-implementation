package uk.nhs.careconnect.ri.database.entity.careTeam;


import uk.nhs.careconnect.ri.database.entity.patient.PatientEntity;
import uk.nhs.careconnect.ri.database.entity.practitioner.PractitionerEntity;

import javax.persistence.*;
import java.util.Date;


@Entity
@Table(name="CareTeamNote", uniqueConstraints= @UniqueConstraint(name="PK_CARE_TEAM_NOTE", columnNames={"CARE_TEAM_NOTE_ID"})
		)
public class CareTeamNote {

	public CareTeamNote() {

	}

	private static final int MAX_DESC_LENGTH = 1024;

	public CareTeamNote(CareTeamEntity team) {
		this.team = team;
	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name= "CARE_TEAM_NOTE_ID")
	private Long noteId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn (name = "CARE_TEAM_ID",foreignKey= @ForeignKey(name="FK_CARE_TEAM_CARE_TEAM_NOTE"))
	private CareTeamEntity team;

	@Column(name= "noteText", length = MAX_DESC_LENGTH)
	private String noteText;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn (name = "PATIENT_ID",foreignKey= @ForeignKey(name="FK_PATIENT_CARE_TEAM_NOTE"))
	private PatientEntity notePatient;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn (name = "PRACTITIONER_ID",foreignKey= @ForeignKey(name="FK_PRACTITIONER_CARE_TEAM_NOTE"))
	private PractitionerEntity notePractitioner;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "noteDate")
	private Date noteDate;

    public Long getNoteId() { return noteId; }
	public void setNoteId(Long noteId) { this.noteId = noteId; }

	public CareTeamEntity getCareTeam() {
	        return this.team;
	}

	public void setCareTeam(CareTeamEntity team) {
	        this.team = team;
	}

	public CareTeamEntity getPrescription() {
		return team;
	}

	public void setPrescription(CareTeamEntity team) {
		this.team = team;
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
