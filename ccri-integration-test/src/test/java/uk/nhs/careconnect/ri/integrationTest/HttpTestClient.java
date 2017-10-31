package uk.nhs.careconnect.ri.integrationTest;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.*;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.IOException;

public class HttpTestClient {

    private String careConnectServer = "http://127.0.0.1:8080/careconnect-gateway/STU3/";

    //private String careConnectServer = "http://purple.testlab.nhs.uk/careconnect-ri/STU3/";

    HttpClient client;

    HttpResponse response;


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
        HttpGet request = new HttpGet(careConnectServer+httpUrl);
     //   System.out.println(request.getURI());
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


}
