package uk.nhs.careconnect.ccri.fhirserver.stu3.provider;

import ca.uhn.fhir.rest.server.IResourceProvider;

public interface ICCResourceProvider extends IResourceProvider {

    Long count();
}
