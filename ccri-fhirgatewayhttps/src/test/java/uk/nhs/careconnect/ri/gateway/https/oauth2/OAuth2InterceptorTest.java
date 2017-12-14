package uk.nhs.careconnect.ri.gateway.https.oauth2;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

public class OAuth2InterceptorTest {

    OAuth2Interceptor interceptor = new OAuth2Interceptor();

    /**
     * Example Access Token with the following Scopes
     *     user/MedicationStatement.read
     *     patient/MedicationOrder.write
     *     patient/Immunization.write
     *     profile patient/Immunization.read 
     *     patient/MedicationOrder.read
     *     patient/Patient.read
     *     patient/Encounter.write
     *     patient/Condition.write
     *     patient/Condition.read
     *     patient/Patient.write
     *     patient/Observation.read
     *     user/MedicationStatement.write
     *     patient/Encounter.read
     *     patient/Observation.write
     */
    public static final String PATIENT_ACCESS_TOKEN = "bearer eyJhbGciOiJSUzI1NiJ9.eyJhdWQiOiJlZDczYjJjYi1hYmQwLTRmNzUtYjlhMi01ZjljMDUzNWI4MmMiLCJzY29wZSI6InVzZXJcL01lZGljYXRpb25TdGF0ZW1lbnQucmVhZCBwYXRpZW50XC9NZWRpY2F0aW9uT3JkZXIud3JpdGUgcGF0aWVudFwvSW1tdW5pemF0aW9uLndyaXRlIHByb2ZpbGUgcGF0aWVudFwvSW1tdW5pemF0aW9uLnJlYWQgcGF0aWVudFwvTWVkaWNhdGlvbk9yZGVyLnJlYWQgcGF0aWVudFwvUGF0aWVudC5yZWFkIHBhdGllbnRcL0VuY291bnRlci53cml0ZSBwYXRpZW50XC9Db25kaXRpb24ud3JpdGUgcGF0aWVudFwvQ29uZGl0aW9uLnJlYWQgcGF0aWVudFwvUGF0aWVudC53cml0ZSBwYXRpZW50XC9PYnNlcnZhdGlvbi5yZWFkIHVzZXJcL01lZGljYXRpb25TdGF0ZW1lbnQud3JpdGUgcGF0aWVudFwvRW5jb3VudGVyLnJlYWQgcGF0aWVudFwvT2JzZXJ2YXRpb24ud3JpdGUiLCJpc3MiOiJodHRwOlwvXC9wdXJwbGUudGVzdGxhYi5uaHMudWs6MjAwODBcLyIsImV4cCI6MTUxMzI1MTQ4OCwiaWF0IjoxNTEzMjQ3ODg4LCJqdGkiOiIwZWE3MDAzNi04ODI4LTQ1ZTktYTVhOS1lYzhhYjVmOTJjYjMifQ.hlEN8PZlGDumfPdalYpRW9ZjX4mTmGIFaUqkZvLTX1F5kffUAwASU5keI70l56XNzlYfGCOKqPa8RGw1tpfVnjFGijJcMoPMzbmFwzoxBjy7KlDhz2s7DF1EdIvzJZhGsE7exk6GbNHsFovL3O-cT4fRsBBF79ztQIlMIJQsYG9bWurx-t9iFXXEmvJtgMDHkfT09RU4j5cm9NkYux0r_q5Q4WbVry_-InUf1ONtml45otXQKRBn-Wh_EZpxto1MxXwqXkZIrKLmnx1SWPofZgEMbcS6keN-DjNWlnQImpOrVDq_o4ugP7Rq_h-GgmRyh3UDDwAA9yok-lF7Z3aJRQ";

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

    @Test
    public void allowedAccessToPatient() {
        OAuthToken authToken = OAuthTokenUtil.parseOAuthToken(PATIENT_ACCESS_TOKEN);

        assertThat(authToken, notNullValue());
        assertThat(authToken.getScopes().contains("patient/Patient.read"), equalTo(true));
        assertThat(authToken.allowsAccess("Patient", "GET"), equalTo(true));
        // Allowed Access
        assertThat(interceptor.allowedAccess("Patient", "GET", authToken), equalTo(true));
        assertThat(interceptor.allowedAccess("MedicationStatement", "GET", authToken), equalTo(true));
        assertThat(interceptor.allowedAccess("Encounter", "GET", authToken), equalTo(true));
        assertThat(interceptor.allowedAccess("Observation", "GET", authToken), equalTo(true));
        assertThat(interceptor.allowedAccess("MedicationStatement", "GET", authToken), equalTo(true));

        // Prevented Access
        assertThat(interceptor.allowedAccess("AllergyIntolerance", "GET", authToken), equalTo(false));
        assertThat(interceptor.allowedAccess("MedicationRequest", "GET", authToken), equalTo(false));

    }
}