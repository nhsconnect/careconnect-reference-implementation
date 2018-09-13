package uk.nhs.careconnect.ri.database.entity;

import uk.nhs.careconnect.ri.database.entity.Terminology.SystemEntity;

import javax.persistence.*;


@MappedSuperclass
public class BaseIdentifier extends BaseResource {



	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "SYSTEM_ID")
	private SystemEntity systemEntity;

    @Column(name = "IDENTIFIER_VALUE")
    private String value;


    @Column(name = "listOrder")
    private Integer order;

    @Enumerated(EnumType.ORDINAL)
    org.hl7.fhir.instance.model.Identifier.IdentifierUse identifierUse;


    public SystemEntity getSystem() {
        return this.systemEntity;
    }
    public String getSystemUri() {
        String result = "";
        if (this.systemEntity != null) result=this.systemEntity.getUri();
        return result;
    }
    public void setSystem(SystemEntity systemEntity) {
        this.systemEntity = systemEntity;
    }

    public void setValue(String value) { this.value = value; }
    public String getValue() { 	return this.value; }

    public void setUse(org.hl7.fhir.instance.model.Identifier.IdentifierUse use) { this.identifierUse = use; }
    public org.hl7.fhir.instance.model.Identifier.IdentifierUse getUse() { 	return this.identifierUse; }

    @Override
    public Long getId() {
        return null;
    }
}
