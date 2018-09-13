package uk.nhs.careconnect.ri.database.entity.consent;

import uk.nhs.careconnect.ri.database.entity.Terminology.ConceptEntity;
import uk.nhs.careconnect.ri.database.entity.BaseReferenceItem;

import javax.persistence.*;

@Entity
@Table(name="ConsentActor", uniqueConstraints= @UniqueConstraint(name="PK_CONSENT_ACTOR", columnNames={"CONSENT_ACTOR_ID"})
		,indexes = {}
		)
public class ConsentActor extends BaseReferenceItem {

	public ConsentActor() {
	}

	private static final int MAX_DESC_LENGTH = 512;

	@Id 
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name= "CONSENT_ACTOR_ID")
    private Long infoId;
	
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn (name = "CONSENT_ID",foreignKey= @ForeignKey(name="FK_CONSENT_ACTOR_CONSENT_ID"))
	private ConsentEntity consent;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name="ROLE_CONCEPT_ID",nullable = true,foreignKey= @ForeignKey(name="FK_CONSENT_ROLE_CONCEPT_ID"))
	private ConceptEntity roleCode;

	@Override
	public Long getId() {
		return null;
	}

	public static int getMaxDescLength() {
		return MAX_DESC_LENGTH;
	}

	public ConceptEntity getRoleCode() {
		return roleCode;
	}

	public void setRoleCode(ConceptEntity roleCode) {
		this.roleCode = roleCode;
	}

	public Long getInfoId() {
		return infoId;
	}

	public void setInfoId(Long infoId) {
		this.infoId = infoId;
	}

	public ConsentEntity getConsent() {
		return consent;
	}

	public void setConsent(ConsentEntity consent) {
		this.consent = consent;
	}



}
