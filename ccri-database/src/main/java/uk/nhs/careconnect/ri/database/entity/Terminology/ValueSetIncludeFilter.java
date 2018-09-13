package uk.nhs.careconnect.ri.database.entity.Terminology;



import org.hl7.fhir.dstu3.model.ValueSet;

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
	private ConceptEntity property;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name="VALUE_ID")
	private ConceptEntity value;

	@Enumerated(EnumType.ORDINAL)
	ValueSet.FilterOperator operator;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name="VALUESET_INCLUDE_ID")
	private ValueSetInclude include;


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

	public ConceptEntity getProperty() {
		return property;
	}

	public ConceptEntity getValue() {
		return value;
	}

	public ValueSet.FilterOperator getOperator() {
		return operator;
	}

	public void setOperator(ValueSet.FilterOperator operator) {
		this.operator = operator;
	}

	public void setProperty(ConceptEntity property) {
		this.property = property;
	}

	public void setValue(ConceptEntity value) {
		this.value = value;
	}
}
