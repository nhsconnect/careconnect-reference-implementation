package uk.org.hl7.fhir.validation.stu3;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import org.apache.commons.io.Charsets;
import org.hl7.fhir.dstu3.hapi.ctx.IValidationSupport;
import org.hl7.fhir.dstu3.model.*;
import org.hl7.fhir.dstu3.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.dstu3.model.CodeSystem.CodeSystemContentMode;
import org.hl7.fhir.dstu3.model.CodeSystem.ConceptDefinitionComponent;
import org.hl7.fhir.dstu3.model.ValueSet.ConceptReferenceComponent;
import org.hl7.fhir.dstu3.model.ValueSet.ConceptSetComponent;
import org.hl7.fhir.dstu3.model.ValueSet.ValueSetExpansionComponent;

import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.utilities.validation.ValidationMessage.IssueSeverity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.*;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import static org.apache.commons.lang3.StringUtils.isNotBlank;


public class CareConnectProfileValidationSupport implements IValidationSupport {

    // KGM 21st May 2018 Incorporated Tim Coates code to use UK FHIR Reference Servers.

    private static final int CACHE_MINUTES = 10;

    /**
     * Milliseconds we'll wait for an http connection.
     */
    private static final int CONNECT_TIMEOUT_MILLIS = 50000;

    private static int SC_OK = 200;

    private FhirContext ctx = null;

