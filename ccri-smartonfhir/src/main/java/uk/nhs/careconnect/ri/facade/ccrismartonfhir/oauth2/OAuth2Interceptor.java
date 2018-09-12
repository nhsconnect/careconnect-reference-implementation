package uk.nhs.careconnect.ri.facade.ccrismartonfhir.oauth2;


import ca.uhn.fhir.rest.server.exceptions.AuthenticationException;
import ca.uhn.fhir.rest.server.exceptions.ForbiddenOperationException;
import ca.uhn.fhir.rest.server.interceptor.InterceptorAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Interceptor which checks that a valid OAuth2 Token has been supplied.
 * Checks the following rules:
 *   1.  A token is supplied in the Authorization Header
 *   2.  The token is a valid OAuth2 Token
 *   3.  The token is for the correct server
 *   4.  The token has not expired
 *
 * Ignored if this request is in the list of excluded URIs (e.g. metadata)
 *
 */
public class OAuth2Interceptor extends InterceptorAdapter {

    private final List<String> excludedPaths;

    Logger logger = LoggerFactory.getLogger(OAuth2Interceptor.class);

    private final Map<String,String> accessRights = getAccessRights();

    private static final Pattern RESOURCE_PATTERN = Pattern.compile("^/(\\w+)[//|\\?]?.*$");

    public OAuth2Interceptor() {
        this.excludedPaths = new ArrayList<>();
        excludedPaths.add("/metadata");

        getAccessRights();

    }

    protected Map<String, String> getAccessRights() {
        Map<String, String> accessRights = new HashMap();
        accessRights.put("Patient","Patient");
        accessRights.put("Observation","Observation");
        accessRights.put("Encounter","Encounter");
        accessRights.put("Condition","Condition");
        accessRights.put("Procedure","Observation");
        accessRights.put("AllergyIntolerance","AllergyIntolerance");
        accessRights.put("MedicationRequest","MedicationRequest");
        accessRights.put("MedicationStatement","MedicationStatement");
        accessRights.put("Immunization","Immunization");
        accessRights.put("Medication","Medication");
        accessRights.put("ReferralRequest","ReferralRequest");

        accessRights.put("DocumentReference","DocumentReference");
        accessRights.put("Binary","Binary");
        accessRights.put("Bundle","Bundle");
        return accessRights;
    }

    @Override
    public boolean incomingRequestPreProcessed(HttpServletRequest theRequest, HttpServletResponse theResponse) {

        String resourcePath = theRequest.getPathInfo();
        logger.info("Accessing Resource" + resourcePath);
        if (excludedPaths.contains(resourcePath)){
            logger.info("Accessing unprotected resource" + resourcePath);
            return true;
        }

        String authorizationHeader = theRequest.getHeader(HttpHeaders.AUTHORIZATION);
        if (authorizationHeader == null){
            logger.warn("OAuth2 Authentication failure.  No OAuth Token supplied in Authorization Header on Request.");
            throw new AuthenticationException("Unauthorised access to protected resource");
        }
        OAuthToken oAuthToken = OAuthTokenUtil.parseOAuthToken(authorizationHeader);

        // Check that the OAuth Token has not expired
        if (oAuthToken.isExpired()){
            logger.warn("OAuth2 Authentication failure due to expired token");
            throw new AuthenticationException("OAuth2 Authentication Token has expired.");
        }

        // Check that the Scopes on the Token allow access to the specified resource
        String resourceName = extractResourceName(resourcePath);
        if (!allowedAccess(resourceName, theRequest.getMethod(), oAuthToken)){
            logger.warn("OAuth2 Authentication failed due to insufficient access rights: ");
            throw new ForbiddenOperationException(String.format("Insufficient Access Rights to access %s.", resourceName));
        }

        logger.debug("Authenticated Access to " + resourcePath);
        return true;
    }

    /**
     * Check if the Scopes on the OAuth Token allow access to the specified Resource
     *
     * @param resourceName
     * @param method
     * @param oAuthToken
     * @return
     */
    public boolean allowedAccess(String resourceName, String method, OAuthToken oAuthToken) {
        if (accessRights.containsKey(resourceName)){
            String requiredAccess = accessRights.get(resourceName);
            return oAuthToken.allowsAccess(requiredAccess, method);
        }
        logger.info(String.format("Access to %s is unrestricted.", resourceName));
        return true;
    }

    public String extractResourceName(String resourcePath) {
        Matcher match = RESOURCE_PATTERN.matcher(resourcePath);
        if (!match.matches()){
            logger.warn(String.format("%s does not match secured pattern", resourcePath));
            return "";
        }
        return match.group(1);
    }

}
