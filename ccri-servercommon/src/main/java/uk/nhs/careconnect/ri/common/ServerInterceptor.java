package uk.nhs.careconnect.ri.common;

import ca.uhn.fhir.rest.api.EncodingEnum;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.server.RestfulServer;
import ca.uhn.fhir.rest.server.RestfulServerUtils;
import ca.uhn.fhir.rest.server.exceptions.AuthenticationException;
import ca.uhn.fhir.rest.server.exceptions.BaseServerResponseException;
import ca.uhn.fhir.rest.server.exceptions.MethodNotAllowedException;
import ca.uhn.fhir.rest.server.interceptor.InterceptorAdapter;
import ca.uhn.fhir.rest.server.servlet.ServletRequestDetails;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.StrLookup;
import org.apache.commons.lang3.text.StrSubstitutor;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.slf4j.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Date;
import java.util.Enumeration;
import java.util.Map;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

public class ServerInterceptor extends InterceptorAdapter {

    // https://en.wikipedia.org/wiki/List_of_HTTP_header_fields#Field_names
    private Logger ourLog = null; //LoggerFactory.getLogger(ServerInterceptor.class);
    private String myErrorMessageFormat = "ERROR - ${operationType} - ${idOrResourceName}";


    public ServerInterceptor(Logger ourLog) {
        super();
        this.ourLog = ourLog;

    }





    @Override
    public boolean incomingRequestPostProcessed(RequestDetails theRequestDetails, HttpServletRequest theRequest, HttpServletResponse theResponse) throws AuthenticationException {

        Enumeration<String> headers = theRequest.getHeaderNames();
        while (headers.hasMoreElements()) {
            String header = headers.nextElement();
            ourLog.debug("Header  = "+ header + "="+ theRequest.getHeader(header));
        }
        // Perform any string substitutions from the message format
        StrLookup<?> lookup = new MyLookup(theRequest, theRequestDetails);
        StrSubstitutor subs = new StrSubstitutor(lookup, "${", "}", '\\');

        // Actually log the line
        String myMessageFormat = "httpVerb[${requestVerb}] Source[${remoteAddr}] Operation[${operationType} ${idOrResourceName}] UA[${requestHeader.user-agent}] Params[${requestParameters}] RequestId[${requestHeader.x-request-id}] ForwardedFor[${requestHeader.x-forwarded-for}] ForwardedHost[${requestHeader.x-forwarded-host}] ";

        String line = subs.replace(myMessageFormat);
        ourLog.info(line);

        return true;
    }

    @Override
    public boolean incomingRequestPreProcessed(HttpServletRequest theRequest, HttpServletResponse theResponse) {

        if (theRequest.getMethod() != null) {
            if (theRequest.getMethod().equals("OPTIONS"))
                throw new MethodNotAllowedException("request must use HTTP GET");

            // May need to readd this at a later date (probably in conjunction with a security uplift)
            if (theRequest.getMethod().equals("POST") && theRequest.getPathInfo() != null && theRequest.getPathInfo().contains("_search"))
                throw new MethodNotAllowedException("request must use HTTP GET");
        }
        return true;
    }


    @Override
    public boolean outgoingResponse(RequestDetails theRequestDetails, IBaseResource theResponseObject) {
        ServletRequestDetails details = (ServletRequestDetails) theRequestDetails;
       // ourLog.info("outgoingResponse2");
        return outgoingResponse(details, theResponseObject, details.getServletRequest(), details.getServletResponse());
    }

    @Override
    public boolean outgoingResponse(RequestDetails theRequestDetails, IBaseResource theResponseObject, HttpServletRequest theServletRequest, HttpServletResponse theServletResponse)
            throws AuthenticationException {

        String val = theRequestDetails.getHeader("x-request-id");

        if (val !=null && !val.isEmpty()) {
            theServletResponse.addHeader("X-Correlation-ID", val);
           // theServletResponse.setHeader("X-Request-ID","");
        }
       return true;

    }

    @Override
    public void processingCompletedNormally(ServletRequestDetails theRequestDetails) {
        // Perform any string substitutions from the message format

        StrLookup<?> lookup = new MyLookup(theRequestDetails.getServletRequest(), theRequestDetails);
        StrSubstitutor subs = new StrSubstitutor(lookup, "${", "}", '\\');

        for (String header : theRequestDetails.getServletResponse().getHeaderNames()) {
            ourLog.debug("Header  = " + header + "=" + theRequestDetails.getServletResponse().getHeader(header));
        }

        String myMessageFormat = "httpVerb[${requestVerb}] Source[${remoteAddr}] Operation[${operationType} ${idOrResourceName}] UA[${requestHeader.user-agent}] Params[${requestParameters}] RequestId[${requestHeader.x-request-id}] ForwardedFor[${requestHeader.x-forwarded-for}] ForwardedHost[${requestHeader.x-forwarded-host}] CorrelationId[${requestHeader.x-request-id}] ProcessingTime[${processingTimeMillis}]";

        String line = subs.replace(myMessageFormat);
        ourLog.info(line+" ResponseCode["+theRequestDetails.getServletResponse().getStatus()+"]");
    }



