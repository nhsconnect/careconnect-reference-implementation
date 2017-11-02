package uk.nhs.careconnect.ri.integrationTest;

import ca.uhn.fhir.context.FhirContext;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.*;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Location;
import org.hl7.fhir.dstu3.model.Patient;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

public class HttpTestClient {

    private static String DEFAULT_SERVER_BASE_URL = "http://127.0.0.1:8080/careconnect-ri/STU3/";
    private final FhirContext ctx;

    private HttpResponse response = null;

    private Bundle bundle = null;

    private String serverBaseUrl = null;

    HttpTestClient(FhirContext ctx) {
        this.ctx = ctx;
    }

    private HttpClient getHttpClient(){
        final HttpClient httpClient = HttpClientBuilder.create().build();
        return httpClient;
    }

    private String constructFullUrl(String url){
        return getServerBaseUrl() + url;
    }

    private String getServerBaseUrl() {
        if (serverBaseUrl == null ){
            serverBaseUrl = System.getProperty("serverBaseUrl", DEFAULT_SERVER_BASE_URL);
        }
        return serverBaseUrl;
    }

    public String encodeUrl(String url) {
        url = url.replace(" ","%20");
        url = url.replace("|","%7C");
        return url;
    }

    public int getResponseCode() throws IOException {
        return response.getStatusLine().getStatusCode();
    }

    public String getHeader(String header) {
        /*
        Header[] headers = response.getAllHeaders();
        for (Header headeritem : headers) {
            System.out.println("Key : " + headeritem.getName()
                    + " ,Value : " + headeritem.getValue());
        }
        */
        return response.getFirstHeader(header).getValue();
    }

    public void doGet(String httpUrl) throws Exception {
        final HttpClient client = getHttpClient();
        final String query = encodeUrl(constructFullUrl(httpUrl));
        final HttpGet request = new HttpGet(query);
        response = client.execute(request);
    }

    public void doDelete(String httpUrl) throws Exception {
        final HttpClient client = getHttpClient();
        final HttpDelete request = new HttpDelete(constructFullUrl(httpUrl));
        response = client.execute(request);
    }

    public void doHead(String httpUrl) throws Exception {
        final HttpClient client = getHttpClient();
        final HttpHead request = new HttpHead(constructFullUrl(httpUrl));
        response = client.execute(request);
    }

    public void doPatch(String httpUrl, String body) throws Exception {
        final HttpClient client = getHttpClient();
        final HttpPatch request = new HttpPatch(constructFullUrl(httpUrl));
        request.setEntity(new StringEntity(body));
        response = client.execute(request);
    }

    public void doPost(String httpUrl, String body) throws Exception {
        final HttpClient client = getHttpClient();
        final HttpPost request = new HttpPost(constructFullUrl(httpUrl));
        if (body != null && !body.isEmpty()) {
            request.setEntity(new StringEntity(body));
        }
        response = client.execute(request);
    }

    public int countResources() {
        if (bundle == null) return -1;
        return bundle.getEntry().size();
    }

    public void convertReplytoBundle(){
        try {
            Reader reader = new InputStreamReader(this.response.getEntity().getContent());
            bundle = (Bundle) ctx.newJsonParser().parseResource(reader);
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }

    public Boolean checkResourceType(String resource) {
        if (bundle == null) return null;

        for (Bundle.BundleEntryComponent entry : bundle.getEntry()) {
            if (!entry.getResource().getResourceType().toString().equals(resource)) return false;
        }
        return true;
    }

    public String getFirstPatientId() {
        if (bundle == null) return null;

        for (Bundle.BundleEntryComponent entry : bundle.getEntry()) {
            Patient patient = (Patient) entry.getResource();
            return patient.getIdElement().getIdPart();
        }
        return null;
    }

    public List<String> getPatientIds() {
        if (bundle == null) return null;

        List<String> ids = new ArrayList<>();
        for (Bundle.BundleEntryComponent entry : bundle.getEntry()) {
            Patient patient = (Patient) entry.getResource();
            ids.add(patient.getIdElement().getIdPart());
        }
        return ids;
    }

    public List<String> getLocationIds() {
        if (bundle == null) return null;

        List<String> ids = new ArrayList<>();
        for (Bundle.BundleEntryComponent entry : bundle.getEntry()) {
            Location location = (Location) entry.getResource();
            ids.add(location.getIdElement().getIdPart());
        }
        return ids;
    }
}
