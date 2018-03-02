
package mayfieldis.careconnect.nosql.entities;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.*;


@Document(collection = "docPatient")
public class PatientEntity  {

    @Id
    private ObjectId id;

    private Date dateOfBirth;

    private String gender;

    private Collection<Identifier> identifiers  = new LinkedHashSet<>();

    private Collection<Telecom> telecoms = new LinkedHashSet<>();

    private Collection<Name> names = new LinkedHashSet<>();

    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public Date getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(Date dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public Collection<Identifier> getIdentifiers() {
        return identifiers;
    }

    public void setIdentifiers(Collection<Identifier> identifiers) {
        this.identifiers = identifiers;
    }

    public Collection<Telecom> getTelecoms() {
        return telecoms;
    }

    public void setTelecoms(Collection<Telecom> telecoms) {
        this.telecoms = telecoms;
    }

    public Collection<Name> getNames() {
        return names;
    }

    public void setNames(Collection<Name> names) {
        this.names = names;
    }
}
