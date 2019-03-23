package uk.nhs.careconnect.ri.dao.transforms;

import org.apache.commons.collections4.Transformer;
import org.hl7.fhir.dstu3.model.Endpoint;
import org.hl7.fhir.dstu3.model.Identifier;
import org.hl7.fhir.dstu3.model.Meta;
import org.hl7.fhir.dstu3.model.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import uk.nhs.careconnect.ri.dao.daoutils;
import uk.nhs.careconnect.ri.database.entity.endpoint.EndpointEntity;
import uk.nhs.careconnect.ri.database.entity.endpoint.EndpointIdentifier;
import uk.nhs.careconnect.ri.database.entity.endpoint.EndpointPayloadMime;
import uk.nhs.careconnect.ri.database.entity.endpoint.EndpointPayloadType;

@Component
public class EndpointEntityToFHIREndpointTransform implements Transformer<EndpointEntity, Endpoint> {

    private static final Logger log = LoggerFactory.getLogger(EndpointEntityToFHIREndpointTransform.class);

    @Override
    public Endpoint transform(EndpointEntity endpointEntity) {
        final Endpoint endpoint = new Endpoint();



        Meta meta = new Meta();

        if (endpointEntity.getUpdated() != null) {
            meta.setLastUpdated(endpointEntity.getUpdated());
        }
        else {
            if (endpointEntity.getCreated() != null) {
                meta.setLastUpdated(endpointEntity.getCreated());
            }
        }
        endpoint.setMeta(meta);


        if (endpointEntity.getStatus() != null) {
            endpoint.setStatus(endpointEntity.getStatus());
        }


        for(EndpointIdentifier identifier: endpointEntity.getIdentifiers())
        {
            Identifier ident = endpoint.addIdentifier();
            ident = daoutils.getIdentifier(identifier, ident);
        }

        endpoint.setId(endpointEntity.getId().toString());

        log.trace("endpoint tfm3");
        if (endpointEntity.getName() != null) {
            endpoint.setName(endpointEntity.getName());
        }

        if (endpointEntity.getManagingOrganisation() != null) {
            endpoint.setManagingOrganization(new Reference("Organization/"+endpointEntity.getManagingOrganisation().getId()));
            endpoint.getManagingOrganization().setDisplay(endpointEntity.getManagingOrganisation().getName());
        }
        log.trace("endpoint tfm4");
        if (endpointEntity.getConnectionType()!=null) {
            endpoint.getConnectionType()
                    .setCode(endpointEntity.getConnectionType().getCode())
                    .setDisplay(endpointEntity.getConnectionType().getDisplay())
                    .setSystem(endpointEntity.getConnectionType().getSystem());
        }

        log.trace("endpoint tfm5");
        if (endpointEntity.getAddress() != null) {
            endpoint.setAddress(endpointEntity.getAddress());
        }

        log.trace("endpoint tfm6");

        for(EndpointPayloadType endpointPayloadType : endpointEntity.getPayloadTypes()) {
            endpoint.addPayloadType()
                    .addCoding()
                    .setSystem(endpointPayloadType.getPayloadType().getSystem())
                    .setCode(endpointPayloadType.getPayloadType().getCode())
                    .setDisplay(endpointPayloadType.getPayloadType().getDisplay());
        }
        for(EndpointPayloadMime endpointPayloadMime : endpointEntity.getPayloadMimes()) {
            endpoint.addPayloadMimeType(endpointPayloadMime.getMimeType().getCode());
        }

        return endpoint;
    }
}
