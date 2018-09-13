package uk.nhs.careconnect.ri.database.daointerface;

import ca.uhn.fhir.rest.api.server.RequestDetails;
import uk.nhs.careconnect.fhir.OperationOutcomeException;
import uk.nhs.careconnect.ri.database.entity.Terminology.CodeSystemEntity;

import java.util.List;

public interface TerminologyLoader {
    String LOINC_URL = "http://loinc.org";
    String SCT_URL = "http://snomed.info/sct";

    UploadStatistics loadLoinc(List<byte[]> theZipBytes, RequestDetails theRequestDetails) throws OperationOutcomeException;

    UploadStatistics loadSnomedCt(List<byte[]> theZipBytes, RequestDetails theRequestDetails) throws OperationOutcomeException;

    void storeCodeSystem(RequestDetails theRequestDetails, final CodeSystemEntity codeSystemVersion) throws OperationOutcomeException;

    public static class UploadStatistics {
        private final int myConceptCount;

        public UploadStatistics(int theConceptCount) {
            myConceptCount = theConceptCount;
        }

        public int getConceptCount() {
            return myConceptCount;
        }



    }
}
