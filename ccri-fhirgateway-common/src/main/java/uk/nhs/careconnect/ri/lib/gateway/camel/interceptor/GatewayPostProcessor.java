package uk.nhs.careconnect.ri.lib.gateway.camel.interceptor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;

public class GatewayPostProcessor implements Processor
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

        // Remove cors headers

        removeHeader(exchange,"Access-Control-Allow-Methods");
        removeHeader(exchange,"Access-Control-Allow-Origin");
        removeHeader(exchange,"Access-Control-Max-Age");
        removeHeader(exchange,"action");

    }

    private void removeHeader(Exchange exchange, String header) {
        if (exchange.getIn().getHeader(header) != null ) {
            exchange.getIn().removeHeader(header);
        }
    }


}
