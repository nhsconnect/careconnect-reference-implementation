package uk.nhs.careconnect.ri.provider.practitioner;

import ca.uhn.fhir.model.primitive.IdDt;
import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.Read;
import ca.uhn.fhir.rest.annotation.RequiredParam;
import ca.uhn.fhir.rest.annotation.Search;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.exceptions.InternalErrorException;
import org.hl7.fhir.instance.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.nhs.careconnect.ri.SystemURL;
import uk.nhs.careconnect.ri.dao.Practitioner.PractitionerSearch;
import uk.nhs.careconnect.ri.model.practitioner.PractitionerDetails;
import uk.org.hl7.fhir.core.dstu2.CareConnectProfile;
import uk.org.hl7.fhir.core.dstu2.CareConnectSystem;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Component
public class PractitionerResourceProvider  implements IResourceProvider {

    @Autowired
    private PractitionerSearch practitionerSearch;

    @Override
    public Class<Practitioner> getResourceType() {
        return Practitioner.class;
    }

    @Read()
    public Practitioner getPractitionerById(@IdParam IdType practitionerId) {
        PractitionerDetails practitionerDetails = practitionerSearch.findPractitionerDetails(practitionerId.getIdPart());

        if (practitionerDetails == null) {
            OperationOutcome operationalOutcome = new OperationOutcome();
         // TODO   operationalOutcome.addIssue().setSeverity(OperationOutcome.IssueSeverity.ERROR).setDetails("No practitioner details found for practitioner ID: " + practitionerId.getIdPart());
            throw new InternalErrorException("No practitioner details found for practitioner ID: " + practitionerId.getIdPart(), operationalOutcome);
        }

        return practitionerDetailsToPractitionerResourceConverter(practitionerDetails);
    }

    @Search
    public List<Practitioner> getPractitionerByPractitionerUserId(@RequiredParam(name = Practitioner.SP_IDENTIFIER) TokenParam practitionerId) {
        return practitionerSearch.findPractitionerByUserId(practitionerId.getValue())
                .stream()
                .map(this::practitionerDetailsToPractitionerResourceConverter)
                .collect(Collectors.toList());
    }

    private Practitioner practitionerDetailsToPractitionerResourceConverter(PractitionerDetails practitionerDetails) {
        Practitioner practitioner = new Practitioner()
                .addIdentifier(new Identifier().setSystem(CareConnectSystem.SDSUserId).setValue(practitionerDetails.getUserId()));

        practitionerDetails.getRoleIds()
                .stream()
                .distinct()
                .map(roleId -> new Identifier().setSystem(CareConnectSystem.SDSJobRoleName).setValue(roleId))
                .forEach(practitioner::addIdentifier);

        practitioner.setId(new IdDt(practitionerDetails.getId()));

        practitioner.getMeta()
                .setLastUpdated(practitionerDetails.getLastUpdated())
                .setVersionId(String.valueOf(practitionerDetails.getLastUpdated().getTime()))
                .addProfile(CareConnectProfile.Practitioner_1);

        HumanName name = new HumanName()
                .addFamily(practitionerDetails.getNameFamily())
                .addGiven(practitionerDetails.getNameGiven())
                .addPrefix(practitionerDetails.getNamePrefix())
                .setUse(HumanName.NameUse.USUAL);

        practitioner.setName(name);

        switch (practitionerDetails.getGender().toLowerCase(Locale.UK)) {
            case "male":
                practitioner.setGender(Enumerations.AdministrativeGender.MALE);
                break;

            case "female":
                practitioner.setGender(Enumerations.AdministrativeGender.FEMALE);
                break;

            case "other":
                practitioner.setGender(Enumerations.AdministrativeGender.OTHER);
                break;

            default:
                practitioner.setGender(Enumerations.AdministrativeGender.UNKNOWN);
                break;
        }

        Coding roleCoding = new Coding().setSystem(SystemURL.VS_SDS_JOB_ROLE_NAME).setCode(practitionerDetails.getRoleCode())
                .setDisplay(practitionerDetails.getRoleDisplay());

        practitioner.addPractitionerRole()
                .setRole(new CodeableConcept().addCoding(roleCoding))
                .setManagingOrganization(new Reference("Organization/"+practitionerDetails.getOrganizationId())); // Associated Organisation

        Coding comCoding = new Coding().setSystem(SystemURL.VS_HUMAN_LANGUAGE).setCode(practitionerDetails.getComCode())
                .setDisplay(practitionerDetails.getComDisplay());

        practitioner.addCommunication().addCoding(comCoding);

        return practitioner;
    }
}
