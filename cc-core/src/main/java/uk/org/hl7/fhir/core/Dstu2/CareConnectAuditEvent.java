package uk.org.hl7.fhir.core.Dstu2;

import ca.uhn.fhir.rest.api.MethodOutcome;
import org.hl7.fhir.instance.model.AuditEvent;
import org.hl7.fhir.instance.model.OperationOutcome;
import org.hl7.fhir.instance.model.Resource;

import java.net.InetAddress;
import java.util.Date;

/**
 * Created by kevinmayfield on 20/07/2017.
 */
public class CareConnectAuditEvent {
    public static AuditEvent buildAuditEvent(Resource resource , MethodOutcome outcome, String typeCode, String subTypeCode, AuditEvent.AuditEventAction actionCode, String systemValue)
    {
        AuditEvent audit = new AuditEvent();

        AuditEvent.AuditEventEventComponent event = audit.getEvent();
        event.getType()
                .setSystem("http://hl7.org/fhir/audit-event-type")
                .setCode(typeCode);
        event.addSubtype()
                .setSystem("http://hl7.org/fhir/restful-interaction")
                .setCode(subTypeCode);

        event.setAction(actionCode);

        audit.addObject()
                .setReferenceTarget(resource);

        Date recordedDate = new Date();
        try {
            //InstantDt instance = new InstantDt(recordedDate);
            event.setDateTime(recordedDate);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            audit.getSource().getIdentifier()
                    .setSystem(String.valueOf(InetAddress.getLocalHost()))
                    .setValue(systemValue);
        } catch(Exception e)
        {

        }
        audit.getSource()
                .setSite("application")
                .addType()
                    .setSystem("http://hl7.org/fhir/security-source-type")
                    .setCode("1")
                    .setDisplay("User Device");

        if (outcome!=null && outcome.getOperationOutcome() instanceof OperationOutcome)
        {
            OperationOutcome operationOutcome = (OperationOutcome) outcome.getOperationOutcome();
            if (operationOutcome.getIssue().size()>0) {
                System.out.println(operationOutcome.getIssue().get(0).getCode());
                switch (operationOutcome.getIssue().get(0).getCode()) {
                    case INFORMATIONAL:
                        event.setOutcome(AuditEvent.AuditEventOutcome._0);
                        break;
                    default:
                        event.setOutcome(AuditEvent.AuditEventOutcome._4);
                }
            }
            try {

                event.setOutcomeDesc(operationOutcome.getText().getDivAsString());
            } catch (Exception e)
            {

            }
            // event.set
        }

        return audit;

    }
}
