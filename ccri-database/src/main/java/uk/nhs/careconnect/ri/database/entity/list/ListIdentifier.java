package uk.nhs.careconnect.ri.database.entity.list;

import uk.nhs.careconnect.ri.database.entity.BaseIdentifier;

import javax.persistence.*;

@Entity
@Table(name="ListIdentifier", uniqueConstraints= @UniqueConstraint(name="PK_LIST_IDENTIFIER", columnNames={"LIST_IDENTIFIER_ID"})
		,indexes = {}
		)
public class ListIdentifier extends BaseIdentifier {

	public ListIdentifier() {
	}
    public ListIdentifier(ListEntity list) {
		this.list = list;
	}


	@Id 
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name= "LIST_IDENTIFIER_ID")
    private Long identifierId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn (name = "LIST_ID",foreignKey= @ForeignKey(name="FK_LIST_IDENTIFIER_LIST_ID"))

    private ListEntity list;


	public Long getIdentifierId() { return identifierId; }
	public void setIdentifierId(Long identifierId) { this.identifierId = identifierId; }

	public ListEntity getList() {
		return list;
	}

	public void setList(ListEntity list) {
		this.list = list;
	}


}
