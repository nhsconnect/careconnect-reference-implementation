package uk.nhs.careconnect.ccri.fhirserver.stu3.provider;

import ca.uhn.fhir.context.FhirVersionEnum;
import ca.uhn.fhir.parser.DataFormatException;
import ca.uhn.fhir.rest.annotation.Metadata;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.RestfulServer;
import ca.uhn.fhir.rest.server.RestulfulServerConfiguration;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.hl7.fhir.dstu3.hapi.rest.server.ServerCapabilityStatementProvider;
import org.hl7.fhir.dstu3.model.*;
import org.hl7.fhir.dstu3.model.CapabilityStatement.ResourceInteractionComponent;
import org.hl7.fhir.instance.model.api.IPrimitiveType;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import uk.nhs.careconnect.ccri.fhirserver.HapiProperties;
import uk.org.hl7.fhir.core.Stu3.CareConnectExtension;
import uk.org.hl7.fhir.core.Stu3.CareConnectProfile;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.UnknownHostException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


// 16/June/2019
@Configuration
public class CareConnectServerConformanceProvider extends ServerCapabilityStatementProvider {

    @Autowired
    private CareConnectServerConformanceProvider ccscp;


    private boolean myCache = true;
    private volatile CapabilityStatement capabilityStatement;

    private RestulfulServerConfiguration serverConfiguration;

