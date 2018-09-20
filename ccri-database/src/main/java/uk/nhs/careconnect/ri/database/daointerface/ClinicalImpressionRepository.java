package uk.nhs.careconnect.ri.database.daointerface;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.annotation.ConditionalUrlParam;
import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.OptionalParam;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenParam;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.ClinicalImpression;
import uk.nhs.careconnect.fhir.OperationOutcomeException;
import uk.nhs.careconnect.ri.database.entity.clinicialImpression.ClinicalImpressionEntity;


import java.util.List;

public interface ClinicalImpressionRepository extends BaseRepository<ClinicalImpressionEntity,ClinicalImpression> {
    void save(FhirContext ctx, ClinicalImpressionEntity team) throws OperationOutcomeException;

    ClinicalImpression read(FhirContext ctx, IdType theId);

    ClinicalImpression create(FhirContext ctx, ClinicalImpression impression, @IdParam IdType theId, @ConditionalUrlParam String theConditional) throws OperationOutcomeException;

    ClinicalImpressionEntity readEntity(FhirContext ctx, IdType theId);

    List<ClinicalImpression> search(FhirContext ctx,
                                @OptionalParam(name = ClinicalImpression.SP_PATIENT) ReferenceParam patient
            , @OptionalParam(name = ClinicalImpression.SP_IDENTIFIER) TokenParam identifier
            , @OptionalParam(name = ClinicalImpression.SP_RES_ID) StringParam id
    );

    List<ClinicalImpressionEntity> searchEntity(FhirContext ctx,
                                            @OptionalParam(name = ClinicalImpression.SP_PATIENT) ReferenceParam patient
            , @OptionalParam(name = ClinicalImpression.SP_IDENTIFIER) TokenParam identifier
            , @OptionalParam(name = ClinicalImpression.SP_RES_ID) StringParam id
    );
}
