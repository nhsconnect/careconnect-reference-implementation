package uk.nhs.careconnect.ri.database.entity;

import org.hl7.fhir.dstu3.model.HumanName;

import javax.persistence.Column;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.MappedSuperclass;
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

    // KGM 15/12/2017 Added trim calls to remove trailing and leading spaces from values.

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

        if (prefix!=null && !prefix.isEmpty()) return prefix.trim();
        else return null;
    }

    public void setPrefix(String prefix) {

        if (prefix !=null) this.prefix = prefix.trim();
        else this.prefix=prefix;
    }

    public String getGivenName() {

        if (givenName != null && !givenName.isEmpty()) return givenName.trim();
        else return null;
    }

    public void setGivenName(String givenName) {

        if (givenName != null) this.givenName = givenName.trim();
        else this.givenName = givenName;
    }

    public String getFamilyName() {

        if (familyName != null && !familyName.isEmpty()) return familyName.trim();
        else return null;
    }

    public void setFamilyName(String familyName) {

        if (familyName != null) this.familyName = familyName.trim();
        else this.familyName = familyName;
    }

    public String getSuffix() {
        if (suffix != null && !suffix.isEmpty()) return suffix.trim();
        else return null;

    }

    public void setSuffix(String suffix) {
        if (suffix!=null) this.suffix = suffix.trim();
        else this.suffix = suffix;
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
