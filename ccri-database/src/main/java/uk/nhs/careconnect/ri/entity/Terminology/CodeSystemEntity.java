package uk.nhs.careconnect.ri.entity.Terminology;

import org.hibernate.annotations.Immutable;
import uk.nhs.careconnect.ri.entity.BaseResource;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Collection;

@Table(name="CodeSystem", uniqueConstraints= {
		@UniqueConstraint(name="IDX_CS_CODESYSTEM", columnNames= {"CODE_SYSTEM_URI"})
	})
@Entity()
public class CodeSystemEntity extends BaseResource {

	@Id()
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "CODESYSTEM_ID")
	private Long myPid;
	public Long getPID()
	{
		return this.myPid;
	}

	
	@Column(name="CODE_SYSTEM_URI", nullable=false)
	private String codeSystemUri;
	public String getCodeSystemUri() {
		return codeSystemUri;
	}
	public void setCodeSystemUri(String theCodeSystemUri) {
		codeSystemUri = theCodeSystemUri;
	}


	@Column(name="name", nullable=false)
	private String name;
	public String getName() {
		return name;
	}
	public void setName(String theName) {
		name = theName;
	}
}
