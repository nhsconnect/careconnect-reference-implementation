package uk.nhs.careconnect.ri.gateway.https.oauth2;

import org.junit.Test;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.*;

public class OAuth2InterceptorTest {

    OAuth2Interceptor interceptor = new OAuth2Interceptor();

    @Test
    public void testExtractResourceName_Patient() {
        assertThat(interceptor.extractResourceName("/Patient/4"), equalTo("Patient"));
        assertThat(interceptor.extractResourceName("/Patient"), equalTo("Patient"));
        assertThat(interceptor.extractResourceName("/Patient?id=1"), equalTo("Patient"));
    }

    @Test
    public void testExtractResourceName_AllergyIntolerance() {
        assertThat(interceptor.extractResourceName("/AllergyIntolerance/4"), equalTo("AllergyIntolerance"));
        assertThat(interceptor.extractResourceName("/AllergyIntolerance"), equalTo("AllergyIntolerance"));
        assertThat(interceptor.extractResourceName("/AllergyIntolerance?id=1"), equalTo("AllergyIntolerance"));
    }

    @Test
    public void testExtractResourceName_MetaData() {
        assertThat(interceptor.extractResourceName("/metadata"), equalTo("metadata"));
    }

}