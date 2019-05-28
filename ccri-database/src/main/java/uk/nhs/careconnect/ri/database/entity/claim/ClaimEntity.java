package uk.nhs.careconnect.ri.database.entity.claim;

import org.hl7.fhir.dstu3.model.Claim;
import uk.nhs.careconnect.ri.database.entity.BaseResource;
import uk.nhs.careconnect.ri.database.entity.organization.OrganisationEntity;
import uk.nhs.careconnect.ri.database.entity.patient.PatientEntity;
import uk.nhs.careconnect.ri.database.entity.practitioner.PractitionerEntity;

import javax.persistence.*;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "Claim",
        indexes = {

        })
public class ClaimEntity extends BaseResource {

    private static final int MAX_DESC_LENGTH = 4096;


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="CLAIM_ID")
    private Long id;

    @OneToMany(mappedBy="claim", targetEntity=ClaimIdentifier.class)
    private Set<ClaimIdentifier> identifiers = new HashSet<>();

    @Enumerated(EnumType.ORDINAL)
    @Column(name="status")
    private Claim.ClaimStatus status;


    @OneToOne(mappedBy = "claim", cascade = CascadeType.ALL)
    private ClaimType type;

    @OneToOne(mappedBy = "claim", cascade = CascadeType.ALL)
    private ClaimSubType subType;

    @Enumerated(EnumType.ORDINAL)
    @Column(name="use")
    private Claim.Use use;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn (name = "PATIENT_ID",foreignKey= @ForeignKey(name="FK_CLAIM_PATIENT_ID"))
    private PatientEntity patient;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "CREATED")
    private Date created;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "PERIOD_START")
    private Date periodStart;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "PERIOD_END")
    private Date periodEnd;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn (name = "ENTERED_PRACTITIONER",foreignKey= @ForeignKey(name="FK_ENTERED_PRACTITIONER_ID"))
    private PractitionerEntity enteredPractitioner;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn (name = "INSURER_ORGANISATION",foreignKey= @ForeignKey(name="FK_INSURER_ORGANISATION_ID"))
    private OrganisationEntity insurerOrganisation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn (name = "PROVIDER_PRACTITIONER",foreignKey= @ForeignKey(name="FK_PROVIDER_PRACTITIONER_ID"))
    private PractitionerEntity providerPractitioner;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn (name = "PROVIDER_ORGANISATION",foreignKey= @ForeignKey(name="FK_PROVIDER_ORGANISATION_ID"))
    private OrganisationEntity providerOrganisation;

    @OneToOne(mappedBy = "claim", cascade = CascadeType.ALL)
    private ClaimPriority priority;

    @OneToMany(mappedBy="claim", targetEntity=ClaimRelated.class)
    private Set<ClaimRelated> relatedClaims = new HashSet<>();


    @OneToMany(mappedBy="claim", targetEntity=ClaimSupportingInformation.class)
    private Set<ClaimSupportingInformation> supportingInfos = new HashSet<>();



    public Long getId() {
        return id;
    }



    public PatientEntity getPatient() {
        return patient;
    }

    public void setPatient(PatientEntity patient) {
        this.patient = patient;
    }

    public void setId(Long id) {
        this.id = id;
    }



    public Set<ClaimIdentifier> getIdentifiers() {
        if (identifiers == null) { identifiers = new HashSet<ClaimIdentifier>(); }
        return identifiers;
    }


    public ClaimEntity setIdentifiers(Set<ClaimIdentifier> identifiers) {
        this.identifiers = identifiers;
        return this;
    }


    public static int getMaxDescLength() {
        return MAX_DESC_LENGTH;
    }

    public Claim.ClaimStatus getStatus() {
        return status;
    }

    public void setStatus(Claim.ClaimStatus status) {
        this.status = status;
    }


    public Claim.Use getUse() {
        return use;
    }

    public void setUse(Claim.Use use) {
        this.use = use;
    }

    public ClaimType getType() {
        return type;
    }

    public void setType(ClaimType type) {
        this.type = type;
    }

    public ClaimSubType getSubType() {
        return subType;
    }

    public void setSubType(ClaimSubType subType) {
        this.subType = subType;
    }

    @Override
    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public Date getPeriodStart() {
        return periodStart;
    }

    public void setPeriodStart(Date periodStart) {
        this.periodStart = periodStart;
    }

    public Date getPeriodEnd() {
        return periodEnd;
    }

    public void setPeriodEnd(Date periodEnd) {
        this.periodEnd = periodEnd;
    }

    public PractitionerEntity getEnteredPractitioner() {
        return enteredPractitioner;
    }

    public void setEnteredPractitioner(PractitionerEntity enteredPractitioner) {
        this.enteredPractitioner = enteredPractitioner;
    }

    public OrganisationEntity getInsurerOrganisation() {
        return insurerOrganisation;
    }

    public void setInsurerOrganisation(OrganisationEntity insurerOrganisation) {
        this.insurerOrganisation = insurerOrganisation;
    }

    public PractitionerEntity getProviderPractitioner() {
        return providerPractitioner;
    }

    public void setProviderPractitioner(PractitionerEntity providerPractitioner) {
        this.providerPractitioner = providerPractitioner;
    }

    public OrganisationEntity getProviderOrganisation() {
        return providerOrganisation;
    }

    public void setProviderOrganisation(OrganisationEntity providerOrganisation) {
        this.providerOrganisation = providerOrganisation;
    }

    public ClaimPriority getPriority() {
        return priority;
    }

    public void setPriority(ClaimPriority priority) {
        this.priority = priority;
    }

    public Set<ClaimRelated> getRelatedClaims() {
        return relatedClaims;
    }

    public void setRelatedClaims(Set<ClaimRelated> relatedClaims) {
        this.relatedClaims = relatedClaims;
    }

    public Set<ClaimSupportingInformation> getSupportingInfos() {
        return supportingInfos;
    }

    public void setSupportingInfos(Set<ClaimSupportingInformation> supportingInfos) {
        this.supportingInfos = supportingInfos;
    }
}
