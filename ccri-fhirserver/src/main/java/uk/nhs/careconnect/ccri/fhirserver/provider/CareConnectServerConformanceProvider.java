package uk.nhs.careconnect.ccri.fhirserver.provider;

import ca.uhn.fhir.rest.annotation.Metadata;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.RestfulServer;
import ca.uhn.fhir.rest.server.RestulfulServerConfiguration;
import org.hl7.fhir.dstu3.hapi.rest.server.ServerCapabilityStatementProvider;
import org.hl7.fhir.dstu3.model.CapabilityStatement;
import org.hl7.fhir.dstu3.model.CapabilityStatement.ResourceInteractionComponent;
import org.hl7.fhir.dstu3.model.DecimalType;
import org.hl7.fhir.dstu3.model.Extension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

//import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.context.ApplicationContext;

@Configuration
public class CareConnectServerConformanceProvider extends ServerCapabilityStatementProvider {
	
	
	
@Value("${ccri.CRUD_read}")
private String CRUD_read12;

@Autowired
private CareConnectServerConformanceProvider ccscp;
/*	
	
    @Value("${ccri.CRUD_update}")
    private String CRUD_update;
    
    @Value("${ccri.CRUD_delete}")
    private String CRUD_delete;
    
    @Value("${ccri.CRUD_create}")
    private String CRUD_create;
    
    @Value("${ccri.role}")
    private String ccri_role;
    
    @Autowired
    FhirContext ctx;
	*/
	
  
	//private ApplicationContext applicationContext;
	//CareConnectServerConformanceProvider(ApplicationContext context) {
  //      this.applicationContext = context;
 //   }
	
    
    private boolean myCache = true;
    private volatile CapabilityStatement myCapabilityStatement;

    private RestulfulServerConfiguration serverConfiguration;

    private RestfulServer restfulServer;

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CareConnectServerConformanceProvider.class);

    public String validate_flag2 = null ;
    
    
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
    	
    	WebApplicationContext ctx = WebApplicationContextUtils.getWebApplicationContext(theRequest.getServletContext());
    	log.info("restful2 Server not null = " + ctx.getEnvironment().getProperty("ccri.validate_flag"));
    	
    	 	String CRUD_update =  ctx.getEnvironment().getProperty("ccri.CRUD_update");
    	 	String CRUD_delete = ctx.getEnvironment().getProperty("ccri.CRUD_delete");
    	 	String CRUD_create = ctx.getEnvironment().getProperty("ccri.CRUD_create");
    	 	String CRUD_read = ctx.getEnvironment().getProperty("ccri.CRUD_read");
    	 
        if (myCapabilityStatement != null && myCache) {
            return myCapabilityStatement;
        }
        
        CapabilityStatement myCapabilityStatement = super.getServerConformance(theRequest);
        log.info("autowired value = " + CRUD_read12);
        log.info("CRUD_read = " + CRUD_read + ", CRUD_update = " + CRUD_update + "CRUD_create = " + CRUD_create + ", CRUD_delete = " + CRUD_delete);
        if (restfulServer != null) {
            log.info("restful Server not null");
            for (CapabilityStatement.CapabilityStatementRestComponent nextRest : myCapabilityStatement.getRest()) {
                for (CapabilityStatement.CapabilityStatementRestResourceComponent restResourceComponent : nextRest.getResource()) {
                    log.info("restResourceComponent.getType - " + restResourceComponent.getType());
                    
                    List<ResourceInteractionComponent> l = restResourceComponent.getInteraction();
                    for(int i=0;i<l.size();i++)
                    	if(CRUD_read.equals("false"))
                    	if (restResourceComponent.getInteraction().get(i).getCode().toString()=="READ")
                    	{
                    		restResourceComponent.getInteraction().remove(i);
                    	}	
                    for(int i=0;i<l.size();i++)
                    	if(CRUD_update.equals("false"))
                    	if (restResourceComponent.getInteraction().get(i).getCode().toString()=="UPDATE")
                    	{
                    		restResourceComponent.getInteraction().remove(i);
                    	}	
                    for(int i=0;i<l.size();i++)
                    	if(CRUD_create.equals("false"))
                    	if (restResourceComponent.getInteraction().get(i).getCode().toString()=="CREATE")
                    	{
                    		restResourceComponent.getInteraction().remove(i);
                    	}	
                    for(int i=0;i<l.size();i++)
                    	if(CRUD_delete.equals("false"))
                    	if (restResourceComponent.getInteraction().get(i).getCode().toString()=="DELETE")
                    	{
                    		restResourceComponent.getInteraction().remove(i);
                    	}	
                    
                    
                   for (IResourceProvider provider : restfulServer.getResourceProviders()) {
                	   	
                        log.info("Provider Resource - " + provider.getResourceType().getSimpleName());
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
                }
            }
        }

        //myCapabilityStatement.children().get(1).getc
        

        return myCapabilityStatement;
    }

}
