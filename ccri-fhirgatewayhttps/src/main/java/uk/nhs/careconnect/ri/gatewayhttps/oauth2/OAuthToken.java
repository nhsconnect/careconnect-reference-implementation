package uk.nhs.careconnect.ri.gatewayhttps.oauth2;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

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

    public boolean isExpired(){
        return expiry < System.currentTimeMillis()/1000;
    }

}
