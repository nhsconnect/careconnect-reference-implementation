package uk.nhs.careconnect.ri.database.entity.list;

import uk.nhs.careconnect.ri.database.entity.BaseReferenceItem;
import uk.nhs.careconnect.ri.database.entity.Terminology.ConceptEntity;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name="ListItem", uniqueConstraints= @UniqueConstraint(name="PK_LIST_ITEM", columnNames={"LIST_ITEM_ID"})
		,indexes = {}
		)
public class ListItem extends BaseReferenceItem {

	public ListItem() {
	}

	private static final int MAX_DESC_LENGTH = 512;

	@Id 
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name= "LIST_ITEM_ID")
    private Long itemId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name="FLAG_CONCEPT_ID",foreignKey= @ForeignKey(name="FK_LIST_FLAG_CONCEPT_ID"))
	private ConceptEntity flag;

	@Column(name="_deleted")
	private Boolean itemDeleted;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "ITEM_DATE")
	private Date itemDateTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn (name = "LIST_ID",foreignKey= @ForeignKey(name="FK_LIST_ITEM_LIST_ID"))
	private ListEntity list;

	@Override
	public Long getId() {
		return null;
	}

	public static int getMaxDescLength() {
		return MAX_DESC_LENGTH;
	}

	public Long getItemId() {
		return itemId;
	}

	public void setItemId(Long itemId) {
		this.itemId = itemId;
	}

	public ListEntity getList() {
		return list;
	}

	public void setList(ListEntity list) {
		this.list = list;
	}

	public ConceptEntity getFlag() {
		return flag;
	}

	public void setFlag(ConceptEntity flag) {
		this.flag = flag;
	}

	public Boolean getItemDeleted() {
		return itemDeleted;
	}

	public void setItemDeleted(Boolean itemDeleted) {
		this.itemDeleted = itemDeleted;
	}

	public Date getItemDateTime() {
		return itemDateTime;
	}

	public void setItemDateTime(Date itemDateTime) {
		this.itemDateTime = itemDateTime;
	}
}
