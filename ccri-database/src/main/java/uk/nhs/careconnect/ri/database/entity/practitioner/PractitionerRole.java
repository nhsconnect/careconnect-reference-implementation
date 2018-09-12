package uk.nhs.careconnect.ri.database.entity.practitioner;

import uk.nhs.careconnect.ri.database.entity.BaseResource;
import uk.nhs.careconnect.ri.database.entity.Terminology.ConceptEntity;
import uk.nhs.careconnect.ri.database.entity.organization.OrganisationEntity;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "PractitionerRole",indexes =
        {


                @Index(name = "IDX_PRACTITIONER_ROLE_PRACTITIONER_ID", columnList="PRACTITIONER_ID"),
                @Index(name = "IDX_PRACTITIONER_ROLE_ORGANISATION_ID", columnList="MANAGING_ORGANISATION_ID")
        })
public class PractitionerRole extends BaseResource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="PRACTITIONER_ROLE_ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PRACTITIONER_ID",foreignKey= @ForeignKey(name="FK_PRACTITIONER_ROLE_PRACTITIONER_ID"))
    private PractitionerEntity practitionerEntity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="MANAGING_ORGANISATION_ID",foreignKey= @ForeignKey(name="FK_PRACTITIONER_ROLE_ORGANISATION_ID"))

    private OrganisationEntity managingOrganisation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="role",foreignKey= @ForeignKey(name="FK_PRACTITIONER_ROLE_ROLE_CONCEPT_ID"))

    private ConceptEntity role;

    @OneToMany(mappedBy="practitionerRole", targetEntity=PractitionerSpecialty.class)

    private List<PractitionerSpecialty> specialties;

    @OneToMany(mappedBy="practitionerRole", targetEntity=PractitionerRoleIdentifier.class)

    private List<PractitionerRoleIdentifier> identifiers;

    @OneToMany(mappedBy="practitionerRole", targetEntity=PractitionerLocation.class)

    private List<PractitionerLocation> locations;

    @OneToMany(mappedBy="practitionerRole", targetEntity=PractitionerHealthcareService.class)

    private List<PractitionerHealthcareService> services;

    public PractitionerEntity getPractitionerEntity() {
        return practitionerEntity;
    }

    public PractitionerRole setPractitionerEntity(PractitionerEntity practitionerEntity) {
        this.practitionerEntity = practitionerEntity;
        return this;
    }

    public OrganisationEntity getManagingOrganisation() {
        return managingOrganisation;
    }

    public PractitionerRole setManagingOrganisation(OrganisationEntity managingOrganisation) {
        this.managingOrganisation = managingOrganisation;
        return this;
    }

    public List<PractitionerLocation> getLocations() {
        return locations;
    }

    public PractitionerRole setLocations(List<PractitionerLocation> locations) {
        this.locations = locations;
        return this;
    }

    public List<PractitionerHealthcareService> getServices() {
        return services;
    }

    public PractitionerRole setServices(List<PractitionerHealthcareService> services) {
        this.services = services;
        return this;
    }

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
        return this.id;
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
