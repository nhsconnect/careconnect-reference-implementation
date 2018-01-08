package uk.nhs.careconnect.ri.gatewaylib.provider;

import org.hl7.fhir.dstu3.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class CompleteBundle {
    private Bundle bundle;

    private Map<String,Practitioner> practitionerMap;

    private Map<String,Organization> organisationMap;

    PractitionerResourceProvider practitionerProvider;

    OrganisationResourceProvider organistionProvider;

    public CompleteBundle(PractitionerResourceProvider practitionerProvider,OrganisationResourceProvider organistionProvider) {
        bundle = new Bundle( );
        practitionerMap = new HashMap<>();

        organisationMap = new HashMap<>();
        this.practitionerProvider = practitionerProvider;
        this.organistionProvider = organistionProvider;
    }

    public Bundle getBundle() {
        return bundle;
    }

    public void setBundle(Bundle bundle) {
        this.bundle = bundle;
    }

    public Map<String, Practitioner> getPractitionerMap() {
        return practitionerMap;
    }

    public void setPractitionerMap(Map<String, Practitioner> practitionerMap) {
        this.practitionerMap = practitionerMap;
    }

    public Map<String, Organization> getOrganisationMap() {
        return organisationMap;
    }

    public void setOrganisationMap(Map<String, Organization> organisationMap) {
        this.organisationMap = organisationMap;
    }

    public void addGetPractitioner(IdType id) {
        Practitioner practitioner = practitionerMap.get(id.getIdPart());
        if (practitioner == null) {
            practitioner = practitionerProvider.getPractitionerById(null, id);
            if (practitioner != null) {
                bundle.addEntry().setResource(practitioner);
                practitionerMap.put(id.getIdPart(),practitioner);
            }
        }
    }

    public void addGetOrganisation(IdType id) {
        Organization organization = organisationMap.get(id.getIdPart());
        if (organization == null) {
            organization = organistionProvider.getOrganizationById(null, id);
            if (organization != null) {
                bundle.addEntry().setResource(organization);
                organisationMap.put(id.getIdPart(),organization);
            }
        }
    }
}
