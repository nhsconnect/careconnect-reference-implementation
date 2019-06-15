package uk.nhs.careconnect.ccri.fhirserver;

import org.springframework.stereotype.Component;
import springfox.documentation.swagger.web.SwaggerResource;

import java.util.ArrayList;
import java.util.List;

@Component
public class ServiceDefinitionsContext {


    public List<SwaggerResource> getSwaggerDefinitions() {

        List<SwaggerResource> resources = new ArrayList<>();
        SwaggerResource resource = new SwaggerResource();
        resource.setLocation("/openapi" );
        resource.setName(HapiProperties.getServerName());
        resource.setSwaggerVersion("2.0");
        resources.add(resource);
        return  resources;
    }
}
