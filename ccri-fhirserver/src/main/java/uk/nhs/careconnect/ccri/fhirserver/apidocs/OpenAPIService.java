package uk.nhs.careconnect.ccri.fhirserver.apidocs;

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
import uk.nhs.careconnect.ccri.fhirserver.HapiProperties;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

@RestController
public class OpenAPIService {

    // Swagger guide https://swagger.io/docs/specification/2-0/basic-structure/

    @Autowired
    private ApplicationContext appCtx;

    @Autowired
    private FhirContext ctx;

    @Value("${server.port}")
    private String serverPort;

    @Value("${server.servlet.context-path}")
    private String serverPath;

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(OpenAPIService.class);

    @RequestMapping("/apidocs")
    public String greeting() {
        HttpClient client = getHttpClient();

        String apidocs = "http://localhost:"+serverPort+serverPath+"/STU3/metadata";

        HttpGet request = new HttpGet(apidocs);
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

        return "Unable to resolve swagger/openapi documentation from " + apidocs;
    }

    private String parseConformanceStatement(CapabilityStatement capabilityStatement) {
        JSONObject obj = new JSONObject();

        obj.put("swagger", "2.0");

        JSONObject info = new JSONObject();
        obj.put("info",info);

        info.put("version", HapiProperties.getSoftwareVersion());
        info.put("title",HapiProperties.getServerName());
        info.put("description","A reference implementation of the "+HapiProperties.getServerName()+" which conforms to the <a href=\"https://nhsconnect.github.io/CareConnectAPI/\" target=\"_blank\">Care Connect API</a> ");
        info.put("termsOfService","http://swagger.io/terms/");
        info.put("basePath",serverPath +"/STU3");
        info.put("schemes", new JSONArray().put("http"));

        JSONObject paths = new JSONObject();
        obj.put("paths",paths);

        JSONObject resObjC = new JSONObject();
        paths.put(serverPath +"/STU3/metadata",resObjC);
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

        Map pathMap= new HashMap<String, JSONObject>();

        for (CapabilityStatement.CapabilityStatementRestComponent rest : capabilityStatement.getRest()) {
            for (CapabilityStatement.CapabilityStatementRestResourceComponent resourceComponent : rest.getResource()) {

                for (CapabilityStatement.ResourceInteractionComponent interactionComponent : resourceComponent.getInteraction()) {
                    JSONObject resObj = null;
                    switch (interactionComponent.getCode()) {
                        case READ:
                            if (pathMap.containsKey(serverPath + "/STU3/"+resourceComponent.getType()+"/{id}")) {
                                resObj = (JSONObject) pathMap.get(serverPath + "/STU3/"+resourceComponent.getType()+"/{id}");
                            } else {
                                resObj = new JSONObject();
                                pathMap.put(serverPath + "/STU3/"+resourceComponent.getType()+"/{id}",resObj);
                                paths.put(serverPath + "/STU3/"+resourceComponent.getType()+"/{id}",resObj);
                            }
                            resObj.put("get",getId(resourceComponent, interactionComponent));
                            break;
                        case SEARCHTYPE:
                            if (pathMap.containsKey(serverPath + "/STU3/"+resourceComponent.getType())) {
                                resObj = (JSONObject) pathMap.get(serverPath + "/STU3/"+resourceComponent.getType());
                            } else {
                                resObj = new JSONObject();
                                pathMap.put(serverPath + "/STU3/"+resourceComponent.getType(),resObj);
                                paths.put(serverPath + "/STU3/"+resourceComponent.getType(),resObj);
                            }
                            resObj.put("get",getSearch(resourceComponent, interactionComponent));
                            break;
                        case DELETE:
                            if (pathMap.containsKey(serverPath +"/STU3/"+resourceComponent.getType()+"/{id}")) {
                                resObj = (JSONObject) pathMap.get(serverPath +"/STU3/"+resourceComponent.getType()+"/{id}");
                            } else {
                                resObj = new JSONObject();
                                pathMap.put(serverPath +"/STU3/"+resourceComponent.getType()+"/{id}",resObj);
                                paths.put(serverPath +"/STU3/"+resourceComponent.getType()+"/{id}",resObj);
                            }
                            resObj.put("delete",getId(resourceComponent, interactionComponent));
                            break;
                        case UPDATE:
                            if (pathMap.containsKey(serverPath + "/STU3/"+resourceComponent.getType()+"/{id}")) {
                                resObj = (JSONObject) pathMap.get(serverPath + "/STU3/"+resourceComponent.getType()+"/{id}");
                            } else {
                                resObj = new JSONObject();
                                pathMap.put(serverPath + "/STU3/"+resourceComponent.getType()+"/{id}",resObj);
                                paths.put(serverPath + "/STU3/"+resourceComponent.getType()+"/{id}",resObj);
                            }
                            resObj.put("put",getId(resourceComponent, interactionComponent));
                            break;
                        case CREATE:

                            if (pathMap.containsKey(serverPath + "/STU3/"+resourceComponent.getType())) {
                                resObj = (JSONObject) pathMap.get(serverPath + "/STU3/"+resourceComponent.getType());
                            } else {
                                resObj = new JSONObject();
                                pathMap.put(serverPath + "/STU3/"+resourceComponent.getType(),resObj);
                                paths.put(serverPath + "/STU3/"+resourceComponent.getType(),resObj);
                            }

                            resObj.put("post",getSearch(resourceComponent, interactionComponent));
                            break;

                    }
                }
            }
        }
        String retStr = obj.toString(2);
        log.trace(retStr);
        return retStr;
    }




