package uk.nhs.careconnect.ri.entity;

import org.hl7.fhir.dstu3.model.HumanName;

import javax.persistence.Column;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.MappedSuperclass;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@MappedSuperclass
public class BaseHumanName extends BaseResource {
    @Column(name = "prefix")
    private String prefix;

    @Column(name = "given_name")
    private String givenName;

    @Column(name = "family_name")
    private String familyName;

    @Column(name = "suffix")
    private String suffix;

    @Enumerated(EnumType.ORDINAL)
    private HumanName.NameUse nameUse;

    public HumanName.NameUse getNameUse() {
        return this.nameUse;
    }
    public org.hl7.fhir.instance.model.HumanName.NameUse getNameUseDstu2() {

        switch (this.nameUse) {
            case USUAL: return org.hl7.fhir.instance.model.HumanName.NameUse.USUAL;
            case OLD: return org.hl7.fhir.instance.model.HumanName.NameUse.OLD;
            case ANONYMOUS: return org.hl7.fhir.instance.model.HumanName.NameUse.ANONYMOUS;
            case MAIDEN: return org.hl7.fhir.instance.model.HumanName.NameUse.MAIDEN;
            default : return null;
        }
    }

    public void setNameUse(HumanName.NameUse nameUse) {
        this.nameUse = nameUse;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public String getGivenName() {
        return givenName;
    }

    public void setGivenName(String givenName) {
        this.givenName = givenName;
    }

    public String getFamilyName() {
        return familyName;
    }

    public void setFamilyName(String familyName) {
        this.familyName = familyName;
    }

    public String getSuffix() {
        return suffix;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }

    public String getDisplayName(){
        List<String> names = Arrays.asList(getPrefix(), getGivenName(), getFamilyName(), getSuffix());
        return names.stream().filter(Objects::nonNull).collect(Collectors.joining(" "));
    }

    @Override
    public Long getId() {
        return null;
    }
}
