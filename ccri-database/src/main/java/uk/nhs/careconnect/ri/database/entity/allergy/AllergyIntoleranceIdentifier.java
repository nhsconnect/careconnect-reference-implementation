package uk.nhs.careconnect.ri.database.entity.allergy;

import uk.nhs.careconnect.ri.database.entity.BaseIdentifier;

import javax.persistence.*;


@Entity
@Table(name="AllergyIntoleranceIdentifier", uniqueConstraints= @UniqueConstraint(name="PK_ALLERGY_IDENTIFIER", columnNames={"ALLERGY_IDENTIFIER_ID"})
		,indexes =
		{
				@Index(name = "IDX_ALLERGY_IDENTIFER", columnList="IDENTIFIER_VALUE,SYSTEM_ID")

		})
public class AllergyIntoleranceIdentifier extends BaseIdentifier {

	public AllergyIntoleranceIdentifier() {

	}

	public AllergyIntoleranceIdentifier(AllergyIntoleranceEntity allergy) {
		this.allergy = allergy;
	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name= "ALLERGY_IDENTIFIER_ID")
	private Long identifierId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn (name = "ALLERGY_ID",foreignKey= @ForeignKey(name="FK_ALLERGY_ALLERGY_IDENTIFIER"))
	private AllergyIntoleranceEntity allergy;


    public Long getIdentifierId() { return identifierId; }
	public void setIdentifierId(Long identifierId) { this.identifierId = identifierId; }

	public AllergyIntoleranceEntity getAllergyIntolerance() {
	        return this.allergy;
	}

	public void setAllergyIntolerance(AllergyIntoleranceEntity allergy) {
	        this.allergy = allergy;
	}




}
