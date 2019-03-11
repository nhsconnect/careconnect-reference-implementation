package uk.nhs.careconnect.ri.database.entity;

import org.hl7.fhir.dstu3.model.Identifier;
import uk.nhs.careconnect.ri.database.entity.codeSystem.ConceptEntity;
import uk.nhs.careconnect.ri.database.entity.codeSystem.SystemEntity;

import javax.persistence.*;


@MappedSuperclass
public class BaseIdentifier extends BaseResource {



	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "SYSTEM_ID")
	private SystemEntity systemEntity;

    @Column(name = "IDENTIFIER_VALUE")
    private String value;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TYPE_CONCEPT_ID")
    private ConceptEntity identifierType;

    @Column(name = "listOrder")
    private Integer order;

    @Enumerated(EnumType.ORDINAL)
    Identifier.IdentifierUse identifierUse;


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

    public void setUse(Identifier.IdentifierUse use) { this.identifierUse = use; }
    public Identifier.IdentifierUse getUse() { 	return this.identifierUse; }


    public ConceptEntity getIdentifierType() {
        return identifierType;
    }

    public void setIdentifierType(ConceptEntity identifierType) {
        this.identifierType = identifierType;
    }

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
