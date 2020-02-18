package uk.nhs.careconnect.ccri.fhirserver.stu3.validationSupport;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.rest.api.Constants;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.dstu3.hapi.ctx.IValidationSupport;
import org.hl7.fhir.dstu3.model.CodeSystem;
import org.hl7.fhir.dstu3.model.StructureDefinition;
import org.hl7.fhir.dstu3.model.ValueSet;
import org.hl7.fhir.utilities.validation.ValidationMessage.IssueSeverity;
import uk.nhs.careconnect.ccri.fhirserver.HapiProperties;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.apache.commons.lang3.StringUtils.isBlank;


public class NHSDigitalProfileValidationSupportSTU3 implements IValidationSupport {

    // KGM 21st May 2018 Incorporated Tim Coates code to use UK FHIR Reference Servers.



    /**
     * Milliseconds we'll wait for an http connection.
     */
    private static final int CONNECT_TIMEOUT_MILLIS = 50000;

    private FhirContext ctxStu3 = null;

    IGenericClient client;


    private IParser parserStu3;
    /**
     * Milliseconds we'll wait to read data over http.
     */
    private static final int READ_TIMEOUT_MILLIS = 50000;


    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(NHSDigitalProfileValidationSupportSTU3.class);

    private Map<String, IBaseResource> cachedResource ;

    private String alternateServer;

    public NHSDigitalProfileValidationSupportSTU3(FhirContext stu3Ctx) {

        this.ctxStu3 = stu3Ctx;
        this.cachedResource = new HashMap<>();

        parserStu3 = ctxStu3.newXmlParser();
        this.alternateServer = HapiProperties.getTerminologyServerSecondary();
    }

    private void logI(String message) {
        log.info(message);
    }
      private void logD(String message) {
          log.debug(message);

      }
    private void logD(String message,Object value) {
        log.debug(String.format(message, value));

    }

    private void logW(String message,Object value) {
        log.warn(String.format(message, value));

    }

    private void logW(String message) {
        log.warn(message);

    }

    private void logT(String message) {
        log.trace(message);
    }

    private void logT(String message,Object value) {
        log.trace(String.format(message, value));
    }

    @Override
    public ValueSet.ValueSetExpansionComponent expandValueSet(FhirContext fhirContext, ValueSet.ConceptSetComponent conceptSetComponent) {
        logI("CareConnect expandValueSet System="+conceptSetComponent.getSystem());
        // CCRI ValueSets tend to be contained, so validateCode will be called instead.
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


    @Override
    public final CodeSystem fetchCodeSystem(
            final FhirContext theContext, final String theSystem) {
        logD(
                "CareConnectValidator asked to fetch Code System: %s%n",
                theSystem);

        if (!isSupported(theSystem)) {
            return null;
        }

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
            return (CodeSystem) cachedResource.get(theSystem);

        }

    }



    private boolean isSupported(String theUrl) {
        // If the terminology server is fully populated, then this should only return Profiles (StructureDefinition)
        return ( theUrl.startsWith("https://fhir.nhs.uk/STU3"));
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
            if (response instanceof StructureDefinition) {
                response = CareConnectProfileFix.fixProfile((StructureDefinition) response);
            }
            if (response != null) {

                cachedResource.put(theUrl, response);
                logT("  Resource added to cache: %s%n", theUrl);
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

            cachedResource.put(theUrl, CareConnectProfileFix.fixProfile((StructureDefinition) response));
            logD("  StructureDefinition now added to the cache: %s%n", theUrl);
        } else {
            logD("  This URL was already loaded: %s%n", theUrl);
        }
        return (StructureDefinition)
                cachedResource.get(theUrl);

    }



  public ValueSet fetchValueSet(FhirContext theContext, String theSystem) {
      logW("CareConnect fetchValueSet: system="+theSystem);
    return null;
  }

  public void flush() {
    cachedResource = null;
  }

  @Override
  public boolean isCodeSystemSupported(FhirContext theContext, String theSystem) {
      if (isBlank(theSystem) || Constants.codeSystemNotNeeded(theSystem)) {
          return false;
      }
    CodeSystem cs = fetchCodeSystem(theContext, theSystem);
    return cs != null && cs.getContent() != CodeSystem.CodeSystemContentMode.NOTPRESENT;
  }


  private CodeValidationResult testIfConceptIsInList(String theCode, List<CodeSystem.ConceptDefinitionComponent> conceptList, boolean theCaseSensitive) {
    logT("CareConnect testIfConceptIsInList: {} code="+ theCode);

    String code = theCode;
    if (!theCaseSensitive) {
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
      if (!theCaseSensitive) {
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
  public CodeValidationResult validateCode(FhirContext theContext, String theCodeSystem, String theCode, String theDisplay, String valueSetUri) {
    CodeSystem cs = fetchCodeSystem(theContext, theCodeSystem);
      logI("CareConnect ValidateCode [System "+theCodeSystem+"] [Code="+theCode+"]");

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
                log.info("Seearching for Questionnaire {}", theUrl);
                if (!results.getEntry().isEmpty()) {
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
                log.info("Seearching for StructureDefinition {}", theUrl);
                if (!results.getEntry().isEmpty()) {
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

        try {
          conn.setRequestMethod("GET");
          try {
            BufferedReader rd
                    = new BufferedReader(
                    new InputStreamReader(
                            conn.getInputStream(),
                            StandardCharsets.UTF_8));
            try {
              int httpCode = conn.getResponseCode();
                int SC_OK = 200;
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
          return parserStu3.parseResource(result.toString());


      } else {
          // Temp fix to get around Questionnaire not being supported by Reference Server 8/March/2019 KGM
          return doCheckCCRI(theUrl);

      }
  }

    @Override
    public StructureDefinition generateSnapshot(StructureDefinition structureDefinition, String s, String s1) {
        return null;
    }

    @Override
    public LookupCodeResult lookupCode(FhirContext fhirContext, String s, String s1) {
        return null;
    }
}
