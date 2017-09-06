package uk.nhs.careconnect.ri.entity.Terminology;

import javax.persistence.*;


@Entity
@Table(name="ValueSetContent", uniqueConstraints= @UniqueConstraint(name="PK_VALUESET_CONTENT", columnNames={"VALUESET_CONTENT_ID"}))
public class ValueSetContent {
	
	private static final int MAX_DESC_LENGTH = 400;
	
	public ValueSetContent() {
		
	}
	
	public ValueSetContent(ValueSetEntity valueSetEntity) {
		this.valueSetEntity = valueSetEntity;
	}
	@Id 
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name= "VALUESET_CONTENT_ID")
	public Integer getContentId() { return contentId; }
	public void setContentId(Integer contentId) { this.contentId = contentId; }
	private Integer contentId;
	
	private ConceptEntity codeCT;
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "codeCT", referencedColumnName = "CONCEPT_ID", foreignKey = @ForeignKey(name = "FK_VALUESET_CONTENT_TARGET_CONCEPT_PID"))
	public ConceptEntity getCodeCT() { return this.codeCT; }
	public void setCodeCT(ConceptEntity codeCT) { 	this.codeCT = codeCT; 	}
	
	
	private ValueSetEntity valueSetEntity;
	@ManyToOne
	@JoinColumn (name = "VALUESET_ID",foreignKey= @ForeignKey(name="FK_VALUESET_VALUESET_CONTENT"))
	public ValueSetEntity getValueSetEntity() {
	        return this.valueSetEntity;
	}
	public void setValueSetEntity(ValueSetEntity valueSetEntity) {
	        this.valueSetEntity = valueSetEntity;
	}
	
	@Column(name = "CODE", length = 100, nullable = false)
	private String myCode;
	public String getCode() {
		return myCode;
	}
	public void setCode(String theCode) {
		myCode = theCode;
	}
	
	@Column(name="DISPLAY", length=MAX_DESC_LENGTH, nullable=true)
	private String myDisplay;
	public String getDisplay() {
		return myDisplay;
	}
	public void setDisplay(String theDisplay) {
		myDisplay = theDisplay;
		if (theDisplay != null && !theDisplay.isEmpty() && theDisplay.length() > MAX_DESC_LENGTH) {
			myDisplay = myDisplay.substring(0, MAX_DESC_LENGTH);
		}
		
	}

	
}
