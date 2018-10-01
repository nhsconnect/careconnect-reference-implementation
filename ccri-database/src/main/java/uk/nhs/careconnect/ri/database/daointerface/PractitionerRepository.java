package uk.nhs.careconnect.ri.database.daointerface;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.annotation.ConditionalUrlParam;
import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.OptionalParam;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenParam;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.Practitioner;
import uk.nhs.careconnect.fhir.OperationOutcomeException;
import uk.nhs.careconnect.ri.database.entity.practitioner.PractitionerEntity;

import java.util.List;

public interface PractitionerRepository extends BaseRepository<PractitionerEntity,Practitioner> {

    void save(FhirContext ctx,PractitionerEntity practitioner) throws OperationOutcomeException;

    Practitioner read(FhirContext ctx, IdType theId);

    PractitionerEntity readEntity(FhirContext ctx, IdType theId);

    Practitioner create(FhirContext ctx, Practitioner practitioner, @IdParam IdType theId, @ConditionalUrlParam String theConditional) throws OperationOutcomeException;


    List<Practitioner> searchPractitioner (FhirContext ctx,
            @OptionalParam(name = Practitioner.SP_IDENTIFIER) TokenParam identifier,
            @OptionalParam(name = Practitioner.SP_NAME) StringParam name,
            @OptionalParam(name = Practitioner.SP_ADDRESS_POSTALCODE) StringParam postCode
            ,@OptionalParam(name= Practitioner.SP_RES_ID) StringParam id


    );

    List<PractitionerEntity> searchPractitionerEntity (FhirContext ctx,
            @OptionalParam(name = Practitioner.SP_IDENTIFIER) TokenParam identifier,
            @OptionalParam(name = Practitioner.SP_NAME) StringParam name,
            @OptionalParam(name = Practitioner.SP_ADDRESS_POSTALCODE) StringParam postCode
            ,@OptionalParam(name= Practitioner.SP_RES_ID) StringParam id
    );


}
