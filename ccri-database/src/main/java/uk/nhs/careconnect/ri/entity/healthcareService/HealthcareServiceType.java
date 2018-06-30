package uk.nhs.careconnect.ri.entity.healthcareService;


import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import uk.nhs.careconnect.ri.entity.BaseResource;
import uk.nhs.careconnect.ri.entity.Terminology.ConceptEntity;

import javax.persistence.*;

@Entity
@Table(name = "HealthcareServiceType")
public class HealthcareServiceType extends BaseResource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="SERVICE_TYPE_ID")
    private Long myId;

    @ManyToOne
    @JoinColumn(name = "SERVICE_ID",foreignKey= @ForeignKey(name="FK_SERVICE_TYPE_SERVICE_ROLE_ID"))
    private HealthcareServiceEntity service;

    @ManyToOne
    @JoinColumn(name="TYPE_CONCEPT_ID",foreignKey= @ForeignKey(name="FK_SERVICE_TYPE_TYPE_CONCEPT_ID"))
    @LazyCollection(LazyCollectionOption.TRUE)
    private ConceptEntity type_;

    public Long getId()
    {
        return this.myId;
    }


    public HealthcareServiceEntity getHealthcareService() {
        return service;
    }

    public HealthcareServiceType setHealthcareService(HealthcareServiceEntity service) {
        this.service = service;
        return this;
    }

    public HealthcareServiceEntity getService() {
        return service;
    }

    public void setService(HealthcareServiceEntity service) {
        this.service = service;
    }

    public ConceptEntity getType_() {
        return type_;
    }

    public void setType_(ConceptEntity type_) {
        this.type_ = type_;
    }
}
