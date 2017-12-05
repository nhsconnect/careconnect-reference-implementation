package uk.nhs.careconnect.ri.gateway.https.oauth2;

import ca.uhn.fhir.rest.server.exceptions.AuthenticationException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNull.notNullValue;

import static org.junit.Assert.assertThat;

public class OAuthTokenUtilTest {

    private static final String JWT_TOKEN = "eyJhbGciOiJSUzI1NiJ9.eyJhdWQiOiJjY3JpIiwiaXNzIjoiaHR0cDpcL1wvcHVycGxlLnRlc3RsYWIubmhzLnVrOjIwMDgwXC8iLCJleHAiOjE1MTIzOTkwNDUsImlhdCI6MTUxMjM5NTQ0NiwianRpIjoiZDA1ZGM5YjgtMjZhMi00Y2EyLWExMzUtOGU0N2U0ZjYxYWMyIn0.jwNzPWOL1NQunGTc-MJKAiOLZhXAPm4_fi8eU3OWbc9tWwkKyQ_N-posHcvXAT7Z0dGYNGkzLvykR2td-Beo5gnYpOzhlCtruEuKniN871ElahgOxgowrFbX5vkl5BCvhXT-ut1VQoXDeqoMTh9sxrSE14vVbkzurNr3LWxdoWo4zru-ZnvjNUshzd7D636vSaScnwIuk7NU-9lI32z-YsByQcrw3cEjhJN7_wu66Zuod1D7PV_7TS_AuhUgOiWPm8vT7QctEFTjFrs0fppUqRtXBXO1PHAZJmBLVL2zMFblh3A6mJN82MOR5Sdhs1Djz6bigIyOf6XA16tFvEYthg";

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
