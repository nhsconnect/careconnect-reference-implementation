package uk.nhs.careconnect.ri.database.entity.Terminology;

import org.hibernate.annotations.CacheConcurrencyStrategy;
import uk.nhs.careconnect.ri.database.entity.BaseResource;

import javax.persistence.*;

@Table(name="System", uniqueConstraints= {
		@UniqueConstraint(name="IDX_CS_SYSTEM", columnNames= {"SYSTEM_URI"})
	})
@Entity()
@Cacheable
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class SystemEntity extends BaseResource {

	@Id()
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "SYSTEM_ID")
	private Long myPid;

	public Long getId()
	{
		return this.myPid;
	}

	
	@Column(name="SYSTEM_URI", nullable=false)
	private String codeSystemUri;

	public String getUri() {
		return codeSystemUri;
	}
	public void setUri(String theCodeSystemUri) {
		codeSystemUri = theCodeSystemUri;
	}


	@Column(name="SYSTEM_NAME", nullable=true)
	private String name;

	public String getName() {
		return name;
	}
	public void setName(String theName) {
		name = theName;
	}


}
