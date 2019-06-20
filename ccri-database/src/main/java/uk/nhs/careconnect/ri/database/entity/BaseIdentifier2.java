package uk.nhs.careconnect.ri.database.entity;

import org.hl7.fhir.dstu3.model.Identifier;
import uk.nhs.careconnect.ri.database.entity.namingSystem.NamingSystemUniqueId;

import javax.persistence.*;


@MappedSuperclass
public class BaseIdentifier2 extends BaseResource {



	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "NAMING_UNIQUE_ID")
	private NamingSystemUniqueId system;

    @Column(name = "IDENTIFIER_VALUE")
    private String value;


    @Column(name = "listOrder")
    private Integer order;

    @Enumerated(EnumType.ORDINAL)
    Identifier.IdentifierUse identifierUse;


    public NamingSystemUniqueId getSystem() {
        return this.system;
    }

    public String getSystemValue() {
        String result = "";
        if (this.system != null) result=this.system.getValue();
        return result;
    }



    public void setValue(String value) { this.value = value; }
    public String getValue() { 	return this.value; }

    public void setUse(Identifier.IdentifierUse use) { this.identifierUse = use; }

    public void setSystem(NamingSystemUniqueId system) {
        this.system = system;
    }

    public Identifier.IdentifierUse getUse() { 	return this.identifierUse; }

    public Integer getOrder() {
        return order;
    }

    public void setOrder(Integer order) {
        this.order = order;
    }

    public Identifier.IdentifierUse getIdentifierUse() {
        return identifierUse;
    }

    public void setIdentifierUse(Identifier.IdentifierUse identifierUse) {
        this.identifierUse = identifierUse;
    }

    @Override
    public Long getId() {
        return null;
    }
}
