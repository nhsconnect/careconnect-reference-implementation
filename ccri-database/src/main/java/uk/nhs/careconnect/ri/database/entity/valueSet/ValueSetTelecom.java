package uk.nhs.careconnect.ri.database.entity.valueSet;

import uk.nhs.careconnect.ri.database.entity.BaseContactPoint;
import uk.nhs.careconnect.ri.database.entity.valueSet.ValueSetEntity;

import javax.persistence.*;


@Entity
@Table(name="ValueSetTelecom", uniqueConstraints= @UniqueConstraint(name="PK_VALUESET_TELECOM", columnNames={"VALUESET_TELECOM_ID"})
		,indexes =
		{
				@Index(name = "IDX_VALUESET_TELECOM", columnList="CONTACT_VALUE,SYSTEM_ID"),
				@Index(name = "IDX_VALUESET_TELECOM_VALUESET_ID", columnList="VALUESET_ID")
		})
public class ValueSetTelecom extends BaseContactPoint {

	public ValueSetTelecom() {

	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name= "VALUESET_TELECOM_ID")
	private Long telecomId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn (name = "VALUESET_ID",foreignKey= @ForeignKey(name="FK_VALUESET_TELECOM_VALUESET_ID"))
	private ValueSetEntity valueSet;

	public Long getTelecomId() {
		return telecomId;
	}

	public void setTelecomId(Long telecomId) {
		this.telecomId = telecomId;
	}

	public ValueSetEntity getValueSet() {
		return valueSet;
	}

	public void setValueSet(ValueSetEntity valueSet) {
		this.valueSet = valueSet;
	}
}
