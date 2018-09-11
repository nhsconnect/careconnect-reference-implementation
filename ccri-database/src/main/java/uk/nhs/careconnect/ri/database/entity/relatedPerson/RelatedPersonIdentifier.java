package uk.nhs.careconnect.ri.database.entity.relatedPerson;

import uk.nhs.careconnect.ri.database.entity.BaseIdentifier;


import javax.persistence.*;

@Entity
@Table(name="RelatedPersonIdentifier", uniqueConstraints= @UniqueConstraint(name="PK_PERSON_IDENTIFIER", columnNames={"PERSON_IDENTIFIER_ID"})
		,indexes =
		{
				@Index(name = "IDX_PERSON_IDENTIFER", columnList="IDENTIFIER_VALUE,SYSTEM_ID")
		})
public class RelatedPersonIdentifier extends BaseIdentifier {
	
	public RelatedPersonIdentifier() {
	}
    public RelatedPersonIdentifier(RelatedPersonEntity personEntity) {
		this.personEntity = personEntity;
	}

	@Id 
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name= "PERSON_IDENTIFIER_ID")
    private Long identifierId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn (name = "PERSON_ID",foreignKey= @ForeignKey(name="FK_PERSON_PERSON_IDENTIFIER"))
    private RelatedPersonEntity personEntity;


	public Long getIdentifierId() { return identifierId; }
	public void setIdentifierId(Long identifierId) { this.identifierId = identifierId; }

	public RelatedPersonEntity getRelatedPerson() {
	        return this.personEntity;
	}
	public void setRelatedPerson(RelatedPersonEntity personEntity) {
	        this.personEntity = personEntity;
	}


}
