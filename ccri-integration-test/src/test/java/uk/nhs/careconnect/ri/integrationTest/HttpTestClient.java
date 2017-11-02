package uk.nhs.careconnect.ri.integrationTest;

import ca.uhn.fhir.context.FhirContext;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.*;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Location;
import org.hl7.fhir.dstu3.model.Organization;
import org.hl7.fhir.dstu3.model.Patient;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

public class HttpTestClient {

    private String careConnectServer = "http://127.0.0.1:8080/careconnect-gateway/STU3/";

    //private String careConnectServer = "http://purple.testlab.nhs.uk/careconnect-ri/STU3/";

    HttpClient client;

    HttpResponse response;

    FhirContext ctx = null;

    Bundle bundle = null;

    HttpTestClient(FhirContext ctx) {
        this.ctx = ctx;
    }

    public String encodeUrl(String url) {
        url = url.replace(" ","%20");

        url = url.replace("|","%7C");
        return url;
    }

    public int getResponseCode() throws IOException
    {
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

        client = HttpClientBuilder.create().build();
        String query = encodeUrl(careConnectServer+httpUrl);

        HttpGet request = new HttpGet(query);

        response = client.execute(request);

    }

    public void doDelete(String httpUrl) throws Exception {

        client = HttpClientBuilder.create().build();
        HttpDelete request = new HttpDelete(careConnectServer+httpUrl);
     //   System.out.println(request.getURI());
        response = client.execute(request);

    }

    public void doHead(String httpUrl) throws Exception {

        client = HttpClientBuilder.create().build();
        HttpHead request = new HttpHead(careConnectServer+httpUrl);
       // System.out.println(request.getURI());
        response = client.execute(request);

    }

    public void doPatch(String httpUrl, String body) throws Exception {

        client = HttpClientBuilder.create().build();
        HttpPatch request = new HttpPatch(careConnectServer+httpUrl);
        request.setEntity(new StringEntity(body));
       // System.out.println(request.getURI());
        response = client.execute(request);

    }

    public void doPost(String httpUrl, String body) throws Exception {

        client = HttpClientBuilder.create().build();
        HttpPost request = new HttpPost(careConnectServer+httpUrl);
        if (body != null && !body.isEmpty()) {
            request.setEntity(new StringEntity(body));
        }
        //System.out.println(request.getURI());
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

    public List<String> getOrganizationIds() {
        if (bundle == null) return null;

        List<String> ids = new ArrayList<>();
        for (Bundle.BundleEntryComponent entry : bundle.getEntry()) {
            Organization organization = (Organization) entry.getResource();
            ids.add(organization.getIdElement().getIdPart());
        }
        return ids;
    }


}
