package uk.nhs.careconnect.ri.dao.Dstu2.CodeSystem;

import ca.uhn.fhir.rest.api.server.RequestDetails;
import uk.nhs.careconnect.ri.entity.Terminology.CodeSystemEntity;

import java.util.List;

public interface TerminologyLoader {
    String LOINC_URL = "http://loinc.org";
    String SCT_URL = "http://snomed.info/sct";

    UploadStatistics loadLoinc(List<byte[]> theZipBytes, RequestDetails theRequestDetails);

    UploadStatistics loadSnomedCt(List<byte[]> theZipBytes, RequestDetails theRequestDetails);

    void storeCodeSystem(RequestDetails theRequestDetails, final CodeSystemEntity codeSystemVersion);

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
