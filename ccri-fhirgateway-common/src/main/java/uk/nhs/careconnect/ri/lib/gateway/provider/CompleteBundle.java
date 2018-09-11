package uk.nhs.careconnect.ri.lib.gateway.provider;

import org.hl7.fhir.dstu3.model.*;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class CompleteBundle {
    private Bundle bundle;

    private Map<String,Practitioner> practitionerMap;

    private Map<String,Organization> organisationMap;

    private Map<String,Location> locationMap;

    PractitionerResourceProvider practitionerProvider;

    OrganisationResourceProvider organistionProvider;

    LocationResourceProvider locationProvider;

    public CompleteBundle(PractitionerResourceProvider practitionerProvider,OrganisationResourceProvider organistionProvider,  LocationResourceProvider locationProvider) {
        bundle = new Bundle( );
        practitionerMap = new HashMap<>();

        organisationMap = new HashMap<>();
        this.practitionerProvider = practitionerProvider;
        this.organistionProvider = organistionProvider;
        this.locationProvider = locationProvider;
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

    public void addGetPractitioner(IdType id) throws Exception {
        if (id !=null) {
            Practitioner practitioner = practitionerMap.get(id.getIdPart());
            if (practitioner == null) {
                practitioner = practitionerProvider.getPractitionerById(null, id);
                if (practitioner != null) {
                    bundle.addEntry().setResource(practitioner);
                    practitionerMap.put(id.getIdPart(), practitioner);
                }
            }
        }
    }

    public void addGetOrganisation(IdType id) throws Exception {
        if (id != null) {
            Organization organization = organisationMap.get(id.getIdPart());
            if (organization == null) {
                organization = organistionProvider.getOrganizationById(null, id);
                if (organization != null) {
                    bundle.addEntry().setResource(organization);
                    organisationMap.put(id.getIdPart(), organization);
                }
            }
        }
    }

    public void addGetLocation(IdType id) throws Exception {
        if (id != null) {
            Location location = locationMap.get(id.getIdPart());
            if (location == null) {
                location = locationProvider.getLocationById(null, id);
                if (location != null) {
                    bundle.addEntry().setResource(location);
                    locationMap.put(id.getIdPart(), location);
                }
            }
        }
    }
}
