package uk.nhs.careconnect.ri.entity;

import javax.persistence.*;

@MappedSuperclass
public abstract class BaseIdentifier {

	@Column(name = "value")
	private String value;
	public void setValue(String value) { this.value = value; } 	
	public String getValue() { 	return this.value; }
	
	@Column(name = "ORDER")
	private Integer order;


}
