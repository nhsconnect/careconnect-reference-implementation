package uk.nhs.careconnect.ccri.fhirserver;

import ca.uhn.fhir.context.FhirContext;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.hl7.fhir.dstu3.model.CapabilityStatement;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.UnknownHostException;

@RestController
public class OpenAPIService {

    @Autowired
    private ApplicationContext appCtx;

    @Autowired
    private FhirContext ctx;

    @Value("${server.port}")
    private String serverPort;

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(OpenAPIService.class);

    @RequestMapping("/openapi")
    public String greeting() {
        HttpClient client = getHttpClient();

        HttpGet request = new HttpGet("http://127.0.0.1:"+serverPort+"/ccri-fhir/STU3/metadata");
        request.setHeader(HttpHeaders.CONTENT_TYPE, "application/json");
        request.setHeader(HttpHeaders.ACCEPT, "application/json");

        try {

            HttpResponse response = client.execute(request);
            log.trace("Response "+response.getStatusLine().toString());
            if (response.getStatusLine().toString().contains("200")) {

                String encoding = "UTF-8";
                String body = IOUtils.toString(response.getEntity().getContent(), encoding);
                log.trace(body);

                CapabilityStatement capabilityStatement = (CapabilityStatement) ctx.newJsonParser().parseResource(body);
                log.trace("Parsing");
                return parseConformanceStatement(capabilityStatement);

            }

        } catch (UnknownHostException e) {
            log.error("Host not known");
        } catch (IOException ex) {
            log.error("IO Exception " + ex.getMessage());
        }

        return "hello FHIR";
    }

    private String parseConformanceStatement(CapabilityStatement capabilityStatement) {
        JSONObject obj = new JSONObject();

        obj.put("openapi", "3.0.0");

        JSONObject info = new JSONObject();
        obj.put("info",info);

        info.put("version",HapiProperties.getSoftwareVersion());
        info.put("title",HapiProperties.getServerName());
        info.put("description","A reference implementation of the "+HapiProperties.getServerName()+" which conforms to the <a href=\"https://nhsconnect.github.io/CareConnectAPI/\">Care Connect API</a> ");
        info.put("termsOfService","http://swagger.io/terms/");
        info.put("basePath","/STU3");
        info.put("schemes", new JSONArray().put("http"));

        JSONObject paths = new JSONObject();
        obj.put("paths",paths);

        JSONObject resObjC = new JSONObject();
        paths.put("/STU3/metadata",resObjC);
        JSONObject opObjC = new JSONObject();
        resObjC.put("get",opObjC);
        opObjC.put("description","FHIR Server Capability Statement");
        opObjC.put("consumes", new JSONArray());
        JSONArray pc = new JSONArray();
        pc.put("application/fhir+json");
        pc.put("application/fhir+xml");
        opObjC.put("produces",pc);
        JSONArray paramsC = new JSONArray();
        opObjC.put("parameters", paramsC);
        opObjC.put("responses", getResponses());

        for (CapabilityStatement.CapabilityStatementRestComponent rest : capabilityStatement.getRest()) {
            for (CapabilityStatement.CapabilityStatementRestResourceComponent resourceComponent : rest.getResource()) {


                //paths.put("/"+resourceComponent.getType(),resObj);

                for (CapabilityStatement.ResourceInteractionComponent interactionComponent : resourceComponent.getInteraction()) {
                    JSONObject resObj = new JSONObject();
                    JSONObject opObj = new JSONObject();

                    switch (interactionComponent.getCode()) {
                        case READ:
                            paths.put("/STU3/"+resourceComponent.getType()+"/{id}",resObj);
                            resObj.put("get",opObj);
                            opObj.put("description",resourceComponent.getType());
                            opObj.put("consumes", new JSONArray());
                            JSONArray p = new JSONArray();
                            p.put("application/fhir+json");
                            p.put("application/fhir+xml");
                            opObj.put("produces",p);
                            JSONArray params = new JSONArray();
                            opObj.put("parameters", params);
                            JSONObject parm = new JSONObject();
                            params.put(parm);
                            parm.put("name","id");
                            parm.put("in", "path");
                            parm.put("description", "The logical id of the resource");
                            parm.put("required", true);
                            parm.put("schema",  new JSONObject()
                                .put("type","string"));
                            opObj.put("responses", getResponses());
                            break;
                        case SEARCHTYPE:
                            paths.put("/STU3/"+resourceComponent.getType(),resObj);
                            resObj.put("get",opObj);
                            opObj.put("description",resourceComponent.getType());
                            opObj.put("consumes", new JSONArray());
                            JSONArray ps = new JSONArray();
                            ps.put("application/fhir+json");
                            ps.put("application/fhir+xml");
                            opObj.put("produces",ps);
                            JSONArray paramss = new JSONArray();
                            opObj.put("parameters", paramss);
                            for ( CapabilityStatement.CapabilityStatementRestResourceSearchParamComponent search : resourceComponent.getSearchParam()) {
                                JSONObject parms = new JSONObject();
                                paramss.put(parms);
                                parms.put("name", search.getName());
                                parms.put("in", "query");
                                parms.put("description", search.getDocumentation());
                                parms.put("required", false);
                                parms.put("schema",  new JSONObject()
                                        .put("type","string"));
                            }
                            opObj.put("responses", getResponses());
                            break;
                        case UPDATE:
                            paths.put("/STU3/"+resourceComponent.getType()+"/{id}",resObj);
                            resObj.put("put",opObj);
                            obj.put("description",resourceComponent.getType());
                            break;
                        case CREATE:
                            paths.put("/STU3/"+resourceComponent.getType(),resObj);
                            resObj.put("post",opObj);
                            obj.put("description",resourceComponent.getType());
                        break;

                    }
                }
            }
        }
        String retStr = obj.toString(2);
        log.trace(retStr);
        return retStr;
    }

    private JSONObject getResponses() {
        JSONObject obj = new JSONObject();
        obj.put("200", new JSONObject()
                .put("description","Success"));
        obj.put("400", new JSONObject()
                .put("description","Bad Request"));
        obj.put("404", new JSONObject()
                .put("description","Not Found"));
        obj.put("default", new JSONObject()
                .put("description","Unexpected error"));
        return obj;
    }

    private HttpClient getHttpClient() {
        final HttpClient httpClient = HttpClientBuilder.create().build();
        return httpClient;
    }
}
