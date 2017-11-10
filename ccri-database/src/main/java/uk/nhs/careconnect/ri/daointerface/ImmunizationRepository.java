package uk.nhs.careconnect.ri.daointerface;

import ca.uhn.fhir.rest.annotation.ConditionalUrlParam;
import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.OptionalParam;
import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.param.TokenParam;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.Immunization;
import uk.nhs.careconnect.ri.entity.immunisation.ImmunisationEntity;

import java.util.List;

public interface ImmunizationRepository {
    void save(ImmunisationEntity immunisation);

    Immunization read(IdType theId);

    Immunization create(Immunization immunisation, @IdParam IdType theId, @ConditionalUrlParam String theImmunizational);


    List<Immunization> search(

            @OptionalParam(name = Immunization.SP_PATIENT) ReferenceParam patient
            , @OptionalParam(name = Immunization.SP_DATE) DateRangeParam date
            , @OptionalParam(name = Immunization.SP_STATUS) TokenParam status

    );

    List<ImmunisationEntity> searchEntity(
            @OptionalParam(name = Immunization.SP_PATIENT) ReferenceParam patient
            , @OptionalParam(name = Immunization.SP_DATE) DateRangeParam date
            , @OptionalParam(name = Immunization.SP_STATUS) TokenParam status
    );
}
