package uk.nhs.careconnect.ri.database.entity.healthcareService;


import uk.nhs.careconnect.ri.database.entity.BaseResource;
import uk.nhs.careconnect.ri.database.entity.Terminology.ConceptEntity;


import javax.persistence.*;

@Entity
@Table(name = "HealthcareServiceSpecialty")
public class HealthcareServiceSpecialty extends BaseResource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="SERVICE_SPECIALTY_ID")
    private Long myId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SERVICE_ID",foreignKey= @ForeignKey(name="FK_SERVICE_SPECIALTY_SERVICE_ROLE_ID"))
    private HealthcareServiceEntity service;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="SPECIALTY_CONCEPT_ID",foreignKey= @ForeignKey(name="FK_SERVICE_SPECIALTY_SPECIALTY_CONCEPT_ID"))

    private ConceptEntity specialty;

    public Long getId()
    {
        return this.myId;
    }

    public ConceptEntity getSpecialty() {
        return specialty;
    }

    public HealthcareServiceEntity getHealthcareService() {
        return service;
    }

    public HealthcareServiceSpecialty setHealthcareService(HealthcareServiceEntity service) {
        this.service = service;
        return this;
    }

    public HealthcareServiceEntity setSpecialty(ConceptEntity specialty) {
        this.specialty = specialty;
        return this.getHealthcareService();
    }
}
