package uk.nhs.careconnect.ccri.fhirserver.stu3.provider;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.annotation.*;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.api.ValidationModeEnum;
import org.hl7.fhir.convertors.VersionConvertor_30_40;
import org.hl7.fhir.dstu3.model.Resource;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.InputStream;


@Component
public class ServerPlainProvider {

    @Autowired
    FhirContext ctx;

    @Autowired
    private ResourceTestProvider resourceTestProvider;

    private static final Logger log = LoggerFactory.getLogger(ServerPlainProvider.class);


    @Validate
    public MethodOutcome testResource(@ResourceParam IBaseResource resource,
                                      @Validate.Mode ValidationModeEnum theMode,
                                      @OptionalParam(name = "profile") @Validate.Profile String theProfile) {

        log.debug("Validate using: "+theProfile);

        return resourceTestProvider.testResource(resource,theMode,theProfile);
    }

    @Operation(name = "$convert", idempotent = true)
    public IBaseResource convertJson(
            @ResourceParam IBaseResource resource
    ) throws Exception {
        return resource;

    }

    @Operation(name = "$convertR4", idempotent = true)
    public IBaseResource convertR4(
            @ResourceParam IBaseResource resource
    ) throws Exception {

        VersionConvertor_30_40 convertor = new VersionConvertor_30_40();
        org.hl7.fhir.dstu3.model.Resource resourceR3 = (Resource) resource;
        org.hl7.fhir.r4.model.Resource resourceR4 = convertor.convertResource(resourceR3,true);

        return resourceR4;

    }




}