    private JSONObject getSearch(CapabilityStatement.CapabilityStatementRestResourceComponent resourceComponent,
                                 CapabilityStatement.ResourceInteractionComponent interactionComponent) {
        JSONObject opObj = new JSONObject();

        opObj.put("description","For detailed description see: "
         +"<a href=\"https://hl7.org/fhir/stu3/"+resourceComponent.getType()+".html\" target=\"_blank\">FHIR "+resourceComponent.getType()+"</a> ");
        JSONArray c = new JSONArray();
        c.put("application/fhir+json");
        c.put("application/fhir+xml");
        opObj.put("consumes", c);

        JSONArray ps = new JSONArray();
        ps.put("application/fhir+json");
        ps.put("application/fhir+xml");
        opObj.put("produces",ps);
        JSONArray paramss = new JSONArray();
        opObj.put("parameters", paramss);

        if (interactionComponent.getCode().equals(CapabilityStatement.TypeRestfulInteraction.SEARCHTYPE)) {
            for (CapabilityStatement.CapabilityStatementRestResourceSearchParamComponent search : resourceComponent.getSearchParam()) {
                JSONObject parms = new JSONObject();
                paramss.put(parms);
                parms.put("name", search.getName());
                parms.put("in", "query");
                parms.put("description", search.getDocumentation());
                parms.put("required", false);
               // parms.put("schema", new JSONObject()
                       parms.put("type", "string");
            }
        }
        if (interactionComponent.getCode().equals(CapabilityStatement.TypeRestfulInteraction.CREATE)) {

            JSONObject parm = new JSONObject();
            paramss.put(parm);
            parm.put("name","body");
            parm.put("in", "body");
            parm.put("description", "The resource ");
            parm.put("required", true);
           // parm.put("schema",  new JSONObject()
                    parm.put("type","object");
            opObj.put("responses", getResponses());
        }

        opObj.put("responses", getResponses());
        return opObj;
    }

    private JSONObject getId(CapabilityStatement.CapabilityStatementRestResourceComponent resourceComponent
            ,
                             CapabilityStatement.ResourceInteractionComponent interactionComponent) {
        JSONObject opObj = new JSONObject();
        opObj.put("description",
                "For detailed description see: <a href=\"https://hl7.org/fhir/stu3/"+resourceComponent.getType()+".html\" target=\"_blank\">FHIR "+resourceComponent.getType()+"</a> ");

        JSONArray c = new JSONArray();
        c.put("application/fhir+json");
        c.put("application/fhir+xml");
        opObj.put("consumes", c);
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
       // parm.put("schema",  new JSONObject()
                parm.put("type","string");
        opObj.put("responses", getResponses());
        if (interactionComponent.getCode().equals(CapabilityStatement.TypeRestfulInteraction.UPDATE)) {

            parm = new JSONObject();
            params.put(parm);
            parm.put("name","body");
            parm.put("in", "body");
            parm.put("description", "The resource ");
            parm.put("required", true);
            //parm.put("schema",  new JSONObject()
                    parm.put("type","object");
            opObj.put("responses", getResponses());
        }
        return opObj;
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
