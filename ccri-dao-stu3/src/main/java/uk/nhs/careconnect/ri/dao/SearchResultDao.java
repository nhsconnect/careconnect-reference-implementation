package uk.nhs.careconnect.ri.dao;

import ca.uhn.fhir.rest.api.server.IBundleProvider;
import org.springframework.stereotype.Repository;
import uk.nhs.careconnect.ri.database.daointerface.ISearchResultDao;
import uk.nhs.careconnect.ri.database.entity.search.SearchResults;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;

@Repository
@Transactional
public class SearchResultDao implements ISearchResultDao {

    @PersistenceContext
    EntityManager em;


    @Override
    public String save(IBundleProvider searchResults) {
        SearchResults search = new SearchResults();
        em.persist(search);
        return search.getId().toString();
    }

    @Override
    public IBundleProvider read(String searchId) {

        em.find(SearchResults.class,searchId);
        return null;
    }
}
