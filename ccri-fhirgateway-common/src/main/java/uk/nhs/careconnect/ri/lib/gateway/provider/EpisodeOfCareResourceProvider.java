package uk.nhs.careconnect.ri.lib.gateway.provider;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.OptionalParam;
import ca.uhn.fhir.rest.annotation.Read;
import ca.uhn.fhir.rest.annotation.Search;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.exceptions.InternalErrorException;
import org.apache.camel.CamelContext;
import org.apache.camel.ExchangePattern;
import org.apache.camel.ProducerTemplate;
import org.hl7.fhir.dstu3.model.*;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.nhs.careconnect.ri.lib.server.ProviderResponseLibrary;

import javax.servlet.http.HttpServletRequest;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

@Component
public class EpisodeOfCareResourceProvider implements IResourceProvider {

    @Autowired
    CamelContext context;

    @Autowired
    FhirContext ctx;

    private static final Logger log = LoggerFactory.getLogger(EpisodeOfCareResourceProvider.class);

    @Override
    public Class<EpisodeOfCare> getResourceType() {
        return EpisodeOfCare.class;
    }



    @Read
    public EpisodeOfCare getEpisodeOfCareById(HttpServletRequest theRequest, @IdParam IdType internalId) throws Exception {

        ProducerTemplate template = context.createProducerTemplate();



        EpisodeOfCare episode = null;
        IBaseResource resource = null;
        try {
            InputStream inputStream = (InputStream)  template.sendBody("direct:FHIREpisodeOfCare",
                    ExchangePattern.InOut,theRequest);


            Reader reader = new InputStreamReader(inputStream);
            resource = ctx.newJsonParser().parseResource(reader);
        } catch(Exception ex) {
            log.error("JSON Parse failed " + ex.getMessage());
            throw new InternalErrorException(ex.getMessage());
        }
        if (resource instanceof EpisodeOfCare) {
            episode = (EpisodeOfCare) resource;
        } else {
            ProviderResponseLibrary.createException(ctx,resource);
        }
        

        return episode;
    }

    @Search
    public List<EpisodeOfCare> searchEpisodeOfCare(HttpServletRequest theRequest,
                                                   @OptionalParam(name = EpisodeOfCare.SP_PATIENT) ReferenceParam patient
          //  , @OptionalParam(name = EpisodeOfCare.SP_DATE) DateRangeParam date
            , @OptionalParam(name = EpisodeOfCare.SP_RES_ID) StringParam resid
                                       ) throws Exception {

        List<EpisodeOfCare> results = new ArrayList<EpisodeOfCare>();

        ProducerTemplate template = context.createProducerTemplate();

        InputStream inputStream = (InputStream) template.sendBody("direct:FHIREpisodeOfCare",
                ExchangePattern.InOut,theRequest);

        Bundle bundle = null;

        Reader reader = new InputStreamReader(inputStream);
        IBaseResource resource = null;
        try {
            resource = ctx.newJsonParser().parseResource(reader);
        } catch(Exception ex) {
            log.error("JSON Parse failed " + ex.getMessage());
            throw new InternalErrorException(ex.getMessage());
        }
        if (resource instanceof Bundle) {
            bundle = (Bundle) resource;
            for (Bundle.BundleEntryComponent entry : bundle.getEntry()) {
                EpisodeOfCare episode = (EpisodeOfCare) entry.getResource();
                results.add(episode);
            }

        } else {
            ProviderResponseLibrary.createException(ctx,resource);
        }

        return results;

    }



}
