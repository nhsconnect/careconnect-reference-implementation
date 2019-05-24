package uk.nhs.careconnect.ri.database.entity.Person;

import uk.nhs.careconnect.ri.database.entity.BaseIdentifier2;

import javax.persistence.*;

@Entity
@Table(name="PersonIdentifier", uniqueConstraints= @UniqueConstraint(name="PK_PERSON_IDENTIFIER", columnNames={"PERSON_IDENTIFIER_ID"})
		,indexes =
		{
				@Index(name = "IDX_PERSON_IDENTIFER", columnList="IDENTIFIER_VALUE,NAMING_UNIQUE_ID")
		})
public class PersonIdentifier extends BaseIdentifier2 {

	public PersonIdentifier() {
	}
    public PersonIdentifier(PersonEntity personEntity) {
		this.personEntity = personEntity;
	}


	@Id 
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name= "PERSON_IDENTIFIER_ID")
    private Long identifierId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn (name = "PERSON_ID",foreignKey= @ForeignKey(name="FK_PERSON_PERSON_IDENTIFIER"))
    private PersonEntity personEntity;


	public Long getIdentifierId() { return identifierId; }
	public void setIdentifierId(Long identifierId) { this.identifierId = identifierId; }

	public PersonEntity getPerson() {
	        return this.personEntity;
	}
	public void setPerson(PersonEntity personEntity) {
	        this.personEntity = personEntity;
	}




}
