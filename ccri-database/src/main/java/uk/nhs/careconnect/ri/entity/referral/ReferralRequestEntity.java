package uk.nhs.careconnect.ri.entity.referral;

import uk.nhs.careconnect.ri.entity.BaseResource;
import uk.nhs.careconnect.ri.entity.Terminology.ConceptEntity;
import uk.nhs.careconnect.ri.entity.organization.OrganisationEntity;
import uk.nhs.careconnect.ri.entity.patient.PatientEntity;
import uk.nhs.careconnect.ri.entity.practitioner.PractitionerEntity;

import javax.persistence.*;

@Entity
@Table(name = "ReferralRequest")
public class ReferralRequestEntity extends BaseResource {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="REFERRAL_ID")
    private Long id;

    @ManyToOne
    @JoinColumn (name = "PATIENT_ID",foreignKey= @ForeignKey(name="FK_PATIENT_REFERRAL"))
    private PatientEntity patient;

    @ManyToOne
    @JoinColumn(name="SPECIALTY_CONCEPT_ID")
    private ConceptEntity specialty;

    @ManyToOne
    @JoinColumn(name="REQUESTOR_ORGANISATION_ID")
    private OrganisationEntity requesterOrganisation;

    @ManyToOne
    @JoinColumn(name="REQUESTOR_PRACTITIONER_ID")
    private PractitionerEntity requesterPractitioner;


    public Long getId() {
        return id;
    }

    public PatientEntity getPatient() {
        return patient;
    }

    public ReferralRequestEntity setPatient(PatientEntity patient) {
        this.patient = patient;
        return this;
    }

    public ConceptEntity getSpecialty() {
        return specialty;
    }

    public ReferralRequestEntity setSpecialty(ConceptEntity specialty) {
        this.specialty = specialty;
        return this;
    }

    public OrganisationEntity getRequesterOrganisation() {
        return requesterOrganisation;
    }

    public ReferralRequestEntity setRequesterOrganisation(OrganisationEntity requesterOrganisation) {
        this.requesterOrganisation = requesterOrganisation;
        return this;
    }

    public PractitionerEntity getRequesterPractitioner() {
        return requesterPractitioner;
    }

    public ReferralRequestEntity setRequesterPractitioner(PractitionerEntity requesterPractitioner) {
        this.requesterPractitioner = requesterPractitioner;
        return this;
    }
}
