package uk.nhs.careconnect.ccri.fhirserver.provider;

import ca.uhn.fhir.context.FhirVersionEnum;
import ca.uhn.fhir.parser.DataFormatException;
import ca.uhn.fhir.rest.annotation.Metadata;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.RestfulServer;
import ca.uhn.fhir.rest.server.RestulfulServerConfiguration;
import org.hl7.fhir.dstu3.hapi.rest.server.ServerCapabilityStatementProvider;
import org.hl7.fhir.dstu3.model.*;
import org.hl7.fhir.dstu3.model.CapabilityStatement.CapabilityStatementRestComponent;
import org.hl7.fhir.dstu3.model.CapabilityStatement.ResourceInteractionComponent;
import org.hl7.fhir.instance.model.api.IPrimitiveType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import uk.org.hl7.fhir.core.Stu3.CareConnectExtension;
import uk.org.hl7.fhir.core.Stu3.CareConnectProfile;

import javax.lang.model.type.ReferenceType;
import javax.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

	@Configuration
	public class CareConnectServerConformanceProvider extends ServerCapabilityStatementProvider {

	@Autowired
	private CareConnectServerConformanceProvider ccscp;
	
	
    
    private boolean myCache = true;
    private volatile CapabilityStatement capabilityStatement;

    private RestulfulServerConfiguration serverConfiguration;

    private RestfulServer restfulServer;

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CareConnectServerConformanceProvider.class);

    public String validate_flag2 = null ;

    private String oauth2authorize;

    private String oauth2token;

    private String oauth2register;

    private String oauth2;

    private Instant lastRefresh;
    
    
    public CareConnectServerConformanceProvider() {
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
    	
        String CRUD_update =  ctx.getEnvironment().getProperty("ccri.CRUD_update");
        String CRUD_delete = ctx.getEnvironment().getProperty("ccri.CRUD_delete");
        String CRUD_create = ctx.getEnvironment().getProperty("ccri.CRUD_create");
        String CRUD_read = ctx.getEnvironment().getProperty("ccri.CRUD_read");

        String CCRI_role = ctx.getEnvironment().getProperty("ccri.role");


        oauth2authorize = ctx.getEnvironment().getProperty("ccri.oauth2.authorize");
        oauth2token = ctx.getEnvironment().getProperty("ccri.oauth2.token");
        oauth2register = ctx.getEnvironment().getProperty("ccri.oauth2.register");
        oauth2 = ctx.getEnvironment().getProperty("ccri.oauth2");
    	    
        if (capabilityStatement != null && myCache) {
            return capabilityStatement;
        }

        capabilityStatement = super.getServerConformance(theRequest);

        capabilityStatement.setPublisher("NHS Digital");
        capabilityStatement.setDateElement(conformanceDate());
        capabilityStatement.setFhirVersion(FhirVersionEnum.DSTU3.getFhirVersionString());
        capabilityStatement.setAcceptUnknown(CapabilityStatement.UnknownContentCode.EXTENSIONS); // TODO: make this configurable - this is a fairly big
        // effort since the parser
        // needs to be modified to actually allow it

        capabilityStatement.getImplementation().setDescription(serverConfiguration.getImplementationDescription());
        capabilityStatement.setKind(CapabilityStatement.CapabilityStatementKind.INSTANCE);


        capabilityStatement.getSoftware().setName(System.getProperty("ccri.software.name"));
        capabilityStatement.getSoftware().setVersion(System.getProperty("ccri.software.version"));
        capabilityStatement.getImplementation().setDescription(System.getProperty("ccri.server"));
        capabilityStatement.getImplementation().setUrl(System.getProperty("ccri.server.base"));

        // KGM only add if not already present
        if (capabilityStatement.getImplementationGuide().size() == 0) {
            capabilityStatement.getImplementationGuide().add(new UriType(System.getProperty("ccri.guide")));
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

                if (CCRI_role.equals("EPRCareConnectAPI")) {
                    // jira https://airelogic-apilabs.atlassian.net/browse/ALP4-815
                    nextRest.setOperation(new ArrayList<>());
                }

                for (CapabilityStatement.CapabilityStatementRestResourceComponent restResourceComponent : nextRest.getResource()) {


                    if (CCRI_role.equals("EPRCareConnectAPI")) {
                        // jira https://airelogic-apilabs.atlassian.net/browse/ALP4-815
                        restResourceComponent.setSearchInclude(new ArrayList<>());
                    }

                    log.info("restResourceComponent.getType - " + restResourceComponent.getType());
                    setProfile(restResourceComponent);

                    List<ResourceInteractionComponent> l = restResourceComponent.getInteraction();
                    List<ResourceInteractionComponent> remove = new ArrayList<>();
                    for (int i = 0; i < l.size(); i++)
                        if (CRUD_read.equals("false"))
                            if (restResourceComponent.getInteraction().get(i).getCode().toString().equals("READ")) {
                                remove.add(restResourceComponent.getInteraction().get(i));
                            }
                    for (int i = 0; i < l.size(); i++)
                        if (CRUD_update.equals("false"))
                            if (restResourceComponent.getInteraction().get(i).getCode().toString().equals("UPDATE")) {
                                remove.add(restResourceComponent.getInteraction().get(i));
                            }
                    for (int i = 0; i < l.size(); i++)
                        if (CRUD_create.equals("false"))
                            if (restResourceComponent.getInteraction().get(i).getCode().toString().equals("CREATE")) {
                                remove.add(restResourceComponent.getInteraction().get(i));
                            }
                    for (int i = 0; i < l.size(); i++)
                        if (CRUD_delete.equals("false"))
                            if (restResourceComponent.getInteraction().get(i).getCode().toString().equals("DELETE")) {
                                remove.add(restResourceComponent.getInteraction().get(i));
                            }

                    restResourceComponent.getInteraction().removeAll(remove);

                    log.info("restResourceComponent.getType() = " + restResourceComponent.getType());
                    for (IResourceProvider provider : restfulServer.getResourceProviders()) {

                        log.trace("Provider Resource - " + provider.getResourceType().getSimpleName());
                        if (restResourceComponent.getType().equals(provider.getResourceType().getSimpleName())
                                || (restResourceComponent.getType().contains("List") && provider.getResourceType().getSimpleName().contains("List")))
                            if (provider instanceof ICCResourceProvider) {
                                log.info("ICCResourceProvider - " + provider.getClass());
                                ICCResourceProvider resourceProvider = (ICCResourceProvider) provider;

                                Extension extension = restResourceComponent.getExtensionFirstRep();
                                if (extension == null) {
                                    extension = restResourceComponent.addExtension();
                                }
                                extension.setUrl("http://hl7api.sourceforge.net/hapi-fhir/res/extdefs.html#resourceCount")
                                        .setValue(new DecimalType(resourceProvider.count()));
                            }
                    }
                    getOperations(restResourceComponent,nextRest);
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
                    if (elements.length>2) {
                        log.info(operationComponent.getDefinition().getReference());
                        String[] defArray = elements[0].split("/");
                        if (defArray.length>1 && defArray[1].equals(resource.getType())) {
                            log.info("MATCH");
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
        switch(resource.getType()) {
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
