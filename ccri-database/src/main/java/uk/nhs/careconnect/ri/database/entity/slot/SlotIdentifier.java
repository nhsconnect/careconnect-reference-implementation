package uk.nhs.careconnect.ri.database.entity.slot;

import uk.nhs.careconnect.ri.database.entity.BaseIdentifier;
import uk.nhs.careconnect.ri.database.entity.condition.ConditionEntity;

import javax.persistence.*;


@Entity
@Table(name="SlotIdentifier", uniqueConstraints= @UniqueConstraint(name="PK_SLOT_IDENTIFIER", columnNames={"SLOT_IDENTIFIER_ID"})
		)
public class SlotIdentifier extends BaseIdentifier {

	public SlotIdentifier() {

	}

	public SlotIdentifier(SlotEntity slot) {
		this.slot = slot;
	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name= "SLOT_IDENTIFIER_ID")
	private Long identifierId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn (name = "SLOT_ID",foreignKey= @ForeignKey(name="FK_SLOT_SLOT_IDENTIFIER"))
	private SlotEntity slot;

	public Long getIdentifierId() { return identifierId; }

	public void setIdentifierId(Long identifierId) { this.identifierId = identifierId; }

	public SlotEntity getSlot() { return slot; }

	public void setSlot(SlotEntity slot) { this.slot = slot; }
}
