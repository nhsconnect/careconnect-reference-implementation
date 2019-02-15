package uk.nhs.careconnect.ri.database.daointerface;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.annotation.OptionalParam;
import ca.uhn.fhir.rest.param.StringParam;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.ValueSet;
import uk.nhs.careconnect.fhir.OperationOutcomeException;
import uk.nhs.careconnect.ri.database.entity.valueSet.ValueSetEntity;

import java.util.List;


public interface ValueSetRepository {



    void save(FhirContext ctx, ValueSetEntity valueset);


    ValueSet create(FhirContext ctx,ValueSet valueSet);

    ValueSet read(FhirContext ctx,IdType theId) ;

    ValueSet readAndExpand(FhirContext ctx,IdType theId)  throws OperationOutcomeException;

    List<ValueSet> searchValueset (FhirContext ctx,
            @OptionalParam(name = ValueSet.SP_NAME) StringParam name
    );

}
