package uk.nhs.careconnect.ri.dao.ValueSet;

import ca.uhn.fhir.rest.annotation.OptionalParam;
import ca.uhn.fhir.rest.param.StringParam;
import org.hl7.fhir.instance.model.IdType;
import org.hl7.fhir.instance.model.ValueSet;
import uk.nhs.careconnect.ri.entity.Terminology.ValueSetEntity;

import java.util.List;


public interface ValueSetRepository {



    void save(ValueSetEntity valueset);

    boolean isNumeric(String s);

    ValueSet create(ValueSet valueSet);

    ValueSet read(IdType theId) ;

    List<ValueSet> searchValueset (
            @OptionalParam(name = ValueSet.SP_NAME) StringParam name
    );

}
