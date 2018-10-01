package uk.nhs.careconnect.ri.database.daointerface;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.annotation.ConditionalUrlParam;
import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.OptionalParam;
import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenParam;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.Immunization;
import uk.nhs.careconnect.fhir.OperationOutcomeException;
import uk.nhs.careconnect.ri.database.entity.immunisation.ImmunisationEntity;

import java.util.List;

public interface ImmunizationRepository extends BaseRepository<ImmunisationEntity, Immunization>  {
    void save(FhirContext ctx,ImmunisationEntity immunisation) throws OperationOutcomeException;

    Immunization read(FhirContext ctx, IdType theId);

    ImmunisationEntity readEntity(FhirContext ctx, IdType theId);

    Immunization create(FhirContext ctx,Immunization immunisation, @IdParam IdType theId, @ConditionalUrlParam String theImmunizational) throws OperationOutcomeException;


    List<Immunization> search(FhirContext ctx,

            @OptionalParam(name = Immunization.SP_PATIENT) ReferenceParam patient
            , @OptionalParam(name = Immunization.SP_DATE) DateRangeParam date
            , @OptionalParam(name = Immunization.SP_STATUS) TokenParam status
            , @OptionalParam(name = Immunization.SP_IDENTIFIER) TokenParam identifier
            ,@OptionalParam(name= Immunization.SP_RES_ID) StringParam id

    );

    List<ImmunisationEntity> searchEntity(FhirContext ctx,
            @OptionalParam(name = Immunization.SP_PATIENT) ReferenceParam patient
            , @OptionalParam(name = Immunization.SP_DATE) DateRangeParam date
            , @OptionalParam(name = Immunization.SP_STATUS) TokenParam status
            , @OptionalParam(name = Immunization.SP_IDENTIFIER) TokenParam identifier
            ,@OptionalParam(name= Immunization.SP_RES_ID) StringParam id
    );
}
