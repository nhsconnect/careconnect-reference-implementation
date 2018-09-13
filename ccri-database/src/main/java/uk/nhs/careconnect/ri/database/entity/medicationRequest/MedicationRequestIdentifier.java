package uk.nhs.careconnect.ri.database.entity.medicationRequest;

import uk.nhs.careconnect.ri.database.entity.BaseIdentifier;

import javax.persistence.*;


@Entity
@Table(name="MedicationRequestIdentifier", uniqueConstraints= @UniqueConstraint(name="PK_PRESCRIPTION_IDENTIFIER", columnNames={"PRESCRIPTION_IDENTIFIER_ID"})
		,indexes =
		{
				@Index(name = "IDX_PRESCRIPTION_IDENTIFER", columnList="IDENTIFIER_VALUE,SYSTEM_ID")

		})
public class MedicationRequestIdentifier extends BaseIdentifier {

	public MedicationRequestIdentifier() {

	}

	public MedicationRequestIdentifier(MedicationRequestEntity prescription) {
		this.prescription = prescription;
	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name= "PRESCRIPTION_IDENTIFIER_ID")
	private Long identifierId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn (name = "PRESCRIPTION_ID",foreignKey= @ForeignKey(name="FK_PRESCRIPTION_PRESCRIPTION_IDENTIFIER"))
	private MedicationRequestEntity prescription;


    public Long getIdentifierId() { return identifierId; }
	public void setIdentifierId(Long identifierId) { this.identifierId = identifierId; }

	public MedicationRequestEntity getMedicationRequest() {
	        return this.prescription;
	}

	public void setMedicationRequest(MedicationRequestEntity prescription) {
	        this.prescription = prescription;
	}




}
