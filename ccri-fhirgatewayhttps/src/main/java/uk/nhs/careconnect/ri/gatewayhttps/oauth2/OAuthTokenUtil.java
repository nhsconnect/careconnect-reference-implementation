package uk.nhs.careconnect.ri.gatewayhttps.oauth2;

import ca.uhn.fhir.rest.server.exceptions.AuthenticationException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.security.jwt.Jwt;
import org.springframework.security.jwt.JwtHelper;

import java.io.IOException;

public class OAuthTokenUtil {

    private static final String TOKEN_PREFIX = "bearer ";

    public static OAuthToken parseOAuthToken(String oauthToken){
        assert oauthToken != null:"OAuth Token should not be null";
        if (oauthToken.toLowerCase().startsWith(TOKEN_PREFIX)){
            return parseJwtToken(oauthToken.substring(TOKEN_PREFIX.length()));
        } else {
            throw new AuthenticationException("Invalid OAuth Token.  Missing Bearer prefix");
        }
    }

    private static OAuthToken parseJwtToken(String jwtToken) {
        try {
            Jwt jwt = JwtHelper.decode(jwtToken);
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(jwt.getClaims().getBytes(), OAuthToken.class);
        } catch (IOException e) {
            throw new AuthenticationException("Invalid OAuth2 Token", e);
        }
    }

}
