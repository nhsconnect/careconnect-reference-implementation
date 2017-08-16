package uk.gov.hscic.practitioner;

import ca.uhn.fhir.model.dstu2.composite.*;
import ca.uhn.fhir.model.dstu2.resource.OperationOutcome;
import ca.uhn.fhir.model.dstu2.resource.Practitioner;
import ca.uhn.fhir.model.dstu2.valueset.AdministrativeGenderEnum;
import ca.uhn.fhir.model.dstu2.valueset.IssueSeverityEnum;
import ca.uhn.fhir.model.dstu2.valueset.NameUseEnum;
import ca.uhn.fhir.model.primitive.IdDt;
import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.Read;
import ca.uhn.fhir.rest.annotation.RequiredParam;
import ca.uhn.fhir.rest.annotation.Search;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.exceptions.InternalErrorException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hscic.SystemURL;
import uk.gov.hscic.model.practitioner.PractitionerDetails;
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
    public Practitioner getPractitionerById(@IdParam IdDt practitionerId) {
        PractitionerDetails practitionerDetails = practitionerSearch.findPractitionerDetails(practitionerId.getIdPart());

        if (practitionerDetails == null) {
            OperationOutcome operationalOutcome = new OperationOutcome();
            operationalOutcome.addIssue().setSeverity(IssueSeverityEnum.ERROR).setDetails("No practitioner details found for practitioner ID: " + practitionerId.getIdPart());
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
                .addIdentifier(new IdentifierDt(CareConnectSystem.SDSUserId, practitionerDetails.getUserId()));

        practitionerDetails.getRoleIds()
                .stream()
                .distinct()
                .map(roleId -> new IdentifierDt(CareConnectSystem.SDSJobRoleName, roleId))
                .forEach(practitioner::addIdentifier);

        practitioner.setId(new IdDt(practitionerDetails.getId()));

        practitioner.getMeta()
                .setLastUpdated(practitionerDetails.getLastUpdated())
                .setVersionId(String.valueOf(practitionerDetails.getLastUpdated().getTime()))
                .addProfile(CareConnectProfile.Practitioner_1);

        HumanNameDt name = new HumanNameDt()
                .addFamily(practitionerDetails.getNameFamily())
                .addGiven(practitionerDetails.getNameGiven())
                .addPrefix(practitionerDetails.getNamePrefix())
                .setUse(NameUseEnum.USUAL);

        practitioner.setName(name);

        switch (practitionerDetails.getGender().toLowerCase(Locale.UK)) {
            case "male":
                practitioner.setGender(AdministrativeGenderEnum.MALE);
                break;

            case "female":
                practitioner.setGender(AdministrativeGenderEnum.FEMALE);
                break;

            case "other":
                practitioner.setGender(AdministrativeGenderEnum.OTHER);
                break;

            default:
                practitioner.setGender(AdministrativeGenderEnum.UNKNOWN);
                break;
        }

        CodingDt roleCoding = new CodingDt(SystemURL.VS_SDS_JOB_ROLE_NAME, practitionerDetails.getRoleCode())
                .setDisplay(practitionerDetails.getRoleDisplay());

        practitioner.addPractitionerRole()
                .setRole(new CodeableConceptDt().addCoding(roleCoding))
                .setManagingOrganization(new ResourceReferenceDt("Organization/"+practitionerDetails.getOrganizationId())); // Associated Organisation

        CodingDt comCoding = new CodingDt(SystemURL.VS_HUMAN_LANGUAGE, practitionerDetails.getComCode())
                .setDisplay(practitionerDetails.getComDisplay());

        practitioner.addCommunication().addCoding(comCoding);

        return practitioner;
    }
}
