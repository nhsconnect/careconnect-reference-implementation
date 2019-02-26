package uk.nhs.careconnect.ri.database.entity.namingSystem;

import uk.nhs.careconnect.ri.database.entity.BaseContactPoint;
import uk.nhs.careconnect.ri.database.entity.namingSystem.NamingSystemEntity;

import javax.persistence.*;


@Entity
@Table(name="NamingSystemTelecom", uniqueConstraints= @UniqueConstraint(name="PK_NAMING_SYSTEM_TELECOM", columnNames={"NAMING_SYSTEM_TELECOM_ID"})
		,indexes =
		{
				@Index(name = "IDX_NAMING_SYSTEM_TELECOM", columnList="CONTACT_VALUE,SYSTEM_ID"),
				@Index(name = "IDX_NAMING_SYSTEM_TELECOM_NAMING_SYSTEM_ID", columnList="NAMING_SYSTEM_ID")
		})
public class NamingSystemTelecom extends BaseContactPoint {

	public NamingSystemTelecom() {

	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name= "NAMING_SYSTEM_TELECOM_ID")
	private Long telecomId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn (name = "NAMING_SYSTEM_ID",foreignKey= @ForeignKey(name="FK_NAMING_SYSTEM_TELECOM_NAMING_SYSTEM_ID"))
	private NamingSystemEntity namingSystem;

	public Long getTelecomId() {
		return telecomId;
	}

	public void setTelecomId(Long telecomId) {
		this.telecomId = telecomId;
	}

	public NamingSystemEntity getNamingSystem() {
		return namingSystem;
	}

	public void setNamingSystem(NamingSystemEntity namingSystem) {
		this.namingSystem = namingSystem;
	}
}
