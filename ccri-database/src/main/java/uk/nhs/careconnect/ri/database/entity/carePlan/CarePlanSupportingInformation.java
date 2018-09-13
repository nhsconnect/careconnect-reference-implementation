package uk.nhs.careconnect.ri.database.entity.carePlan;

import uk.nhs.careconnect.ri.database.entity.BaseReferenceItem;

import javax.persistence.*;

@Entity
@Table(name="CarePlanSupportingInformation", uniqueConstraints= @UniqueConstraint(name="PK_CARE_SUPP_INFO", columnNames={"CARE_SUPP_INFO_ID"})
		,indexes = {}
		)
public class CarePlanSupportingInformation extends BaseReferenceItem {

	public CarePlanSupportingInformation() {
	}

	private static final int MAX_DESC_LENGTH = 512;

	@Id 
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name= "CARE_SUPP_INFO_ID")
    private Long infoId;
	
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn (name = "CARE_PLAN_ID",foreignKey= @ForeignKey(name="FK_CARE_SUPP_INFO_CARE_PLAN_ID"))
	private CarePlanEntity carePlan;

	@Override
	public Long getId() {
		return null;
	}

	public static int getMaxDescLength() {
		return MAX_DESC_LENGTH;
	}

	public Long getInfoId() {
		return infoId;
	}

	public void setInfoId(Long infoId) {
		this.infoId = infoId;
	}

	public CarePlanEntity getCarePlan() {
		return carePlan;
	}

	public void setCarePlan(CarePlanEntity carePlan) {
		this.carePlan = carePlan;
	}



}
