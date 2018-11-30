package uk.nhs.careconnect.ri.database.entity.careTeam;

import uk.nhs.careconnect.ri.database.entity.Terminology.ConceptEntity;
import uk.nhs.careconnect.ri.database.entity.carePlan.CarePlanEntity;

import javax.persistence.*;

@Entity
@Table(name="CareTeamCategory", uniqueConstraints= @UniqueConstraint(name="PK_CARE_TEAM_CATEGORY", columnNames={"CARE_TEAM_CATEGORY_ID"})
        ,indexes = { @Index(name="IDX_CARE_TEAM_CATEGORY", columnList = "category")}
)
public class CareTeamCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name= "CARE_TEAM_CATEGORY_ID")
    private Long Id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn (name = "CARE_TEAM_ID",foreignKey= @ForeignKey(name="FK_CARE_TEAM_CATEGORY_CARE_TEAM_ID"))
    private CareTeamEntity team;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="category",foreignKey= @ForeignKey(name="FK_CARE_TEAM_CATEGORY_CATEGORY_CONCEPT_ID"))
    private ConceptEntity category;

    public void setId(Long id) {
        Id = id;
    }

    public Long getId() {
        return Id;
    }

    public ConceptEntity getCategory() {
        return category;
    }

    public void setCategory(ConceptEntity category) {
        this.category = category;
    }

    public CareTeamEntity getTeam() {
        return team;
    }

    public void setTeam(CareTeamEntity team) {
        this.team = team;
    }
}
