package uk.nhs.careconnect.ri.database.entity;

import com.sun.jndi.toolkit.url.Uri;
import org.hl7.fhir.dstu3.model.*;
import uk.nhs.careconnect.ri.database.entity.codeSystem.ConceptEntity;

import javax.persistence.*;


@MappedSuperclass
public class BaseExtension extends BaseResource {

    private static final int MAX_LENGTH = 10000;

    @Column(name = "URL")
    private String url;

    @Column(name = "VALUE_BOOLEAN")
    private Boolean valueBoolean;

    @Column(name = "VALUE_STRING", length = MAX_LENGTH)
    private String valueString;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="VALUE_CONCEPT_ID",nullable = false,foreignKey= @ForeignKey())
    private ConceptEntity valueConcept;

    @Column(name = "VALUE_CODEABLE_CONCEPT_TEXT", length = MAX_LENGTH)
    private String valueCodeableConceptText;

    @Column(name = "VALUE_URI", length = MAX_LENGTH)
    private String valueUri;

    @Override
    public Long getId() {
        return null;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Boolean getValueBoolean() {
        return valueBoolean;
    }

    public void setValueBoolean(Boolean valueBoolean) {
        this.valueBoolean = valueBoolean;
    }

    public static int getMaxLength() {
        return MAX_LENGTH;
    }

    public String getValueString() {
        return valueString;
    }

    public void setValueString(String valueString) {
        this.valueString = valueString;
    }

    public ConceptEntity getValueConcept() {
        return valueConcept;
    }

    public void setValueConcept(ConceptEntity valueConcept) {
        this.valueConcept = valueConcept;
    }

    public String getValueCodeableConceptText() {
        return valueCodeableConceptText;
    }

    public void setValueCodeableConceptText(String valueCodeableConceptText) {
        this.valueCodeableConceptText = valueCodeableConceptText;
    }

    public String getValueUri() {
        return valueUri;
    }

    public void setValueUri(String valueUri) {
        this.valueUri = valueUri;
    }

    public Type getValue() {
        if (this.valueUri != null) {
            return new UriType().setValue(this.valueUri);
        }
        if (this.valueString != null) {
            return new StringType().setValue(this.valueString);
        }
        if (this.valueBoolean != null) {
            return new BooleanType().setValue(this.valueBoolean);
        }
        return null;
    }

    public void setValue(Type type) {
        if (type instanceof BooleanType) {
            BooleanType bool = (BooleanType) type;
            this.valueBoolean = bool.booleanValue();
        }
        if (type instanceof StringType) {
            StringType string = (StringType) type;
            this.valueString = string.getValue();
        }
        if (type instanceof UriType) {
            UriType uri = (UriType) type;
            this.valueUri = uri.getValue();
        }
        if (type instanceof CodeableConcept) {
            CodeableConcept concept = (CodeableConcept) type;
            // TODO
        }
    }
}
