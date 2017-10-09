package uk.nhs.careconnect.ri.gateway.camelfhir;

import org.apache.camel.Endpoint;
import org.apache.camel.impl.UriEndpointComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.Map;

public class HAPIComponent extends UriEndpointComponent {

    final HAPIConfiguration config;

    private static final Logger log = LoggerFactory.getLogger(HAPIComponent.class);



    public HAPIComponent() {
        super(HAPIEndPoint.class);

        config = new HAPIConfiguration();

    }
    @Override
    protected Endpoint createEndpoint(String uri, String remaining, Map<String, Object> parameters) throws Exception {
        Endpoint endpoint = new HAPIEndPoint(uri,this);
        setProperties(endpoint, parameters);

        config.parseURI(new URI(uri), parameters, this);
        return endpoint;
    }
}
