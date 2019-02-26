package uk.nhs.careconnect.ri.database.entity.condition;

import uk.nhs.careconnect.ri.database.entity.practitioner.PractitionerEntity;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name="ConditionNote", uniqueConstraints= @UniqueConstraint(name="PK_CONDITION_NOTE", columnNames={"CONDITION_NOTE_ID"})

)
public class ConditionNote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name= "CONDITION_NOTE_ID")
    private Long Id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn (name = "CONDITION_ID",foreignKey= @ForeignKey(name="FK_CONDITION_CONDITION_NOTE"))
    private ConditionEntity condition;

    @Column(name="note")
    private String note;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn (name = "AUTHOR_PRACTITIONER_ID",foreignKey= @ForeignKey(name="FK_CONDITION_NOTE_AUTHOR_PRACTITIONER"))
    private PractitionerEntity authorPractitoner;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "dateTime")
    private Date dateTime;


    public void setId(Long id) {
        Id = id;
    }

    public Long getId() {
        return Id;
    }


    public ConditionNote setCondition(ConditionEntity condition) {
        this.condition = condition;
        return this;
    }

    public ConditionEntity getCondition() {
        return condition;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public PractitionerEntity getAuthorPractitoner() {
        return authorPractitoner;
    }

    public void setAuthorPractitoner(PractitionerEntity authorPractitoner) {
        this.authorPractitoner = authorPractitoner;
    }

    public Date getDateTime() {
        return dateTime;
    }

    public void setDateTime(Date dateTime) {
        this.dateTime = dateTime;
    }
}
