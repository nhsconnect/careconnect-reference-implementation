package mayfieldis.careconnect.nosql.dao;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.server.exceptions.ResourceVersionConflictException;
import mayfieldis.careconnect.nosql.entities.BundleEntity;
import mayfieldis.careconnect.nosql.entities.Entry;
import mayfieldis.careconnect.nosql.entities.PatientEntity;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.hl7.fhir.dstu3.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;

@Transactional
@Repository
public class BundleDao implements IBundle {

    @Autowired
    MongoOperations mongo;

    @Autowired IResource resourceDao;

    @Autowired IPatient patientDao;

    private static final Logger log = LoggerFactory.getLogger(BundleDao.class);

    @Override
    public OperationOutcome create(FhirContext ctx, Bundle bundle, IdType theId, String theConditional) {

        log.debug("BundleDao.save");
        OperationOutcome operationOutcome = new OperationOutcome();

        BundleEntity bundleEntity = new BundleEntity();
        bundleEntity.setType(bundle.getType().toCode());


       if (bundle.hasIdentifier()) {
           mayfieldis.careconnect.nosql.entities.Identifier identifierE = new mayfieldis.careconnect.nosql.entities.Identifier();
           identifierE.setValue(bundle.getIdentifier().getValue());
           identifierE.setSystem(bundle.getIdentifier().getSystem());
           bundleEntity.setIdentifier(identifierE);

           Query qry = Query.query(Criteria.where("identifier.system").is(bundle.getIdentifier().getSystem()).and("identifier.value").is(bundle.getIdentifier().getValue()));

           BundleEntity bundleE = mongo.findOne(qry, BundleEntity.class);
           if (bundleE!=null) throw new ResourceVersionConflictException("FHIR Document already exists");
       }

        for (Bundle.BundleEntryComponent entry : bundle.getEntry()) {
            Entry entry1 = new Entry();
            if (entry.hasFullUrl()) entry1.setFullUrl(entry.getFullUrl());

            if (entry.hasResource()) {
                if (entry.getResource() instanceof Patient) {
                    // TODO ensure this is the correcct Patient (one referred to in the Composition)
                    bundleEntity.setPatient(patientDao.findInsert(ctx,(Patient) entry.getResource()));
                }

                entry1.setObjectId(resourceDao.save(ctx,entry.getResource()));
                entry1.setResourceType(entry.getResource().getResourceType().name());
                entry1.setOriginalId(StringUtils.remove(entry.getResource().getId(),"urn:uuid:"));
                if (entry.getResource() instanceof Composition) {
                    operationOutcome.setId("Composition/"+StringUtils.remove(entry.getResource().getId(),"urn:uuid:"));
                }
            }


            bundleEntity.getEntry().add(entry1);
        }

        mongo.save(bundleEntity);

        return operationOutcome;
    }
}
