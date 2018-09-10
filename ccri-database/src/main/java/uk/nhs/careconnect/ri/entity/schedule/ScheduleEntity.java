package uk.nhs.careconnect.ri.entity.schedule;

import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

import uk.nhs.careconnect.ri.entity.BaseResource;

import uk.nhs.careconnect.ri.entity.Terminology.ConceptEntity;
import uk.nhs.careconnect.ri.entity.organization.OrganisationEntity;


import javax.persistence.*;
import java.util.*;

@Entity
@Table(name = "Schedule")
public class ScheduleEntity extends BaseResource {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="SERVICE_ID")
    private Long id;

    @Column(name="ACTIVE")
    private Boolean active;

    @Column(name="NAME")
    private String name;


    @OneToMany(mappedBy="schedule", targetEntity = ScheduleIdentifier.class)
    Set<ScheduleIdentifier> identifiers = new HashSet<>();


    @OneToMany(mappedBy="schedule", targetEntity = ScheduleActor.class)
    Set<ScheduleActor> actors = new HashSet<>();

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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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
}
