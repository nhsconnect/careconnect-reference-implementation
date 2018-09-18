package uk.nhs.careconnect.itksrp.index;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class vocabulary {


    @XmlAttribute
    private String version;

    @XmlAttribute
    private String name;

    @XmlAttribute
    private String id;

    @XmlAttribute
    private String memberCount;


    public String getId() {
        return id;
    }

    public String getMemberCount() {
        return memberCount;
    }

    public String getName() {
        return name;
    }

    public String getVersion() {
        return version;
    }


}
