package uk.nhs.careconnect.ri.database.daointerface;

import ca.uhn.fhir.rest.annotation.OptionalParam;
import ca.uhn.fhir.rest.param.StringParam;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.ValueSet;
import uk.nhs.careconnect.ri.database.entity.Terminology.ValueSetEntity;

import java.util.List;


public interface ValueSetRepository {



    void save(ValueSetEntity valueset);


    ValueSet create(ValueSet valueSet);

    ValueSet read(IdType theId) ;

    List<ValueSet> searchValueset (
            @OptionalParam(name = ValueSet.SP_NAME) StringParam name
    );

}
