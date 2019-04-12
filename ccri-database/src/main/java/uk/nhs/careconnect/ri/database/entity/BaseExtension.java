package uk.nhs.careconnect.ri.database.entity;

import org.hl7.fhir.dstu3.model.ContactPoint;
import org.hl7.fhir.dstu3.model.Extension;

import javax.persistence.Column;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.MappedSuperclass;


@MappedSuperclass
public class BaseExtension extends BaseResource {

    private static final int MAX_LENGTH = 10000;

    @Column(name = "URL")
    private String url;

    @Column(name = "VALUE_BOOLEAN")
    private Boolean valueBoolean;

    @Column(name = "VALUE_STRING", length = MAX_LENGTH)
    private Boolean valueString;

    @Column(name = "VALUE_CODEABLE_CONCEPT")
    private Boolean valueCodeableConcept;

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

    public Boolean getValueString() {
        return valueString;
    }

    public void setValueString(Boolean valueString) {
        this.valueString = valueString;
    }
}
