package uk.nhs.careconnect.ri.entity.episode;

import uk.nhs.careconnect.ri.entity.BaseResource;
import uk.nhs.careconnect.ri.entity.Terminology.ConceptEntity;
import uk.nhs.careconnect.ri.entity.organization.OrganisationEntity;
import uk.nhs.careconnect.ri.entity.patient.PatientEntity;
import uk.nhs.careconnect.ri.entity.practitioner.PractitionerEntity;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "EpisodeOfCare")
public class EpisodeOfCareEntity extends BaseResource {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="EPISODE_ID")
    private Long id;

    @ManyToOne
    @JoinColumn (name = "PATIENT_ID",foreignKey= @ForeignKey(name="FK_PATIENT_EPISODE"))
    private PatientEntity patient;

    @ManyToOne
    @JoinColumn(name="ORGANISATION_ID")
    private OrganisationEntity managingOrganisation;

    @ManyToOne
    @JoinColumn(name="PRACTITIONER_ID")
    private PractitionerEntity careManager;

    @ManyToOne
    @JoinColumn (name = "TYPE_CONCEPT_ID",foreignKey= @ForeignKey(name="FK_EPISODE_TYPE"))
    private ConceptEntity type;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "periodStartDate")
    private Date periodStartDate;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "periodEndDate")
    private Date periodEndDate;

    public Long getId() {
        return id;
    }

    public EpisodeOfCareEntity setPatient(PatientEntity patient) {
        this.patient = patient;
        return this;
    }

    public PatientEntity getPatient() {
        return patient;
    }

    public OrganisationEntity getManagingOrganisation() {
        return managingOrganisation;
    }

    public PractitionerEntity getCareManager() {
        return careManager;
    }

    public EpisodeOfCareEntity setCareManager(PractitionerEntity careManager) {
        this.careManager = careManager;
        return this;
    }

    public EpisodeOfCareEntity setManagingOrganisation(OrganisationEntity managingOrganisation) {
        this.managingOrganisation = managingOrganisation;
        return this;
    }

    public ConceptEntity getType() {
        return type;
    }

    public EpisodeOfCareEntity setType(ConceptEntity type) {
        this.type = type;
        return this;
    }

    public EpisodeOfCareEntity setPeriodStartDate(Date periodStartDate) {
        this.periodStartDate = periodStartDate;
        return this;
    }

    public EpisodeOfCareEntity setPeriodEndDate(Date periodEndDate) {
        this.periodEndDate = periodEndDate;
        return this;
    }

    public Date getPeriodStartDate() {
        return periodStartDate;
    }

    public Date getPeriodEndDate() {
        return periodEndDate;
    }
}
