package mayfieldis.careconnect.nosql.dao;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenParam;
import mayfieldis.careconnect.nosql.dao.transform.PatientEntityToFHIRPatient;

import mayfieldis.careconnect.nosql.entities.Name;
import mayfieldis.careconnect.nosql.entities.PatientEntity;
import org.bson.types.ObjectId;
import org.hl7.fhir.dstu3.model.HumanName;
import org.hl7.fhir.dstu3.model.Identifier;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.dstu3.model.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;

import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;

@Transactional
@Repository
public class PatientDao implements IPatient {

    @Autowired
    MongoOperations mongo;

    @Autowired
    private PatientEntityToFHIRPatient
            patientEntityToFHIRPatient;

    @Override
    public ObjectId findInsert(FhirContext ctx, Patient patient) {

        // TODO This is a basic patient find and would need extending for a real implementtion.

        for (Identifier identifier : patient.getIdentifier()) {
            identifier.setValue(identifier.getValue().replace(" ",""));
            Query qry = Query.query(Criteria.where("identifiers.system").is(identifier.getSystem()).and("identifiers.value").is(identifier.getValue()));

            PatientEntity patientE = mongo.findOne(qry, PatientEntity.class);
            // Patient found, quit and do not add new record.
            if (patientE!=null) return patientE.getId();
        }

        PatientEntity patientE = new PatientEntity();

        for (Identifier identifier : patient.getIdentifier()) {
            mayfieldis.careconnect.nosql.entities.Identifier identifierE = new mayfieldis.careconnect.nosql.entities.Identifier();
            identifierE.setSystem(identifier.getSystem());
            identifierE.setValue(identifier.getValue());

            patientE.getIdentifiers().add(identifierE);
        }
        for (HumanName name : patient.getName()) {
            Name nameE = new Name();
            nameE.setFamilyName(name.getFamily());
            nameE.setGivenName(name.getGivenAsSingleString());

            patientE.getNames().add(nameE);
        }
        if (patient.hasBirthDate()) {
            patientE.setDateOfBirth(patient.getBirthDate());
        }
        mongo.save(patientE);


        ObjectId bundleId = patientE.getId();

        return bundleId;
    }

    @Override
    public List<Resource> search(FhirContext ctx, DateRangeParam birthDate, StringParam familyName, StringParam gender, StringParam givenName, TokenParam identifier, StringParam name) {

        List<Resource> resources = new ArrayList<>();

        Criteria criteria = null;

        // http://127.0.0.1:8181/STU3/Patient?identifier=https://fhir.leedsth.nhs.uk/Id/pas-number|LOCAL1098
        if (identifier != null) {
            if (criteria ==null) {
                criteria = Criteria.where("identifiers.system").is(identifier.getSystem()).and("identifiers.value").is(identifier.getValue());
            } else {
                criteria.and("identifiers.system").is(identifier.getSystem()).and("identifiers.value").is(identifier.getValue());
            }
        }
        if (familyName!=null) {
            if (criteria ==null) {
                criteria = Criteria.where("names.familyName").regex(familyName.getValue());
            } else {
                criteria.and("names.familyName").regex(familyName.getValue());
            }
        }
        if (givenName!=null) {
            if (criteria ==null) {
                criteria = Criteria.where("names.givenName").regex(givenName.getValue());
            } else {
                criteria.and("names.givenName").regex(givenName.getValue());
            }
        }
        if (name!=null) {
            if (criteria ==null) {
                criteria = new Criteria().orOperator(Criteria.where("names.familyName").regex(name.getValue()),Criteria.where("names.givenName").regex(name.getValue()));
            } else {
                criteria.orOperator(Criteria.where("names.familyName").regex(name.getValue()),Criteria.where("names.givenName").regex(name.getValue()));
            }
        }


        if (criteria != null) {
            Query qry = Query.query(criteria);

            List<PatientEntity> patientResults = mongo.find(qry, PatientEntity.class);

            for (PatientEntity patientEntity : patientResults) {
                resources.add(patientEntityToFHIRPatient.transform(patientEntity));
            }
        }

        return resources;
    }
}
