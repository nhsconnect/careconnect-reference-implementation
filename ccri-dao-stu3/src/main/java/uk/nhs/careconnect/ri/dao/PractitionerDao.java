package uk.nhs.careconnect.ri.dao;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.annotation.ConditionalUrlParam;
import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.OptionalParam;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenParam;
import org.hl7.fhir.dstu3.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import uk.nhs.careconnect.fhir.OperationOutcomeException;
import uk.nhs.careconnect.ri.database.daointerface.CodeSystemRepository;
import uk.nhs.careconnect.ri.database.daointerface.ConceptRepository;
import uk.nhs.careconnect.ri.database.daointerface.OrganisationRepository;
import uk.nhs.careconnect.ri.database.daointerface.PractitionerRepository;
import uk.nhs.careconnect.ri.dao.transforms.PractitionerEntityToFHIRPractitionerTransformer;
import uk.nhs.careconnect.ri.database.entity.AddressEntity;
import uk.nhs.careconnect.ri.database.entity.practitioner.*;
import uk.nhs.careconnect.ri.database.entity.practitioner.*;
import uk.org.hl7.fhir.core.Stu3.CareConnectSystem;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.*;
import javax.transaction.Transactional;
import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

@Repository
@Transactional
public class PractitionerDao implements PractitionerRepository {

    @PersistenceContext
    EntityManager em;
    @Autowired
    private PractitionerEntityToFHIRPractitionerTransformer practitionerEntityToFHIRPractitionerTransformer;

    @Autowired
    private CodeSystemRepository codeSystemSvc;

    @Autowired
    private OrganisationRepository organisationRepository;

    @Autowired
    private ConceptRepository codeSvc;


    private static final Logger log = LoggerFactory.getLogger(PractitionerDao.class);

    public void save(FhirContext ctx, PractitionerEntity practitioner)
    {
        em.persist(practitioner);
    }

    public Practitioner read(FhirContext ctx, IdType theId) {
        if (daoutils.isNumeric(theId.getIdPart())) {
            PractitionerEntity practitionerEntity = (PractitionerEntity) em.find(PractitionerEntity.class, Long.parseLong(theId.getIdPart()));

            return practitionerEntity == null
                    ? null
                    : practitionerEntityToFHIRPractitionerTransformer.transform(practitionerEntity);

        } else {
            return null;
        }
    }

    @Override
    public Long count() {

        CriteriaBuilder qb = em.getCriteriaBuilder();
        CriteriaQuery<Long> cq = qb.createQuery(Long.class);
        cq.select(qb.count(cq.from(PractitionerEntity.class)));
        //cq.where(/*your stuff*/);
        return em.createQuery(cq).getSingleResult();
    }

    public PractitionerEntity readEntity(FhirContext ctx, IdType theId) {

        PractitionerEntity practitionerEntity = (PractitionerEntity) em.find(PractitionerEntity.class,Long.parseLong(theId.getIdPart()));

        return practitionerEntity;
    }