    private static final class MyLookup extends StrLookup<String> {
        private final Throwable myException;
        private final HttpServletRequest myRequest;
        private final RequestDetails myRequestDetails;

        private MyLookup(HttpServletRequest theRequest, RequestDetails theRequestDetails) {
            myRequest = theRequest;
            myRequestDetails = theRequestDetails;
            myException = null;
        }

        public MyLookup(HttpServletRequest theServletRequest, BaseServerResponseException theException, RequestDetails theRequestDetails) {
            myException = theException;
            myRequestDetails = theRequestDetails;
            myRequest = theServletRequest;
        }

        @Override
        public String lookup(String theKey) {

			/*
			 * TODO: this method could be made more efficient through some sort of lookup map
			 */

            if ("operationType".equals(theKey)) {
                if (myRequestDetails.getRestOperationType() != null) {
                    return myRequestDetails.getRestOperationType().getCode();
                }
                return "";
            } else if ("operationName".equals(theKey)) {
                if (myRequestDetails.getRestOperationType() != null) {
                    switch (myRequestDetails.getRestOperationType()) {
                        case EXTENDED_OPERATION_INSTANCE:
                        case EXTENDED_OPERATION_SERVER:
                        case EXTENDED_OPERATION_TYPE:
                            return myRequestDetails.getOperation();
                        default:
                            return "";
                    }
                }
                return "";
            } else if ("id".equals(theKey)) {
                if (myRequestDetails.getId() != null) {
                    return myRequestDetails.getId().getValue();
                }
                return "";
            } else if ("servletPath".equals(theKey)) {
                return StringUtils.defaultString(myRequest.getServletPath());
            } else if ("idOrResourceName".equals(theKey)) {
                if (myRequestDetails.getId() != null) {
                    return myRequestDetails.getId().getValue();
                }
                if (myRequestDetails.getResourceName() != null) {
                    return myRequestDetails.getResourceName();
                }
                return "";
            } else if (theKey.equals("requestParameters")) {
                StringBuilder b = new StringBuilder();
                for (Map.Entry<String, String[]> next : myRequestDetails.getParameters().entrySet()) {
                    for (String nextValue : next.getValue()) {
                        if (b.length() == 0) {
                            b.append('?');
                        } else {
                            b.append('&');
                        }
                        try {
                            b.append(URLEncoder.encode(next.getKey(), "UTF-8"));
                            b.append('=');
                            b.append(URLEncoder.encode(nextValue, "UTF-8"));
                        } catch (UnsupportedEncodingException e) {
                            throw new ca.uhn.fhir.context.ConfigurationException("UTF-8 not supported", e);
                        }
                    }
                }
                return b.toString();
            } else if (theKey.startsWith("requestHeader.")) {
                String val = myRequest.getHeader(theKey.substring("requestHeader.".length()));
                return StringUtils.defaultString(val);
            } else if (theKey.startsWith("remoteAddr")) {
                return StringUtils.defaultString(myRequest.getRemoteAddr());
            } else if (theKey.equals("responseEncodingNoDefault")) {
                RestfulServerUtils.ResponseEncoding encoding = RestfulServerUtils.determineResponseEncodingNoDefault(myRequestDetails, myRequestDetails.getServer().getDefaultResponseEncoding());
                if (encoding != null) {
                    return encoding.getEncoding().name();
                }
                return "";
            } else if (theKey.equals("exceptionMessage")) {
                return myException != null ? myException.getMessage() : null;
            } else if (theKey.equals("requestUrl")) {
                return myRequest.getRequestURL().toString();
            } else if (theKey.equals("requestVerb")) {
                return myRequest.getMethod();
            } else if (theKey.equals("requestBodyFhir")) {
                String contentType = myRequest.getContentType();
                if (isNotBlank(contentType)) {
                    int colonIndex = contentType.indexOf(';');
                    if (colonIndex != -1) {
                        contentType = contentType.substring(0, colonIndex);
                    }
                    contentType = contentType.trim();

                    EncodingEnum encoding = EncodingEnum.forContentType(contentType);
                    if (encoding != null) {
                        byte[] requestContents = myRequestDetails.loadRequestContents();
                        return requestContents.toString();
                    }
                }
                return "";
            } else if ("processingTimeMillis".equals(theKey)) {
                Date startTime = (Date) myRequest.getAttribute(RestfulServer.REQUEST_START_TIME);
                if (startTime != null) {
                    long time = System.currentTimeMillis() - startTime.getTime();
                    return Long.toString(time);
                }
            }


            return "!VAL!";
        }
    }
}