    private IParser parser;
    /**
     * Milliseconds we'll wait to read data over http.
     */
    private static final int READ_TIMEOUT_MILLIS = 50000;

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CareConnectProfileValidationSupport .class);

    private Map<String, IBaseResource> cachedResource ;


    public CareConnectProfileValidationSupport(final FhirContext theCtx) {
        this.ctx = theCtx;
        this.cachedResource = new HashMap<String, IBaseResource>();
        parser = ctx.newXmlParser();
    }

      private void logD(String message) {
          log.debug(message);
       //   System.out.printf(message);
      }
    private void logD(String message,Object value) {
        log.debug(String.format(message, value));
     //   System.out.printf(message,value);
    }

    private void logW(String message,Object value) {
        log.warn(String.format(message, value));
     //   System.out.printf(message,value);
    }

    private void logW(String message) {
        log.warn(message);
       // System.out.println(message);
    }
  @Override
  public ValueSetExpansionComponent expandValueSet(FhirContext theContext, ConceptSetComponent theInclude) {
    logD("CareConnect expandValueSet System="+theInclude.getSystem());
    ValueSetExpansionComponent retVal = new ValueSetExpansionComponent();

    Set<String> wantCodes = new HashSet<String>();
    for (ConceptReferenceComponent next : theInclude.getConcept()) {
      wantCodes.add(next.getCode());
      logD("CareConnect expandValueSet System="+theInclude.getSystem()+" wantCodes.add "+next.getCode());
    }

    CodeSystem system = fetchCodeSystem(theContext, theInclude.getSystem());
    for (ConceptDefinitionComponent next : system.getConcept()) {
      if (wantCodes.isEmpty() || wantCodes.contains(next.getCode())) {
        retVal.addContains().setSystem(theInclude.getSystem()).setCode(next.getCode()).setDisplay(next.getDisplay());

      }
    }

    return retVal;
  }

  @Override
  public List<IBaseResource> fetchAllConformanceResources(FhirContext theContext) {
    ArrayList<IBaseResource> retVal = new ArrayList<>();
    retVal.addAll(cachedResource.values());

    return retVal;
  }

  @Override
  public List<StructureDefinition> fetchAllStructureDefinitions(FhirContext theContext) {

    return new ArrayList<StructureDefinition>();

  }

  /*
  @Override
  public CodeSystem fetchCodeSystem(FhirContext theContext, String theSystem) {
    logD("CareConnect fetchCodeSystem "+theSystem);
    return (CodeSystem) fetchCodeSystemOrValueSet(theContext, theSystem, true);
  }
  */

    /**
     * Method to fetch a CodeSystem based on a provided URL. Tries to fetch it
     * from remote server. If it succeeds, it caches it in our global cache.
     *
     * @param theContext FHIR Context
     * @param theSystem The CodeSystem URL
     * @return Returns the retrieved FHIR Resource object or null
     */
    @Override
    public final CodeSystem fetchCodeSystem(
            final FhirContext theContext, final String theSystem) {
        logD(
                "MyInstanceValidator asked to fetch Code System: %s%n",
                theSystem);


        if (!isSupported(theSystem)) {
            log.trace("  Returning null as it's an HL7 one");
            return null;
        }

        CodeSystem newCS;

        if (cachedResource.get(theSystem) == null) {
            log.trace(" Not cached");
            IBaseResource response = fetchURL(theSystem);
            if (response != null) {
                log.trace("  Retrieved");
                cachedResource.put(theSystem, (CodeSystem) response);
                log.trace("   Cached");
            }
        }
        if (cachedResource.get(theSystem) == null && cachedResource.get(theSystem) instanceof CodeSystem) {
            log.trace("  Couldn't fetch it, so returning null");
            return null;
        } else {
            log.trace(" Provided from cache");
            return newCS = (CodeSystem) cachedResource.get(theSystem);

        }
        //return newCS;
    }

    /*
     * Method to retrieve any old FHIR Resource based on a URL.
     *
     * @param <T> The type to return.
     * @param theContext FHIR Conetxt.
     * @param theClass The Class type we're being asked for.
     * @param theUrl The URL to fetch from.
     * @return Returns an arbitrary FHIR Resource object, or null if it can't be
     * retrieved.
     */

    private Boolean isSupported(String theUrl) {

        // For extensions not contained in HAPI jar
        if (theUrl.startsWith("http://hl7.org/fhir/StructureDefinition/")) {
            log.trace(" *** TEST ");
            return true;
        }

        if (theUrl.startsWith("http://hl7.org/fhir/ValueSet/name-use")) {
            log.trace(" *** TEST ");
            return true;
        }



        if (theUrl.startsWith("http://hl7.org/fhir/")
                || theUrl.startsWith("https://hl7.org/fhir/")  ||
                theUrl.startsWith("http://snomed.info/sct")

                ) {
            log.trace("  Returning null as it's an HL7 one");
            return false;
        }
        if (!theUrl.startsWith("https://fhir.hl7.org.uk")  &&
                !theUrl.startsWith("https://fhir.nhs.uk")
                ) {

            return false;
        }
        return true;
    }

    @Override
    public final <T extends IBaseResource> T fetchResource(
            final FhirContext theContext,
            final Class<T> theClass,
            final String theUrl) {



        if (!isSupported(theUrl)) {
            log.trace("  Returning null as it's an HL7 one");
            return null;
        }

        logD(
                "MyInstanceValidator asked to fetch Resource: %s%n",
                theUrl);

        if (cachedResource.get(theUrl) == null) {
            IBaseResource response = fetchURL(theUrl);
            if (response != null) {
                log.trace("  About to parse response into a Resource");
                cachedResource.put(theUrl, response);
                logD("  Resource added to cache: %s%n", theUrl);
            } else {
                logW("  No data returned from: %s%n", theUrl);
            }
        } else {
            logD( "  This URL was already loaded: %s%n", theUrl);
        }

        return (T) cachedResource.get(theUrl);
    }

    /**
     * Method to fetch a remote StructureDefinition resource.
     *
     * Caches results.
     *
     * @param theCtx FHIR Context
     * @param theUrl The URL to fetch from
     * @return The StructureDefinition resource or null
     */
    @Override
    public final StructureDefinition fetchStructureDefinition(
            final FhirContext theCtx,
            final String theUrl) {
        logD(
                "MyInstanceValidator asked to fetch StructureDefinition: %s%n",
                theUrl);

        if (!isSupported(theUrl)) {
            log.trace("  Returning null as it's an HL7 one");
            return null;
        }

        if (cachedResource.get(theUrl) == null) {
            IBaseResource response = fetchURL(theUrl);
            log.trace("  About to parse response into a StructureDefinition");

            cachedResource.put(theUrl, (StructureDefinition) response);
            logD("  StructureDefinition now added to the cache: %s%n", theUrl);
        } else {
            logD("  This URL was already loaded: %s%n", theUrl);
        }
        StructureDefinition sd
                = (StructureDefinition)
                cachedResource.get(theUrl);
        return sd;
    }

  private DomainResource fetchCodeSystemOrValueSet(FhirContext theContext, String theSystem, boolean codeSystem) {
    synchronized (this) {
      logD("******* CareConnect fetchCodeSystemOrValueSet: system="+theSystem);

      Map<String, IBaseResource> codeSystems = cachedResource;

      return null;
    }
  }


  ValueSet fetchValueSet(FhirContext theContext, String theSystem) {
    return (ValueSet) fetchCodeSystemOrValueSet(theContext, theSystem, false);
  }

  public void flush() {
    cachedResource = null;
  }

  @Override
  public boolean isCodeSystemSupported(FhirContext theContext, String theSystem) {
    CodeSystem cs = fetchCodeSystem(theContext, theSystem);
    return cs != null && cs.getContent() != CodeSystemContentMode.NOTPRESENT;
  }


  private CodeValidationResult testIfConceptIsInList(String theCode, List<ConceptDefinitionComponent> conceptList, boolean theCaseSensitive) {
    logD("CareConnect testIfConceptIsInList: {} code="+ theCode);

    String code = theCode;
    if (theCaseSensitive == false) {
      code = code.toUpperCase();
    }

    return testIfConceptIsInListInner(conceptList, theCaseSensitive, code);
  }

  private CodeValidationResult testIfConceptIsInListInner(List<ConceptDefinitionComponent> conceptList, boolean theCaseSensitive, String code) {
    logD("CareConnect testIfConceptIsInListInner: code=" + code);
    CodeValidationResult retVal = null;
    for (ConceptDefinitionComponent next : conceptList) {
      // KGM
      logD("CareConnect testIfConceptIsInListInner NextCode = "+next.getCode());
      String nextCandidate = next.getCode();
      if (theCaseSensitive == false) {
        nextCandidate = nextCandidate.toUpperCase();
      }
      if (nextCandidate.equals(code)) {
        logD("The Code "+code+" is in the list");
        retVal = new CodeValidationResult(next);
        break;
      }

      // recurse
      retVal = testIfConceptIsInList(code, next.getConcept(), theCaseSensitive);
      if (retVal != null) {
        break;
      }
    }

    return retVal;
  }

  @Override
  public CodeValidationResult validateCode(FhirContext theContext, String theCodeSystem, String theCode, String theDisplay) {
    CodeSystem cs = fetchCodeSystem(theContext, theCodeSystem);
    logD("CareConnect validateCode system = "+ theCodeSystem);

    if (cs != null) {
      boolean caseSensitive = true;
      if (cs.hasCaseSensitive()) {
        caseSensitive = cs.getCaseSensitive();
      }

      CodeValidationResult retVal = testIfConceptIsInList(theCode, cs.getConcept(), caseSensitive);

      if (retVal != null) {
        return retVal;
      }
    }

    return new CodeValidationResult(IssueSeverity.WARNING, "CareConnect Unknown code: " + theCodeSystem + " / " + theCode);
  }



  private IBaseResource fetchURL(final String theUrl) {
    logD("  This URL not yet loaded: %s%n", theUrl);

    StringBuilder result = new StringBuilder();

    try {
      URL url = new URL(theUrl);
      try {
        HttpURLConnection conn
                = (HttpURLConnection) url.openConnection();
        // We need to supply a header value here. otherwise the
        // fhir.nhs.uk server assumes we're a browser, and returns us a
        // pretty html view of the requested resource.
        conn.setRequestProperty("Accept", "application/xml");
        conn.setConnectTimeout(CONNECT_TIMEOUT_MILLIS);
        conn.setReadTimeout(READ_TIMEOUT_MILLIS);
        logD("    Connected");
        try {
          conn.setRequestMethod("GET");
          try {
            BufferedReader rd
                    = new BufferedReader(
                    new InputStreamReader(
                            conn.getInputStream(),
                            Charset.forName("UTF-8")));
            try {
              int httpCode = conn.getResponseCode();
              if (httpCode == SC_OK) {
                log.trace("    Got a 200 status");
                String line;
                try {
                  while ((line = rd.readLine()) != null) {
                    result.append(line);
                  }
                  log.trace("    No more data");
                  rd.close();
                } catch (IOException ex) {
                  logW(
                          "IOException 1 caught trying to read from: %s%n",
                          theUrl);
                  log.warn(ex.getMessage());
                }
              } else {

                logW(theUrl +"  http status code was: %s%n", httpCode);
              }
            } catch (IOException ex) {
              logW(
                      "IOException 2 caught trying to fetch: %s%n",
                      theUrl);
              log.warn(ex.getMessage());
            }
          } catch (IOException ex) {
            logW(
                    "IOException 3 caught trying to fetch: %s%n",
                    theUrl);
          }
        } catch (ProtocolException ex) {
          logW(
                  "ProtocolException 4 caught trying to fetch: %s%n",
                  theUrl);
          log.warn(ex.getMessage());
        }
      } catch (IOException ex) {
        logW(
                "IOException 5 caught trying to fetch: %s%n",
                theUrl);
        log.warn(ex.getMessage());
      }
    } catch (MalformedURLException ex) {
      logW(
              "MalformedURLException 6 caught trying to fetch: %s%n",
              theUrl);
      log.warn(ex.getMessage());
    }
    if (result.length() > 0) {

      return parser.parseResource(result.toString());
    } else {
      return null;
    }
  }

}
