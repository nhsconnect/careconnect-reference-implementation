package uk.nhs.careconnect.ri.extranet.providers;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenParam;

import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.dstu3.model.Resource;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;


@Component
public class PatientDao implements IPatient {




    @Override
    public String findInsert(FhirContext ctx, Patient patient) {


        return null;
    }

    @Override
    public List<Resource> search(FhirContext ctx, DateRangeParam birthDate, StringParam familyName, StringParam gender, StringParam givenName, TokenParam identifier, StringParam name) {

        List<Resource> resources = new ArrayList<>();


        return resources;
    }
}
