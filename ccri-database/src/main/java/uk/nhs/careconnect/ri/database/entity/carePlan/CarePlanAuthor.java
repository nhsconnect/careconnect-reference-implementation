package uk.nhs.careconnect.ri.database.entity.carePlan;

import uk.nhs.careconnect.ri.database.entity.organization.OrganisationEntity;
import uk.nhs.careconnect.ri.database.entity.patient.PatientEntity;
import uk.nhs.careconnect.ri.database.entity.practitioner.PractitionerEntity;

import javax.persistence.*;

@Entity
@Table(name="CarePlanAuthor", uniqueConstraints= @UniqueConstraint(name="PK_CARE_PLAN_AUTHOR", columnNames={"CARE_PLAN_AUTHOR_ID"})
        ,indexes = {}
)
public class CarePlanAuthor {
    public enum author {
        Patient, Practitioner, Organisation, Device
    }
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name= "CARE_PLAN_AUTHOR_ID")
    private Long Id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn (name = "CARE_PLAN_ID",foreignKey= @ForeignKey(name="FK_CARE_PLAN_AUTHOR_CARE_PLAN_ID"))
    private CarePlanEntity carePlan;

    @Enumerated(EnumType.ORDINAL)
    private author authorType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="authorPractitioner",foreignKey= @ForeignKey(name="FK_CARE_PLAN_AUTHOR_PRACTITIONER_ID"))

    private PractitionerEntity authorPractitioner;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="authorPatient",foreignKey= @ForeignKey(name="FK_CARE_PLAN_AUTHOR_PATIENT_ID"))

    private PatientEntity authorPatient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="authorOrganisation",foreignKey= @ForeignKey(name="FK_CARE_PLAN_AUTHOR_ORGANISATION_ID"))

    private OrganisationEntity authorOrganisation;

    public void setId(Long id) {
        Id = id;
    }

    public Long getId() {
        return Id;
    }

    public CarePlanEntity getCarePlan() {
        return carePlan;
    }

    public void setCarePlan(CarePlanEntity carePlan) {
        this.carePlan = carePlan;
    }

    public OrganisationEntity getOrganisation() {
        return authorOrganisation;
    }

    public PatientEntity getPatient() {
        return authorPatient;
    }

    public PractitionerEntity getPractitioner() {
        return authorPractitioner;
    }

    public CarePlanAuthor setOrganisation(OrganisationEntity authorOrganisation) {
        this.authorOrganisation = authorOrganisation;
        return this;
    }

    public CarePlanAuthor setPatient(PatientEntity performerPatient) {
        this.authorPatient = performerPatient;
        return this;
    }

    public CarePlanAuthor setPractitioner(PractitionerEntity performerPractitioner) {
        this.authorPractitioner = performerPractitioner;
        return this;
    }

    public author getAuthorType() {
        return authorType;
    }

    public void setAuthorType(author performerType) {
        this.authorType = performerType;
    }
}
