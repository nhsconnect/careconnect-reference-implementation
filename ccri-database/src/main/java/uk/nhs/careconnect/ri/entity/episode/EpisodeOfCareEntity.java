package uk.nhs.careconnect.ri.entity.episode;

import uk.nhs.careconnect.ri.entity.BaseResource;
import uk.nhs.careconnect.ri.entity.organization.OrganisationEntity;
import uk.nhs.careconnect.ri.entity.patient.PatientEntity;
import uk.nhs.careconnect.ri.entity.practitioner.PractitionerEntity;

import javax.persistence.*;

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
}
