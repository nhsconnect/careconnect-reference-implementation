package uk.nhs.careconnect.ccri.fhirserver.r4.provider;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.annotation.Operation;
import ca.uhn.fhir.rest.annotation.ResourceParam;
import ca.uhn.fhir.rest.annotation.Validate;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.api.ValidationModeEnum;
import org.hl7.fhir.convertors.VersionConvertor_30_40;
import org.hl7.fhir.r4.model.Resource;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component
public class ServerPlainProviderR4 {

    @Autowired
    FhirContext ctx;

    @Autowired
    private ResourceTestProviderR4 resourceTestProvider;

    private static final Logger log = LoggerFactory.getLogger(ServerPlainProviderR4.class);


    @Validate
    public MethodOutcome testResource(@ResourceParam IBaseResource resource,
                                      @Validate.Mode ValidationModeEnum theMode,
                                     @Validate.Profile String theProfile) {

        log.debug("Validate using: "+theProfile);

        return resourceTestProvider.testResource(resource,theMode,theProfile);
    }

    @Operation(name = "$convert", idempotent = true)
    public IBaseResource convertJson(
            @ResourceParam IBaseResource resource
    ) throws Exception {
        return resource;

    }




}
