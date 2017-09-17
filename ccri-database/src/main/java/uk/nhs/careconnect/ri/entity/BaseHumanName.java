package uk.nhs.careconnect.ri.entity;

import org.hl7.fhir.instance.model.HumanName;

import javax.persistence.Column;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.MappedSuperclass;

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


}
