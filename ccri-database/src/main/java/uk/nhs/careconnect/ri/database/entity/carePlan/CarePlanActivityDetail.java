package uk.nhs.careconnect.ri.database.entity.carePlan;

import org.hl7.fhir.dstu3.model.CarePlan;
import uk.nhs.careconnect.ri.database.entity.Terminology.ConceptEntity;

import javax.persistence.*;

@Entity
@Table(name="CarePlanActivityDetail", uniqueConstraints= @UniqueConstraint(name="PK_CAREPLAN_ACTIVITY_DETAIL", columnNames={"CAREPLAN_ACTIVITY_DETAIL_ID"})
		,indexes = {}
		)
public class CarePlanActivityDetail {

	public CarePlanActivityDetail() {
	}
    public CarePlanActivityDetail(CarePlanActivity carePlanActivity) {
		this.carePlanActivity = carePlanActivity;
	}


	@Id 
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name= "CAREPLAN_ACTIVITY_DETAIL_ID")
    private Long carePlanActivityDetailId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn (name = "CAREPLAN_ACTIVITY_ID",foreignKey= @ForeignKey(name="FK_CAREPLAN_ACTIVITY_DETAIL_CAREPLAN_ACTIVITY_ID"))
    private CarePlanActivity carePlanActivity;


	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name="CATEGORY_CONCEPT_ID",nullable = true,foreignKey= @ForeignKey(name="FK_CAREPLAN_ACTIVITY_DETAIL_CATEGORY_CONCEPT_ID"))
	private ConceptEntity category;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name="CODE_CONCEPT_ID",nullable = true,foreignKey= @ForeignKey(name="FK_CAREPLAN_ACTIVITY_DETAIL_CODE_CONCEPT_ID"))
	private ConceptEntity code;

	@Enumerated(EnumType.ORDINAL)
	@Column(name="status")
	CarePlan.CarePlanActivityStatus status;

	@Column(name="DESCRIPTION")
	String description;

	public Long getCarePlanActivityDetailId() {
		return carePlanActivityDetailId;
	}

	public void setCarePlanActivityDetailId(Long carePlanActivityDetailId) {
		this.carePlanActivityDetailId = carePlanActivityDetailId;
	}

	public CarePlanActivity getCarePlanActivity() {
		return carePlanActivity;
	}

	public void setCarePlanActivity(CarePlanActivity carePlanActivity) {
		this.carePlanActivity = carePlanActivity;
	}

	public ConceptEntity getCode() {
		return code;
	}

	public void setCode(ConceptEntity code) {
		this.code = code;
	}

	public ConceptEntity getCategory() {
		return category;
	}

	public void setCategory(ConceptEntity category) {
		this.category = category;
	}

	public CarePlan.CarePlanActivityStatus getStatus() {
		return status;
	}

	public void setStatus(CarePlan.CarePlanActivityStatus status) {
		this.status = status;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
}
