package uk.nhs.careconnect.ri.lib;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.api.EncodingEnum;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.server.RestfulServer;
import ca.uhn.fhir.rest.server.RestfulServerUtils;
import ca.uhn.fhir.rest.server.exceptions.*;
import ca.uhn.fhir.rest.server.interceptor.InterceptorAdapter;
import ca.uhn.fhir.rest.server.servlet.ServletRequestDetails;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.StrLookup;
import org.apache.commons.lang3.text.StrSubstitutor;
import org.hl7.fhir.dstu3.model.OperationOutcome;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.slf4j.Logger;
import org.springframework.http.InvalidMediaTypeException;
import org.springframework.http.MediaType;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.*;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

public class ServerInterceptor extends InterceptorAdapter {

    // https://en.wikipedia.org/wiki/List_of_HTTP_header_fields#Field_names
    private Logger log = null; //LoggerFactory.getLogger(ServerInterceptor.class);
    private String myErrorMessageFormat = "ERROR - ${operationType} - ${idOrResourceName}";


    public ServerInterceptor(Logger ourLog) {
        super();
        this.log = ourLog;

    }


    @Override
    public boolean handleException(RequestDetails theRequestDetails, BaseServerResponseException theException, HttpServletRequest theServletRequest, HttpServletResponse theServletResponse) throws ServletException, IOException {

        // Return false when overriding hapi behaviour.

        // tickets #41 #43 #44 #45

        log.info("Exception = "+theException.getClass().getCanonicalName());
        if (theException instanceof InvalidRequestException) {
            if (theException.getOperationOutcome() !=null && theException.getOperationOutcome() instanceof OperationOutcome) {
                FhirContext ctx = FhirContext.forDstu3();

                OperationOutcome outcome  = (OperationOutcome) theException.getOperationOutcome();
                log.info("Exception intercept. Diagnostic Response = "+outcome.getIssueFirstRep().getDiagnostics()+ " "+outcome.getIssueFirstRep().getCode().getDisplay());
                if (outcome.getIssueFirstRep().getCode().equals(OperationOutcome.IssueType.PROCESSING)) {
                    if (outcome.getIssueFirstRep().getDiagnostics().contains("The FHIR endpoint on this server does not know how to handle")) {

                        if (outcome.getIssueFirstRep().getDiagnostics().contains("handle GET")) {
                            if (outcome.getIssueFirstRep().getDiagnostics().contains("[coffee]"))
                                theServletResponse.setStatus(418);
                            else theServletResponse.setStatus(404);
                        } else {
                            theServletResponse.setStatus(501);
                        }
                        if (outcome.getIssueFirstRep().getDiagnostics() != null) {
                            log.debug("Diagnostic Response = " + outcome.getIssueFirstRep().getDiagnostics());

                        }

                        // Provide a response yourself
                        theServletResponse.setContentType("application/json+fhir;charset=UTF-8");

                        theServletResponse.getWriter().append(ctx.newJsonParser().encodeResourceToString(theException.getOperationOutcome()));
                        theServletResponse.getWriter().close();
                        return false;
                    }
                    if (outcome.getIssueFirstRep().getDiagnostics().contains("Unsupported media type:")) {
                        theServletResponse.setStatus(415);
                        theServletResponse.setContentType("application/json+fhir;charset=UTF-8");
                        theServletResponse.getWriter().append(ctx.newJsonParser().encodeResourceToString(theException.getOperationOutcome()));
                        theServletResponse.getWriter().close();
                        return false;
                    }
                }
            }

        }
        if (theException instanceof InternalErrorException) {
            if (theException.getOperationOutcome() !=null && theException.getOperationOutcome() instanceof OperationOutcome) {
                FhirContext ctx = FhirContext.forDstu3();

                OperationOutcome outcome  = (OperationOutcome) theException.getOperationOutcome();
                log.error("InternalErrorException: Diagnostics = "+outcome.getIssueFirstRep().getDiagnostics()+ " "+outcome.getIssueFirstRep().getCode().getDisplay());
                if (outcome.getIssueFirstRep().getCode().equals(OperationOutcome.IssueType.PROCESSING)) {

                    theServletResponse.setStatus(400);
                    if (outcome.getIssueFirstRep().getDiagnostics() != null){
                        log.debug("Diagnostic Response = "+outcome.getIssueFirstRep().getDiagnostics());

                    }

                    // Provide a response ourself
                    theServletResponse.setContentType("application/json+fhir;charset=UTF-8");

                    theServletResponse.getWriter().append(ctx.newJsonParser().encodeResourceToString(theException.getOperationOutcome()));
                    theServletResponse.getWriter().close();
                    return false;
                }
            }
        }

        return true;
    }



