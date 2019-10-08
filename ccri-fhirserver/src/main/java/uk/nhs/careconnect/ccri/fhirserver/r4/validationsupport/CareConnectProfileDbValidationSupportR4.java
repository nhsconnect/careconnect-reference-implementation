package uk.nhs.careconnect.ccri.fhirserver.r4.validationsupport;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import org.hl7.fhir.convertors.VersionConvertor_30_40;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.hapi.ctx.IValidationSupport;
import org.hl7.fhir.r4.model.CodeSystem;
import org.hl7.fhir.r4.model.DomainResource;
import org.hl7.fhir.r4.model.StructureDefinition;
import org.hl7.fhir.r4.model.ValueSet;
import org.hl7.fhir.r4.terminologies.ValueSetExpander;
import org.hl7.fhir.utilities.validation.ValidationMessage.IssueSeverity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class CareConnectProfileDbValidationSupportR4 implements IValidationSupport {

    // KGM 21st May 2018 Incorporated Tim Coates code to use UK FHIR Reference Servers.

    private static final int CACHE_MINUTES = 10;

    /**
     * Milliseconds we'll wait for an http connection.
     */
    private static final int CONNECT_TIMEOUT_MILLIS = 50000;

    private static int SC_OK = 200;

    private FhirContext ctxR4 = null;

    private FhirContext ctxStu3 = null;

    IGenericClient client;

    private IParser parserR4;

    private IParser parserStu3;
    /**
     * Milliseconds we'll wait to read data over http.
     */
    private static final int READ_TIMEOUT_MILLIS = 50000;


    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CareConnectProfileDbValidationSupportR4.class);

    private Map<String, IBaseResource> cachedResource ;

    private String alternateServer;

    public CareConnectProfileDbValidationSupportR4(final FhirContext r4Ctx, FhirContext stu3Ctx, String alternateServer) {
        this.ctxR4 = r4Ctx;
        this.ctxStu3 = stu3Ctx;
        this.cachedResource = new HashMap<String, IBaseResource>();
        parserR4 = ctxR4.newXmlParser();
        parserStu3 = ctxStu3.newXmlParser();
        this.alternateServer = alternateServer;
    }

      private void logD(String message) {
          log.debug(message);
          System.out.println(message);
      }
    private void logD(String message,Object value) {
        log.debug(String.format(message, value));
      //  System.out.printf(message,value);
    }

    private void logW(String message,Object value) {
        log.warn(String.format(message, value));
        System.out.printf(message,value);
    }

    private void logW(String message) {
        log.warn(message);
        System.out.println(message);
    }

    private void logT(String message) {
        log.trace(message);
    }

    private void logT(String message,Object value) {
        log.warn(String.format(message, value));
    }

    @Override
    public ValueSetExpander.ValueSetExpansionOutcome expandValueSet(FhirContext fhirContext, ValueSet.ConceptSetComponent conceptSetComponent) {
        logW("CareConnect expandValueSet System="+conceptSetComponent.getSystem());



        // todo
         return null;
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
                "CareConnectValidator asked to fetch Code System: %s%n",
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
        /*
        if (theUrl.startsWith("http://hl7.org/fhir/StructureDefinition/")) {
            log.trace(" *** TEST ");
            return true;
        }

        if (theUrl.startsWith("http://hl7.org/fhir/ValueSet/name-use")) {
            log.trace(" *** TEST ");
            return true;
        }

        */
        if (theUrl.startsWith("https://fhir.hl7.org.uk/STU3")) {

            return true;
        }
        if (theUrl.startsWith("https://fhir.nhs.uk/STU3")) {

            return true;
        }

        return false;
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

        logT(
                "CareConnectValidator asked to fetch Resource: %s%n",
                theUrl);

        if (cachedResource.get(theUrl) == null) {
            IBaseResource response = fetchURL(theUrl);
            if (response != null) {

                cachedResource.put(theUrl, response);
                logD("  Resource added to cache: %s%n", theUrl);
            } else {
                logW("  No data returned from: %s%n", theUrl);
            }
        } else {
            logT( "  This URL was already loaded: %s%n", theUrl);
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
                "CareConnectValidator asked to fetch StructureDefinition: %s%n",
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
      logW("******* CareConnect fetchCodeSystemOrValueSet: system="+theSystem);

      Map<String, IBaseResource> codeSystems = cachedResource;

      return null;
    }
  }


  public ValueSet fetchValueSet(FhirContext theContext, String theSystem) {
      logW("CareConnect fetchValueSet: system="+theSystem);
    return (ValueSet) fetchCodeSystemOrValueSet(theContext, theSystem, false);
  }

  public void flush() {
    cachedResource = null;
  }

  @Override
  public boolean isCodeSystemSupported(FhirContext theContext, String theSystem) {
    CodeSystem cs = fetchCodeSystem(theContext, theSystem);
    return cs != null && cs.getContent() != CodeSystem.CodeSystemContentMode.NOTPRESENT;
  }


  private CodeValidationResult testIfConceptIsInList(String theCode, List<CodeSystem.ConceptDefinitionComponent> conceptList, boolean theCaseSensitive) {
    logT("CareConnect testIfConceptIsInList: {} code="+ theCode);

    String code = theCode;
    if (theCaseSensitive == false) {
      code = code.toUpperCase();
    }

    return testIfConceptIsInListInner(conceptList, theCaseSensitive, code);
  }

  private CodeValidationResult testIfConceptIsInListInner(List<CodeSystem.ConceptDefinitionComponent> conceptList, boolean theCaseSensitive, String code) {
    logT("CareConnect testIfConceptIsInListInner: code=" + code);
    CodeValidationResult retVal = null;
    for (CodeSystem.ConceptDefinitionComponent next : conceptList) {
      // KGM
      logT("CareConnect testIfConceptIsInListInner NextCode = "+next.getCode());
      String nextCandidate = next.getCode();
      if (theCaseSensitive == false) {
        nextCandidate = nextCandidate.toUpperCase();
      }
      if (nextCandidate.equals(code)) {
        logD("Code "+code+" is in the list");
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

    private IBaseResource doCheckCCRI(final String theUrl) {

        if (client == null && this.alternateServer !=null) {
            log.info("Alternate source for profiles " + this.alternateServer);
            client = ctxStu3.newRestfulGenericClient(this.alternateServer);
        }

        if (client != null) {
            if (theUrl.contains("Questionnaire") && (!theUrl.contains("QuestionnaireResponse") ) ) {
                org.hl7.fhir.dstu3.model.Bundle results = client.search().forResource(org.hl7.fhir.dstu3.model.Questionnaire.class).where(org.hl7.fhir.dstu3.model.Questionnaire.URL.matches().value(theUrl)).returnBundle(org.hl7.fhir.dstu3.model.Bundle.class).execute();
                log.info("Seearching for Questionnaire " + theUrl);
                if (results.getEntry().size() > 0) {
                    log.info("Found Questionnaire");
                    org.hl7.fhir.dstu3.model.Bundle.BundleEntryComponent entry = results.getEntry().get(0);
                    return entry.getResource();
                } else {
                    log.info("NOT FOUND Questionnaire");
                    return null;
                }
            }
            if (theUrl.contains("StructureDefinition")  )  {
                org.hl7.fhir.dstu3.model.Bundle results = client.search().forResource(org.hl7.fhir.dstu3.model.StructureDefinition.class).where(org.hl7.fhir.dstu3.model.StructureDefinition.URL.matches().value(theUrl)).returnBundle(org.hl7.fhir.dstu3.model.Bundle.class).execute();
                log.info("Seearching for StructureDefinition " + theUrl);
                if (results.getEntry().size() > 0) {
                    log.info("Found StructureDefinition");
                    org.hl7.fhir.dstu3.model.Bundle.BundleEntryComponent entry = results.getEntry().get(0);
                    return entry.getResource();
                } else {
                    log.info("NOT FOUND StructureDefinition");
                    return null;
                }
            }
            return null;

        }
        else {
            return null;
        }
    }

  private IBaseResource fetchURL(final String theUrl) {
    logT("  This URL not yet loaded: %s%n", theUrl);

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

                String line;
                try {
                  while ((line = rd.readLine()) != null) {
                    result.append(line);
                  }

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
          return convertToR4(parserStu3.parseResource(result.toString()));


      } else {
          // Temp fix to get around Questionnaire not being supported by Reference Server 8/March/2019 KGM
          return doCheckCCRI(theUrl);

      }
  }

  public IBaseResource convertToR4(IBaseResource stu3resource) {
        if (stu3resource == null) return null;
      VersionConvertor_30_40 convertor = new VersionConvertor_30_40();
      return convertor.convertResource((org.hl7.fhir.dstu3.model.Resource) stu3resource, true);


     // log.info(this.r4ctx.newXmlParser().setPrettyPrint(true).encodeResourceToString(convertedResource));

  }

    @Override
    public StructureDefinition generateSnapshot(StructureDefinition structureDefinition, String s, String s1, String s2) {
        return null;
    }

    @Override
    public LookupCodeResult lookupCode(FhirContext fhirContext, String s, String s1) {
        return null;
    }
}