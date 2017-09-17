package uk.nhs.careconnect.ri.dao.CodeSystem;

import ca.uhn.fhir.rest.method.RequestDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.util.List;

@Repository
@Transactional
public class RITerminologyLoader implements TerminologyLoader {

    @PersistenceContext
    EntityManager em;


    private static final Logger log = LoggerFactory.getLogger(RITerminologyLoader.class);


    @Override
    public UploadStatistics loadLoinc(List<byte[]> theZipBytes, RequestDetails theRequestDetails) {
        return null;
    }

    @Override
    public UploadStatistics loadSnomedCt(List<byte[]> theZipBytes, RequestDetails theRequestDetails) {
        return null;
    }

}
