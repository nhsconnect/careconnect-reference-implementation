package uk.nhs.careconnect.ri.facade.ccrismartonfhir.oauth2;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

/**
 * DTO which contains the details of a OAuth2 JWT Token
 *
 * These tokens will be passed in the Authorisation Header when a client uses OAuth2 authentication
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class OAuthToken {

    @JsonProperty("iss")
    public String issuer;

    @JsonProperty("aud")
    public String audience;

    @JsonProperty("exp")
    public Integer expiry;

    @JsonProperty("iat")
    public Integer issuedAt;

    @JsonProperty("scope")
    public String scope;

    public boolean isExpired(){
        return expiry < System.currentTimeMillis()/1000;
    }

    public List<String> getScopes(){
        return (scope != null) ? Arrays.asList(scope.split(" ")) : new ArrayList<>();
    }

    /**
     * Check if there are any scopes on the Token which grant access to the protected resource
     *
     * @param requiredScope
     * @param method
     * @return true is access is granted by 1 or more scopes otherwise false
     */
    public boolean allowsAccess(String requiredScope, String method){
        return getScopes().stream().anyMatch(getScopeFilter(requiredScope, method));
    }

    /**
     *  Create a Predicate which will be used to filter the list of available scopes.
     *
     * TODO - At present we are not associating the HTTP Request Method with the *.read, or *.write suffixes on the Scope
     * TODO - This is left as a future exercise since we are currently only allowing read-only access.
     *
     * @param requiredScoped - The Scope that is required to access the resource e.g. 'Patient'
     * @param method - The HTTP Method which is being processed e.g. 'GET' or 'POST'
     * @return A Predicate to find the Scopes which match this Resource.
     */
    private Predicate<String> getScopeFilter(String requiredScoped, String method) {
        // Match either the resource name or wildcard '*'
        String scopeRegex = String.format("^\\w*/(?:\\*|%1s)\\..*", requiredScoped);
        return scope -> scope.matches(scopeRegex);
    }
}
