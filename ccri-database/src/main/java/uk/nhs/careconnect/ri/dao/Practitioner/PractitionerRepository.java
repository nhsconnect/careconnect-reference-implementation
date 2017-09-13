package uk.nhs.careconnect.ri.dao.Practitioner;

import ca.uhn.fhir.rest.annotation.OptionalParam;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenParam;
import org.hl7.fhir.instance.model.IdType;
import org.hl7.fhir.instance.model.Location;
import org.hl7.fhir.instance.model.Practitioner;
import uk.nhs.careconnect.ri.entity.practitioner.PractitionerEntity;

import java.util.List;

public interface PractitionerRepository {

    void save(PractitionerEntity practitioner);

    Practitioner read(IdType theId);

    List<Practitioner> searchPractitioner (
            @OptionalParam(name = Practitioner.SP_IDENTIFIER) TokenParam identifier,
            @OptionalParam(name = Location.SP_NAME) StringParam name
    );


}
