package uk.nhs.careconnect.ccri.fhirserver.r4.provider;

import ca.uhn.fhir.parser.DataFormatException;
import ca.uhn.fhir.rest.annotation.Metadata;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.RestfulServer;
import ca.uhn.fhir.rest.server.RestulfulServerConfiguration;
import org.hl7.fhir.instance.model.api.IPrimitiveType;
import org.hl7.fhir.r4.hapi.rest.server.ServerCapabilityStatementProvider;
import org.hl7.fhir.r4.model.*;
import org.hl7.fhir.r4.model.CapabilityStatement.ResourceInteractionComponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import uk.nhs.careconnect.ccri.fhirserver.HapiProperties;
import uk.nhs.careconnect.ccri.fhirserver.stu3.provider.ICCResourceProvider;

import javax.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Configuration
public class CareConnectServerConformanceR4Provider extends ServerCapabilityStatementProvider {

    @Autowired
    private CareConnectServerConformanceR4Provider ccscp;


    private boolean myCache = true;
    private volatile CapabilityStatement capabilityStatement;

    private RestulfulServerConfiguration serverConfiguration;

    private RestfulServer restfulServer;

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CareConnectServerConformanceR4Provider.class);

    public String validate_flag2 = null;

    private String oauth2authorize;

    private String oauth2token;

    private String oauth2register;

    private String oauth2;

    private Instant lastRefresh;


    public CareConnectServerConformanceR4Provider() {
        super();
//    validate_flag2 = this.env.getProperty("ccri.validate_flag");
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
                if ((duration.getSeconds() * 60) < 2) return capabilityStatement;
            }
        }
        lastRefresh = Instant.now();

        WebApplicationContext ctx = WebApplicationContextUtils.getWebApplicationContext(theRequest.getServletContext());
        log.info("restful2 Server not null = " + ctx.getEnvironment().getProperty("ccri.validate_flag"));


        if (capabilityStatement != null && myCache) {
            return capabilityStatement;
        }

        capabilityStatement = super.getServerConformance(theRequest);

        capabilityStatement.setPublisher("Mayfield IS");
        capabilityStatement.setDateElement(conformanceDate());
        capabilityStatement.setFhirVersion(Enumerations.FHIRVersion._4_0_0);

        capabilityStatement.getImplementation().setDescription(serverConfiguration.getImplementationDescription());
        capabilityStatement.setKind(CapabilityStatement.CapabilityStatementKind.INSTANCE);


        capabilityStatement.getSoftware().setName(System.getProperty("ccri.software.name"));
        capabilityStatement.getSoftware().setVersion(System.getProperty("ccri.software.version"));
        capabilityStatement.getImplementation().setDescription(System.getProperty("ccri.server"));
        capabilityStatement.getImplementation().setUrl(System.getProperty("ccri.server.base"));

// KGM only add if not already present
        if (capabilityStatement.getImplementationGuide().size() == 0) {
            capabilityStatement.getImplementationGuide().add(new CanonicalType(System.getProperty("ccri.guide")));
            capabilityStatement.setPublisher("NHS Digital");
        }

        if (restfulServer != null) {
            log.info("restful Server not null");
            for (CapabilityStatement.CapabilityStatementRestComponent nextRest : capabilityStatement.getRest()) {
                nextRest.setMode(CapabilityStatement.RestfulCapabilityMode.SERVER);

                // KGM only add if not already present
                if (nextRest.getSecurity().getService().size() == 0 && oauth2.equals("true")) {
                    if (oauth2token != null && oauth2register != null && oauth2authorize != null) {
                        nextRest.getSecurity()
                                .addService().addCoding()
                                .setSystem("http://hl7.org/fhir/restful-security-service")
                                .setDisplay("SMART-on-FHIR")
                                .setSystem("SMART-on-FHIR");
                        Extension securityExtension = nextRest.getSecurity().addExtension()
                                .setUrl("http://fhir-registry.smarthealthit.org/StructureDefinition/oauth-uris");

                        securityExtension.addExtension()
                                .setUrl("authorize")
                                .setValue(new UriType(oauth2authorize));

                        securityExtension.addExtension()
                                .setUrl("register")
                                .setValue(new UriType(oauth2register));

                        securityExtension.addExtension()
                                .setUrl("token")
                                .setValue(new UriType(oauth2token));
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
                    // Removed, no R4 profiles setProfile(restResourceComponent);


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
                    // Need to revist getOperations(restResourceComponent, nextRest);
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

/*
private void getOperations(CapabilityStatement.CapabilityStatementRestResourceComponent resource, CapabilityStatement.CapabilityStatementRestComponent rest) {
// Fix for HAPI putting operations as system level entries
if (rest.getOperation() != null) {
for (CapabilityStatement.CapabilityStatementRestOperationComponent operationComponent : rest.getOperation()) {
if (operationComponent.hasDefinition() && operationComponent.getDefinition().hasReference()) {
String[] elements = operationComponent.getDefinition().getReference().split("-");
if (elements.length>2) {
log.debug(operationComponent.getDefinition().getReference());
String[] defArray = elements[0].split("/");
if (defArray.length>1 && defArray[1].equals(resource.getType())) {
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

 */


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
