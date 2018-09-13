package uk.nhs.careconnect.ri.database.entity.careTeam;

import org.hl7.fhir.dstu3.model.CareTeam;
import uk.nhs.careconnect.ri.database.entity.Terminology.ConceptEntity;
import uk.nhs.careconnect.ri.database.entity.encounter.EncounterEntity;
import uk.nhs.careconnect.ri.database.entity.organization.OrganisationEntity;
import uk.nhs.careconnect.ri.database.entity.BaseResource;
import uk.nhs.careconnect.ri.database.entity.patient.PatientEntity;
import javax.persistence.*;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "CareTeam",
        indexes = {

        })
public class CareTeamEntity extends BaseResource {

    private static final int MAX_DESC_LENGTH = 4096;

    public enum CareTeamType  { component, valueQuantity }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="CARE_TEAM_ID")
    private Long id;

    @OneToMany(mappedBy="team", targetEntity=CareTeamIdentifier.class)
    private Set<CareTeamIdentifier> identifiers = new HashSet<>();

    @Enumerated(EnumType.ORDINAL)
    @Column(name="status")
    private CareTeam.CareTeamStatus status;

    @Column(name="NAME")
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn (name = "PATIENT_ID",foreignKey= @ForeignKey(name="FK_CARE_TEAM_PATIENT_ID"))
    private PatientEntity patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="CONTEXT_ENCOUNTER_ID",nullable = true,foreignKey= @ForeignKey(name="FK_CARE_TEAM_CONTEXT_ENCOUNTER_ID"))
    private EncounterEntity contextEncounter;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "PERIOD_START_DATETIME")
    private Date periodStartDateTime;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "PERIOD_END_DATETIME")
    private Date periodEndDateTime;

    @OneToMany(mappedBy="team", targetEntity=CareTeamMember.class)
    private Set<CareTeamMember> members = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="REASON_CONCEPT_ID",nullable = true,foreignKey= @ForeignKey(name="FK_CARE_TEAM_REASON_CONCEPT_ID"))
    private ConceptEntity reasonCode;

    @OneToMany(mappedBy="team", targetEntity=CareTeamReason.class)
    private Set<CareTeamReason> reasons = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="MANAGING_ORGANISATION",foreignKey= @ForeignKey(name="FK_CARE_TEAM_MANAGING_ORGANISATION"))
    private OrganisationEntity managingOrganisation;

    @Column(name="NOTE",length = MAX_DESC_LENGTH,nullable = true)
    private String note;


    public Long getId() {
        return id;
    }



    public PatientEntity getPatient() {
        return patient;
    }

    public void setPatient(PatientEntity patient) {
        this.patient = patient;
    }

    public void setId(Long id) {
        this.id = id;
    }



    public Set<CareTeamIdentifier> getIdentifiers() {
        if (identifiers == null) { identifiers = new HashSet<CareTeamIdentifier>(); }
        return identifiers;
    }

    public CareTeam.CareTeamStatus getStatus() {
        return status;
    }

    public CareTeamEntity setIdentifiers(Set<CareTeamIdentifier> identifiers) {
        this.identifiers = identifiers;
        return this;
    }


    public CareTeamEntity setStatus(CareTeam.CareTeamStatus status) {
        this.status = status;
        return this;
    }

    public static int getMaxDescLength() {
        return MAX_DESC_LENGTH;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public EncounterEntity getContextEncounter() {
        return contextEncounter;
    }

    public void setContextEncounter(EncounterEntity contextEncounter) {
        this.contextEncounter = contextEncounter;
    }

    public Date getPeriodStartDateTime() {
        return periodStartDateTime;
    }

    public void setPeriodStartDateTime(Date periodStartDateTime) {
        this.periodStartDateTime = periodStartDateTime;
    }

    public Date getPeriodEndDateTime() {
        return periodEndDateTime;
    }

    public void setPeriodEndDateTime(Date periodEndDateTime) {
        this.periodEndDateTime = periodEndDateTime;
    }

    public Set<CareTeamMember> getMembers() {
        return members;
    }

    public void setMembers(Set<CareTeamMember> members) {
        this.members = members;
    }

    public ConceptEntity getReasonCode() {
        return reasonCode;
    }

    public void setReasonCode(ConceptEntity reasonCode) {
        this.reasonCode = reasonCode;
    }

    public Set<CareTeamReason> getReasons() {
        return reasons;
    }

    public void setReasons(Set<CareTeamReason> reasons) {
        this.reasons = reasons;
    }

    public OrganisationEntity getManagingOrganisation() {
        return managingOrganisation;
    }

    public void setManagingOrganisation(OrganisationEntity managingOrganisation) {
        this.managingOrganisation = managingOrganisation;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
