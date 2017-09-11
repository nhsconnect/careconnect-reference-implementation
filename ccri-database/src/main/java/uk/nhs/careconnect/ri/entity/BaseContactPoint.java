package uk.nhs.careconnect.ri.entity;

import org.hl7.fhir.instance.model.ContactPoint;

import javax.persistence.Column;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.MappedSuperclass;


@MappedSuperclass
public class BaseContactPoint {

    @Column(name = "value")
    private String value;

	@Enumerated(EnumType.ORDINAL)
    ContactPoint.ContactPointUse telecomUse;

    @Enumerated(EnumType.ORDINAL)
    ContactPoint.ContactPointSystem system;


    public ContactPoint.ContactPointSystem getSystem() {
        return this.system;
    }
    public void setSystem(ContactPoint.ContactPointSystem systemEntity) {
        this.system = systemEntity;
    }

    public void setValue(String value) { this.value = value; }
    public String getValue() { 	return this.value; }

	public void setTelecomUse(ContactPoint.ContactPointUse use) { this.telecomUse = use; }
	public ContactPoint.ContactPointUse getTelecomUse() { 	return this.telecomUse; }
}
