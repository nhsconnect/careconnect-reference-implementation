package uk.nhs.careconnect.ri.database.entity.procedure;

import uk.nhs.careconnect.ri.database.entity.Terminology.ConceptEntity;
import uk.nhs.careconnect.ri.database.entity.organization.OrganisationEntity;
import uk.nhs.careconnect.ri.database.entity.practitioner.PractitionerEntity;

import javax.persistence.*;

@Entity
@Table(name="ProcedurePerformer", uniqueConstraints= @UniqueConstraint(name="PK_PROCEDURE_PERFORMER", columnNames={"PROCEDURE_PERFORMER_ID"})
        ,indexes = { @Index(name="IDX_PROCEDURE_PERFORMER", columnList = "PROCEDURE_PERFORMER_ID")}
)
public class ProcedurePerformer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name= "PROCEDURE_PERFORMER_ID")
    private Long Id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn (name = "PROCEDURE_ID",foreignKey= @ForeignKey(name="FK_PROCEDURE_PROCEDURE_PERFORMER"))
    private ProcedureEntity procedure;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="ROLE_CONCEPT_ID")
    private ConceptEntity role;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="ACTOR_PRACTITIONER",foreignKey= @ForeignKey(name="FK_PROCEDURE_PERFORMER_ACTOR_PRACTITIONER"))
    private PractitionerEntity actorPractitioner;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="ACTOR_ORGNANISATION",foreignKey= @ForeignKey(name="FK_PROCEDURE_PERFORMER_ACTOR_ORGNANISATION"))
    private OrganisationEntity actorOrganisation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="ON_BEHALF_OF_ORGANISATION",foreignKey= @ForeignKey(name="FK_PROCEDURE_ORGANISATION"))
    private OrganisationEntity onBehalfOrganisation;

    public void setId(Long id) {
        Id = id;
    }

    public Long getId() {
        return Id;
    }

    public ConceptEntity getRole() {
        return role;
    }

    public OrganisationEntity getActorOrganisation() {
        return actorOrganisation;
    }

    public OrganisationEntity getOnBehalfOrganisation() {
        return onBehalfOrganisation;
    }

    public PractitionerEntity getActorPractitioner() {
        return actorPractitioner;
    }

    public ProcedureEntity getProcedure() {
        return procedure;
    }

    public ProcedurePerformer setActorOrganisation(OrganisationEntity actorOrganisation) {
        this.actorOrganisation = actorOrganisation;
        return this;
    }

    public ProcedurePerformer setActorPractitioner(PractitionerEntity actorPractitioner) {
        this.actorPractitioner = actorPractitioner;
        return this;
    }

    public ProcedurePerformer setOnBehalfOrganisation(OrganisationEntity onBehalfOrganisation) {
        this.onBehalfOrganisation = onBehalfOrganisation;
        return this;
    }

    public ProcedurePerformer setProcedure(ProcedureEntity procedure) {
        this.procedure = procedure;
        return this;
    }

    public ProcedurePerformer setRole(ConceptEntity role) {
        this.role = role;
        return this;
    }
}
