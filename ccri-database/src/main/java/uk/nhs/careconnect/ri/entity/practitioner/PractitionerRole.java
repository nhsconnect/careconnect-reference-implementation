package uk.nhs.careconnect.ri.entity.practitioner;

import uk.nhs.careconnect.ri.entity.Terminology.ConceptEntity;
import uk.nhs.careconnect.ri.entity.organization.OrganisationEntity;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "PractitionerRole")
public class PractitionerRole {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="PRACTITIONER_ROLE_ID")
    private Long myId;

    @ManyToOne
    @JoinColumn(name = "PRACTITIONER_ID",foreignKey= @ForeignKey(name="FK_PRACTITIONER_PRACTITIONER_ROLE"))
    private PractitionerEntity practitionerEntity;

    @ManyToOne
    @JoinColumn(name="managingOrganisation")
    private OrganisationEntity managingOrganisation;

    @ManyToOne
    @JoinColumn(name="role")
    private ConceptEntity role;

    @ElementCollection
    @CollectionTable(name = "PractitionerSpecialty", joinColumns = @JoinColumn(name = "PRACTITIONER_ROLE_ID"))
    private Set<ConceptEntity> specialties = new HashSet<ConceptEntity>();

    public Set<ConceptEntity> getSpecialties() {
        return specialties;
    }
    public void setSpecialties(Set<ConceptEntity> specialties) {
        this.specialties = specialties;
    }

    public void setRole(ConceptEntity role) {
        this.role = role;
    }

    public ConceptEntity getRole() {
        return role;
    }

    public OrganisationEntity getManaginsOrganisation() {
        return managingOrganisation;
    }
    public void setManaginsOrganisation(OrganisationEntity managinsOrganisation) {
        this.managingOrganisation = managinsOrganisation;
    }

    public Long getId()
    {
        return this.myId;
    }

    public PractitionerEntity getPractitioner() {
        return this.practitionerEntity;
    }
    public void setPractitioner(PractitionerEntity practitionerEntity) {
        this.practitionerEntity = practitionerEntity;
    }

}
