package uk.nhs.careconnect.ri.database.entity.namingSystem;


import org.hl7.fhir.dstu3.model.NamingSystem;

import javax.persistence.*;
import java.util.Date;


@Entity
@Table(name="NamingSystemGroup", uniqueConstraints= @UniqueConstraint(name="PK_NAMING_SYSTEM_UNIQUE", columnNames={"NAMING_SYSTEM_UNIQUE_ID"})
		)
public class NamingSystemUniqueId {

	public NamingSystemUniqueId() {

	}

	public NamingSystemUniqueId(NamingSystemEntity conceptmapEntity) {
		this.namingSystem = conceptmapEntity;
	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name= "NAMING_SYSTEM_UNIQUE_ID")
	private Long groupId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn (name = "NAMING_SYSTEM_ID",foreignKey= @ForeignKey(name="FK_NAMING_SYSTEM_NAMING_SYSTEM_UNIQUE"))
	private NamingSystemEntity namingSystem;

	@Enumerated(EnumType.ORDINAL)
	@Column(name="identifierType", nullable = false)
	NamingSystem.NamingSystemIdentifierType identifierType;

	@Column(name = "_VALUE")
	private String _value;

	@Column(name = "PREFERRED")
	private String preferred;

	@Column(name = "COMMENT")
	private String comment;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "PERIOD_START")
	private Date periodStart;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "PERIOD_END")
	private Date periodEnd;

	public Long getGroupId() {
		return groupId;
	}

	public void setGroupId(Long groupId) {
		this.groupId = groupId;
	}

	public NamingSystemEntity getNamingSystem() {
		return namingSystem;
	}

	public void setNamingSystem(NamingSystemEntity namingSystem) {
		this.namingSystem = namingSystem;
	}

	public NamingSystem.NamingSystemIdentifierType getIdentifierType() {
		return identifierType;
	}

	public void setIdentifierType(NamingSystem.NamingSystemIdentifierType identifierType) {
		this.identifierType = identifierType;
	}

	public String get_value() {
		return _value;
	}

	public void set_value(String _value) {
		this._value = _value;
	}

	public String getPreferred() {
		return preferred;
	}

	public void setPreferred(String preferred) {
		this.preferred = preferred;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public Date getPeriodStart() {
		return periodStart;
	}

	public void setPeriodStart(Date periodStart) {
		this.periodStart = periodStart;
	}

	public Date getPeriodEnd() {
		return periodEnd;
	}

	public void setPeriodEnd(Date periodEnd) {
		this.periodEnd = periodEnd;
	}
}
