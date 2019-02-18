package uk.nhs.careconnect.ri.database.entity.codeSystem;

import uk.nhs.careconnect.ri.database.entity.BaseContactPoint;
import javax.persistence.*;


@Entity
@Table(name="CodeSystemTelecom", uniqueConstraints= @UniqueConstraint(name="PK_CODESYSTEM_TELECOM", columnNames={"CODESYSTEM_TELECOM_ID"})
		,indexes =
		{
				@Index(name = "IDX_CODESYSTEM_TELECOM", columnList="CONTACT_VALUE,SYSTEM_ID"),
				@Index(name = "IDX_CODESYSTEM_TELECOM_CODESYSTEM_ID", columnList="CODESYSTEM_ID")
		})
public class CodeSystemTelecom extends BaseContactPoint {

	public CodeSystemTelecom() {

	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name= "CODESYSTEM_TELECOM_ID")
	private Long telecomId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn (name = "CODESYSTEM_ID",foreignKey= @ForeignKey(name="FK_CODESYSTEM_TELECOM_CODESYSTEM_ID"))
	private CodeSystemEntity codeSystemEntity;

	public Long getTelecomId() {
		return telecomId;
	}

	public void setTelecomId(Long telecomId) {
		this.telecomId = telecomId;
	}

	public CodeSystemEntity getCodeSystem() {
		return codeSystemEntity;
	}

	public void setCodeSystem(CodeSystemEntity codeSystemEntity) {
		this.codeSystemEntity = codeSystemEntity;
	}
}
