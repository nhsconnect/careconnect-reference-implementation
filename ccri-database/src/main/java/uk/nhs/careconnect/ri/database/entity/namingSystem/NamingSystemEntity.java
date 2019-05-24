package uk.nhs.careconnect.ri.database.entity.namingSystem;

import org.hl7.fhir.dstu3.model.Enumerations;
import org.hl7.fhir.dstu3.model.NamingSystem;
import uk.nhs.careconnect.ri.database.entity.BaseResource;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "NamingSystem")
public class NamingSystemEntity extends BaseResource {

	/*

Does not currently include target dependsOn TODO not required at present

ditto for target product

	 */

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="NAMING_SYSTEM_ID")
	private Long id;

	@Column(name = "NAME")
	private String name;

	@Enumerated(EnumType.ORDINAL)
	@Column(name="status", nullable = false)
	Enumerations.PublicationStatus status;

	@Enumerated(EnumType.ORDINAL)
	@Column(name="kind", nullable = false)
	NamingSystem.NamingSystemType kind;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "CHANGE_DATE")
	private Date changedDate;



	@OneToMany(mappedBy="namingSystem", targetEntity= NamingSystemUniqueId.class)
	private List<NamingSystemUniqueId> namingSystemUniqueIds;


	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn (name = "REPLACED_BY_NAMING_SYSTEM_ID",foreignKey= @ForeignKey(name="FK_NAMING_SYSTEM_REPLACED_BY"))
	private NamingSystemEntity replacedBy;


	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}


	public Enumerations.PublicationStatus getStatus() {
		return status;
	}

	public void setStatus(Enumerations.PublicationStatus status) {
		this.status = status;
	}


	public Date getChangedDate() {
		return changedDate;
	}

	public void setChangedDate(Date changedDate) {
		this.changedDate = changedDate;
	}

	public NamingSystem.NamingSystemType getKind() {
		return kind;
	}

	public void setKind(NamingSystem.NamingSystemType kind) {
		this.kind = kind;
	}



	public NamingSystemEntity getReplacedBy() {
		return replacedBy;
	}

	public void setReplacedBy(NamingSystemEntity replacedBy) {
		this.replacedBy = replacedBy;
	}

	public List<NamingSystemUniqueId> getNamingSystemUniqueIds() {
		if (namingSystemUniqueIds  == null) {
			namingSystemUniqueIds  = new ArrayList<>();
		}
		return namingSystemUniqueIds;
	}

	public void setNamingSystemUniqueIds(List<NamingSystemUniqueId> namingSystemUniqueIds) {
		this.namingSystemUniqueIds = namingSystemUniqueIds;
	}
}
