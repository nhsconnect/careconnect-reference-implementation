package mayfieldis.careconnect.nosql.entities;


public class Identifier {


	private String system;

	private String value;

	private Integer order;

	org.hl7.fhir.dstu3.model.Identifier.IdentifierUse identifierUse;

	public String getSystem() {
		return system;
	}

	public void setSystem(String system) {
		this.system = system;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public Integer getOrder() {
		return order;
	}

	public void setOrder(Integer order) {
		this.order = order;
	}

	public org.hl7.fhir.dstu3.model.Identifier.IdentifierUse getIdentifierUse() {
		return identifierUse;
	}

	public void setIdentifierUse(org.hl7.fhir.dstu3.model.Identifier.IdentifierUse identifierUse) {
		this.identifierUse = identifierUse;
	}
}
