package uk.nhs.careconnect.ri.database.entity.practitioner;

import uk.nhs.careconnect.ri.database.entity.BaseIdentifier;

import javax.persistence.*;


@Entity
@Table(name="PractitionerRoleIdentifier",
		uniqueConstraints= @UniqueConstraint(name="PK_PRACTITIONER_ROLE_IDENTIFIER", columnNames={"PRACTITIONER_ROLE_IDENTIFIER_ID"})
		,indexes =
		{
				@Index(name = "IDX_PRACTITIONER_ROLE_IDENTIFER", columnList="IDENTIFIER_VALUE,SYSTEM_ID"),
				@Index(name = "IDX_PRACTITIONER_ROLE_PRACTITIONER_ROLE_ID", columnList="PRACTITIONER_ROLE_ID")

		})
public class PractitionerRoleIdentifier extends BaseIdentifier {

	public PractitionerRoleIdentifier() {

	}

	public PractitionerRoleIdentifier(PractitionerRole practitionerRole) {
		this.practitionerRole = practitionerRole;
	}
	@Id 
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name= "PRACTITIONER_ROLE_IDENTIFIER_ID")
	private Integer identifierId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn (name = "PRACTITIONER_ROLE_ID",foreignKey= @ForeignKey(name="FK_PRACTITIONER_ROLE_PRACTITIONER_ROLE_IDENTIFIER"))
	private PractitionerRole practitionerRole;


    public Integer getIdentifierId() { return identifierId; }
	public void setIdentifierId(Integer identifierId) { this.identifierId = identifierId; }

	public PractitionerRole getPractitionerRole() {
	        return this.practitionerRole;
	}
	public void setPractitionerRole(PractitionerRole practitionerRole) {
	        this.practitionerRole = practitionerRole;
	}


}
