package uk.nhs.careconnect.ri.database.daointerface;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.annotation.OptionalParam;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenParam;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.NamingSystem;
import uk.nhs.careconnect.fhir.OperationOutcomeException;
import uk.nhs.careconnect.ri.database.entity.namingSystem.NamingSystemEntity;


import java.util.List;


public interface NamingSystemRepository extends BaseRepository<NamingSystemEntity,NamingSystem> {



    void save(FhirContext ctx, NamingSystemEntity namingSystem);


    NamingSystem create(FhirContext ctx, NamingSystem valueSet);

    NamingSystem read(FhirContext ctx, IdType theId) ;

    List<NamingSystem> search(FhirContext ctx,
                                      @OptionalParam(name = NamingSystem.SP_NAME) StringParam name,
                                      @OptionalParam(name = NamingSystem.SP_PUBLISHER) StringParam publisher,
                                      @OptionalParam(name = NamingSystem.SP_VALUE)TokenParam unique
                                      );

    List<NamingSystemEntity> searchEntity(FhirContext ctx,
                              @OptionalParam(name = NamingSystem.SP_NAME) StringParam name,
                              @OptionalParam(name = NamingSystem.SP_PUBLISHER) StringParam publisher,
                              @OptionalParam(name = NamingSystem.SP_VALUE)TokenParam unique
    );

}
