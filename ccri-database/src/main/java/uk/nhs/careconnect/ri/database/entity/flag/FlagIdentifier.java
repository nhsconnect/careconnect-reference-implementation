package uk.nhs.careconnect.ri.database.entity.flag;

import uk.nhs.careconnect.ri.database.entity.BaseIdentifier;

import javax.persistence.*;

@Entity
@Table(name="FlagIdentifier", uniqueConstraints= @UniqueConstraint(name="PK_FLAG_IDENTIFIER", columnNames={"FLAG_IDENTIFIER_ID"})
		,indexes = {}
		)
public class FlagIdentifier extends BaseIdentifier {

	public FlagIdentifier() {
	}
    public FlagIdentifier(FlagEntity flag) {
		this.flag = flag;
	}


	@Id 
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name= "FLAG_IDENTIFIER_ID")
    private Long identifierId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn (name = "FLAG_ID",foreignKey= @ForeignKey(name="FK_FLAG_IDENTIFIER_FLAG_ID"))

    private FlagEntity flag;


	public Long getIdentifierId() { return identifierId; }
	public void setIdentifierId(Long identifierId) { this.identifierId = identifierId; }

	public FlagEntity getFlag() {
		return flag;
	}

	public void setFlag(FlagEntity flag) {
		this.flag = flag;
	}


}
