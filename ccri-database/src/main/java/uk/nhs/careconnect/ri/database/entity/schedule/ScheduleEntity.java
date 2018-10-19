package uk.nhs.careconnect.ri.database.entity.schedule;

import uk.nhs.careconnect.ri.database.entity.BaseResource;
import uk.nhs.careconnect.ri.database.entity.Terminology.ConceptEntity;
import uk.nhs.careconnect.ri.database.entity.slot.SlotEntity;


import javax.persistence.*;
import java.util.*;

@Entity
@Table(name = "Schedule")
public class ScheduleEntity extends BaseResource {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="SCHEDULE_ID")
    private Long id;

    @Column(name="ACTIVE")
    private Boolean active;

    @Column(name="COMMENT")
    private String comment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="CATEGORY_CONCEPT_ID",foreignKey= @ForeignKey(name="FK_SCHEDULE_CATEGORY_CONCEPT"))
    private ConceptEntity category;

    @OneToMany(mappedBy="schedule", targetEntity = ScheduleIdentifier.class)
    Set<ScheduleIdentifier> identifiers = new HashSet<>();


    @OneToMany(mappedBy="schedule", targetEntity = ScheduleActor.class)
    Set<ScheduleActor> actors = new HashSet<>();

    @OneToMany(mappedBy="schedule", targetEntity = SlotEntity.class)
    Set<SlotEntity> slots = new HashSet<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }


    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Set<ScheduleIdentifier> getIdentifiers() {
        return identifiers;
    }

    public void setIdentifiers(Set<ScheduleIdentifier> identifiers) {
        this.identifiers = identifiers;
    }

    public Set<ScheduleActor> getActors() {
        return actors;
    }

    public void setActors(Set<ScheduleActor> actors) {
        this.actors = actors;
    }

    public ConceptEntity getCategory() {
        return category;
    }

    public void setCategory(ConceptEntity category) {
        this.category = category;
    }

    public Set<SlotEntity> getSlots() {
        return slots;
    }

    public void setSlots(Set<SlotEntity> slots) {
        this.slots = slots;
    }
}
