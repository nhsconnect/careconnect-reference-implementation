package uk.nhs.careconnect.ccri.fhirserver.support;

import ca.uhn.fhir.rest.param.UriParam;
import org.hl7.fhir.dstu3.hapi.validation.ValidationSupportChain;
import org.hl7.fhir.dstu3.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.nhs.careconnect.ccri.fhirserver.stu3.provider.ResourceTestProvider;

import java.util.List;

public class MessageInstanceValidator {
    public MessageInstanceValidator(ValidationSupportChain _validationSupportChain) {
        this.validationSupportChain = _validationSupportChain;

    }

    private static final Logger log = LoggerFactory.getLogger(MessageInstanceValidator.class);

    ValidationSupportChain validationSupportChain;

    public OperationOutcome validateMessageBundle(Bundle bundle) {

        if (bundle.getEntryFirstRep().getResource() instanceof MessageHeader) {
            /*
            MessageHeader messageHeader = (MessageHeader) bundle.getEntryFirstRep().getResource();
            if (messageHeader.hasExtension()) {
                List<Extension> definitionExtension = messageHeader.getExtensionsByUrl("http://hl7.org/fhir/4.0/StructureDefinition/extension-MessageHeader.definition");
                if (!definitionExtension.isEmpty()) {
                    UriType uri = (UriType) definitionExtension.get(0).getValue();
                    List<MessageDefinition> messages = messageDefinitionDao.search(ctx,null,null,new UriParam().setValue(uri.getValue()), null);
                    for(MessageDefinition message : messages) {
                        List<Extension> graphExtensions = message.getExtensionsByUrl("http://hl7.org/fhir/4.0/StructureDefinition/extension-MessageDefinition.graph");
                        for(Extension graphExtension : graphExtensions) {
                            log.info("Graph id = "+ graphExtension.getValue().toString());
                        }
                    }
                }
            }*/
        }
        return null;
    }

}
