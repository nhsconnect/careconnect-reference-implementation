package uk.nhs.careconnect.ri.dao.CodeSystem;

import ca.uhn.fhir.rest.method.RequestDetails;
import uk.nhs.careconnect.ri.entity.Terminology.CodeSystemEntity;
import uk.nhs.careconnect.ri.entity.Terminology.ConceptEntity;

import java.util.List;
import java.util.Map;

public interface TerminologyLoader {
    String LOINC_URL = "http://loinc.org";
    String SCT_URL = "http://snomed.info/sct";

    UploadStatistics loadLoinc(List<byte[]> theZipBytes, RequestDetails theRequestDetails);

    UploadStatistics loadSnomedCt(List<byte[]> theZipBytes, RequestDetails theRequestDetails);

    void storeCodeSystem(RequestDetails theRequestDetails, final CodeSystemEntity codeSystemVersion, String url);

    void storeConcepts(Map<String, ConceptEntity> code2concept, String codeSystemUri, RequestDetails theRequestDetails);



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
