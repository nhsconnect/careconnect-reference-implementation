package uk.nhs.careconnect.ri.dao.transforms;


import org.apache.commons.collections4.Transformer;
import org.hl7.fhir.dstu3.model.CodeSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import uk.nhs.careconnect.ri.database.entity.Terminology.CodeSystemEntity;
import uk.nhs.careconnect.ri.database.entity.Terminology.CodeSystemTelecom;
import uk.nhs.careconnect.ri.database.entity.Terminology.ConceptEntity;



@Component
public class CodeSystemEntityToFHIRCodeSystemTransformer implements Transformer<CodeSystemEntity, CodeSystem> {

    private static final Logger log = LoggerFactory.getLogger(CodeSystemEntityToFHIRCodeSystemTransformer.class);

    @Override
    public CodeSystem transform(final CodeSystemEntity codeSystemEntity) {
        final CodeSystem codeSystem = new CodeSystem();


        codeSystem.setId(codeSystemEntity.getId().toString());

        codeSystem.setStatus(codeSystemEntity.getStatus());



        if (codeSystemEntity.getName() != null) {
            codeSystem.setName(codeSystemEntity.getName());
        }
        if (codeSystemEntity.getCodeSystemUri() != null) {
            codeSystem.setUrl(codeSystemEntity.getCodeSystemUri());
        }

        if (codeSystemEntity.getVersion() != null) {
            codeSystem.setVersion(codeSystemEntity.getVersion());
        }

        codeSystem.setName(codeSystemEntity.getName());

        if (codeSystemEntity.getTitle() != null) {
            codeSystem.setTitle(codeSystemEntity.getTitle());
        }

        codeSystem.setStatus(codeSystemEntity.getStatus());

        if (codeSystemEntity.getExperimental() != null) {
            codeSystem.setExperimental(codeSystemEntity.getExperimental());
        }

        if (codeSystemEntity.getChangeDateTime() != null) {
            codeSystem.setDate(codeSystemEntity.getChangeDateTime());
        }
        if (codeSystemEntity.getPublisher() != null) {
            codeSystem.setPublisher(codeSystemEntity.getPublisher());
        }

        codeSystem.setDescription(codeSystemEntity.getDescription());



        if (codeSystemEntity.getPurpose() != null) {
            codeSystem.setPurpose(codeSystemEntity.getPurpose());
        }

        if (codeSystemEntity.getCopyright() != null) {
            codeSystem.setCopyright(codeSystemEntity.getCopyright());
        }




        // Hard coded to not attempt to retrieve SNOMED!


        for (CodeSystemTelecom telecom : codeSystemEntity.getContacts()) {
            codeSystem.addContact()
                    .addTelecom()
                    .setUse(telecom.getTelecomUse())
                    .setValue(telecom.getValue())
                    .setSystem(telecom.getSystem());
        }


        if (codeSystemEntity.getContent() != null) {
            codeSystem.setContent(codeSystem.getContent());
        } else {
            codeSystem.setContent(CodeSystem.CodeSystemContentMode.COMPLETE);
        }

        for (ConceptEntity
                conceptEntity : codeSystemEntity.getConcepts()) {
            codeSystem.addConcept()
                    .setCode(conceptEntity.getCode())
                    .setDisplay(conceptEntity.getDisplay());
        }

        return codeSystem;


    }
}
