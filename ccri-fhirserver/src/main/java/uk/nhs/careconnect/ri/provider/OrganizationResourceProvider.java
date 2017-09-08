package uk.nhs.careconnect.ri.provider;

import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.Read;
import ca.uhn.fhir.rest.annotation.RequiredParam;
import ca.uhn.fhir.rest.annotation.Search;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.rest.server.IResourceProvider;
import org.hl7.fhir.instance.model.IdType;
import org.hl7.fhir.instance.model.Organization;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class OrganizationResourceProvider implements IResourceProvider {



    @Override
    public Class<Organization> getResourceType() {
        return Organization.class;
    }


    @Read()
    public Organization getOrganizationById(@IdParam IdType organizationId) {



        return null;
    }

    @Search
    public List<Organization> getOrganizationsByODSCode(@RequiredParam(name = Organization.SP_IDENTIFIER) TokenParam tokenParam) {
       return null;
    }


    

}
