package uk.nhs.careconnect.ri.entity.practitioner;

import uk.nhs.careconnect.ri.entity.BaseHumanName;

import javax.persistence.*;

@Entity
@Table(name = "PractitionerName",indexes =
        {
                @Index(name = "IDX_PRACTITIONER_FAMILY", columnList="family_name")
                , @Index(name = "IDX_PRACTIONER_GIVEN", columnList="given_name")
        })
public class PractitionerName extends BaseHumanName {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="PRACTITIONER_NAME_ID")
    private Long myId;

    @ManyToOne
    @JoinColumn(name = "PRACTITIONER_ID",foreignKey= @ForeignKey(name="FK_PRACTITIONER_PRACTITIONER_NAME"))
    private PractitionerEntity practitionerEntity;

    public Long getId()
    {
        return this.myId;
    }

    public PractitionerEntity getPractitionerEntity() {
        return this.practitionerEntity;
    }
    public void setPractitionerEntity(PractitionerEntity practitionerEntity) {
        this.practitionerEntity = practitionerEntity;
    }

}
