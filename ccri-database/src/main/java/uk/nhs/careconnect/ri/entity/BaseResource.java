package uk.nhs.careconnect.ri.entity;

import ca.uhn.fhir.context.FhirVersionEnum;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.util.Date;


@MappedSuperclass
public abstract class BaseResource {
	/**
	 * 
	 */
	
	
	@Column(name = "RES_UPDATED",insertable=false, updatable=false)
	@UpdateTimestamp
	private Date resUpdated;
	public Date getResourceUpdated() {
		return this.resUpdated;
	}
	
	@Column(name = "RES_CREATED", nullable = true)
	@CreationTimestamp
	private Date resCreated;
	public Date getResourceCreated() {
		return this.resCreated;
	}
	
	
	@Column(name = "RES_DELETED", nullable = true)
	@Temporal(TemporalType.TIMESTAMP)
	private Date myDeleted;
	public Date getDeleted() {
		return this.myDeleted;
	}
	public void setDeleted(Date theDate) {
		this.myDeleted = theDate;
	}
	
	@Column(name = "RES_MESSAGE_REF", nullable = true)
	
	private String resMessage;
	public String getResourceMessage() {
		return this.resMessage;
	}
	public void setResourceMessage(String resMessage) {
		this.resMessage = resMessage;
	}


	
	
}
