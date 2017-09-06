package uk.nhs.careconnect.ri.entity.Terminology;

import javax.persistence.*;


@Entity
@Table(name="ValueSetIdentifier", uniqueConstraints= @UniqueConstraint(name="PK_VALUESET_IDENTIFIER", columnNames={"VALUESET_IDENTIFIER_ID"}))
public class ValueSetIdentifier  {
	
	public ValueSetIdentifier() {
		
	}
	
	public ValueSetIdentifier(ValueSetEntity valueSetEntity) {
		this.valueSetEntity = valueSetEntity;
	}


	@Id 
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name= "VALUESET_IDENTIFIER_ID")
	private Integer identifierId;

	@ManyToOne
	@JoinColumn (name = "VALUESET_ID",foreignKey= @ForeignKey(name="FK_VALUESET_VALUESET_IDENTIFIER"))
	private ValueSetEntity valueSetEntity;

    @Column(name = "value")
    private String value;

    @Column(name = "ORDER")
    private Integer order;


	public Integer getIdentifierId() { return identifierId; }
	public void setIdentifierId(Integer identifierId) { this.identifierId = identifierId; }

	public ValueSetEntity getValueSetEntity() {
	        return this.valueSetEntity;
	}
	public void setValueSetEntity(ValueSetEntity valueSetEntity) {
	        this.valueSetEntity = valueSetEntity;
	}

    public void setValue(String value) { this.value = value; }
    public String getValue() { 	return this.value; }
}
