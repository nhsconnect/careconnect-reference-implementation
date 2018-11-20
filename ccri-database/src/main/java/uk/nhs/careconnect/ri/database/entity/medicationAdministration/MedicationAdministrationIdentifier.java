package uk.nhs.careconnect.ri.database.entity.medicationAdministration;

import uk.nhs.careconnect.ri.database.entity.BaseIdentifier;

import javax.persistence.*;


@Entity
@Table(name="MedicationAdministrationIdentifier", uniqueConstraints= @UniqueConstraint(name="PK_ADMINISTRATION_IDENTIFIER", columnNames={"ADMINISTRATION_IDENTIFIER_ID"})
		,indexes =
		{
				@Index(name = "IDX_ADMINISTRATION_IDENTIFER", columnList="IDENTIFIER_VALUE,SYSTEM_ID")

		})
public class MedicationAdministrationIdentifier extends BaseIdentifier {

	public MedicationAdministrationIdentifier() {

	}

	public MedicationAdministrationIdentifier(MedicationAdministrationEntity administration) {
		this.administration = administration;
	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name= "ADMINISTRATION_IDENTIFIER_ID")
	private Long identifierId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn (name = "ADMINISTRATION_ID",foreignKey= @ForeignKey(name="FK_ADMINISTRATION_ADMINISTRATION_IDENTIFIER"))
	private MedicationAdministrationEntity administration;


    public Long getIdentifierId() { return identifierId; }
	public void setIdentifierId(Long identifierId) { this.identifierId = identifierId; }

	public MedicationAdministrationEntity getMedicationAdministration() {
	        return this.administration;
	}

	public void setMedicationAdministration(MedicationAdministrationEntity administration) {
	        this.administration = administration;
	}




}
