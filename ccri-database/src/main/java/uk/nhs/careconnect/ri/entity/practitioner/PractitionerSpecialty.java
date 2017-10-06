package uk.nhs.careconnect.ri.entity.practitioner;


import uk.nhs.careconnect.ri.entity.BaseResource;
import uk.nhs.careconnect.ri.entity.Terminology.ConceptEntity;

import javax.persistence.*;

@Entity
@Table(name = "PractitionerSpecialty")
public class PractitionerSpecialty extends BaseResource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="PRACTITIONER_SPECIALTY_ID")
    private Long myId;

    @ManyToOne
    @JoinColumn(name = "PRACTITIONER_ROLE_ID",foreignKey= @ForeignKey(name="FK_PRACTITIONER_ROLE_PRACTITIONER_SPECIALTY"))
    private PractitionerRole practitionerRole;

    @ManyToOne
    @JoinColumn(name="specialty")
    private ConceptEntity specialty;

    public Long getId()
    {
        return this.myId;
    }

    public ConceptEntity getSpecialty() {
        return specialty;
    }

    public PractitionerRole getPractitionerRole() {
        return practitionerRole;
    }

    public PractitionerSpecialty setPractitionerRole(PractitionerRole practitionerRole) {
        this.practitionerRole = practitionerRole;
        return this;
    }

    public PractitionerSpecialty setSpecialty(ConceptEntity specialty) {
        this.specialty = specialty;
        return this;
    }
}
