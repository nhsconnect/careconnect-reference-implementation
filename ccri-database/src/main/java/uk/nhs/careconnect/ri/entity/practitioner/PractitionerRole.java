package uk.nhs.careconnect.ri.entity.practitioner;

import uk.nhs.careconnect.ri.entity.BaseResource;
import uk.nhs.careconnect.ri.entity.Terminology.ConceptEntity;
import uk.nhs.careconnect.ri.entity.organization.OrganisationEntity;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "PractitionerRole")
public class PractitionerRole extends BaseResource {

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

    @OneToMany(mappedBy="practitionerRole", targetEntity=PractitionerSpecialty.class)
    private List<PractitionerSpecialty> specialties;

    @OneToMany(mappedBy="practitionerRole", targetEntity=PractitionerRoleIdentifier.class)
    private List<PractitionerRoleIdentifier> identifiers;

    public List<PractitionerSpecialty> getSpecialties() {
        if (specialties ==null) {
            specialties = new ArrayList<>();
        }
        return specialties;
    }

    public void setSpecialties(List<PractitionerSpecialty> specialties) {
        this.specialties = specialties;
    }

    public PractitionerRole setRole(ConceptEntity role) {
        this.role = role;
        return this;
    }

    public ConceptEntity getRole() {
        return role;
    }

    public OrganisationEntity getOrganisation() {
        return managingOrganisation;
    }
    public PractitionerRole setOrganisation(OrganisationEntity managinsOrganisation) {
        this.managingOrganisation = managinsOrganisation;
        return this;
    }

    public Long getId()
    {
        return this.myId;
    }

    public PractitionerEntity getPractitioner() {
        return this.practitionerEntity;
    }
    public PractitionerRole setPractitioner(PractitionerEntity practitionerEntity) {
        this.practitionerEntity = practitionerEntity;
        return this;
    }

    public PractitionerRole setIdentifiers(List<PractitionerRoleIdentifier> identifiers) {
        this.identifiers = identifiers;
        return this;
    }

    public List<PractitionerRoleIdentifier> getIdentifiers() {
        if (identifiers==null) {
            identifiers= new ArrayList<>();
        }
        return identifiers;
    }
}
