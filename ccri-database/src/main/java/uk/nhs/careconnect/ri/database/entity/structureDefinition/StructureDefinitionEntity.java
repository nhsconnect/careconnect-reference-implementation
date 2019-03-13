package uk.nhs.careconnect.ri.database.entity.structureDefinition;


import org.hl7.fhir.dstu3.model.Enumerations;
import uk.nhs.careconnect.ri.database.entity.BaseResource;


import javax.persistence.*;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "StructureDefinition",
        indexes = {

        })
public class StructureDefinitionEntity extends BaseResource {

    private static final int MAX_DESC_LENGTH = 4096;


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="STRUCTURE_ID")
    private Long id;

    @Column(name="URL",length = MAX_DESC_LENGTH,nullable = true)
    private String url;

    @OneToMany(mappedBy="structureDefinition", targetEntity= StructureDefinitionIdentifier.class)
    private List<StructureDefinitionIdentifier> identifiers;

    @Column(name="VERSION",length = MAX_DESC_LENGTH,nullable = true)
    private String version;

    @Column(name="NAME",length = MAX_DESC_LENGTH,nullable = true)
    private String name;

    @Enumerated(EnumType.ORDINAL)
    @Column(name="status")
    private Enumerations.PublicationStatus status;

    @Column(name = "experimental")
    private Boolean experimental;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "DATETIME")
    private Date dateTime;

    @Column(name = "publisher")
    private String publisher;

    @Column(name = "COPYRIGHT")
    private String copyright;

    // Ignore usage context and jurisdiction for now


    @Column(name = "description",length = MAX_DESC_LENGTH)
    private String description;


    @Column(name = "purpose",length = MAX_DESC_LENGTH)
    private String purpose;

    @Column(name="START")
    String start;

    @Column(name = "profile")
    private String profile;


    public Enumerations.PublicationStatus getStatus() {
        return status;
    }

    public void setStatus(Enumerations.PublicationStatus status) {
        this.status = status;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public Date getDateTime() {
        return dateTime;
    }

    public void setDateTime(Date dateTime) {
        this.dateTime = dateTime;
    }



    public Long getId() {
        return id;
    }


    public void setId(Long id) {
        this.id = id;
    }


    public static int getMaxDescLength() {
        return MAX_DESC_LENGTH;
    }

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Boolean getExperimental() {
        return experimental;
    }

    public void setExperimental(Boolean experimental) {
        this.experimental = experimental;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }


    public String getStart() {
        return start;
    }

    public void setStart(String start) {
        this.start = start;
    }

    public String getProfile() {
        return profile;
    }

    public void setProfile(String profile) {
        this.profile = profile;
    }

    public List<StructureDefinitionIdentifier> getIdentifiers() {
        return identifiers;
    }

    public void setIdentifiers(List<StructureDefinitionIdentifier> identifiers) {
        this.identifiers = identifiers;
    }

    public String getCopyright() {
        return copyright;
    }

    public void setCopyright(String copyright) {
        this.copyright = copyright;
    }
}
