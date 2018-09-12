package uk.nhs.careconnect.ccri.fhirserver.provider;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.server.servlet.ServletRequestDetails;
import org.apache.commons.lang3.StringUtils;
import org.jboss.logging.MDC;

import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;
import java.util.Set;
import java.util.TreeSet;

public class BaseProvider {

    private static final org.slf4j.Logger ourLog = org.slf4j.LoggerFactory.getLogger(BaseProvider.class);
    public static final String REMOTE_ADDR = "req.remoteAddr";
    public static final String REMOTE_UA = "req.userAgent";

    private FhirContext myContext;

    public FhirContext getContext() {
        return myContext;
    }

    public void endRequest(HttpServletRequest theRequest) {
        MDC.remove(REMOTE_ADDR);
        MDC.remove(REMOTE_UA);
    }

    public void endRequest(ServletRequestDetails theRequest) {
        endRequest(theRequest.getServletRequest());
    }


    public void startRequest(HttpServletRequest theRequest) {
        if (theRequest == null) {
            return;
        }

        Set<String> headerNames = new TreeSet<String>();
        for (Enumeration<String> enums = theRequest.getHeaderNames(); enums.hasMoreElements();) {
            headerNames.add(enums.nextElement());
        }
        ourLog.debug("Request headers: {}", headerNames);

        Enumeration<String> forwardedFors = theRequest.getHeaders("x-forwarded-for");
        StringBuilder b = new StringBuilder();
        for (Enumeration<String> enums = forwardedFors; enums != null && enums.hasMoreElements();) {
            if (b.length() > 0) {
                b.append(" / ");
            }
            b.append(enums.nextElement());
        }

        String forwardedFor = b.toString();
        String ip = theRequest.getRemoteAddr();
        if (StringUtils.isBlank(forwardedFor)) {
            org.slf4j.MDC.put(REMOTE_ADDR, ip);
            ourLog.debug("Request is from address: {}", ip);
        } else {
            org.slf4j.MDC.put(REMOTE_ADDR, forwardedFor);
            ourLog.debug("Request is from forwarded address: {}", forwardedFor);
        }

        String userAgent = StringUtils.defaultString(theRequest.getHeader("user-agent"));
        org.slf4j.MDC.put(REMOTE_UA, userAgent);

    }

    public void startRequest(ServletRequestDetails theRequest) {
        startRequest(theRequest.getServletRequest());
    }


}
