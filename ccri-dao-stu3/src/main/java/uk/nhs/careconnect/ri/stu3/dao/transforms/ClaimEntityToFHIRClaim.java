package uk.nhs.careconnect.ri.stu3.dao.transforms;

import ca.uhn.fhir.context.FhirContext;
import org.apache.commons.collections4.Transformer;
import org.hl7.fhir.dstu3.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.nhs.careconnect.ri.database.entity.careTeam.CareTeamEntity;
import uk.nhs.careconnect.ri.database.entity.claim.ClaimDiagnosis;
import uk.nhs.careconnect.ri.database.entity.claim.ClaimEntity;
import uk.nhs.careconnect.ri.stu3.dao.LibDao;
import java.util.ArrayList;

@Component
public class ClaimEntityToFHIRClaim implements Transformer<ClaimEntity, Claim> {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CareTeamEntity.class);


    @Autowired
    private LibDao libDao;

    @Override
    public Claim transform(ClaimEntity claimEntity) {
        return null;
    }

    public Claim transform(ClaimEntity claimEntity, FhirContext ctx) {

        final Claim claim = (Claim) ctx.newJsonParser().parseResource(claimEntity.getResource());

        claim.setId(claimEntity.getId().toString());

        claim.setPatient(null);
        if (claimEntity.getPatient() != null) {
            claim
                    .setPatient(new Reference("Patient/"+claimEntity.getPatient().getId())
                            .setDisplay(claimEntity.getPatient().getNames().get(0).getDisplayName()));
        }

        claim.setEnterer(null);
        claim.setExtension(new ArrayList<>());
        if (claimEntity.getEntererPatient() != null) {
            Extension extension = new Extension();
            extension.setUrl("https://fhir.gov.uk/Extension/claimEntererPatient");
            Reference patRef = new Reference("Patient/"+claimEntity.getEntererPatient().getId())
                    .setDisplay(claimEntity.getEntererPatient().getNames().get(0).getDisplayName());

            patRef.addExtension(libDao.getResourceTypeExt("Patient"));
            extension.setValue(patRef);

            claim.addExtension(extension);
        }
        if (claimEntity.getEntererPractitioner() != null) {
            claim.setEnterer(new Reference("Practitioner/"+claimEntity.getEntererPractitioner().getId())
                    .setDisplay(claimEntity.getEntererPractitioner().getNames().get(0).getDisplayName()));
        }
        claim.setOrganization(null);
        if (claimEntity.getProviderOrganisation() != null) {
            claim.setOrganization(new Reference("Organization/"+claimEntity.getProviderOrganisation().getId())
                    .setDisplay(claimEntity.getProviderOrganisation().getName()));
        }
        claim.setType(null);
        if (claimEntity.getType() != null) {
            CodeableConcept concept = new CodeableConcept();

            if (claimEntity.getType().getConceptText() != null) {
                concept.setText(claimEntity.getType().getConceptText());
            }
            if (claimEntity.getType().getConceptCode() != null) {
                concept.addCoding()
                        .setCode(claimEntity.getType().getConceptCode().getCode())
                        .setDisplay(claimEntity.getType().getConceptCode().getDisplay())
                        .setSystem(claimEntity.getType().getConceptCode().getSystem());
            }
            claim.setType(concept);
        }

        claim.setDiagnosis(new ArrayList<>());
        for (ClaimDiagnosis diagnosis : claimEntity.getDiagnoses()) {
            Claim.DiagnosisComponent component = new Claim.DiagnosisComponent();

            if (diagnosis.getCondition() != null) {
                Reference ref = new Reference("Condition/"+diagnosis.getId());
                ref.setDisplay(diagnosis.getCondition().getCodeText());
                component.setDiagnosis(ref);
            }
            claim.getDiagnosis().add(component);
        }
        claim.setCreated(claimEntity.getCreated());

        return claim;
    }
}
