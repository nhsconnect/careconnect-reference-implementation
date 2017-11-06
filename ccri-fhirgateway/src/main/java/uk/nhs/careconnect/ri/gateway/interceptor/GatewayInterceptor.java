package uk.nhs.careconnect.ri.gateway.interceptor;

import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.server.exceptions.AuthenticationException;
import ca.uhn.fhir.rest.server.exceptions.BaseServerResponseException;
import ca.uhn.fhir.rest.server.interceptor.InterceptorAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class GatewayInterceptor extends InterceptorAdapter {
    private static final Logger log = LoggerFactory.getLogger(GatewayInterceptor.class);

    // https://en.wikipedia.org/wiki/List_of_HTTP_header_fields#Field_names

    @Override
    public boolean handleException(RequestDetails theRequestDetails, BaseServerResponseException theException, HttpServletRequest theServletRequest, HttpServletResponse theServletResponse)
            throws ServletException, IOException {
        log.info("CCRI Gateway - exception = "+theException.getStatusCode());

        return true;
    }

    @Override
    public boolean incomingRequestPostProcessed(RequestDetails theRequestDetails, HttpServletRequest theRequest, HttpServletResponse theResponse) throws AuthenticationException {
        log.info("CCRI Gateway - Post Procesed Called");

        return true;
    }



    @Override
    public boolean incomingRequestPreProcessed(HttpServletRequest theRequest, HttpServletResponse theResponse) {

        log.info("CCRI Gateway -  Pre Procesed Called");

        return true;
    }
}
