package uk.nhs.careconnect.ri.entity.Terminology;

import uk.nhs.careconnect.ri.entity.BaseResource;

import javax.persistence.*;

@Table(name="System", uniqueConstraints= {
		@UniqueConstraint(name="IDX_CS_SYSTEM", columnNames= {"SYSTEM_URI"})
	})
@Entity()
public class SystemEntity extends BaseResource {

	@Id()
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "SYSTEM_ID")
	private Long myPid;

	public Long getPID()
	{
		return this.myPid;
	}

	
	@Column(name="SYSTEM_URI", nullable=false)
	private String codeSystemUri;

	public String getCodeSystemUri() {
		return codeSystemUri;
	}
	public void setCodeSystemUri(String theCodeSystemUri) {
		codeSystemUri = theCodeSystemUri;
	}


	@Column(name="name", nullable=true)
	private String name;

	public String getName() {
		return name;
	}
	public void setName(String theName) {
		name = theName;
	}


}
