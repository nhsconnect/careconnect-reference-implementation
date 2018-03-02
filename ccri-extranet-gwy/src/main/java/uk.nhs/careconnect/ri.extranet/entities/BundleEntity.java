package mayfieldis.careconnect.nosql.entities;


import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;


import java.util.Collection;
import java.util.LinkedHashSet;


@Document(collection = "docBundle")
public class BundleEntity {

    @Id
    private ObjectId id;
    public ObjectId getId() { return id; }
    public void setId(ObjectId id) { this.id = id; }

    private String type;

    private Collection<Entry> entry = new LinkedHashSet<>();

    private ObjectId patient;

    private Identifier identifier;

    String originalId;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Collection<Entry> getEntry() {
        return entry;
    }

    public void setEntry(Collection<Entry> entry) {
        this.entry = entry;
    }

    public ObjectId getPatient() {
        return patient;
    }

    public void setPatient(ObjectId patient) {
        this.patient = patient;
    }

    public String getOriginalId() {
        return originalId;
    }

    public void setOriginalId(String originalId) {
        this.originalId = originalId;
    }

    public Identifier getIdentifier() {
        return identifier;
    }

    public void setIdentifier(Identifier identifier) {
        this.identifier = identifier;
    }
}