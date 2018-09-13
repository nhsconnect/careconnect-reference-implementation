package uk.nhs.careconnect.ri.database.entity;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.OptimisticLock;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.util.Date;


@MappedSuperclass
public abstract class BaseResource implements IBaseResource {

	private static final int MAX_PROFILE_LENGTH = 10000;

	@Column(name = "RES_UPDATED")
	@UpdateTimestamp
	@Temporal(TemporalType.TIMESTAMP)
	private Date resUpdated;
	public Date getUpdated() {
		return this.resUpdated;
	}

	public void setResUpdated(Date resUpdated) {
		this.resUpdated = resUpdated;
	}

	@Column(name = "RES_CREATED", nullable = true)
	@CreationTimestamp
	@Temporal(TemporalType.TIMESTAMP)
	private Date resCreated;
	public Date getCreated() {
		return this.resCreated;
	}
	
	
	@Column(name = "RES_DELETED", nullable = true)
	@Temporal(TemporalType.TIMESTAMP)
	private Date resDeleted;
	public Date getDeleted() {
		return this.resDeleted;
	}
	public void setDeleted(Date theDate) {
		this.resDeleted = theDate;
	}
	
	@Column(name = "RES_MESSAGE_REF", nullable = true)
	
	private String resMessage;
	public String getResourceMessage() {
		return this.resMessage;
	}
	public void setResourceMessage(String resMessage) {
		this.resMessage = resMessage;
	}

	@Column(name = "RESOURCE", length = MAX_PROFILE_LENGTH, nullable = true)
	@OptimisticLock(
			excluded = true)
	private String Resource;

	public String getResource() {
		return Resource;
	}

	public void setResource(String resource) {
		Resource = resource;
	}

}
