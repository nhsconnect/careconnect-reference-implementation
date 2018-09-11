package uk.nhs.careconnect.ri.database.entity.immunisation;

import uk.nhs.careconnect.ri.database.entity.BaseIdentifier;

import javax.persistence.*;


@Entity
@Table(name="ImmunizationIdentifier", uniqueConstraints= @UniqueConstraint(name="PK_IMMUNISATION_IDENTIFIER", columnNames={"IMMUNISATION_IDENTIFIER_ID"})
		,indexes =
		{
				@Index(name = "IDX_IMMUNISATION_IDENTIFER", columnList="IDENTIFIER_VALUE,SYSTEM_ID")

		})
public class ImmunisationIdentifier extends BaseIdentifier {

	public ImmunisationIdentifier() {

	}

	public ImmunisationIdentifier(ImmunisationEntity Immunisation) {
		this.immunisation = Immunisation;
	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name= "IMMUNISATION_IDENTIFIER_ID")
	private Long identifierId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn (name = "IMMUNISATION_ID",foreignKey= @ForeignKey(name="FK_IMMUNISATION_IMMUNISATION_IDENTIFIER"))
	private ImmunisationEntity immunisation;


    public Long getIdentifierId() { return identifierId; }
	public void setIdentifierId(Long identifierId) { this.identifierId = identifierId; }

	public ImmunisationEntity getImmunisation() {
	        return this.immunisation;
	}

	public void setImmunisation(ImmunisationEntity immunisation) {
	        this.immunisation = immunisation;
	}




}
