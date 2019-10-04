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
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.nhs.careconnect.ccri.fhirserver.HapiProperties;
import uk.nhs.careconnect.ccri.fhirserver.support.FhirMediaType;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

@RestController
public class OpenAPIService {

        // Swagger guide https://swagger.io/docs/specification/2-0/basic-structure/


        @Autowired
        private FhirContext ctx;

        @Value("${server.port}")
        private String serverPort;

        @Value("${server.servlet.context-path}")
        private String serverPath;

        private static final String FHIR_VERSION = "/STU3/";

        JSONObject paths = null;

        private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(OpenAPIService.class);

        @GetMapping(path = "/apidocs")
        public String greeting() {
            HttpClient client = getHttpClient();

            String apidocs = "http://localhost:"+serverPort+serverPath+"/STU3/metadata";

            HttpGet request = new HttpGet(apidocs);
            request.setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
            request.setHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);

            try {

                HttpResponse response = client.execute(request);
                log.trace("Response {}", response.getStatusLine());
                if (response.getStatusLine().toString().contains("200")) {

                    String encoding = "UTF-8";
                    String body = IOUtils.toString(response.getEntity().getContent(), encoding);
                    log.trace("{}",body);

                    CapabilityStatement capabilityStatement = (CapabilityStatement) ctx.newJsonParser().parseResource(body);

                    return parseConformanceStatement(capabilityStatement);

                }

            } catch (UnknownHostException e) {
                log.error("Host not known");
            } catch (IOException ex) {
                log.error("IO Exception {}", ex.getMessage());
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

            paths = new JSONObject();
            obj.put("paths",paths);

            JSONObject resObjC = new JSONObject();
            paths.put(serverPath +"/STU3/metadata",resObjC);
            JSONObject opObjC = new JSONObject();
            resObjC.put("get",opObjC);
            opObjC.put("description","FHIR Server Capability Statement");
            opObjC.put("consumes", new JSONArray());
            JSONArray pc = new JSONArray();
            pc.put(FhirMediaType.APPLICATION_FHIR_JSON_VALUE);
            pc.put(FhirMediaType.APPLICATION_FHIR_XML_VALUE);
            opObjC.put("produces",pc);
            JSONArray paramsC = new JSONArray();
            opObjC.put("parameters", paramsC);
            opObjC.put("responses", getResponses());

            Map pathMap= new HashMap<String, JSONObject>();

            for (CapabilityStatement.CapabilityStatementRestComponent rest : capabilityStatement.getRest()) {
                for (CapabilityStatement.CapabilityStatementRestResourceComponent resourceComponent : rest.getResource()) {

                    for (CapabilityStatement.ResourceInteractionComponent interactionComponent : resourceComponent.getInteraction()) {

                        switch (interactionComponent.getCode()) {
                            case READ:
                                processMethodId("get", pathMap, resourceComponent, interactionComponent);
                                break;
                            case SEARCHTYPE:
                                processMethodType("get", pathMap, resourceComponent, interactionComponent);
                                break;
                            case DELETE:
                                processMethodId("delete", pathMap, resourceComponent, interactionComponent);
                                break;
                            case UPDATE:
                                processMethodId("put", pathMap, resourceComponent, interactionComponent);
                                break;
                            case CREATE:
                                processMethodType("post", pathMap, resourceComponent, interactionComponent);
                                break;
                            default:
                        }
                    }
                }
            }
            String retStr = obj.toString(2);
            log.trace(retStr);
            return retStr;
        }

        private void processMethodId(String method, Map pathMap,
                                     CapabilityStatement.CapabilityStatementRestResourceComponent resourceComponent,
                                     CapabilityStatement.ResourceInteractionComponent interactionComponent) {
            JSONObject resObj = null;
            if (pathMap.containsKey(serverPath + FHIR_VERSION+resourceComponent.getType()+"/{id}")) {
                resObj = (JSONObject) pathMap.get(serverPath + FHIR_VERSION+resourceComponent.getType()+"/{id}");
            } else {
                resObj = new JSONObject();
                pathMap.put(serverPath + FHIR_VERSION+resourceComponent.getType()+"/{id}",resObj);
                paths.put(serverPath + FHIR_VERSION+resourceComponent.getType()+"/{id}",resObj);
            }
            resObj.put(method,getId(resourceComponent, interactionComponent));
        }
        private void processMethodType(String method, Map pathMap,
                                       CapabilityStatement.CapabilityStatementRestResourceComponent resourceComponent,
                                       CapabilityStatement.ResourceInteractionComponent interactionComponent) {
            JSONObject resObj = null;
            if (pathMap.containsKey(serverPath + FHIR_VERSION+resourceComponent.getType())) {
                resObj = (JSONObject) pathMap.get(serverPath + FHIR_VERSION+resourceComponent.getType());
            } else {
                resObj = new JSONObject();
                pathMap.put(serverPath + FHIR_VERSION+resourceComponent.getType(),resObj);
                paths.put(serverPath + FHIR_VERSION+resourceComponent.getType(),resObj);
            }
            resObj.put(method,getSearch(resourceComponent, interactionComponent));

        }


        private JSONObject getSearch(CapabilityStatement.CapabilityStatementRestResourceComponent resourceComponent,
                                     CapabilityStatement.ResourceInteractionComponent interactionComponent) {
            JSONObject opObj = new JSONObject();

            opObj.put("description","For detailed description see: "
                    +"<a href=\"https://hl7.org/fhir/stu3/"+resourceComponent.getType()+".html\" target=\"_blank\">FHIR "+resourceComponent.getType()+"</a> ");
            JSONArray c = new JSONArray();
            c.put(FhirMediaType.APPLICATION_FHIR_JSON_VALUE);
            c.put(FhirMediaType.APPLICATION_FHIR_XML_VALUE);
            opObj.put("consumes", c);

            JSONArray ps = new JSONArray();
            ps.put(FhirMediaType.APPLICATION_FHIR_JSON_VALUE);
            ps.put(FhirMediaType.APPLICATION_FHIR_XML_VALUE);
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
            c.put(FhirMediaType.APPLICATION_FHIR_JSON_VALUE);
            c.put(FhirMediaType.APPLICATION_FHIR_XML_VALUE);
            opObj.put("consumes", c);
            JSONArray p = new JSONArray();
            p.put(FhirMediaType.APPLICATION_FHIR_JSON_VALUE);
            p.put(FhirMediaType.APPLICATION_FHIR_XML_VALUE);
            opObj.put("produces",p);
            JSONArray params = new JSONArray();
            opObj.put("parameters", params);
            JSONObject parm = new JSONObject();
            params.put(parm);
            parm.put("name","id");
            parm.put("in", "path");
            parm.put("description", "The logical id of the resource");
            parm.put("required", true);

            parm.put("type","string");
            opObj.put("responses", getResponses());
            if (interactionComponent.getCode().equals(CapabilityStatement.TypeRestfulInteraction.UPDATE)) {

                parm = new JSONObject();
                params.put(parm);
                parm.put("name","body");
                parm.put("in", "body");
                parm.put("description", "The resource ");
                parm.put("required", true);

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
            return HttpClientBuilder.create().build();
        }
    }
