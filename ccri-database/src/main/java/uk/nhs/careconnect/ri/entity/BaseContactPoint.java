package uk.nhs.careconnect.ri.entity;

import org.hl7.fhir.instance.model.ContactPoint;
import uk.nhs.careconnect.ri.entity.Terminology.SystemEntity;

import javax.persistence.*;


@MappedSuperclass
public class BaseContactPoint {


	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "SYSTEM_ID")
	private SystemEntity systemEntity;

    @Column(name = "value")
    private String value;

	@Enumerated(EnumType.STRING)
    ContactPoint.ContactPointUse telecomUse;

    public SystemEntity getSystem() {
        return this.systemEntity;
    }
    public void setSystem(SystemEntity systemEntity) {
        this.systemEntity = systemEntity;
    }

    public void setValue(String value) { this.value = value; }
    public String getValue() { 	return this.value; }

	public void setTelecomUse(ContactPoint.ContactPointUse use) { this.telecomUse = use; }
	public ContactPoint.ContactPointUse getTelecomUse() { 	return this.telecomUse; }
}
