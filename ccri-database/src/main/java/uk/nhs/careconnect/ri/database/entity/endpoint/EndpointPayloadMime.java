package uk.nhs.careconnect.ri.database.entity.endpoint;

import uk.nhs.careconnect.ri.database.entity.BaseResource;
import uk.nhs.careconnect.ri.database.entity.Terminology.ConceptEntity;


import javax.persistence.*;

@Entity
@Table(name = "EndpointPayloadMime")
public class EndpointPayloadMime extends BaseResource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="ENDPOINT_PAYLOAD_MIME_ID")
    private Long myId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ENDPOINT_ID",foreignKey= @ForeignKey(name="FK_ENDPOINT_PAYLOAD_MIME_ENDPOINT_ROLE_ID"))
    private EndpointEntity endpoint;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="PAYLOAD_MIME_CONCEPT_ID",foreignKey= @ForeignKey(name="FK_ENDPOINT_PAYLOAD_MIME_PAYLOAD_MIME_CONCEPT_ID"))
    private ConceptEntity mimeType;

    public Long getId()
    {
        return this.myId;
    }

    public Long getMyId() {
        return myId;
    }

    public void setMyId(Long myId) {
        this.myId = myId;
    }

    public ConceptEntity getMimeType() {
        return mimeType;
    }

    public void setMimeType(ConceptEntity mimeType) {
        this.mimeType = mimeType;
    }
}
