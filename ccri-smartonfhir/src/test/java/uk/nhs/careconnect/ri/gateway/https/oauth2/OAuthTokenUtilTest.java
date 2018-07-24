package uk.nhs.careconnect.ri.gateway.https.oauth2;

import ca.uhn.fhir.rest.server.exceptions.AuthenticationException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;

public class OAuthTokenUtilTest {

    private static final String JWT_TOKEN = "eyJhbGciOiJSUzI1NiJ9.eyJhdWQiOiJzYW5kX21hbiIsImlzcyI6Imh0dHBzOlwvXC9hdXRoLmhzcGNvbnNvcnRpdW0ub3JnXC8iLCJleHAiOjE1MTMyNDU1ODUsImlhdCI6MTUxMzE1OTE4NSwianRpIjoiYzUwMjRhNDQtMjcwMC00MzE3LWE1YTgtNjNiZDVmYzVlNTVkIn0.k-dGOjoe6kx-aE5lRnR6RXHGsyrLdgb_mmoDhZV_4JgI5y9x_17GLdL5SVsIBcmbBjkPQCwxEVJgtHTZNnMedfEk_HqDr-MLs9JNZIFPdhWquvA4Ys7IJqRaEwC3Y8ybYxnHAekM1wavBwIcaujQpcP3y_PWW0r2lyKfTQS15aqBEvBayxnnlJPpqp_RuZtygEyPNZr2CBi6f8-19rnQSFCtoYV5pQxEMoRdd5HdaDmhSn2Ra768AGG0KogZ-60cUQUBdxaWR-JchbwqjBAi2tmyU4g0O_SoHdJFm6_5t4RQYhZaK3x5pQi1R_uxrv1KS1e53v8-fEHFxTWPVyqV8Q";

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void parseOauth2TokenTest(){
        OAuthToken token = OAuthTokenUtil.parseOAuthToken("Bearer " + JWT_TOKEN);
        assertThat(token, notNullValue());
        assertThat(token.issuer, equalTo("http://purple.testlab.nhs.uk:20080/"));
        assertThat(token.audience, equalTo("ccri"));
        assertThat(token.expiry, equalTo(1512399045));
        assertThat(token.issuedAt, equalTo(1512395446));
        assertThat(token.isExpired(), equalTo(true));
    }

    @Test
    public void parseOauth2TokenWithoutPrefixTest(){
        thrown.expectMessage("Invalid OAuth Token.  Missing Bearer prefix");
        thrown.expect(AuthenticationException.class);
        OAuthTokenUtil.parseOAuthToken(JWT_TOKEN);
    }

    @Test
    public void parseNullTokenTest(){
        thrown.expectMessage("OAuth Token should not be null");
        thrown.expect(AssertionError.class);
        OAuthTokenUtil.parseOAuthToken(null);
    }

}
