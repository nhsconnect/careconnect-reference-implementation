package uk.nhs.careconnect.ri.daointerface;

import ca.uhn.fhir.rest.annotation.ConditionalUrlParam;
import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.OptionalParam;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenParam;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.Practitioner;
import uk.nhs.careconnect.ri.entity.practitioner.PractitionerEntity;

import java.util.List;

public interface PractitionerRepository {

    void save(PractitionerEntity practitioner);

    Practitioner read(IdType theId);

    PractitionerEntity readEntity(IdType theId);

    Practitioner create(Practitioner practitioner, @IdParam IdType theId, @ConditionalUrlParam String theConditional);

    List<Practitioner> searchPractitioner (
            @OptionalParam(name = Practitioner.SP_IDENTIFIER) TokenParam identifier,
            @OptionalParam(name = Practitioner.SP_NAME) StringParam name,
            @OptionalParam(name = Practitioner.SP_ADDRESS_POSTALCODE) StringParam postCode

    );

    List<PractitionerEntity> searchPractitionerEntity (
            @OptionalParam(name = Practitioner.SP_IDENTIFIER) TokenParam identifier,
            @OptionalParam(name = Practitioner.SP_NAME) StringParam name,
            @OptionalParam(name = Practitioner.SP_ADDRESS_POSTALCODE) StringParam postCode
    );


}
