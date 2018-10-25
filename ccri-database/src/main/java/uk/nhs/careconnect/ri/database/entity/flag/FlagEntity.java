package uk.nhs.careconnect.ri.database.entity.flag;

import org.hl7.fhir.dstu3.model.Flag;
import org.hl7.fhir.dstu3.model.ListResource;
import uk.nhs.careconnect.ri.database.entity.BaseResource;
import uk.nhs.careconnect.ri.database.entity.Terminology.ConceptEntity;
import uk.nhs.careconnect.ri.database.entity.encounter.EncounterEntity;
import uk.nhs.careconnect.ri.database.entity.organization.OrganisationEntity;
import uk.nhs.careconnect.ri.database.entity.patient.PatientEntity;
import uk.nhs.careconnect.ri.database.entity.practitioner.PractitionerEntity;

import javax.persistence.*;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "Flag",
        indexes = {

        })
public class FlagEntity extends BaseResource {

    private static final int MAX_DESC_LENGTH = 4096;

    public enum FlagType  { component, valueQuantity }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="FLAG_ID")
    private Long id;

    @OneToMany(mappedBy="flag", targetEntity= FlagIdentifier.class)
    private Set<FlagIdentifier> identifiers = new HashSet<>();

    @Enumerated(EnumType.ORDINAL)
    @Column(name="status")
    private Flag.FlagStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="CATEGORY_CONCEPT_ID",foreignKey= @ForeignKey(name="FK_FLAG_CATEGORY_CONCEPT_ID"))
    private ConceptEntity category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="CODE_CONCEPT_ID",foreignKey= @ForeignKey(name="FK_FLAG_CODE_CONCEPT_ID"))
    private ConceptEntity code;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn (name = "PATIENT_ID",foreignKey= @ForeignKey(name="FK_FLAG_PATIENT_ID"))
    private PatientEntity patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="ENCOUNTER_ID",foreignKey= @ForeignKey(name="FK_FLAG_ENCOUNTER_ID"))
    private EncounterEntity encounter;


    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "START_DATETIME")
    private Date startDateTime;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "END_DATETIME")
    private Date endDateTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="AUTHOR_PRACTITIONER_ID",foreignKey= @ForeignKey(name="FK_FLAG_AUTHOR_PRACTITIONER_ID"))
    private PractitionerEntity authorPractitioner;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="AUTHOR_ORGANISATION_ID",foreignKey= @ForeignKey(name="FK_FLAG_AUTHOR_ORGANISATION_ID"))
    private OrganisationEntity authorOrganisation;

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

    public Set<FlagIdentifier> getIdentifiers() {
        if (identifiers == null) { identifiers = new HashSet<FlagIdentifier>(); }
        return identifiers;
    }

   

    public FlagEntity setIdentifiers(Set<FlagIdentifier> identifiers) {
        this.identifiers = identifiers;
        return this;
    }


   

    public static int getMaxDescLength() {
        return MAX_DESC_LENGTH;
    }

    public Flag.FlagStatus getStatus() {
        return status;
    }

    public void setStatus(Flag.FlagStatus status) {
        this.status = status;
    }

    public ConceptEntity getCategory() {
        return category;
    }

    public void setCategory(ConceptEntity category) {
        this.category = category;
    }

    public EncounterEntity getEncounter() {
        return encounter;
    }

    public void setEncounter(EncounterEntity encounter) {
        this.encounter = encounter;
    }

    public Date getStartDateTime() {
        return startDateTime;
    }

    public void setStartDateTime(Date startDateTime) {
        this.startDateTime = startDateTime;
    }

    public Date getEndDateTime() {
        return endDateTime;
    }

    public void setEndDateTime(Date endDateTime) {
        this.endDateTime = endDateTime;
    }

    public PractitionerEntity getAuthorPractitioner() {
        return authorPractitioner;
    }

    public void setAuthorPractitioner(PractitionerEntity authorPractitioner) {
        this.authorPractitioner = authorPractitioner;
    }

    public OrganisationEntity getAuthorOrganisation() {
        return authorOrganisation;
    }

    public void setAuthorOrganisation(OrganisationEntity authorOrganisation) {
        this.authorOrganisation = authorOrganisation;
    }

    public ConceptEntity getCode() {
        return code;
    }

    public void setCode(ConceptEntity code) {
        this.code = code;
    }


}
