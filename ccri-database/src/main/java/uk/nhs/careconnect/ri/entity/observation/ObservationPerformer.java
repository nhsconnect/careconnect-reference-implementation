package uk.nhs.careconnect.ri.entity.observation;

import uk.nhs.careconnect.ri.entity.organization.OrganisationEntity;
import uk.nhs.careconnect.ri.entity.patient.PatientEntity;
import uk.nhs.careconnect.ri.entity.practitioner.PractitionerEntity;

import javax.persistence.*;

@Entity
@Table(name="ObservationPerformer", uniqueConstraints= @UniqueConstraint(name="PK_OBSERVATION_PERFORMER", columnNames={"OBSERVATION_PERFORMER_ID"})
        ,indexes = {}
)
public class ObservationPerformer {
    public enum performer {
        Patient, Practitioner, Organisation
    }
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name= "OBSERVATION_PERFORMER_ID")
    private Long Id;

    @ManyToOne
    @JoinColumn (name = "OBSERVATION_ID",foreignKey= @ForeignKey(name="FK_OBSERVATION_OBSERVATION_PERFORMER"))
    private ObservationEntity observation;

    @Enumerated(EnumType.ORDINAL)
    private performer performerType;

    @ManyToOne
    @JoinColumn(name="performerPractitioner")
    private PractitionerEntity performerPractitioner;

    @ManyToOne
    @JoinColumn(name="performerPatient")
    private PatientEntity performerPatient;

    @ManyToOne
    @JoinColumn(name="performerOrganisation")
    private OrganisationEntity performerOrganisation;

    public void setId(Long id) {
        Id = id;
    }

    public Long getId() {
        return Id;
    }

    public ObservationEntity getObservation() {
        return observation;
    }

    public void setObservation(ObservationEntity observation) {
        this.observation = observation;
    }

    public OrganisationEntity getPerformerOrganisation() {
        return performerOrganisation;
    }

    public PatientEntity getPerformerPatient() {
        return performerPatient;
    }

    public PractitionerEntity getPerformerPractitioner() {
        return performerPractitioner;
    }

    public ObservationPerformer setPerformerOrganisation(OrganisationEntity performerOrganisation) {
        this.performerOrganisation = performerOrganisation;
        return this;
    }

    public ObservationPerformer setPerformerPatient(PatientEntity performerPatient) {
        this.performerPatient = performerPatient;
        return this;
    }

    public ObservationPerformer setPerformerPractitioner(PractitionerEntity performerPractitioner) {
        this.performerPractitioner = performerPractitioner;
        return this;
    }

    public performer getPerformerType() {
        return performerType;
    }

    public void setPerformerType(performer performerType) {
        this.performerType = performerType;
    }
}
