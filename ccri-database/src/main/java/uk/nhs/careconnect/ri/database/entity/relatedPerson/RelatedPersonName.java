package uk.nhs.careconnect.ri.database.entity.relatedPerson;


import uk.nhs.careconnect.ri.database.entity.BaseHumanName;

import javax.persistence.*;

@Entity
@Table(name = "RelatedPersonName",indexes =
        {
            @Index(name = "IDX_PERSON_FAMILY", columnList="family_name")
                , @Index(name = "IDX_PERSON_GIVEN", columnList="given_name")
        })
public class RelatedPersonName extends BaseHumanName {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="PERSON_NAME_ID")
    private Long myId;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PERSON_ID",foreignKey= @ForeignKey(name="FK_PERSON_NAME_PERSON_ID"))
    private RelatedPersonEntity personEntity;

    public Long getId()
    {
        return this.myId;
    }

    public RelatedPersonEntity getRelatedPersonEntity() {
        return this.personEntity;
    }
    public void setRelatedPersonEntity(RelatedPersonEntity personEntity) {
        this.personEntity = personEntity;
    }


}
