package uk.nhs.careconnect.ri.database.entity.encounter;

import uk.nhs.careconnect.ri.database.entity.BaseExtension;
import uk.nhs.careconnect.ri.database.entity.patient.PatientEntity;

import javax.persistence.*;

@Entity
@Table(name = "EncounterExtension")
public class EncounterExtension extends BaseExtension {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="ENCOUNTER_EXTENSION_ID")
    private Long extensionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ENCOUNTER_ID",foreignKey= @ForeignKey(name="FK_ENCOUNTER_EXTENSION_ENCOUNTER"))
    private EncounterEntity encounter;

    public Long getExtensionId() {
        return extensionId;
    }

    public void setExtensionId(Long extensionId) {
        this.extensionId = extensionId;
    }

    public EncounterEntity getEncounter() {
        return encounter;
    }

    public void setEncounter(EncounterEntity encounter) {
        this.encounter = encounter;
    }
}
