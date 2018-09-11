package uk.nhs.careconnect.ccri.fhirserver.provider;

import ca.uhn.fhir.rest.annotation.*;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.OperationOutcome;
import org.hl7.fhir.dstu3.model.StructureDefinition;
import org.springframework.stereotype.Component;
import uk.nhs.careconnect.ri.lib.server.OperationOutcomeFactory;

import javax.servlet.http.HttpServletRequest;

@Component
public class StructureDefinitionProvider implements IResourceProvider {


   

    @Override
    public Class<StructureDefinition> getResourceType() {
        return StructureDefinition.class;
    }



    @Update()
    public MethodOutcome updateStructureDefinition(HttpServletRequest theRequest,@ResourceParam StructureDefinition structureDefinition) {


        MethodOutcome method = new MethodOutcome();
        method.setCreated(true);
        OperationOutcome opOutcome = new OperationOutcome();

        method.setOperationOutcome(opOutcome);


        StructureDefinition newStructureDefinition = new StructureDefinition();
        method.setId(newStructureDefinition.getIdElement());
        method.setResource(newStructureDefinition);


        return method;
    }



    @Create
    public MethodOutcome createPatient(HttpServletRequest theRequest, @ResourceParam StructureDefinition structureDefinition) {


        MethodOutcome method = new MethodOutcome();
        method.setCreated(true);
        OperationOutcome opOutcome = new OperationOutcome();

        method.setOperationOutcome(opOutcome);

     structureDefinition = new StructureDefinition();

        return method;
    }

    @Read
    public StructureDefinition getStructureDefinition
            (@IdParam IdType internalId) {

        // TODO
      StructureDefinition structureDefinition = new StructureDefinition();

        if ( structureDefinition == null) {
            throw OperationOutcomeFactory.buildOperationOutcomeException(
                    new ResourceNotFoundException("No StructureDefinition/" + internalId.getIdPart()),
                    OperationOutcome.IssueSeverity.ERROR, OperationOutcome.IssueType.NOTFOUND);
        }

        return structureDefinition;
    }
    

}
