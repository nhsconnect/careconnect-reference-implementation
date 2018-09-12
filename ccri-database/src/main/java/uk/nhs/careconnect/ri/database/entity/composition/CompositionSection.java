package uk.nhs.careconnect.ri.database.entity.composition;

import org.hl7.fhir.instance.model.List_;

import org.hl7.fhir.dstu3.model.Narrative;
import uk.nhs.careconnect.ri.database.entity.Terminology.ConceptEntity;

import javax.persistence.*;

@Entity
@Table(name = "CompositionSection")
public class CompositionSection {

    private static final int MAX_DESC_LENGTH = 16384;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name= "COMPOSITION_SECTION_ID")
    private Long sectionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn (name = "COMPOSITION_ID",foreignKey= @ForeignKey(name="FK_COMPOSITION_COMPOSITION_SECTION"))
    private CompositionEntity composition;

    @Column
    private String title;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn (name = "CODE_CONCEPT_ID",foreignKey= @ForeignKey(name="FK_COMPOSITION_SECTION_CODE"))

    private ConceptEntity code;

    @Column(name="narrative", length=MAX_DESC_LENGTH, nullable=true)
    private String narrative;

    @Column(name="narrativeStatus", length=MAX_DESC_LENGTH, nullable=true)
    private Narrative.NarrativeStatus narrativeStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn (name = "ORDERBY_CONCEPT_ID",foreignKey= @ForeignKey(name="FK_COMPOSITION_SECTION_ORDERBY"))

    private ConceptEntity orderBy;

    @Column(name="mode")
    private List_.ListMode mode;

    public Long getSectionId() {
        return sectionId;
    }

    public void setSectionId(Long sectionId) {
        this.sectionId = sectionId;
    }

    public CompositionEntity getComposition() {
        return composition;
    }

    public void setComposition(CompositionEntity composition) {
        this.composition = composition;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public ConceptEntity getCode() {
        return code;
    }

    public void setCode(ConceptEntity code) {
        this.code = code;
    }

    public String getNarrative() {
        return narrative;
    }

    public void setNarrative(String narrative) {
        this.narrative = narrative;
    }

    public ConceptEntity getOrderBy() {
        return orderBy;
    }

    public void setOrderBy(ConceptEntity orderBy) {
        this.orderBy = orderBy;
    }

    public List_.ListMode getMode() {
        return mode;
    }

    public void setMode(List_.ListMode mode) {
        this.mode = mode;
    }

    public Narrative.NarrativeStatus getNarrativeStatus() {
        return narrativeStatus;
    }

    public void setNarrativeStatus(Narrative.NarrativeStatus narrativeStatus) {
        this.narrativeStatus = narrativeStatus;
    }
}
