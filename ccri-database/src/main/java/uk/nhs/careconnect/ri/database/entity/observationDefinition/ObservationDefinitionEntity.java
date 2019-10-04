package uk.nhs.careconnect.ri.database.entity.observationDefinition;

import org.hl7.fhir.dstu3.model.Observation;
import org.hl7.fhir.r4.model.ObservationDefinition;
import uk.nhs.careconnect.ri.database.entity.BaseResource;
import uk.nhs.careconnect.ri.database.entity.codeSystem.ConceptEntity;
import uk.nhs.careconnect.ri.database.entity.encounter.EncounterEntity;
import uk.nhs.careconnect.ri.database.entity.observation.*;
import uk.nhs.careconnect.ri.database.entity.patient.PatientEntity;
import uk.nhs.careconnect.ri.database.entity.valueSet.ValueSetEntity;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "ObservationDefinition", indexes = {

})
public class ObservationDefinitionEntity extends BaseResource {

    private static final int MAX_DESC_LENGTH = 4096;

    public enum ObservationType  { component, valueQuantity }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="OBSERVATION_DEF_ID")
    private Long id;

    @OneToMany(mappedBy="observationDefinition", targetEntity=ObservationDefinitionCategory.class)
    private Set<ObservationDefinitionCategory> categories = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="CODE_CONCEPT_ID",nullable = false,foreignKey= @ForeignKey(name="FK_OBSERVATION_CODE_CONCEPT_ID"))
    private ConceptEntity code;

    @Column(name="CODE_TEXT", length = MAX_DESC_LENGTH)
    private String codeText;

    @OneToMany(mappedBy="observationDefinition", targetEntity= ObservationDefinitionIdentifier.class)
    private Set<ObservationDefinitionIdentifier> identifiers = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="VALID_VALUESET_ID",foreignKey= @ForeignKey(name="FK_OBSERVATION_DEF_VALID_VS_ID"))
    private ValueSetEntity validValueSet;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="NORMAL_VALUESET_ID",foreignKey= @ForeignKey(name="FK_OBSERVATION_DEF_NORMAL_VS_ID"))
    private ValueSetEntity normalValueSet;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="ABNORMAL_VALUESET_ID",foreignKey= @ForeignKey(name="FK_OBSERVATION_DEF_ABNORMAL_VS_ID"))
    private ValueSetEntity abnormalValueSet;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="CRITICAL_VALUESET_ID",foreignKey= @ForeignKey(name="FK_OBSERVATION_DEF_CRITICAL_VS_ID"))
    private ValueSetEntity criticalValueSet;

    public static int getMaxDescLength() {
        return MAX_DESC_LENGTH;
    }

    @Override
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ConceptEntity getCode() {
        return code;
    }

    public void setCode(ConceptEntity code) {
        this.code = code;
    }

    public String getCodeText() {
        return codeText;
    }

    public void setCodeText(String codeText) {
        this.codeText = codeText;
    }

    public Set<ObservationDefinitionIdentifier> getIdentifiers() {
        return identifiers;
    }

    public void setIdentifiers(Set<ObservationDefinitionIdentifier> identifiers) {
        this.identifiers = identifiers;
    }

    public Set<ObservationDefinitionCategory> getCategories() {
        return categories;
    }

    public void setCategories(Set<ObservationDefinitionCategory> categories) {
        this.categories = categories;
    }

    public ValueSetEntity getValidValueSet() {
        return validValueSet;
    }

    public void setValidValueSet(ValueSetEntity validValueSet) {
        this.validValueSet = validValueSet;
    }

    public ValueSetEntity getNormalValueSet() {
        return normalValueSet;
    }

    public void setNormalValueSet(ValueSetEntity normalValueSet) {
        this.normalValueSet = normalValueSet;
    }

    public ValueSetEntity getAbnormalValueSet() {
        return abnormalValueSet;
    }

    public void setAbnormalValueSet(ValueSetEntity abnormalValueSet) {
        this.abnormalValueSet = abnormalValueSet;
    }

    public ValueSetEntity getCriticalValueSet() {
        return criticalValueSet;
    }

    public void setCriticalValueSet(ValueSetEntity criticalValueSet) {
        this.criticalValueSet = criticalValueSet;
    }
}
