package uk.nhs.careconnect.ri.database.entity.Person;


import uk.nhs.careconnect.ri.database.entity.BaseHumanName;

import javax.persistence.*;

@Entity
@Table(name = "PersonName",indexes =
        {
            @Index(name = "IDX_PERSON_FAMILY", columnList="family_name")
                , @Index(name = "IDX_PERSON_GIVEN", columnList="given_name")
        })
public class PersonName extends BaseHumanName {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="PERSON_NAME_ID")
    private Long myId;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PERSON_ID",foreignKey= @ForeignKey(name="FK_PERSON_NAME_PERSON_ID"))
    private PersonEntity personEntity;

    public Long getId()
    {
        return this.myId;
    }

    public PersonEntity getPersonEntity() {
        return this.personEntity;
    }
    public void setPersonEntity(PersonEntity personEntity) {
        this.personEntity = personEntity;
    }


}
