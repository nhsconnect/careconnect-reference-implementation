package uk.nhs.careconnect.ri.dao.CodeSystem;

import ca.uhn.fhir.rest.method.RequestDetails;

import java.util.List;

public interface TerminologyLoader {
    String LOINC_URL = "http://loinc.org";
    String SCT_URL = "http://snomed.info/sct";

    UploadStatistics loadLoinc(List<byte[]> theZipBytes, RequestDetails theRequestDetails);

    UploadStatistics loadSnomedCt(List<byte[]> theZipBytes, RequestDetails theRequestDetails);

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
