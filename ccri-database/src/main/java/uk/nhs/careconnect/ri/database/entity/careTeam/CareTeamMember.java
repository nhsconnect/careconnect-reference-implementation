package uk.nhs.careconnect.ri.database.entity.careTeam;

import uk.nhs.careconnect.ri.database.entity.Terminology.ConceptEntity;
import uk.nhs.careconnect.ri.database.entity.organization.OrganisationEntity;
import uk.nhs.careconnect.ri.database.entity.practitioner.PractitionerEntity;
import uk.nhs.careconnect.ri.database.entity.patient.PatientEntity;
import uk.nhs.careconnect.ri.database.entity.relatedPerson.RelatedPersonEntity;
import javax.persistence.*;

@Entity
@Table(name="CareTeamMember", uniqueConstraints= @UniqueConstraint(name="PK_CARE_TEAM_MEMBER", columnNames={"CARE_TEAM_MEMBER_ID"})
        ,indexes = { @Index(name="IDX_CARE_TEAM_MEMBER", columnList = "CARE_TEAM_MEMBER_ID")}
)
public class CareTeamMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name= "CARE_TEAM_MEMBER_ID")
    private Long Id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn (name = "CARE_TEAM_ID",foreignKey= @ForeignKey(name="FK_PROCEDURE_CARE_TEAM_MEMBER"))
    private CareTeamEntity team;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="ROLE_CONCEPT_ID")
    private ConceptEntity role;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="MEMBER_PRACTITIONER",foreignKey= @ForeignKey(name="FK_CARE_TEAM_MEMBER_PRACTITIONER"))
    private PractitionerEntity memberPractitioner;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="MEMBER_ORGNANISATION",foreignKey= @ForeignKey(name="FK_CARE_TEAM_MEMBER_ORGNANISATION"))
    private OrganisationEntity memberOrganisation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="MEMBER_PATIENT",foreignKey= @ForeignKey(name="FK_CARE_TEAM_MEMBER_PATIENT"))
    private PatientEntity memberPatient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="MEMBER_PERSON",foreignKey= @ForeignKey(name="FK_CARE_TEAM_MEMBER_PERSON"))
    private RelatedPersonEntity memberPerson;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="ON_BEHALF_OF_ORGANISATION",foreignKey= @ForeignKey(name="FK_CARE_TEAM_ONBEHALF_ORGANISATION"))
    private OrganisationEntity onBehalfOrganisation;


    public void setId(Long id) {
        Id = id;
    }

    public Long getId() {
        return Id;
    }

    public ConceptEntity getRole() {
        return role;
    }

    public OrganisationEntity getOnBehalfOrganisation() {
        return onBehalfOrganisation;
    }

    public CareTeamEntity getCareTeam() {
        return team;
    }

    public CareTeamMember setOnBehalfOrganisation(OrganisationEntity onBehalfOrganisation) {
        this.onBehalfOrganisation = onBehalfOrganisation;
        return this;
    }

    public CareTeamMember setCareTeam(CareTeamEntity team) {
        this.team = team;
        return this;
    }

    public CareTeamMember setRole(ConceptEntity role) {
        this.role = role;
        return this;
    }

    public CareTeamEntity getTeam() {
        return team;
    }

    public void setTeam(CareTeamEntity team) {
        this.team = team;
    }

    public PractitionerEntity getMemberPractitioner() {
        return memberPractitioner;
    }

    public void setMemberPractitioner(PractitionerEntity memberPractitioner) {
        this.memberPractitioner = memberPractitioner;
    }

    public OrganisationEntity getMemberOrganisation() {
        return memberOrganisation;
    }

    public void setMemberOrganisation(OrganisationEntity memberOrganisation) {
        this.memberOrganisation = memberOrganisation;
    }

    public PatientEntity getMemberPatient() {
        return memberPatient;
    }

    public void setMemberPatient(PatientEntity memberPatient) {
        this.memberPatient = memberPatient;
    }

    public RelatedPersonEntity getMemberPerson() {
        return memberPerson;
    }

    public void setMemberPerson(RelatedPersonEntity memberPerson) {
        this.memberPerson = memberPerson;
    }
}
