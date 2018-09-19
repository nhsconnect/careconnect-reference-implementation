package uk.nhs.careconnect.ri.database.daointerface;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.annotation.ConditionalUrlParam;
import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.OptionalParam;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenOrListParam;
import ca.uhn.fhir.rest.param.TokenParam;
import org.hl7.fhir.dstu3.model.Appointment;
import org.hl7.fhir.dstu3.model.HealthcareService;
import org.hl7.fhir.dstu3.model.IdType;
import uk.nhs.careconnect.fhir.OperationOutcomeException;
import uk.nhs.careconnect.ri.database.entity.appointment.AppointmentEntity;
import uk.nhs.careconnect.ri.database.entity.healthcareService.HealthcareServiceEntity;

import java.util.List;

public interface AppointmentRepository extends BaseRepository<AppointmentEntity, Appointment> {
    void save(FhirContext ctx, HealthcareServiceEntity location) throws OperationOutcomeException;

    Appointment read(FhirContext ctx, IdType theId);

    AppointmentEntity readEntity(FhirContext ctx, IdType theId);

    Appointment create(FhirContext ctx, Appointment location, @IdParam IdType theId, @ConditionalUrlParam String theConditional) throws OperationOutcomeException;



    List<Appointment> searchAppointment(FhirContext ctx,

                                                    @OptionalParam(name = Appointment.SP_IDENTIFIER) TokenParam identifier,
                                                    @OptionalParam(name = Appointment.SP_APPOINTMENT_TYPE) StringParam type,
                                                    @OptionalParam(name = Appointment.SP_STATUS) StringParam codes,
                                                    @OptionalParam(name = Appointment.SP_DATE) StringParam date,
                                                    @OptionalParam(name = Appointment.SP_LOCATION) StringParam location

    );

    List<Appointment> searchAppointment(FhirContext ctx,

                                                                @OptionalParam(name = Appointment.SP_IDENTIFIER) TokenParam identifier,
                                                                @OptionalParam(name = Appointment.SP_STATUS) StringParam status,
                                                                @OptionalParam(name = Appointment.SP_DATE) StringParam date

    );
}
