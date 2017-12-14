package uk.nhs.careconnect.ri.gateway.https.oauth2;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

public class OAuthTokenTest {

    public static final String TOKEN_JSON =  "{ \"aud\": \"413c1fcb-235f-48ed-bb28-5c80e30b5e84\", \"scope\": \"patient/Patient.write patient/Patient.read openid offline_access\", " +
            "\"iss\": \"http://purple.testlab.nhs.uk:20080/\", \"exp\": 1513178048,\"iat\": 1513174448,\"jti\": \"9638eb06-ab71-47d1-9da4-ed98bde3791e\" }";

    private OAuthToken oAuthToken;

    @Before
    public void setup() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        oAuthToken = mapper.readValue(TOKEN_JSON.getBytes(), OAuthToken.class);
    }


    @Test
    public void testGetScopes() {
        assertThat(oAuthToken.getScopes(), notNullValue());
        assertThat(oAuthToken.getScopes().size(), equalTo(4));
        assertThat(oAuthToken.getScopes().contains("patient/Patient.write"), equalTo(true));
        assertThat(oAuthToken.getScopes().contains("patient/Patient.read"), equalTo(true));
        assertThat(oAuthToken.getScopes().contains("openid"), equalTo(true));
        assertThat(oAuthToken.getScopes().contains("offline_access"), equalTo(true));
    }

    @Test
    public void testAllowsAccessToPatient() {
        assertThat(oAuthToken.allowsAccess("Patient", "GET"), equalTo(true));
        assertThat(oAuthToken.allowsAccess("Patient", "POST"), equalTo(true));
    }

    @Test
    public void testPreventsAccessToMedication() {
        assertThat(oAuthToken.allowsAccess("Medication", "GET"), equalTo(false));
        assertThat(oAuthToken.allowsAccess("Medication", "POST"), equalTo(false));
    }
}