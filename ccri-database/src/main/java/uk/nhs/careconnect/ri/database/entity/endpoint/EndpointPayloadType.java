package uk.nhs.careconnect.ri.database.entity.endpoint;

import uk.nhs.careconnect.ri.database.entity.BaseResource;
import uk.nhs.careconnect.ri.database.entity.codeSystem.ConceptEntity;

import javax.persistence.*;

@Entity
@Table(name = "EndpointPayloadType")
public class EndpointPayloadType extends BaseResource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="ENDPOINT_PAYLOAD_TYPE_ID")
    private Long myId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ENDPOINT_ID",foreignKey= @ForeignKey(name="FK_ENDPOINT_PAYLOAD_TYPE_ENDPOINT_ROLE_ID"))
    private EndpointEntity endpoint;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="PAYLOAD_TYPE_CONCEPT_ID",foreignKey= @ForeignKey(name="FK_ENDPOINT_PAYLOAD_TYPE_PAYLOAD_TYPE_CONCEPT_ID"))
    private ConceptEntity payloadType;

    public Long getId()
    {
        return this.myId;
    }

    public EndpointEntity getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(EndpointEntity endpoint) {
        this.endpoint = endpoint;
    }

    public Long getMyId() {
        return myId;
    }

    public void setMyId(Long myId) {
        this.myId = myId;
    }

    public ConceptEntity getPayloadType() {
        return payloadType;
    }

    public void setPayloadType(ConceptEntity payloadType) {
        this.payloadType = payloadType;
    }
}
