package uk.nhs.careconnect.ri.database.entity.encounter;

import uk.nhs.careconnect.ri.database.entity.Terminology.ConceptEntity;
import uk.nhs.careconnect.ri.database.entity.practitioner.PractitionerEntity;
import uk.nhs.careconnect.ri.database.entity.relatedPerson.RelatedPersonEntity;

import javax.persistence.*;

@Entity
@Table(name="EncounterParticipant", uniqueConstraints= @UniqueConstraint(name="PK_ENCOUNTER_PARTICIPANT", columnNames={"ENCOUNTER_PARTICIPANT_ID"})
        ,indexes = {
        @Index(name="IDX_ENCOUNTER_PARTICIPANT_ENCOUNTER_ID", columnList = "ENCOUNTER_ID")
}
)
public class EncounterParticipant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name= "ENCOUNTER_PARTICIPANT_ID")
    private Long Id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn (name = "ENCOUNTER_ID",foreignKey= @ForeignKey(name="FK_ENCOUNTER_PARTICIPANT_ENCOUNTER_ID"))
    private EncounterEntity encounter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="PARTICIPANT_PRACTITIONER_ID",foreignKey= @ForeignKey(name="FK_ENCOUNTER_PRACTITIONER_ID"))
    private PractitionerEntity participant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="PARTICIPANT_PERSON_ID",foreignKey= @ForeignKey(name="FK_ENCOUNTER_PERSON_ID"))
    private RelatedPersonEntity person;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="PARTICIPANT_TYPE_CONCEPT_ID",foreignKey= @ForeignKey(name="FK_ENCOUNTER_PARTICIPANT_TYPE_CONCEPT_ID"))
    private ConceptEntity participantType;

    public ConceptEntity getParticipantType() {
        return participantType;
    }

    public void setParticipantType(ConceptEntity participantType) {
        this.participantType = participantType;
    }


    public void setId(Long id) {
        Id = id;
    }

    public Long getId() {
        return Id;
    }

    public EncounterParticipant setEncounter(EncounterEntity encounter) {
        this.encounter = encounter;
        return this;
    }

    public PractitionerEntity getParticipant() {
        return participant;
    }

    public void setParticipant(PractitionerEntity participant) {
        this.participant = participant;
    }

    public RelatedPersonEntity getPerson() {
        return person;
    }

    public void setPerson(RelatedPersonEntity person) {
        this.person = person;
    }

    public EncounterEntity getEncounter() {
        return encounter;
    }
}