    private RestfulServer restfulServer;

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CareConnectServerConformanceProvider.class);

    public String validate_flag2 = null;

    private JSONObject openIdObj;

    private Instant lastRefresh;


    public CareConnectServerConformanceProvider() {
        super();

    }

    @Override
    public void setRestfulServer(RestfulServer theRestfulServer) {

        serverConfiguration = theRestfulServer.createConfiguration();
        restfulServer = theRestfulServer;
        super.setRestfulServer(theRestfulServer);
    }

    @Override
    @Metadata
    public CapabilityStatement getServerConformance(HttpServletRequest theRequest) {


        if (capabilityStatement != null) {
            if (lastRefresh != null) {
                java.time.Duration duration = java.time.Duration.between(Instant.now(), lastRefresh);
                // May need to revisit
                if ((duration.getSeconds() * 60) < 1) return capabilityStatement;
                capabilityStatement = null;
            }
        }
        lastRefresh = Instant.now();

        WebApplicationContext ctx = WebApplicationContextUtils.getWebApplicationContext(theRequest.getServletContext());
        log.info("restful2 Server not null = " + HapiProperties.getValidationFlag());


        if (capabilityStatement != null && myCache) {
            return capabilityStatement;
        }

        capabilityStatement = super.getServerConformance(theRequest);

        capabilityStatement.setPublisher("NHS Digital & Dept for Work and Pensions");
        capabilityStatement.setDateElement(conformanceDate());
        capabilityStatement.setFhirVersion(FhirVersionEnum.DSTU3.getFhirVersionString());
        capabilityStatement.setAcceptUnknown(CapabilityStatement.UnknownContentCode.EXTENSIONS); // TODO: make this configurable - this is a fairly big
        // effort since the parser
        // needs to be modified to actually allow it

        capabilityStatement.getImplementation().setDescription(serverConfiguration.getImplementationDescription());
        capabilityStatement.setKind(CapabilityStatement.CapabilityStatementKind.INSTANCE);


        capabilityStatement.getSoftware().setName(HapiProperties.getSoftwareName());
        capabilityStatement.getSoftware().setVersion(HapiProperties.getSoftwareVersion());
        capabilityStatement.getImplementation().setDescription(HapiProperties.getServerName());
        capabilityStatement.getImplementation().setUrl(HapiProperties.getServerBase());

        // KGM only add if not already present
        if (capabilityStatement.getImplementationGuide().size() == 0) {
            capabilityStatement.getImplementationGuide().add(new UriType(HapiProperties.getSoftwareImplementationDesc()));
            capabilityStatement.setPublisher("NHS Digital & Dept for Work and Pensions");
        }

        if (restfulServer != null) {
            log.info("restful Server not null");
            for (CapabilityStatement.CapabilityStatementRestComponent nextRest : capabilityStatement.getRest()) {
                nextRest.setMode(CapabilityStatement.RestfulCapabilityMode.SERVER);

                if (HapiProperties.getSecurityOauth()) {

                    nextRest.getSecurity()
                            .addService().addCoding()
                            .setSystem("http://hl7.org/fhir/restful-security-service")
                            .setDisplay("SMART-on-FHIR")
                            .setSystem("SMART-on-FHIR");

                    if (HapiProperties.getSecurityOpenidConfig() != null) {
                        Extension securityExtension = nextRest.getSecurity().addExtension()
                                .setUrl("http://fhir-registry.smarthealthit.org/StructureDefinition/oauth-uris");
                        HttpClient client = getHttpClient();
                        HttpGet request = new HttpGet(HapiProperties.getSecurityOpenidConfig());
                        request.setHeader(HttpHeaders.CONTENT_TYPE, "application/json");
                        request.setHeader(HttpHeaders.ACCEPT, "application/json");
                        if (openIdObj == null) {
                            try {

                                HttpResponse response = client.execute(request);
                                //System.out.println(response.getStatusLine());
                                if (response.getStatusLine().toString().contains("200")) {
                                    InputStreamReader reader = new InputStreamReader(response.getEntity().getContent());
                                    BufferedReader bR = new BufferedReader(reader);
                                    String line = "";

                                    StringBuilder responseStrBuilder = new StringBuilder();
                                    while ((line = bR.readLine()) != null) {

                                        responseStrBuilder.append(line);
                                    }
                                    openIdObj = new JSONObject(responseStrBuilder.toString());
                                }
                            } catch (UnknownHostException e) {
                                System.out.println("Host not known");
                            } catch (Exception ex) {
                                System.out.println(ex.getMessage());
                            }
                        }
                        if (openIdObj != null) {
                            if (openIdObj.has("token_endpoint")) {
                                securityExtension.addExtension()
                                        .setUrl("token")
                                        .setValue(new UriType(openIdObj.getString("token_endpoint")));
                            }
                            if (openIdObj.has("authorization_endpoint")) {
                                securityExtension.addExtension()
                                        .setUrl("authorize")
                                        .setValue(new UriType(openIdObj.getString("authorization_endpoint")));
                            }
                            if (openIdObj.has("register_endpoint")) {
                                securityExtension.addExtension()
                                        .setUrl("register")
                                        .setValue(new UriType(openIdObj.getString("register_endpoint")));
                            }
                        }
                    } else {
                        if (HapiProperties.getSecurityOauth2Authorize() != null && HapiProperties.getSecurityOauth2Register() != null && HapiProperties.getSecurityOauth2Token() != null) {

                            Extension securityExtension = nextRest.getSecurity().addExtension()
                                    .setUrl("http://fhir-registry.smarthealthit.org/StructureDefinition/oauth-uris");

                            securityExtension.addExtension()
                                    .setUrl("authorize")
                                    .setValue(new UriType(HapiProperties.getSecurityOauth2Authorize()));

                            securityExtension.addExtension()
                                    .setUrl("register")
                                    .setValue(new UriType(HapiProperties.getSecurityOauth2Register()));

                            securityExtension.addExtension()
                                    .setUrl("token")
                                    .setValue(new UriType(HapiProperties.getSecurityOauth2Token()));
                        }
                    }
                }

                if (HapiProperties.getServerRole().equals("EPRCareConnectAPI")) {
                    // jira https://airelogic-apilabs.atlassian.net/browse/ALP4-815
                    nextRest.setOperation(new ArrayList<>());
                }

                for (CapabilityStatement.CapabilityStatementRestResourceComponent restResourceComponent : nextRest.getResource()) {


                    if (HapiProperties.getServerRole().equals("EPRCareConnectAPI")) {
                        // jira https://airelogic-apilabs.atlassian.net/browse/ALP4-815
                        restResourceComponent.setSearchInclude(new ArrayList<>());
                    }

                    log.debug("restResourceComponent.getType - " + restResourceComponent.getType());
                    setProfile(restResourceComponent);


                    List<ResourceInteractionComponent> remove = new ArrayList<>();
                    for (ResourceInteractionComponent l : restResourceComponent.getInteraction()) {
                        if (!HapiProperties.getServerCrudRead())
                            if (l.getCode().toString().equals("READ")) {
                                remove.add(l);
                            }

                        if (!HapiProperties.getServerCrudUpdate())
                            if (l.getCode().toString().equals("UPDATE")) {
                                remove.add(l);
                            }

                        if (!HapiProperties.getServerCrudCreate())
                            if (l.getCode().toString().equals("CREATE")) {
                                remove.add(l);
                            }

                        if (!HapiProperties.getServerCrudDelete())
                            if (l.getCode().toString().equals("DELETE")) {
                                remove.add(l);
                            }
                    }

                    restResourceComponent.getInteraction().removeAll(remove);

                    log.debug("restResourceComponent.getType() = " + restResourceComponent.getType());
                    for (IResourceProvider provider : restfulServer.getResourceProviders()) {

                        log.trace("Provider Resource - " + provider.getResourceType().getSimpleName());
                        if (restResourceComponent.getType().equals(provider.getResourceType().getSimpleName())
                                || (restResourceComponent.getType().contains("List") && provider.getResourceType().getSimpleName().contains("List")))
                            if (provider instanceof ICCResourceProvider) {
                                log.debug("ICCResourceProvider - " + provider.getClass());
                                ICCResourceProvider resourceProvider = (ICCResourceProvider) provider;

                                Extension extension = restResourceComponent.getExtensionFirstRep();
                                if (extension == null) {
                                    extension = restResourceComponent.addExtension();
                                }
                                extension.setUrl("http://hl7api.sourceforge.net/hapi-fhir/res/extdefs.html#resourceCount")
                                        .setValue(new DecimalType(resourceProvider.count()));
                            }
                    }
                    getOperations(restResourceComponent, nextRest);
                }

/*
                for (CapabilityStatement.CapabilityStatementRestResourceComponent restResourceComponent : nextRest.getResource()) {
                    if (restResourceComponent.getType().equals("StructureDefinition")) {
                        nextRest.getResource().remove(restResourceComponent);
                        break;
                    }
                }
                */
                for (CapabilityStatement.CapabilityStatementRestResourceComponent restResourceComponent : nextRest.getResource()) {

                    if (restResourceComponent.getType().equals("OperationDefinition")) {
                        nextRest.getResource().remove(restResourceComponent);
                        break;
                    }
                }
            }
        }

        return capabilityStatement;
    }

    private void getOperations(CapabilityStatement.CapabilityStatementRestResourceComponent resource, CapabilityStatement.CapabilityStatementRestComponent rest) {
// Fix for HAPI putting operations as system level entries
        if (rest.getOperation() != null) {
            for (CapabilityStatement.CapabilityStatementRestOperationComponent operationComponent : rest.getOperation()) {
                if (operationComponent.hasDefinition() && operationComponent.getDefinition().hasReference()) {
                    String[] elements = operationComponent.getDefinition().getReference().split("-");
                    if (elements.length > 2) {
                        log.debug(operationComponent.getDefinition().getReference());
                        String[] defArray = elements[0].split("/");
                        if (defArray.length > 1 && defArray[1].equals(resource.getType())) {
                            log.debug("MATCH");
                            Extension extension = resource.addExtension()
                                    .setUrl(CareConnectExtension.UrlCapabilityStatementRestOperation);
                            extension.addExtension()
                                    .setUrl("name")
                                    .setValue(new StringType(operationComponent.getName()));
                            if (operationComponent.getName().equals("validate")) {
                                extension.addExtension()
                                        .setUrl("definition")
                                        .setValue(new Reference("http://hl7.org/fhir/OperationDefinition/Resource-validate"));
                            }
                            if (operationComponent.getName().equals("expand")) {
                                extension.addExtension()
                                        .setUrl("definition")
                                        .setValue(new Reference("http://hl7.org/fhir/OperationDefinition/ValueSet-expand"));
                            }
                            if (operationComponent.getName().equals("translate")) {
                                extension.addExtension()
                                        .setUrl("definition")
                                        .setValue(new Reference("http://hl7.org/fhir/OperationDefinition/ConceptMap-translate"));
                            }
                        }
                    }
                }
            }
        }
    }


    public void setProfile(CapabilityStatement.CapabilityStatementRestResourceComponent resource) {
        switch (resource.getType()) {
            case "Patient":
                resource.getProfile().setReference(CareConnectProfile.Patient_1);
                break;
            case "Practitioner":
                resource.getProfile().setReference(CareConnectProfile.Practitioner_1);
                break;
            case "PractitionerRole":
                resource.getProfile().setReference(CareConnectProfile.PractitionerRole_1);
                break;
            case "Organization":
                log.info("Org called");
                resource.getProfile().setReference(CareConnectProfile.Organization_1);
                break;
            case "Location":
                resource.getProfile().setReference(CareConnectProfile.Location_1);
                break;
            case "Observation":
                // Observation is not currently profiled on STU3 Care Connect
                resource.getProfile().setReference(CareConnectProfile.Observation_1);
                break;
            case "Encounter":
                resource.getProfile().setReference(CareConnectProfile.Encounter_1);
                break;
            case "Condition":
                resource.getProfile().setReference(CareConnectProfile.Condition_1);
                break;
            case "Procedure":
                resource.getProfile().setReference(CareConnectProfile.Procedure_1);
                break;
            case "Immunization":
                resource.getProfile().setReference(CareConnectProfile.Immunization_1);
                break;
            case "MedicationRequest":
                resource.getProfile().setReference(CareConnectProfile.MedicationRequest_1);
                break;
            case "MedicationStatement":
                resource.getProfile().setReference(CareConnectProfile.MedicationStatement_1);
                break;
            case "AllergyIntolerance":
                resource.getProfile().setReference(CareConnectProfile.AllergyIntolerance_1);
                break;
            case "Medication":
                resource.getProfile().setReference(CareConnectProfile.Medication_1);
                break;
            case "Flag":
                resource.getProfile().setReference("https://fhir.hl7.org.uk/STU3/StructureDefinition/CareConnect-Flag-1");
                break;
            case "DocumentReference":
                resource.getProfile().setReference("https://fhir.nhs.uk/STU3/StructureDefinition/GPConnect-DocumentReference-1");
                break;


        }

    }

    private HttpClient getHttpClient(){
        final HttpClient httpClient = HttpClientBuilder.create().build();
        return httpClient;
    }

    private DateTimeType conformanceDate() {
        IPrimitiveType<Date> buildDate = serverConfiguration.getConformanceDate();
        if (buildDate != null) {
            try {
                return new DateTimeType(buildDate.getValue());
            } catch (DataFormatException e) {
                // fall through
            }
        }
        return DateTimeType.now();
    }
}
