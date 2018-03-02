package mayfieldis.careconnect.nosql.providers;

import ca.uhn.fhir.context.FhirContext;

import ca.uhn.fhir.rest.annotation.OptionalParam;
import ca.uhn.fhir.rest.annotation.Search;
import ca.uhn.fhir.rest.param.DateParam;
import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.rest.server.IResourceProvider;

import mayfieldis.careconnect.nosql.dao.IPatient;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.dstu3.model.Resource;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

import java.util.ArrayList;
import java.util.List;

@Component
public class PatientProvider implements IResourceProvider {

    @Autowired
    FhirContext ctx;

    @Autowired
    IPatient patientDao;

    @Override
    public Class<? extends IBaseResource> getResourceType() {
        return Patient.class;
    }

    @Search
    public List<Resource> searchPatient(HttpServletRequest request,

                                        @OptionalParam(name= Patient.SP_BIRTHDATE) DateRangeParam birthDate,

                                        @OptionalParam(name = Patient.SP_FAMILY) StringParam familyName,
                                        @OptionalParam(name= Patient.SP_GENDER) StringParam gender ,
                                        @OptionalParam(name= Patient.SP_GIVEN) StringParam givenName ,
                                        @OptionalParam(name = Patient.SP_IDENTIFIER) TokenParam identifier,
                                        @OptionalParam(name= Patient.SP_NAME) StringParam name
                                    //  MAY  @OptionalParam(name= Patient.SP_PHONE) StringParam phone
            , @OptionalParam(name = Patient.SP_RES_ID) TokenParam resid

    ) {

        List<Resource> results = patientDao.search(ctx,birthDate,familyName,gender,givenName,identifier,name);


        return results;

    }

}
