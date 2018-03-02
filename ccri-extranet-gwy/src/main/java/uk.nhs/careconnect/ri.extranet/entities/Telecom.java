package mayfieldis.careconnect.nosql.entities;


import org.hl7.fhir.dstu3.model.ContactPoint;

public class Telecom  {

	private String value;

	ContactPoint.ContactPointUse telecomUse;


	ContactPoint.ContactPointSystem system;

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public ContactPoint.ContactPointUse getTelecomUse() {
		return telecomUse;
	}

	public void setTelecomUse(ContactPoint.ContactPointUse telecomUse) {
		this.telecomUse = telecomUse;
	}

	public ContactPoint.ContactPointSystem getSystem() {
		return system;
	}

	public void setSystem(ContactPoint.ContactPointSystem system) {
		this.system = system;
	}

}
