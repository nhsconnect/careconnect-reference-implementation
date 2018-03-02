package mayfieldis.careconnect.nosql.entities;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.persistence.*;

@Document(collection = "documentReference")
public class DocumentReferenceEntity {
    @Id
    private ObjectId id;
    public ObjectId getId() { return id; }
    public void setId(ObjectId id) { this.id = id; }


    private String name;
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    private String json;
    public String getJson() {
        return json;
    }

    public void setJson(String json) {
        this.json = json;
    }
    /*
    @ManyToOne
    public BundleEntity getBreed() { return breed; }
    public void setBreed(BundleEntity breed) { this.breed = breed; }
    private BundleEntity breed;*/
}

