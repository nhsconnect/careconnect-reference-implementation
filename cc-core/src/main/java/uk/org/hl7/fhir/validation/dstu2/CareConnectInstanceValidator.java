package uk.org.hl7.fhir.validation.dstu2;

import org.hl7.fhir.instance.model.ElementDefinition;
import org.hl7.fhir.instance.utils.IWorkerContext;
import org.hl7.fhir.instance.validation.InstanceValidator;
import org.w3c.dom.Element;

public class CareConnectInstanceValidator extends InstanceValidator {
    public CareConnectInstanceValidator(IWorkerContext theContext) throws Exception {
        super(theContext);
    }
    private Element getValueForDiscriminator(WrapperElement element, String discriminator, ElementDefinition criteria) {
      	   System.out.println("Slicing Validation is not done");
            return null;
    }
}
