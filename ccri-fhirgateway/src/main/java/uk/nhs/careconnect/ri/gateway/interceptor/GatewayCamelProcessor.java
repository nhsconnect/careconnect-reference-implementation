package uk.nhs.careconnect.ri.gateway.interceptor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;

import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;

public class GatewayCamelProcessor implements Processor
{
    @Override
    public void process(Exchange exchange) throws Exception {

        HttpServletRequest theRequest = (HttpServletRequest) exchange.getIn().getBody();

        // Copy over headers from call

        Enumeration<String> headers = theRequest.getHeaderNames();
        while (headers.hasMoreElements()) {
            String header = headers.nextElement();
            exchange.getIn().setHeader(header,theRequest.getHeader(header));
        }

        exchange.getIn().setHeader(Exchange.HTTP_METHOD, theRequest.getMethod());
        if (theRequest.getQueryString() != null) {
            exchange.getIn().setHeader(Exchange.HTTP_QUERY, theRequest.getQueryString().replace("format=xml","format=json"));
        } else {
            exchange.getIn().setHeader(Exchange.HTTP_QUERY,null);
        }

        exchange.getIn().setHeader(Exchange.HTTP_PATH,  theRequest.getPathInfo());
        exchange.getIn().setHeader("Accept", "application/json");

        if (exchange.getIn().getHeader("X-Request-ID") == null || exchange.getIn().getHeader("X-Request-ID").toString().isEmpty()) {
            exchange.getIn().setHeader("X-Request-ID",exchange.getExchangeId());
        }

        if (theRequest.getRemoteAddr() !=null && !theRequest.getRemoteAddr().isEmpty()) {
            exchange.getIn().setHeader("X-Forwarded-For", theRequest.getRemoteAddr());
        }
        if (theRequest.getRemoteHost() !=null && !theRequest.getRemoteHost().isEmpty()) {
            exchange.getIn().setHeader("X-Forwarded-Host", theRequest.getRemoteHost());
        }

        exchange.getIn().setBody(theRequest.getInputStream());



    }


}
