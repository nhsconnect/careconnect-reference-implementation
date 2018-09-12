package uk.nhs.careconnect.ri.database.entity.practitioner;


import uk.nhs.careconnect.ri.database.entity.BaseResource;
import uk.nhs.careconnect.ri.database.entity.Terminology.ConceptEntity;

import javax.persistence.*;

@Entity
@Table(name = "PractitionerSpecialty")
public class PractitionerSpecialty extends BaseResource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="PRACTITIONER_SPECIALTY_ID")
    private Long myId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PRACTITIONER_ROLE_ID",foreignKey= @ForeignKey(name="FK_PRACTITIONER_SPECIALTY_PRACTITIONER_ROLE_ID"))
    private PractitionerRole practitionerRole;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="specialty",foreignKey= @ForeignKey(name="FK_PRACTITIONER_SPECIALTY_SPECIALTY_CONCEPT_ID"))

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
