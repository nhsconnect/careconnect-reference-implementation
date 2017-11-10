package uk.nhs.careconnect.ri.daointerface;

import ca.uhn.fhir.rest.annotation.ConditionalUrlParam;
import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.OptionalParam;
import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.ReferenceParam;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.Procedure;
import uk.nhs.careconnect.ri.entity.procedure.ProcedureEntity;

import java.util.List;

public interface ProcedureRepository {
    void save(ProcedureEntity procedure);

    Procedure read(IdType theId);

    Procedure create(Procedure procedure, @IdParam IdType theId, @ConditionalUrlParam String theProcedureal);


    List<Procedure> search(
            @OptionalParam(name = Procedure.SP_PATIENT) ReferenceParam patient
            ,@OptionalParam(name = Procedure.SP_DATE) DateRangeParam date
            , @OptionalParam(name = Procedure.SP_SUBJECT) ReferenceParam subject
    );

    List<ProcedureEntity> searchEntity(
            @OptionalParam(name = Procedure.SP_PATIENT) ReferenceParam patient
            ,@OptionalParam(name = Procedure.SP_DATE) DateRangeParam date
            , @OptionalParam(name = Procedure.SP_SUBJECT) ReferenceParam subject
    );
}
