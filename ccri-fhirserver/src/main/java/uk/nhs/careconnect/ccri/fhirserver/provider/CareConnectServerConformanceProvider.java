package uk.nhs.careconnect.ccri.fhirserver.provider;

import ca.uhn.fhir.rest.annotation.Metadata;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.RestfulServer;
import ca.uhn.fhir.rest.server.RestulfulServerConfiguration;
import org.hl7.fhir.dstu3.hapi.rest.server.ServerCapabilityStatementProvider;
import org.hl7.fhir.dstu3.model.CapabilityStatement;
import org.hl7.fhir.dstu3.model.DecimalType;
import org.hl7.fhir.dstu3.model.Extension;

import javax.servlet.http.HttpServletRequest;


public class CareConnectServerConformanceProvider extends ServerCapabilityStatementProvider {

    private boolean myCache = true;
    private volatile CapabilityStatement myCapabilityStatement;

    private RestulfulServerConfiguration serverConfiguration;

    private RestfulServer restfulServer;

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CareConnectServerConformanceProvider.class);


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
        if (myCapabilityStatement != null && myCache) {
            return myCapabilityStatement;
        }
        CapabilityStatement myCapabilityStatement = super.getServerConformance(theRequest);


        /*
        if (serverConfiguration != null) {
            for (ResourceBinding resourceBinding : serverConfiguration.getResourceBindings()) {
                log.info("resourceBinding.getResourceName() = "+resourceBinding.getResourceName());
                log.info("resourceBinding.getMethodBindings().size() = "+resourceBinding.getMethodBindings().size());
            }
        }
        */
        if (restfulServer != null) {
            log.trace("restful Server not null");
            for (CapabilityStatement.CapabilityStatementRestComponent nextRest : myCapabilityStatement.getRest()) {
                for (CapabilityStatement.CapabilityStatementRestResourceComponent restResourceComponent : nextRest.getResource()) {
                    log.trace("restResourceComponent.getType - " + restResourceComponent.getType());
                   for (IResourceProvider provider : restfulServer.getResourceProviders()) {

                        log.trace("Provider Resource - " + provider.getResourceType().getSimpleName());
                        if (restResourceComponent.getType().equals(provider.getResourceType().getSimpleName())
                                || (restResourceComponent.getType().contains("List") && provider.getResourceType().getSimpleName().contains("List")))
                            if (provider instanceof ICCResourceProvider) {
                                log.trace("ICCResourceProvider - " + provider.getClass());
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


        return myCapabilityStatement;
    }

}
