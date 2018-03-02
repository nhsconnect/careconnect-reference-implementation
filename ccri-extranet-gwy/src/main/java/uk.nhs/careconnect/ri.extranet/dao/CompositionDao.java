package mayfieldis.careconnect.nosql.dao;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.param.TokenParam;
import com.fasterxml.jackson.databind.util.JSONPObject;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;
import mayfieldis.careconnect.nosql.entities.BundleEntity;
import mayfieldis.careconnect.nosql.entities.Entry;
import mayfieldis.careconnect.nosql.entities.PatientEntity;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Composition;
import org.hl7.fhir.dstu3.model.IdType;

import org.hl7.fhir.dstu3.model.Resource;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;

@Transactional
@Repository
public class CompositionDao implements IComposition {


    @Autowired
    protected MongoTemplate mongo;

    @Autowired IResource resourceDao;

    private static final Logger log = LoggerFactory.getLogger(CompositionDao.class);

    @Override
    public List<Resource> search(FhirContext ctx, TokenParam resid, ReferenceParam patient) {

        List<Resource> resources = new ArrayList<>();

        Criteria criteria = null;

        if (resid != null) {
            if (criteria == null) {
                criteria = Criteria.where("entry.originalId").is(resid.getValue()).and("entry.resourceType").is("Composition");
            } else {
                criteria = criteria.and("entry.originalId").is(resid.getValue()).and("entry.resourceType").is("Composition");
            }
        }
        if (patient != null) {
            //log.info("Patient = "+patient.getValue());
            if (criteria == null) {
                criteria = Criteria.where("patient").is(converToObjectId(patient.getValue()));
            } else {
                criteria = criteria.and("patient").is(converToObjectId(patient.getValue()));
            }
        }
        if (criteria != null) {
            Query qry = Query.query(criteria);

            List<BundleEntity> results = mongo.find(qry, BundleEntity.class);
           // log.info("Bundle size = "+results.size());
            for (BundleEntity bundleEntity : results) {
             //   log.info("Found Entry "+bundleEntity.getPatient().toString());
                for (Entry entry : bundleEntity.getEntry()) {
                    if (entry.getResourceType().equals("Composition")) {
                        resources.add(read(ctx,new IdType().setValue(entry.getOriginalId())));
                    }
                }
            }
        }

        return resources;
    }

    @Override
    public Composition read(FhirContext ctx, IdType theId) {
        Query qry = Query.query(Criteria.where("entry.originalId").is(theId.getIdPart()).and("entry.resourceType").is("Composition"));
        System.out.println(qry.toString());
        BundleEntity document = mongo.findOne(qry, BundleEntity.class);
        if (document!=null) {
            log.info("Found = "+document.getEntry().size());
            Composition composition = null;
            for (Entry entry : document.getEntry()) {
                if (entry.getOriginalId().equals(theId.getIdPart())) {


                    qry = Query.query(Criteria.where("_id").is(entry.getObjectId()));
                    DBObject resourceObj = mongo.findOne(qry,DBObject.class,entry.getResourceType());
                    if (resourceObj != null) {

                        // Remove Mongo Elements
                        JsonParser jsonParser = new JsonParser();
                        JsonObject jo = (JsonObject)jsonParser.parse(resourceObj.toString());
                        jo.remove("_class");
                        jo.remove("_id");

                        IBaseResource resource = ctx.newJsonParser().parseResource(jo.toString());
                        resource.setId(StringUtils.remove(entry.getOriginalId(),"urn:uuid:"));

                        if (resource instanceof  Composition) composition = (Composition) resource;
                    }

                }
            }
            return composition;
        } else {
            return null;
        }
    }

    private ObjectId converToObjectId(String id) {
        return new ObjectId(id);
    }

    @Override
    public Bundle readDocument(FhirContext ctx, IdType theId) {
        // Search for document bundle rather than composition (this contains a link to the Composition

        // {'entry.objectId': ObjectId("5a95166bbc5b249440975d8f"), 'entry.resourceType' : 'Composition'}
        Bundle bundle = new Bundle();
        bundle.setType(Bundle.BundleType.DOCUMENT);

        Query qry = Query.query(Criteria.where("entry.originalId").is(theId.getIdPart()).and("entry.resourceType").is("Composition"));
        System.out.println(qry.toString());
        BundleEntity document = mongo.findOne(qry, BundleEntity.class);
        if (document!=null) {
            log.info(document.toString());
            for (Entry entry :document.getEntry()) {
               // System.out.println(entry.getObjectId());

                qry = Query.query(Criteria.where("_id").is(entry.getObjectId()));
                DBObject resourceObj = mongo.findOne(qry,DBObject.class,entry.getResourceType());
                if (resourceObj != null) {
                    //System.out.println("DBO-"+resourceObj.toString());

                    JsonParser jsonParser = new JsonParser();
                    JsonObject jo = (JsonObject)jsonParser.parse(resourceObj.toString());
                    jo.remove("_class");
                    jo.remove("_id");
                    //System.out.println("JO-"+jo.toString());
                    IBaseResource resource = ctx.newJsonParser().parseResource(jo.toString());
                    resource.setId(StringUtils.remove(entry.getOriginalId(),"urn:uuid:"));
                    bundle.addEntry().setResource((Resource) resource).setFullUrl("urn:uuid:"+((Resource) resource).getId());
                }
            }
        } else {
            return null;
        }

        return bundle;
    }
}
