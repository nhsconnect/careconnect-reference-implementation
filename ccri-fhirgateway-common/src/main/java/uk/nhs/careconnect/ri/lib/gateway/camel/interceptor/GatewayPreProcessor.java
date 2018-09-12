package uk.nhs.careconnect.ri.lib.gateway.camel.interceptor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

import javax.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.List;
import java.util.ListIterator;

public class GatewayPreProcessor implements Processor
{

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(GatewayPreProcessor.class);


    @Override
    public void process(Exchange exchange) throws Exception {




        if (exchange.getIn().getBody() instanceof HttpServletRequest) {
            // This is a result of a passthrough operation in the resource provider.

            HttpServletRequest httpRequest = (HttpServletRequest) exchange.getIn().getBody();

            exchange.getIn().removeHeaders("*" );

            exchange.getIn().setBody(httpRequest.getInputStream());

            exchange.getIn().setHeader(Exchange.HTTP_METHOD, httpRequest.getMethod());

            if (httpRequest.getMethod().equals("POST") || httpRequest.getMethod().equals("PUT")) {
                exchange.getIn().setHeader(Exchange.CONTENT_TYPE,"application/fhir+json");
            } else {
                exchange.getIn().setHeader(Exchange.ACCEPT_CONTENT_TYPE, "application/json");
            }

            if (httpRequest.getQueryString() != null) {

                //log.info("QueryString = "+httpRequest.getQueryString());
                List<NameValuePair> params = URLEncodedUtils.parse(new URI("http://dummy?" + httpRequest.getQueryString()), "UTF-8");

                ListIterator paramlist = params.listIterator();
                while (paramlist.hasNext()) {
                    // Remove format. This causes errors in the server (internally we work in JSON only)
                    // KGM 3/1/2018
                    NameValuePair param = (NameValuePair) paramlist.next();
                    // log.info("QS Name ="+param.getName()+" Value="+param.getValue());
                    if (param.getName().equals("_format")) paramlist.remove();

                }
                String queryString = URLEncodedUtils.format(params, "UTF-8");
                //log.info("New QS="+queryString);

                exchange.getIn().setHeader(Exchange.HTTP_QUERY, queryString);
            } else {
                exchange.getIn().setHeader(Exchange.HTTP_QUERY, null);
            }


            exchange.getIn().setHeader(Exchange.HTTP_PATH, httpRequest.getPathInfo());

            if (httpRequest.getRemoteAddr() !=null && !httpRequest.getRemoteAddr().isEmpty()) {
                exchange.getIn().setHeader("X-Forwarded-For", httpRequest.getRemoteAddr());
            }
            if (httpRequest.getRemoteHost() !=null && !httpRequest.getRemoteHost().isEmpty()) {
                exchange.getIn().setHeader("X-Forwarded-Host", httpRequest.getRemoteHost());
            }
        }



        if (exchange.getIn().getHeader("X-Request-ID") == null || exchange.getIn().getHeader("X-Request-ID").toString().isEmpty()) {
            exchange.getIn().setHeader("X-Request-ID",exchange.getExchangeId());
        }

    }


}
