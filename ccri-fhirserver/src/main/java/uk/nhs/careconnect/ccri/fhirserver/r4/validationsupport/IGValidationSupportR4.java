package uk.nhs.careconnect.ccri.fhirserver.r4.validationsupport;

import ca.uhn.fhir.context.FhirContext;
import org.apache.commons.lang3.Validate;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.fhir.ucum.Value;
import org.hl7.fhir.r4.hapi.ctx.IValidationSupport;
import org.hl7.fhir.r4.model.CodeSystem;
import org.hl7.fhir.r4.model.DomainResource;
import org.hl7.fhir.r4.model.StructureDefinition;
import org.hl7.fhir.r4.model.ValueSet;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.terminologies.ValueSetExpander;
import org.hl7.fhir.utilities.cache.NpmPackage;
import org.hl7.fhir.utilities.cache.PackageCacheManager;
import org.hl7.fhir.utilities.cache.ToolsVersion;

import javax.annotation.Nonnull;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;


public class IGValidationSupportR4 implements IValidationSupport {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(IGValidationSupportR4.class);

    private Map<String, CodeSystem> myCodeSystems;
    private Map<String, StructureDefinition> myStructureDefinitions;
    private Map<String, ValueSet> myValueSets;
    FhirContext stu3ctx;
    PackageCacheManager pcm;
    NpmPackage npm = null;

    public IGValidationSupportR4(FhirContext stu3ctx, NpmPackage _validation) throws Exception {
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
        for (String resource : npm.listResources( "ValueSet")) {

            ValueSet valueSet = (ValueSet) stu3ctx.newJsonParser().parseResource(npm.load("package", resource));
            LOG.info(valueSet.getUrl());
            this.myValueSets.put(valueSet.getUrl(),valueSet);
        }
        for (String resource : npm.listResources( "CodeSystem")) {

            CodeSystem codeSystem = (CodeSystem) stu3ctx.newJsonParser().parseResource(npm.load("package", resource));
            LOG.info(codeSystem.getUrl());
            this.myCodeSystems.put(codeSystem.getUrl(),codeSystem);
        }
    }





    @Override
    public CodeValidationResult validateCodeInValueSet(FhirContext theContext, String theCodeSystem, String theCode, String theDisplay, @Nonnull IBaseResource theValueSet) {
        return null;
    }

    @Override
    public ValueSetExpander.ValueSetExpansionOutcome expandValueSet(FhirContext fhirContext, ValueSet.ConceptSetComponent conceptSetComponent) {
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
    public StructureDefinition generateSnapshot(StructureDefinition structureDefinition, String s, String s1, String s2) {
        return null;
    }

    @Override
    public boolean isValueSetSupported(FhirContext theContext, String theValueSetUrl) {
        return false;
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
        } else if (theClass.equals(ValueSet.class)) {
            return (T) this.fetchValueSet(fhirContext, theUri);
        } else if (theClass.equals(CodeSystem.class)) {
            return (T) this.fetchCodeSystem(fhirContext, theUri);
        }
        else {
            return null;
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
