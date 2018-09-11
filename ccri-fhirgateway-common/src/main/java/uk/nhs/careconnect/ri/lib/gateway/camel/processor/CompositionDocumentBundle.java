package uk.nhs.careconnect.ri.lib.gateway.camel.processor;

import ca.uhn.fhir.context.FhirContext;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.processor.aggregate.AggregationStrategy;
import org.hl7.fhir.dstu3.model.*;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.nhs.careconnect.ri.lib.server.OperationOutcomeFactory;

import java.util.Map;

public class CompositionDocumentBundle implements AggregationStrategy {


    /*

    This takes the original documentBundle, send the FHIR Document to EDMS
    and then takes the response, converts to a DocumentReference and updates
     the DocumentIndex server (ccri - EPR)

     */
    public CompositionDocumentBundle(FhirContext ctx, String hapiBase) {
        this.ctx = ctx;
        this.hapiBase = hapiBase;
    }

    CamelContext context;

    FhirContext ctx;
    private Map<String,Resource> resourceMap;

    private Bundle bundle;

    private Patient patient;
    private Composition composition;

    private static final Logger log = LoggerFactory.getLogger(CompositionDocumentBundle.class);

    private String hapiBase;

    @Override
    public Exchange aggregate(Exchange originalExchange, Exchange edmsExchange) {

        this.context = originalExchange.getContext();

        if (edmsExchange.getIn().getHeader(Exchange.HTTP_RESPONSE_CODE) != null) {


            if (edmsExchange.getIn().getHeader(Exchange.HTTP_RESPONSE_CODE).toString().equals("201")) {
                IBaseResource resource = ctx.newXmlParser().parseResource((String) originalExchange.getIn().getBody());

                if (resource instanceof Bundle) {
                    bundle = (Bundle) resource;

                    for (Bundle.BundleEntryComponent entry : bundle.getEntry()) {

                        if (entry.getResource() instanceof Patient) {
                            patient = (Patient) entry.getResource();
                        }
                        if (entry.getResource() instanceof Composition) {
                            composition = (Composition) entry.getResource();
                        }

                    }
                    try {
                        BundleCore bundleCore = new BundleCore(ctx, context, bundle);
                        if (patient != null) {
                            if (patient.getId() != null) {
                                bundleCore.searchAddResource(patient.getId());
                            } else {
                                patient.setId(java.util.UUID.randomUUID().toString());
                                bundleCore.searchAddResource(patient.getId());
                            }

/*
                Need to build DocumentReference add it to bundle and then process it.
                Use location from edms post
*/
                            if (composition != null) {
                                DocumentReference documentReference = new DocumentReference();
                                documentReference.setId(java.util.UUID.randomUUID().toString());
                                if (bundle.getIdentifier() != null) {
                                    documentReference.addIdentifier().setSystem(bundle.getIdentifier().getSystem())
                                            .setValue(bundle.getIdentifier().getValue());
                                }
                                // This should be resolved
                                documentReference.setSubject(new Reference(patient.getId()));
                                if (composition.hasType()) {
                                    documentReference.setType(composition.getType());
                                }
                                if (composition.hasClass_()) {
                                    documentReference.setClass_(composition.getClass_());
                                }
                                if (composition.hasStatus()) {
                                    // TODO convert from Composition status
                                    documentReference.setStatus(Enumerations.DocumentReferenceStatus.CURRENT);
                                }
                                if (composition.hasAuthor()) {
                                    for (Reference reference : composition.getAuthor()) {
                                        documentReference.addAuthor(reference);
                                    }
                                }
                                if (composition.hasCustodian()) {
                                    documentReference.setCustodian(composition.getCustodian());
                                }
                                for (Extension extension : composition.getExtension()) {
                                    if (extension.getUrl().equals("https://fhir.nhs.uk/STU3/StructureDefinition/Extension-ITK-CareSettingType-1")) {
                                        documentReference.getContext().setPracticeSetting((CodeableConcept) extension.getValue());
                                    }
                                }
                                if (composition.hasEncounter()) {
                                    documentReference.getContext().setEncounter(composition.getEncounter());
                                }
                                if (composition.hasDate()) {
                                    documentReference.setCreated(composition.getDate());
                                }
                                DocumentReference.DocumentReferenceContentComponent content = documentReference.addContent();
                                //log.info("### Content-Location = " + edmsExchange.getIn().getHeader("Content-Location"));
                                //log.info("### Location = " + edmsExchange.getIn().getHeader("Location"));
                                if (edmsExchange.getIn().getHeader("Location") != null) {

                                    String[] path = edmsExchange.getIn().getHeader("Location").toString().split("/");
                                    String resourceId = path[path.length - 1];
                                    log.trace("Binary resource Id = " + resourceId);
                                    content.getAttachment().setContentType("application/fhir+xml").setUrl(hapiBase + "/Binary/" + resourceId);
                                } else {
                                    if (edmsExchange.getIn().getHeader("Content-Location") != null) {

                                        String[] path = edmsExchange.getIn().getHeader("Content-Location").toString().split("/");
                                        String resourceId = path[path.length - 1];
                                        log.trace("Binary resource Id = " + resourceId);
                                        content.getAttachment().setContentType("application/fhir+xml").setUrl(hapiBase + "/Binary/" + resourceId);
                                    }
                                }

                                // Add the new DocumentReference to the bundle
                                bundleCore.getBundle().addEntry().setResource(documentReference);
                                bundleCore.searchAddResource(documentReference.getId());

                                log.trace("Document Reference Location " + documentReference.getId());
                                edmsExchange.getIn().setHeader("Location", documentReference.getId());
                                edmsExchange.getIn().setHeader("Content-Location", documentReference.getId());
                            }

                        }
                        edmsExchange.getIn().setBody(ctx.newXmlParser().encodeResourceToString(bundleCore.getUpdatedBundle()));

                        //log.info(ctx.newXmlParser().encodeResourceToString(bundleCore.getBundle()));
                    } catch (Exception ex) {
                        // A number of the HAPI related function will return exceptions.
                        // Convert to operational outcomes
                        OperationOutcome operationOutcome = new OperationOutcome();
                        OperationOutcome.IssueType issueType = OperationOutcomeFactory.getIssueType(ex);

                        operationOutcome.addIssue()
                                .setSeverity(OperationOutcome.IssueSeverity.ERROR)
                                .setCode(issueType)
                                .setDiagnostics(ex.getMessage());
                        edmsExchange.getIn().setBody(ctx.newXmlParser().encodeResourceToString(operationOutcome));
                    }

                }
            }
        }

        return edmsExchange;
    }
}
