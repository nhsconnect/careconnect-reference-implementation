package uk.nhs.careconnect.ri.gateway.camelfhir;

import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.spi.UriParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.Map;

public class HAPIConfiguration {

    String uri;

    private static final Logger log = LoggerFactory.getLogger(HAPIConfiguration.class);



    @UriParam(label = "advanced")
    private ProducerTemplate innerProducerTemplate;

    @UriParam(label = "advanced")
    private CamelContext innerContext;

    public ProducerTemplate getInnerProducerTemplate() {

        return innerProducerTemplate;
    }

    public CamelContext getInnerContext() {
        return innerContext;
    }

    public void setInnerContext(CamelContext innerContext) {
        this.innerContext = innerContext;
    }

    public void setInnerProducerTemplate(ProducerTemplate innerProducerTemplate) {
        this.innerProducerTemplate = innerProducerTemplate;
    }

    @SuppressWarnings("unchecked")
    public void parseURI(URI uri, Map<String, Object> parameters, HAPIComponent component) throws Exception {
        log.info("parseUri");
        innerContext = component.resolveAndRemoveReferenceParameter(parameters, "innerContext", CamelContext.class, new DefaultCamelContext());

        innerProducerTemplate = innerContext.createProducerTemplate();

    }

}
