package uk.nhs.careconnect.itksrp.index;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement(name="vocabularyIndex")
@XmlAccessorType(XmlAccessType.FIELD)
public class VocabularyIndex {


    private String vocabularyIndex;

    @XmlAttribute
    private String vocabularyName;


    public String getVocabularyName() {
        return vocabularyName;
    }

    @XmlElement
    List<vocabulary> vocabulary = new ArrayList<>();;

    public List<vocabulary> getVocabulary() {
        return vocabulary;
    }

    public void setVocabulary(List<vocabulary> vocabulary) {
        this.vocabulary = vocabulary;
    }
}
