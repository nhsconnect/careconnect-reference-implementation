package uk.nhs.careconnect.ri.database.entity.valueSet;



import org.hl7.fhir.dstu3.model.ValueSet;
import uk.nhs.careconnect.ri.database.entity.codeSystem.ConceptEntity;

import javax.persistence.*;


@Entity
@Table(name="ValueSetIncludeFilter")
public class ValueSetIncludeFilter {

	private static final int MAX_DESC_LENGTH = 400;

	public ValueSetIncludeFilter() {

	}


	@Id 
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name= "VALUESET_INCLUDE_FILTER_ID")
	private Integer contentId;


	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name="PROPERTY_ID")
	private ConceptEntity xproperty;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name="VALUE_ID")
	private ConceptEntity xvalue;

	@Enumerated(EnumType.ORDINAL)
	ValueSet.FilterOperator operator;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name="VALUESET_INCLUDE_ID")
	private ValueSetInclude include;

	@Column(name="PROPERTY_CODE")
	private String propertyCode;

	@Column(name="VALUE_CODE", length = MAX_DESC_LENGTH)
	private String valueCode;

    public ValueSetInclude getInclude() {
		return include;
	}

	public Integer getId() {
		return contentId;
	}

	public void setId(Integer contentId) {
		this.contentId = contentId;
	}

	public void setInclude(ValueSetInclude include) {
		this.include = include;
	}



	public ValueSet.FilterOperator getOperator() {
		return operator;
	}

	public void setOperator(ValueSet.FilterOperator operator) {
		this.operator = operator;
	}

	public Integer getContentId() {
		return contentId;
	}

	public void setContentId(Integer contentId) {
		this.contentId = contentId;
	}

	public ConceptEntity getXproperty() {
		return xproperty;
	}

	public void setXproperty(ConceptEntity xproperty) {
		this.xproperty = xproperty;
	}

	public ConceptEntity getXvalue() {
		return xvalue;
	}

	public void setXvalue(ConceptEntity xvalue) {
		this.xvalue = xvalue;
	}

	public String getPropertyCode() {
		return propertyCode;
	}

	public void setPropertyCode(String propertyCode) {
		this.propertyCode = propertyCode;
	}

	public String getValueCode() {
		return valueCode;
	}

	public void setValueCode(String valueCode) {
		this.valueCode = valueCode;
	}
}
