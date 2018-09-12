package uk.nhs.careconnect.ri.database.entity.practitioner;

import uk.nhs.careconnect.ri.database.entity.BaseHumanName;

import javax.persistence.*;

@Entity
@Table(name = "PractitionerName",indexes =
        {
                @Index(name = "IDX_PRACTITIONER_NAME_PRACTITIONER_ID", columnList="PRACTITIONER_ID")
        })
public class PractitionerName extends BaseHumanName {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="PRACTITIONER_NAME_ID")
    private Long myId;

    @ManyToOne(fetch = FetchType.LAZY)
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
