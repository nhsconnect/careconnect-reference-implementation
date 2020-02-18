package uk.nhs.careconnect.ccri.fhirserver.stu3.validationSupport;

import ca.uhn.fhir.context.FhirContext;
import org.apache.commons.lang3.Validate;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.hl7.fhir.dstu3.hapi.ctx.IValidationSupport;
import org.hl7.fhir.dstu3.model.CodeSystem;
import org.hl7.fhir.dstu3.model.DomainResource;
import org.hl7.fhir.dstu3.model.StructureDefinition;
import org.hl7.fhir.dstu3.model.ValueSet;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.utilities.cache.NpmPackage;
import org.hl7.fhir.utilities.cache.PackageCacheManager;
import org.hl7.fhir.utilities.cache.ToolsVersion;

import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;


public class IGValidationSupport implements IValidationSupport {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(IGValidationSupport.class);

    private Map<String, CodeSystem> myCodeSystems;
    private Map<String, StructureDefinition> myStructureDefinitions;
    private Map<String, ValueSet> myValueSets;
    FhirContext stu3ctx;
    PackageCacheManager pcm;
    NpmPackage npm = null;

    public IGValidationSupport(FhirContext stu3ctx, NpmPackage _validation) throws Exception {
        pcm = new PackageCacheManager(true, ToolsVersion.TOOLS_VERSION);

        this.myCodeSystems = new HashMap();
        this.myValueSets = new HashMap<>();
        this.myStructureDefinitions = new HashMap<>();

        npm = _validation;

        for (String resource : npm.listResources( "StructureDefinition")) {

            StructureDefinition structureDefinition = (StructureDefinition) stu3ctx.newJsonParser().parseResource(npm.load("package", resource));
            LOG.info(structureDefinition.getUrl());
            this.myStructureDefinitions.put(structureDefinition.getUrl(),structureDefinition);
        }

    }


    public IGValidationSupport(FhirContext stu3ctx, String igUri) throws Exception {
        LOG.info("IG Validation Support Constructor");
        HttpClient client = getHttpClient();
        LOG.info("Retrieving Validate Pack from {}",igUri + "validate.pack");
        HttpGet request = new HttpGet(igUri + "validator.pack");
        this.stu3ctx = stu3ctx;
        this.myCodeSystems = new HashMap();
        this.myValueSets = new HashMap<>();
        this.myStructureDefinitions = new HashMap<>();

        getRequest(client,request);
    }
    private HttpClient getHttpClient(){
        final HttpClient httpClient = HttpClientBuilder.create().build();
        return httpClient;
    }


    private void getRequest(HttpClient client1, HttpGet request) throws Exception {

        HttpResponse response;
        Reader reader;
        try {
            response = client1.execute(request);
            if (response.getStatusLine().getStatusCode() == 200) {
                LOG.info("Retrieved Validate Pack");
                ZipInputStream zis = new ZipInputStream(response.getEntity().getContent());
                ZipEntry zipEntry = zis.getNextEntry();
                while (zipEntry != null) {

                    if (zipEntry.getName().endsWith(".json")) {
                        reader = new InputStreamReader(zis);
                        IBaseResource resource = stu3ctx.newJsonParser().parseResource(reader);
                        if (resource instanceof StructureDefinition) {
                            StructureDefinition sd = (StructureDefinition) resource;
                            this.myStructureDefinitions.put(sd.getUrl(),sd);
                        } else if (resource instanceof CodeSystem) {
                            CodeSystem cs = (CodeSystem) resource;
                           // this.myCodeSystems.put(cs.getUrl(),cs);
                        } else if (resource instanceof ValueSet) {
                            ValueSet vs = (ValueSet) resource;
                           // this.myValueSets.put(vs.getUrl(),vs);
                        }
                        LOG.debug(zipEntry.getName());
                        LOG.debug(stu3ctx.newXmlParser().encodeResourceToString(resource));
                    }
                    zipEntry = zis.getNextEntry();
                }
                zis.closeEntry();
                zis.close();
            } else {
                LOG.error("Failed to retrieve validator pack: {} ", response.getStatusLine().getReasonPhrase());
                throw new Exception("Unable to load validation pack");
            }
        } catch (Exception e) {
            LOG.error(e.getStackTrace().toString());
            LOG.error(e.getMessage());
            throw e;
        }
    }

    @Override
    public ValueSet.ValueSetExpansionComponent expandValueSet(FhirContext fhirContext, ValueSet.ConceptSetComponent conceptSetComponent) {
        return null;
    }

    @Override
    public List<StructureDefinition> fetchAllStructureDefinitions(FhirContext fhirContext) {
        return new ArrayList<>(this.myStructureDefinitions.values());
    }

    @Override
    public CodeSystem fetchCodeSystem(FhirContext theContext, String theSystem) {
        return (CodeSystem)this.fetchCodeSystemOrValueSet(theContext, theSystem, true);
    }

    @Override
    public ValueSet fetchValueSet(FhirContext fhirContext, String uri) {
        return (ValueSet)this.fetchCodeSystemOrValueSet(fhirContext, uri, false);
    }

    @Override
    public StructureDefinition fetchStructureDefinition(FhirContext fhirContext, String url) {
        return (StructureDefinition)this.myStructureDefinitions.get(url);
    }

    @Override
    public boolean isCodeSystemSupported(FhirContext fhirContext, String s) {
        return false;
    }

    @Override
    public StructureDefinition generateSnapshot(StructureDefinition structureDefinition, String s, String s1) {
        return null;
    }

    @Override
    public List<IBaseResource> fetchAllConformanceResources(FhirContext fhirContext) {
        ArrayList<IBaseResource> retVal = new ArrayList();
        retVal.addAll(this.myCodeSystems.values());
        retVal.addAll(this.myStructureDefinitions.values());
        retVal.addAll(this.myValueSets.values());
        return retVal;
    }

    @Override
    public <T extends IBaseResource> T fetchResource(FhirContext fhirContext, Class<T> theClass, String theUri) {
        Validate.notBlank(theUri, "theUri must not be null or blank", new Object[0]);
        if (theClass.equals(StructureDefinition.class)) {
            return (T) this.fetchStructureDefinition(fhirContext, theUri);
        } else {
            return !theClass.equals(ValueSet.class) && !theUri.startsWith("http://hl7.org/fhir/ValueSet/") ? null : (T) this.fetchValueSet(fhirContext, theUri);
        }
    }

    @Override
    public CodeValidationResult validateCode(FhirContext fhirContext, String s, String s1, String s2, String s3) {
        return null;
    }

    @Override
    public LookupCodeResult lookupCode(FhirContext fhirContext, String s, String s1) {
        return null;
    }

    private DomainResource fetchCodeSystemOrValueSet(FhirContext theContext, String theSystem, boolean codeSystem) {
        synchronized(this) {
            return codeSystem ? (DomainResource)((Map)this.myCodeSystems).get(theSystem) : (DomainResource)((Map)this.myValueSets).get(theSystem);
        }
    }
}
