package uk.nhs.careconnect.ri.database.entity;

import org.hl7.fhir.dstu3.model.ContactPoint;

import javax.persistence.Column;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.MappedSuperclass;


@MappedSuperclass
public class BaseContactPoint extends BaseResource {

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
    public org.hl7.fhir.instance.model.ContactPoint.ContactPointSystem getSystemDstu2() {
        switch (this.system) {
            case EMAIL: return org.hl7.fhir.instance.model.ContactPoint.ContactPointSystem.EMAIL;
            case PHONE: return org.hl7.fhir.instance.model.ContactPoint.ContactPointSystem.PHONE;

            default:
                return null;
        }



    }
    public void setSystem(ContactPoint.ContactPointSystem systemEntity) {
        this.system = systemEntity;
    }

    public void setSystemDstu2(org.hl7.fhir.instance.model.ContactPoint.ContactPointSystem systemEntity) {
        switch (systemEntity) {
            case EMAIL: {
                this.system = ContactPoint.ContactPointSystem.EMAIL;
                break;
            }
            case PHONE: {
                this.system = ContactPoint.ContactPointSystem.PHONE;
                break;
            }
            case FAX: {
                this.system = ContactPoint.ContactPointSystem.FAX;
                break;
            }
        }
    }


    public void setValue(String value) { this.value = value; }
    public String getValue() { 	return this.value; }

	public void setTelecomUse(ContactPoint.ContactPointUse use) { this.telecomUse = use; }

    public void setTelecomUseDstu2(org.hl7.fhir.instance.model.ContactPoint.ContactPointUse use) {
        switch (use) {
            case HOME: {
                this.telecomUse = ContactPoint.ContactPointUse.HOME;
                break;
            }
            case MOBILE: {
                this.telecomUse = ContactPoint.ContactPointUse.MOBILE;
                break;
            }
            case WORK: {
                this.telecomUse = ContactPoint.ContactPointUse.WORK;
                break;
            }
        }

    }

	public ContactPoint.ContactPointUse getTelecomUse() { 	return this.telecomUse; }

    public org.hl7.fhir.instance.model.ContactPoint.ContactPointUse getTelecomUseDstu2() {
        switch (this.telecomUse)
        {
            case HOME: return org.hl7.fhir.instance.model.ContactPoint.ContactPointUse.HOME;
            case WORK: return org.hl7.fhir.instance.model.ContactPoint.ContactPointUse.WORK;
            case MOBILE: return org.hl7.fhir.instance.model.ContactPoint.ContactPointUse.MOBILE;
            default:
                return null;
        }
    }

    @Override
    public Long getId() {
        return null;
    }
}
