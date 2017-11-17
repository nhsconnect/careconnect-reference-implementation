package uk.nhs.careconnect.ri.gateway.interceptor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;

public class GatewayCamelPostProcessor implements Processor
{
    @Override
    public void process(Exchange exchange) throws Exception {


        if (exchange.getIn().getHeader("X-Request-ID") == null || exchange.getIn().getHeader("X-Request-ID").toString().isEmpty()) {
            exchange.getIn().removeHeader("X-Request-ID");
        }

        if (exchange.getIn().getHeader("X-Forwarded-For") == null || exchange.getIn().getHeader("X-Forwarded-For").toString().isEmpty()) {
            exchange.getIn().removeHeader("X-Forwarded-For");
        }

        if (exchange.getIn().getHeader("X-Forwarded-Host") == null || exchange.getIn().getHeader("X-Forwarded-Host").toString().isEmpty()) {
            exchange.getIn().removeHeader("X-Forwarded-Host");
        }

    }


}