    @Override
    public boolean incomingRequestPostProcessed(RequestDetails theRequestDetails, HttpServletRequest theRequest, HttpServletResponse theResponse) throws AuthenticationException {

        Enumeration<String> headers = theRequest.getHeaderNames();
        while (headers.hasMoreElements()) {
            String header = headers.nextElement();
            log.debug("Header  = "+ header + "="+ theRequest.getHeader(header));
        }
        // Perform any string substitutions from the message format
        StrLookup<?> lookup = new MyLookup(theRequest, theRequestDetails);
        StrSubstitutor subs = new StrSubstitutor(lookup, "${", "}", '\\');

        // Actually log the line
        String myMessageFormat = "httpVerb[${requestVerb}] Source[${remoteAddr}] Operation[${operationType} ${idOrResourceName}] UA[${requestHeader.user-agent}] Params[${requestParameters}] RequestId[${requestHeader.x-request-id}] ForwardedFor[${requestHeader.x-forwarded-for}] ForwardedHost[${requestHeader.x-forwarded-host}] CorrelationId[] ProcessingTime[]  ResponseCode[]";

        String line = subs.replace(myMessageFormat);
        log.info(line);

        return true;
    }

    @Override
    public boolean incomingRequestPreProcessed(HttpServletRequest request, HttpServletResponse theResponse) {

        if (request.getMethod() != null) {

            /* KGM 3/1/2018 This is now handled by CORS headers

           if (theRequest.getMethod().equals("OPTIONS"))
                throw new MethodNotAllowedException("request must use HTTP GET");
            */

            if (request.getContentType() != null) {
               checkContentType(request.getContentType());
            }

            if (request.getQueryString() != null) {


                List<NameValuePair> params = null;
                try {
                    params = URLEncodedUtils.parse(new URI("http://dummy?" + request.getQueryString()), "UTF-8");
                } catch (Exception ex) {
                }

                ListIterator paramlist = params.listIterator();
                while (paramlist.hasNext()) {
                    NameValuePair param = (NameValuePair) paramlist.next();
                    if (param.getName().equals("_format"))
                        checkContentType(param.getValue());

                }
            }


            // May need to re-add this at a later date (probably in conjunction with a security uplift)
            /*
            KGM 3/1/2018 disabled for crucible testing

            if (request.getMethod().equals("POST") && request.getPathInfo() != null && request.getPathInfo().contains("_search"))
                throw new MethodNotAllowedException("request must use HTTP GET");
                */
        }
        return true;
    }
    public void checkContentType(String contentType) {
        try {
                MediaType media = MediaType.parseMediaType(contentType);
                // TODO improve the logic here
                if (media.getSubtype() != null && !media.getSubtype().contains("xml") && !media.getSubtype().contains("fhir") && !media.getSubtype().contains("json") && !media.getSubtype().contains("plain")) {
                    log.info("Unsupported media type: " + contentType);
                    throw new InvalidRequestException("Unsupported media type: sub " + contentType);
                } else {
                    if (!contentType.contains("xml") && !contentType.contains("json")) {
                        log.info("Unsupported media type: " + contentType);
                        throw new InvalidRequestException("Unsupported media type: content " + contentType);
                    }
                }

        } catch (InvalidMediaTypeException e) {
            log.info("Unsupported media type: " + contentType);
            // KGM 26/02/2018 Don't throw error for xml and json
            if (!contentType.contains("xml") && !contentType.contains("json")) {
                log.info("Unsupported media type: " + contentType);
                throw new InvalidRequestException("Unsupported media type: content " + contentType);
            }
        }
    }


    @Override
    public boolean outgoingResponse(RequestDetails theRequestDetails, IBaseResource theResponseObject) {
        ServletRequestDetails details = (ServletRequestDetails) theRequestDetails;
        log.debug("outgoingResponse2 = ");
        return outgoingResponse(details, theResponseObject, details.getServletRequest(), details.getServletResponse());
    }

    @Override
    public boolean outgoingResponse(RequestDetails theRequestDetails, IBaseResource theResponseObject, HttpServletRequest theServletRequest, HttpServletResponse theServletResponse)
            throws AuthenticationException {

        //log.info("Outgoing object class = "+theResponseObject.getClass().getCanonicalName());

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
            log.debug("Header  = " + header + "=" + theRequestDetails.getServletResponse().getHeader(header));
        }

        String myMessageFormat = "httpVerb[${requestVerb}] Source[${remoteAddr}] Operation[${operationType} ${idOrResourceName}] UA[${requestHeader.user-agent}] Params[${requestParameters}] RequestId[${requestHeader.x-request-id}] ForwardedFor[${requestHeader.x-forwarded-for}] ForwardedHost[${requestHeader.x-forwarded-host}] CorrelationId[${requestHeader.x-request-id}] ProcessingTime[${processingTimeMillis}]";

        String line = subs.replace(myMessageFormat);
        log.info(line+" ResponseCode["+theRequestDetails.getServletResponse().getStatus()+"]");
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
