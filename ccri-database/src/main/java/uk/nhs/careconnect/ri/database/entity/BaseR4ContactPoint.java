package uk.nhs.careconnect.ri.database.entity;

import org.hl7.fhir.r4.model.ContactPoint;

import javax.persistence.Column;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.MappedSuperclass;


@MappedSuperclass
public class BaseR4ContactPoint extends BaseResource {

    @Column(name = "CONTACT_VALUE")
    private String value;

    @Column(name = "TELECOM_USE")
	@Enumerated(EnumType.ORDINAL)
    ContactPoint.ContactPointUse telecomUse;

	@Column(name = "SYSTEM_ID")
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



    @Override
    public Long getId() {
        return null;
    }
}
