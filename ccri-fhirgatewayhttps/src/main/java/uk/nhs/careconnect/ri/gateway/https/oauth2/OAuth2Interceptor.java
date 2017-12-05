package uk.nhs.careconnect.ri.gateway.https.oauth2;


import ca.uhn.fhir.rest.server.exceptions.AuthenticationException;
import ca.uhn.fhir.rest.server.interceptor.InterceptorAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

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

    private final String serverName;
    private final List<String> excludedPaths;

    Logger logger = LoggerFactory.getLogger(OAuth2Interceptor.class);

    public OAuth2Interceptor(String serverName) {
        this.serverName = serverName;
        this.excludedPaths = new ArrayList<>();
        excludedPaths.add("/metadata");
    }

    @Override
    public boolean incomingRequestPreProcessed(HttpServletRequest theRequest, HttpServletResponse theResponse) {

        String contextPath = theRequest.getContextPath();
        if (excludedPaths.contains(contextPath)){
            logger.info("Accessing unprotected resource" + contextPath);
            return true;
        }

        String authorizationHeader = theRequest.getHeader(HttpHeaders.AUTHORIZATION);
        if (authorizationHeader == null){
            logger.warn("OAuth2 Authentication failure.  No OAuth Token supplied in Authorization Header on Request.");
            throw new AuthenticationException("Unauthorised access to protected resource");
        }
        OAuthToken oAuthToken = OAuthTokenUtil.parseOAuthToken(authorizationHeader);

        // Check that the OAuth Token is for the correct server
        if (oAuthToken.issuer != serverName){
            logger.warn(String.format("OAuth2 Authentication failure.  Token issued for %1 not %2", oAuthToken, serverName));
            throw new AuthenticationException("Unauthorised access to protected resource");
        }

        // Check that the OAuth Token has not expired
        if (oAuthToken.isExpired()){
            logger.warn("OAuth2 Authentication failure due to expired token");
            throw new AuthenticationException();
        }
        logger.debug("Authenticated Access to " + contextPath);
        return true;
    }

}
