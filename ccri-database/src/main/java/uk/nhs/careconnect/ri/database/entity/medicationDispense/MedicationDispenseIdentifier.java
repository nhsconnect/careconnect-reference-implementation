package uk.nhs.careconnect.ri.database.entity.medicationDispense;

import uk.nhs.careconnect.ri.database.entity.BaseIdentifier;
import javax.persistence.*;


@Entity
@Table(name="MedicationDispenseIdentifier", uniqueConstraints= @UniqueConstraint(name="PK_DISPENSE_IDENTIFIER", columnNames={"DISPENSE_IDENTIFIER_ID"})
		,indexes =
		{
				@Index(name = "IDX_DISPENSE_IDENTIFER", columnList="IDENTIFIER_VALUE,SYSTEM_ID")

		})
public class MedicationDispenseIdentifier extends BaseIdentifier {

	public MedicationDispenseIdentifier() {

	}

	public MedicationDispenseIdentifier(MedicationDispenseEntity dispense) {
		this.dispense = dispense;
	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name= "DISPENSE_IDENTIFIER_ID")
	private Long identifierId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn (name = "DISPENSE_ID",foreignKey= @ForeignKey(name="FK_DISPENSE_DISPENSE_IDENTIFIER"))
	private MedicationDispenseEntity dispense;


    public Long getIdentifierId() { return identifierId; }
	public void setIdentifierId(Long identifierId) { this.identifierId = identifierId; }

	public MedicationDispenseEntity getMedicationDispense() {
	        return this.dispense;
	}

	public void setMedicationDispense(MedicationDispenseEntity dispense) {
	        this.dispense = dispense;
	}




}