    @Override
    public Practitioner create(FhirContext ctx, Practitioner practitioner, @IdParam IdType theId, @ConditionalUrlParam String theConditional) throws OperationOutcomeException {
        PractitionerEntity practitionerEntity = null;

        if (practitioner.hasId()) {
            practitionerEntity =  (PractitionerEntity) em.find(PractitionerEntity.class,Long.parseLong(practitioner.getId()));
        }
        if (theConditional != null) {
            try {

                //CareConnectSystem.ODSPractitionerCode
                if (theConditional.contains("fhir.nhs.uk/Id/sds-user-id")) {
                    URI uri = new URI(theConditional);

                    String scheme = uri.getScheme();
                    String host = uri.getHost();
                    String query = uri.getRawQuery();
                    log.debug(query);
                    String[] spiltStr = query.split("%7C");
                    log.debug(spiltStr[1]);

                    List<PractitionerEntity> results = searchPractitionerEntity(ctx, new TokenParam().setValue(spiltStr[1]).setSystem(CareConnectSystem.SDSUserId),null,null,null);
                    for (PractitionerEntity org : results) {
                        practitionerEntity = org;
                        break;
                    }
                } else {
                    log.info("NOT SUPPORTED: Conditional Url = "+theConditional);
                }

            } catch (Exception ex) {

            }
        }
        if (practitionerEntity == null) {
            practitionerEntity = new PractitionerEntity();
        }


        practitionerEntity.setActive(practitioner.getActive());

        em.persist(practitionerEntity);

        for (HumanName name : practitioner.getName()) {
            PractitionerName practitionerName = null;
            if (practitionerEntity.getNames().size() > 0) {
                practitionerName = practitionerEntity.getNames().get(0);
            } else {
                practitionerName = new PractitionerName();
                practitionerName.setPractitionerEntity(practitionerEntity);
            }

            if (name.getFamily()!= null) {
                practitionerName.setFamilyName(name.getFamily());
            }
            if (name.getGiven().size() > 0) {
                practitionerName.setGivenName(name.getGiven().get(0).getValue());
            }
            if (name.getPrefix().size() > 0) {
                practitionerName.setPrefix(name.getPrefix().get(0).getValue());
            }
            em.persist(practitionerName);
        }

        for (Identifier ident : practitioner.getIdentifier()) {
            PractitionerIdentifier practitionerIdentifier = null;

            for (PractitionerIdentifier orgSearch : practitionerEntity.getIdentifiers()) {
                if (ident.getSystem().equals(orgSearch.getSystemUri()) && ident.getValue().equals(orgSearch.getValue())) {
                    practitionerIdentifier = orgSearch;
                    break;
                }
            }
            if (practitionerIdentifier == null)  practitionerIdentifier = new PractitionerIdentifier();

            practitionerIdentifier.setValue(ident.getValue());
            practitionerIdentifier.setSystem(codeSystemSvc.findSystem(ident.getSystem()));
            practitionerIdentifier.setPractitioner(practitionerEntity);
            em.persist(practitionerIdentifier);
        }

        for (ContactPoint telecom : practitioner.getTelecom()) {
            PractitionerTelecom practitionerTelecom = null;

            for (PractitionerTelecom orgSearch : practitionerEntity.getTelecoms()) {
                if (telecom.getValue().equals(orgSearch.getValue())) {
                    practitionerTelecom = orgSearch;
                    break;
                }
            }
            if (practitionerTelecom == null) {
                practitionerTelecom = new PractitionerTelecom();
                practitionerTelecom.setPractitionerEntity(practitionerEntity);
            }

            practitionerTelecom.setValue(telecom.getValue());
            practitionerTelecom.setSystem(telecom.getSystem());
            practitionerTelecom.setTelecomUse(telecom.getUse());

            em.persist(practitionerTelecom);
        }
        for (Address address  :practitioner.getAddress()) {

            PractitionerAddress practitionerAddress = null;

            if (practitionerEntity.getAddresses().size()>0) practitionerAddress = practitionerEntity.getAddresses().get(0);
            if (practitionerAddress == null) {
                practitionerAddress = new PractitionerAddress();
                practitionerAddress.setPractitioner(practitionerEntity);
            }
            AddressEntity addr = practitionerAddress.getAddress();
            if (addr == null) {
                addr = practitionerAddress.setAddress(new AddressEntity());
            }

            if (address.getLine().size()>0) addr.setAddress1(address.getLine().get(0).getValue().trim());
            if (address.getLine().size()>1) addr.setAddress2(address.getLine().get(1).getValue().trim());
            if (address.getLine().size()>2) addr.setAddress3(address.getLine().get(2).getValue().trim());
            if (address.getCity() != null) addr.setCity(address.getCity());
            if (address.getDistrict() != null) addr.setCounty(address.getDistrict());
            if (address.getPostalCode() != null) addr.setPostcode(address.getPostalCode());
            if (address.getUse() != null) practitionerAddress.setAddressUse(address.getUse());
            em.persist(addr);
            em.persist(practitionerAddress);

        }

        log.debug("Called PERSIST id="+practitionerEntity.getId().toString());
        practitioner.setId(practitionerEntity.getId().toString());

        return practitioner;
    }


