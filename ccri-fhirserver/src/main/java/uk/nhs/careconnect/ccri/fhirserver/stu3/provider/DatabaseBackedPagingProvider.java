package uk.nhs.careconnect.ccri.fhirserver.stu3.provider;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.server.BasePagingProvider;
import ca.uhn.fhir.rest.server.IPagingProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.PlatformTransactionManager;
import uk.nhs.careconnect.ri.database.daointerface.ISearchResultDao;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.EntityManager;


public class DatabaseBackedPagingProvider extends BasePagingProvider implements IPagingProvider {

    @Autowired
    private FhirContext myContext;

    @Autowired
    private EntityManager myEntityManager;
    @Autowired
    private PlatformTransactionManager myPlatformTransactionManager;


    @Autowired
    private ISearchResultDao searchResultDao;

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DatabaseBackedPagingProvider.class);
    /**
     * Constructor
     */
    public DatabaseBackedPagingProvider() {
        super();
    }

    @Deprecated
    public DatabaseBackedPagingProvider(int theSize) {
        this();
        log.info(" The Size = "+theSize);

    }

    @Override
    public IBundleProvider retrieveResultList(@Nullable RequestDetails requestDetails, @Nonnull String searchId) {
        log.info(" The theId = "+searchId);
        return searchResultDao.read(searchId);
    }

    @Override
    public String storeResultList(@Nullable RequestDetails requestDetails, IBundleProvider iBundleProvider) {
        log.info(" storeResultList theList.size = "+iBundleProvider.size());
        String uuid = searchResultDao.save(iBundleProvider);
        log.info(" storeResultList theList.uuid = "+uuid);
        return uuid;
    }


}
