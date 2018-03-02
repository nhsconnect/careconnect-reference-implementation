package mayfieldis.careconnect.nosql.dao;

import ca.uhn.fhir.context.FhirContext;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;

@Transactional
@Repository
public class ResourceDao implements IResource {

    @Autowired
    protected MongoTemplate mongoTemplate;



    private static final Logger log = LoggerFactory.getLogger(ResourceDao.class);


    @Override
    public ObjectId save(FhirContext ctx, Resource resource) {

        Document doc = Document.parse(ctx.newJsonParser().encodeResourceToString(resource));
        // Convert to BasicDBObject to get object id
        DBObject mObj = new BasicDBObject(doc);
        mongoTemplate.insert(mObj, resource.getResourceType().name());

        return (ObjectId) mObj.get("_id");
    }

    @Override
    public Resource read(FhirContext ctx, IdType theId) {
        return null;
    }
}
