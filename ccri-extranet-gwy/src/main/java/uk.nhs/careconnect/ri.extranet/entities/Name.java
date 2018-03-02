package mayfieldis.careconnect.nosql.entities;


import org.hl7.fhir.dstu3.model.HumanName;

public class Name {


    private String prefix;


    private String givenName;

    private String familyName;


    private String suffix;

    private HumanName.NameUse nameUse;

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

    public HumanName.NameUse getNameUse() {
        return nameUse;
    }

    public void setNameUse(HumanName.NameUse nameUse) {
        this.nameUse = nameUse;
    }
}
