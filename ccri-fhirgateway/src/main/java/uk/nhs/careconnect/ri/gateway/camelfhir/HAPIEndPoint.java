package uk.nhs.careconnect.ri.gateway.camelfhir;


import org.apache.camel.*;
import org.apache.camel.impl.DefaultEndpoint;
import org.apache.camel.spi.UriEndpoint;
import org.apache.camel.spi.UriParam;


@UriEndpoint( scheme = "hapi", title = "HAPI FHIR", syntax = "hapi:hapiName", consumerClass = HAPIConsumer.class, label = "eventbus")
public class HAPIEndPoint extends DefaultEndpoint {

    @UriParam
    private HAPIConfiguration config;

    public HAPIEndPoint(String uri, HAPIComponent component) {

    }
    public HAPIConfiguration getConfig() {

        if (config == null) {
            this.config = new HAPIConfiguration();
        }
        return config;
    }

    @Override
    protected String createEndpointUri() {
        return "";
    }


    @Override
    public Producer createProducer() throws Exception {
        System.out.println("HAPIEndPoint: createProducer()");
        return new HAPIProducer(this);
    }

    @Override
    public Consumer createConsumer(Processor processor) throws Exception {

        System.out.println("HAPIEndPoint: createConsumer()");
        return new HAPIConsumer(this, processor);
    }


    @Override
    public boolean isSingleton() {
        return false;
    }
}
