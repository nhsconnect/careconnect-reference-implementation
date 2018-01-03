package uk.nhs.careconnect.ri.gatewaylib.interceptor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

import javax.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.List;
import java.util.ListIterator;

public class GatewayCamelProcessor implements Processor
{

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(GatewayCamelProcessor.class);


    @Override
    public void process(Exchange exchange) throws Exception {

        HttpServletRequest theRequest = (HttpServletRequest) exchange.getIn().getBody();

        /*
        Enumeration<String> headers = theRequest.getHeaderNames();
        while (headers.hasMoreElements()) {
            String header = headers.nextElement();
            exchange.getIn().setHeader(header,theRequest.getHeader(header));
        }
        */

        exchange.getIn().setBody(theRequest.getInputStream());

        exchange.getIn().removeHeaders("*" );

        exchange.getIn().setHeader(Exchange.HTTP_METHOD, theRequest.getMethod());





        if (theRequest.getQueryString() != null) {

            //log.info("QueryString = "+theRequest.getQueryString());
            List<NameValuePair> params = URLEncodedUtils.parse(new URI("http://dummy?"+theRequest.getQueryString()), "UTF-8");

            ListIterator paramlist = params.listIterator();
            while (paramlist.hasNext()) {
                // Remove format. This causes errors in the server (internally we work in JSON only)
                // KGM 3/1/2018
                NameValuePair param = (NameValuePair) paramlist.next();
               // log.info("QS Name ="+param.getName()+" Value="+param.getValue());
                if (param.getName().equals("_format")) paramlist.remove();

            }
            String queryString =  URLEncodedUtils.format(params,"UTF-8");
            //log.info("New QS="+queryString);

            exchange.getIn().setHeader(Exchange.HTTP_QUERY, queryString);
        } else {
            exchange.getIn().setHeader(Exchange.HTTP_QUERY,null);
        }


        exchange.getIn().setHeader(Exchange.HTTP_PATH,  theRequest.getPathInfo());

        exchange.getIn().setHeader(Exchange.ACCEPT_CONTENT_TYPE, "application/json");

        if (exchange.getIn().getHeader("X-Request-ID") == null || exchange.getIn().getHeader("X-Request-ID").toString().isEmpty()) {
            exchange.getIn().setHeader("X-Request-ID",exchange.getExchangeId());
        }

        if (theRequest.getRemoteAddr() !=null && !theRequest.getRemoteAddr().isEmpty()) {
            exchange.getIn().setHeader("X-Forwarded-For", theRequest.getRemoteAddr());
        }
        if (theRequest.getRemoteHost() !=null && !theRequest.getRemoteHost().isEmpty()) {
            exchange.getIn().setHeader("X-Forwarded-Host", theRequest.getRemoteHost());
        }





    }


}
