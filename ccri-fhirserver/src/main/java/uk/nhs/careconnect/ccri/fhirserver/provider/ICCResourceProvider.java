package uk.nhs.careconnect.ccri.fhirserver.provider;

import ca.uhn.fhir.rest.server.IResourceProvider;

public interface ICCResourceProvider extends IResourceProvider {

    Long count();
}