    @Override
    public List<PractitionerEntity> searchPractitionerEntity(FhirContext ctx,
            @OptionalParam(name = Practitioner.SP_IDENTIFIER) TokenParam identifier,
            @OptionalParam(name = Location.SP_NAME) StringParam name,
            @OptionalParam(name = Practitioner.SP_ADDRESS_POSTALCODE) StringParam postCode
            , StringParam resid
    ) {
        {
            List<PractitionerEntity> qryResults = null;

            CriteriaBuilder builder = em.getCriteriaBuilder();

            CriteriaQuery<PractitionerEntity> criteria = builder.createQuery(PractitionerEntity.class);
            Root<PractitionerEntity> root = criteria.from(PractitionerEntity.class);


            List<Predicate> predList = new LinkedList<Predicate>();
            List<Practitioner> results = new ArrayList<Practitioner>();

            if (identifier != null) {

                Join<PractitionerEntity, PractitionerIdentifier> join = root.join("identifiers", JoinType.LEFT);

                Predicate p = builder.equal(join.get("value"), identifier.getValue());
                predList.add(p);
                // TODO predList.add(builder.equal(join.get("system"),identifier.getSystem()));

            }
            if (resid != null) {
                Predicate p = builder.equal(root.get("id"),resid.getValue());
                predList.add(p);
            }
            if ( (name != null) /*(familyName != null) || (givenName != null) ||*/ ) {

                Join<PractitionerEntity, PractitionerName> namejoin = root.join("names", JoinType.LEFT);

                if (name != null) {
                    Predicate pgiven = builder.like(
                            builder.upper(namejoin.get("givenName").as(String.class)),
                            builder.upper(builder.literal(name.getValue() + "%"))
                    );
                    Predicate pfamily = builder.like(
                            builder.upper(namejoin.get("familyName").as(String.class)),
                            builder.upper(builder.literal(name.getValue() + "%"))
                    );
                    Predicate p = builder.or(pfamily, pgiven);
                    predList.add(p);
                }
                 /*
            if (familyName != null) {
                Predicate p =
                        builder.like(
                                builder.upper(namejoin.get("familyName").as(String.class)),
                                builder.upper(builder.literal(familyName.getValue()+"%"))
                        );
                predList.add(p);
            }
            if (givenName != null) {
                Predicate p =
                        builder.like(
                                builder.upper(namejoin.get("givenName").as(String.class)),
                                builder.upper(builder.literal(givenName.getValue()+"%"))
                        );

                predList.add(p);
            }
             */
            }
            if (postCode != null )
            {
                Join<PractitionerEntity, PractitionerAddress> joinAdr = root.join("addresses", JoinType.LEFT);
                Join<PractitionerAddress, AddressEntity> joinAdrTable = joinAdr.join("address", JoinType.LEFT);
                if (postCode!=null) {
                    Predicate p = builder.like(
                            joinAdrTable.get("postcode").as(String.class),
                            builder.upper(builder.literal(postCode.getValue()+"%"))
                    );
                    predList.add(p);
                }
            }


            Predicate[] predArray = new Predicate[predList.size()];
            predList.toArray(predArray);
            if (predList.size() > 0) {
                criteria.select(root).where(predArray);
            } else {
                criteria.select(root);
            }

            return em.createQuery(criteria).setMaxResults(daoutils.MAXROWS).getResultList();

        }

    }
    @Override
    public List<Practitioner> searchPractitioner(FhirContext ctx,
            @OptionalParam(name = Practitioner.SP_IDENTIFIER) TokenParam identifier,
            @OptionalParam(name = Location.SP_NAME) StringParam name,
            @OptionalParam(name = Practitioner.SP_ADDRESS_POSTALCODE) StringParam postCode
            , StringParam resid
    ) {
        List<Practitioner> results = new ArrayList<>();
        List<PractitionerEntity> qryResults = searchPractitionerEntity(ctx, identifier, name,postCode,resid);
        for (PractitionerEntity practitionerEntity : qryResults)
        {
            // log.trace("HAPI Custom = "+doc.getId());
            Practitioner practitioner = practitionerEntityToFHIRPractitionerTransformer.transform(practitionerEntity);
            results.add(practitioner);
        }

        return results;
    }




}
