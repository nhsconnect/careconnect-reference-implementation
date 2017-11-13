package uk.nhs.careconnect.ri.entity.episode;

import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.hl7.fhir.dstu3.model.EpisodeOfCare;
import uk.nhs.careconnect.ri.entity.BaseResource;
import uk.nhs.careconnect.ri.entity.Terminology.ConceptEntity;
import uk.nhs.careconnect.ri.entity.organization.OrganisationEntity;
import uk.nhs.careconnect.ri.entity.patient.PatientEntity;
import uk.nhs.careconnect.ri.entity.practitioner.PractitionerEntity;

import javax.persistence.*;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "EpisodeOfCare")
public class EpisodeOfCareEntity extends BaseResource {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="EPISODE_ID")
    private Long id;

    @Enumerated(EnumType.ORDINAL)
    EpisodeOfCare.EpisodeOfCareStatus status;


    @ManyToOne
    @JoinColumn (name = "PATIENT_ID",foreignKey= @ForeignKey(name="FK_PATIENT_EPISODE"))
    @LazyCollection(LazyCollectionOption.TRUE)
    private PatientEntity patient;

    @ManyToOne
    @JoinColumn(name="ORGANISATION_ID",foreignKey= @ForeignKey(name="FK_EPISODE_ORGANISATION"))
    @LazyCollection(LazyCollectionOption.TRUE)
    private OrganisationEntity managingOrganisation;

    @ManyToOne
    @JoinColumn(name="PRACTITIONER_ID",foreignKey= @ForeignKey(name="FK_EPISODE_PRACTITIONER"))
    @LazyCollection(LazyCollectionOption.TRUE)
    private PractitionerEntity careManager;

    @ManyToOne
    @JoinColumn (name = "TYPE_CONCEPT_ID",foreignKey= @ForeignKey(name="FK_EPISODE_TYPE_CONCEPT"))
    private ConceptEntity type;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "periodStartDate")
    private Date periodStartDate;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "periodEndDate")
    private Date periodEndDate;

    @OneToMany(mappedBy="episode", targetEntity = EpisodeOfCareIdentifier.class)
    @LazyCollection(LazyCollectionOption.TRUE)
    Set<EpisodeOfCareIdentifier> identifiers = new HashSet<>();

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

    public EpisodeOfCareEntity setStatus(EpisodeOfCare.EpisodeOfCareStatus status) {
        this.status = status;
        return this;
    }

    public EpisodeOfCareEntity setIdentifiers(Set<EpisodeOfCareIdentifier> identifiers) {
        this.identifiers = identifiers;
        return this;
    }

    public EpisodeOfCare.EpisodeOfCareStatus getStatus() {
        return status;
    }

    public Set<EpisodeOfCareIdentifier> getIdentifiers() {
        if (identifiers == null) {
            identifiers = new HashSet<>();
        }
        return identifiers;
    }
}
