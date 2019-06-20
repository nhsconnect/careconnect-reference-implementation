package uk.nhs.careconnect.ccri.fhirserver.stu3.provider;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import org.hl7.fhir.dstu3.model.ValueSet.ConceptSetComponent;
import org.hl7.fhir.dstu3.model.ValueSet.ValueSetExpansionComponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ValidationSupportProvider  {

    // KGM 21st May 2018 Incorporated Tim Coates code to use UK FHIR Reference Servers.


    @Autowired
    private FhirContext ctx = null;

    IGenericClient client;

    private IParser parser;
    /**
     * Milliseconds we'll wait to read data over http.
     */
    ValidationSupportProvider(FhirContext xstu3) {

    }

  public ValueSetExpansionComponent expandValueSet(ConceptSetComponent theInclude) {

    ValueSetExpansionComponent retVal = new ValueSetExpansionComponent();


    return retVal;
  }


}
