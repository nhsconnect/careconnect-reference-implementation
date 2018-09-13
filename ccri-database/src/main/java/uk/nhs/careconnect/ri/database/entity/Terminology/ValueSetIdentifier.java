package uk.nhs.careconnect.ri.database.entity.Terminology;

import uk.nhs.careconnect.ri.database.entity.BaseIdentifier;

import javax.persistence.*;


@Entity
@Table(name="ValueSetIdentifier", uniqueConstraints= @UniqueConstraint(name="PK_VALUESET_IDENTIFIER", columnNames={"VALUESET_IDENTIFIER_ID"}))
public class ValueSetIdentifier extends BaseIdentifier {
	
	public ValueSetIdentifier() {
		
	}
	
	public ValueSetIdentifier(ValueSetEntity valueSetEntity) {
		this.valueSetEntity = valueSetEntity;
	}


	@Id 
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name= "VALUESET_IDENTIFIER_ID")
	private Integer identifierId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn (name = "VALUESET_ID",foreignKey= @ForeignKey(name="FK_VALUESET_VALUESET_IDENTIFIER"))
	private ValueSetEntity valueSetEntity;



	public Integer getIdentifierId() { return identifierId; }
	public void setIdentifierId(Integer identifierId) { this.identifierId = identifierId; }

	public ValueSetEntity getValueSetEntity() {
	        return this.valueSetEntity;
	}
	public void setValueSetEntity(ValueSetEntity valueSetEntity) {
	        this.valueSetEntity = valueSetEntity;
	}


}
