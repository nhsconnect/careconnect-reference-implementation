package uk.nhs.careconnect.ri.gateway.camelfhir;

import org.apache.camel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class HAPIConsumer implements Consumer {
    protected ProducerTemplate producer;
    private final Processor processor;
    protected HAPIEndPoint endPoint;

    @Autowired
    CamelContext context;

    private static final Logger log = LoggerFactory.getLogger(HAPIConsumer.class);



    public HAPIConsumer(HAPIEndPoint endpoint, Processor processor) {
      //  super((Endpoint) endpoint);
        log.info("create HAPIConsumer");
        this.processor = processor;
        this.endPoint = endpoint;
        producer = endpoint.getConfig().getInnerProducerTemplate();
    }


    @Override
    public Endpoint getEndpoint() {
        log.info("getEndpoint()");
        return this.endPoint;
    }

    @Override
    public void start() throws Exception {

    }

    @Override
    public void stop() throws Exception {

    }
}
