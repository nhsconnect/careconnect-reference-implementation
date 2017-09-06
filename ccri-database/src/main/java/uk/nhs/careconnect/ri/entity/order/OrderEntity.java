package uk.nhs.careconnect.ri.entity.order;

import uk.nhs.careconnect.ri.entity.BaseResource;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "OrderEntity")
public class OrderEntity extends BaseResource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "identifier")
    private String identifier;
    
    @Column(name = "orderDate")
    private Date orderDate;
    
    @Column(name = "subjectPatientId")
    private Long subjectPatientId;
    
    @Column(name = "sourceOrgId")
    private Long sourceOrgId;
    
    @Column(name = "targetOrgId")
    private Long targetOrgId;
    
    @Column(name = "reasonCode")
    private String reasonCode;
    
    @Column(name = "reasonDescription")
    private String reasonDescription;
    
    @Column(name = "reasonText")
    private String reasonText;
    
    @Column(name = "detail")
    private String detail;
    
    @Column(name = "recieved")
    private boolean recieved;
    
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }
    
    public Date getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(Date orderDate) {
        this.orderDate = orderDate;
    }

    public Long getSubjectPatientId() {
        return subjectPatientId;
    }

    public void setSubjectPatientId(Long subjectPatientId) {
        this.subjectPatientId = subjectPatientId;
    }

    public Long getSourceOrgId() {
        return sourceOrgId;
    }

    public void setSourceOrgId(Long sourceOrgId) {
        this.sourceOrgId = sourceOrgId;
    }

    public Long getTargetOrgId() {
        return targetOrgId;
    }

    public void setTargetOrgId(Long targetOrgId) {
        this.targetOrgId = targetOrgId;
    }

    public String getReasonCode() {
        return reasonCode;
    }

    public void setReasonCode(String reasonCode) {
        this.reasonCode = reasonCode;
    }

    public String getReasonDescription() {
        return reasonDescription;
    }

    public void setReasonDescription(String reasonDescription) {
        this.reasonDescription = reasonDescription;
    }

    public String getReasonText() {
        return reasonText;
    }

    public void setReasonText(String reasonText) {
        this.reasonText = reasonText;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    public boolean getRecieved() {
        return recieved;
    }

    public void setRecieved(boolean recieved) {
        this.recieved = recieved;
    }
}
