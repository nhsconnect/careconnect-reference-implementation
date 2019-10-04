package uk.nhs.careconnect.ri.database.entity;


import uk.nhs.careconnect.ri.database.entity.codeSystem.ConceptEntity;
import javax.persistence.*;


@MappedSuperclass
public class BaseCodeableConcept extends BaseResource {

    private static final int MAX_DESC_LENGTH = 4096;

    @Column(name = "CONCEPT_TEXT", length = MAX_DESC_LENGTH)
    private String conceptText;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CONCEPT_CODE")
    private ConceptEntity conceptCode;

    @Column(name = "listOrder")
    private Integer order;

    public static int getMaxDescLength() {
        return MAX_DESC_LENGTH;
    }

    public String getConceptText() {
        return conceptText;
    }

    public void setConceptText(String conceptText) {
        this.conceptText = conceptText;
    }

    public ConceptEntity getConceptCode() {
        return conceptCode;
    }

    public void setConceptCode(ConceptEntity conceptCode) {
        this.conceptCode = conceptCode;
    }

    public Integer getOrder() {
        return order;
    }

    public void setOrder(Integer order) {
        this.order = order;
    }

    @Override
    public Long getId() {
        return null;
    }
}
