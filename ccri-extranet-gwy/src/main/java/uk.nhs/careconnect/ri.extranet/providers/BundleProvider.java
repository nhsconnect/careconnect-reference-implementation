package mayfieldis.careconnect.nosql.providers;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.annotation.Create;
import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.ResourceParam;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.server.IResourceProvider;
import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.OperationOutcome;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import mayfieldis.careconnect.nosql.dao.IBundle;


import javax.servlet.http.HttpServletRequest;


@Component
public class BundleProvider implements IResourceProvider {

    @Autowired
    FhirContext ctx;

    @Autowired
    IBundle bundleDao;

    private static final Logger log = LoggerFactory.getLogger(BundleProvider.class);

    @Override
    public Class<Bundle> getResourceType() {
        return Bundle.class;
    }
    @Create
    public MethodOutcome create(HttpServletRequest httpRequest, @ResourceParam Bundle bundle) {

        OperationOutcome opOutcome = bundleDao.create(ctx,bundle, null,null);

        MethodOutcome method = new MethodOutcome();
        method.setCreated(true);

        method.setOperationOutcome(opOutcome);
        method.setId(opOutcome.getIdElement());

        return method;
    }


}
