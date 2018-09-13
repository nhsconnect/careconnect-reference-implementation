package uk.nhs.careconnect.ri.database.entity.composition;

import uk.nhs.careconnect.ri.database.entity.BaseIdentifier;

import javax.persistence.*;


@Entity
@Table(name="CompositionIdentifier", uniqueConstraints= @UniqueConstraint(name="PK_COMPOSITION_IDENTIFIER", columnNames={"COMPOSITION_IDENTIFIER_ID"})
		,indexes =
		{
				@Index(name = "IDX_COMPOSITION_IDENTIFER", columnList="IDENTIFIER_VALUE,SYSTEM_ID")

		})
public class CompositionIdentifier extends BaseIdentifier {

	public CompositionIdentifier() {

	}

	public CompositionIdentifier(CompositionEntity composition) {
		this.composition = composition;
	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name= "COMPOSITION_IDENTIFIER_ID")
	private Long identifierId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn (name = "COMPOSITION_ID",foreignKey= @ForeignKey(name="FK_COMPOSITION_COMPOSITION_IDENTIFIER"))
	private CompositionEntity composition;


    public Long getIdentifierId() { return identifierId; }
	public void setIdentifierId(Long identifierId) { this.identifierId = identifierId; }

	public CompositionEntity getComposition() {
	        return this.composition;
	}

	public void setComposition(CompositionEntity composition) {
	        this.composition = composition;
	}




}
