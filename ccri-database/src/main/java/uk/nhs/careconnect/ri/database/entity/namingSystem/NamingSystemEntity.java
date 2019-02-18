package uk.nhs.careconnect.ri.database.entity.namingSystem;

import org.hl7.fhir.dstu3.model.Enumerations;
import org.hl7.fhir.dstu3.model.NamingSystem;
import uk.nhs.careconnect.ri.database.entity.BaseResource;
import uk.nhs.careconnect.ri.database.entity.codeSystem.ConceptEntity;

import javax.persistence.*;
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

	@Column(name = "PUBLISHER")
	private String publisher;

	@OneToMany(mappedBy="namingSystem", targetEntity= NamingSystemTelecom.class)
	private List<NamingSystemTelecom> contacts;

	@Column(name = "RESPONSIBLE")
	private String responsible;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name="TYPE_CONCEPT_ID")
	private ConceptEntity _type;

	@Column(name = "DESCRIPTION")
	private String description;

	// useContext .. implement if required

	// jurisdiction ... hard code to UK


	@OneToMany(mappedBy="namingSystem", targetEntity= NamingSystemUniqueId.class)
	private List<NamingSystemUniqueId> namingSystemUniqueIds;



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

	public String getPublisher() {
		return publisher;
	}

	public void setPublisher(String publisher) {
		this.publisher = publisher;
	}

	
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}


	public List<NamingSystemUniqueId> getNamingSystemUniqueIds() {
		return namingSystemUniqueIds;
	}

	public void setNamingSystemUniqueIds(List<NamingSystemUniqueId> namingSystemUniqueIds) {
		this.namingSystemUniqueIds = namingSystemUniqueIds;
	}

	public NamingSystem.NamingSystemType getKind() {
		return kind;
	}

	public void setKind(NamingSystem.NamingSystemType kind) {
		this.kind = kind;
	}

	public List<NamingSystemTelecom> getContacts() {
		return contacts;
	}

	public void setContacts(List<NamingSystemTelecom> contacts) {
		this.contacts = contacts;
	}

	public String getResponsible() {
		return responsible;
	}

	public void setResponsible(String responsible) {
		this.responsible = responsible;
	}

	public ConceptEntity get_type() {
		return _type;
	}

	public void set_type(ConceptEntity _type) {
		this._type = _type;
	}

}
