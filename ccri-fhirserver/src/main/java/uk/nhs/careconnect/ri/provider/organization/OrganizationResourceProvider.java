package uk.nhs.careconnect.ri.provider.organization;

import ca.uhn.fhir.model.dstu2.resource.Parameters.Parameter;
import ca.uhn.fhir.model.primitive.DateTimeDt;
import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.Read;
import ca.uhn.fhir.rest.annotation.RequiredParam;
import ca.uhn.fhir.rest.annotation.Search;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.exceptions.InvalidRequestException;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import ca.uhn.fhir.rest.server.exceptions.UnprocessableEntityException;
import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.instance.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.nhs.careconnect.ri.OperationOutcomeFactory;
import uk.nhs.careconnect.ri.SystemCode;
import uk.nhs.careconnect.ri.dao.Organisation.OrganizationSearch;
import uk.nhs.careconnect.ri.model.organization.OrganizationDetails;
import uk.org.hl7.fhir.core.dstu2.CareConnectProfile;
import uk.org.hl7.fhir.core.dstu2.CareConnectSystem;

import java.text.ParseException;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Component
public class OrganizationResourceProvider implements IResourceProvider {



    @Autowired
    private OrganizationSearch organizationSearch;

    @Override
    public Class<Organization> getResourceType() {
        return Organization.class;
    }

    @Read()
    public Organization getOrganizationById(@IdParam IdType organizationId) {

        OrganizationDetails organizationDetails = organizationSearch.findOrganizationDetails(organizationId.getIdPart());

        if (organizationDetails == null) {
            throw OperationOutcomeFactory.buildOperationOutcomeException(
                    new ResourceNotFoundException("No organization details found for organization ID: " + organizationId.getIdPart()),
                    SystemCode.ORGANISATION_NOT_FOUND, OperationOutcome.IssueType.INVALID);
        }

        return convertOrganizaitonDetailsListToOrganizationList(Collections.singletonList(organizationDetails)).get(0);
    }

    @Search
    public List<Organization> getOrganizationsByODSCode(@RequiredParam(name = Organization.SP_IDENTIFIER) TokenParam tokenParam) {
        if (StringUtils.isBlank(tokenParam.getSystem()) || StringUtils.isBlank(tokenParam.getValue())) {
            throw OperationOutcomeFactory.buildOperationOutcomeException(
                    new InvalidRequestException("Missing identifier token"),
                    SystemCode.INVALID_PARAMETER, OperationOutcome.IssueType.INVALID);
        }

        switch (tokenParam.getSystem()) {
            case CareConnectSystem.ODSOrganisationCode:
                return convertOrganizaitonDetailsListToOrganizationList(organizationSearch.findOrganizationDetailsByOrgODSCode(tokenParam.getValue()));

			/*
			Sites are locations not organization KGM
            case CareConnectSystem.ODSSiteCode:
                return convertOrganizaitonDetailsListToOrganizationList(organizationSearch.findOrganizationDetailsBySiteODSCode(tokenParam.getValue()));
			*/
            default:
            	// TODO it isn't an error to not search on ODS code
                throw OperationOutcomeFactory.buildOperationOutcomeException(
                        new InvalidRequestException("Invalid system code"),
                        SystemCode.INVALID_PARAMETER, OperationOutcome.IssueType.INVALID);
        }
    }


    
    private Date getAfter(DateTimeDt target) throws ParseException {
    	Date after = null;
    	
    	Calendar calendar = null;
    	
    	TimeZone timeZone = target.getTimeZone();
    	if(timeZone != null) {
    		calendar = Calendar.getInstance(timeZone);
    	}
    	else {
    		calendar = Calendar.getInstance();
    	}
    	
    	calendar.setTime(target.getValue());
    	
    	switch(target.getPrecision()) {
	    	case MINUTE : calendar.add(Calendar.MINUTE, 1);
	    	break;
	    	case DAY : calendar.add(Calendar.DAY_OF_MONTH, 1);
	    	break;
	    	case MONTH : calendar.add(Calendar.MONTH, 1);
	    	break;	    	
	    	case YEAR : calendar.add(Calendar.YEAR, 1);
	    	break;
	    	default : ; // do nothing
	    	break;    		
    	}
    	
    	after = calendar.getTime();
    	
    	return after;
    }
    
    private Period getTimePeriod(List<Parameter> parameters) {
    	Period timePeriod = null;
    	
    	// first we need a param called timePeriod. If we don't have one then we cannot proceed   
    	// similarly if there's more than one then we cannot proceed.
        timePeriod = parameters.stream()
		        	    .filter(parameter -> "timePeriod".equals(parameter.getName()))
		                .map(Parameter::getValue)
		                .map(Period.class::cast)
		        	    .reduce((a, b) -> {
		        	    	throw OperationOutcomeFactory.buildOperationOutcomeException(
		                            new InvalidRequestException("Multiple timePeriod parameters. Only one is permitted"),
		                            SystemCode.BAD_REQUEST, OperationOutcome.IssueType.INVALID);
		                })
		                .get();   

        return timePeriod;   	
    }
    
    private void validateTimePeriod(Period timePeriod) {
        if(timePeriod != null) {

        	// TODO rough conversion
        	String startString = timePeriod.getStartElement().toString();
        	String endString = timePeriod.getEndElement().toHumanDisplay();
        	
        	if(startString != null && endString != null) {
        		Date start = timePeriod.getStart();
        		Date end = timePeriod.getEnd();
        		
        		if(start != null && end != null) {
        			long period = ChronoUnit.DAYS.between(start.toInstant(), end.toInstant());
        			if(period < 0l || period > 14l) {
        				throw OperationOutcomeFactory.buildOperationOutcomeException(
        						new UnprocessableEntityException("Invalid timePeriods, was " + period + " days between (max is 14)"),
        						SystemCode.INVALID_PARAMETER, OperationOutcome.IssueType.INVALID);
        			}
        		}
        		else {
        			throw OperationOutcomeFactory.buildOperationOutcomeException(
        					new UnprocessableEntityException("Invalid timePeriod one or both of start and end date are not valid dates"),
        					SystemCode.BAD_REQUEST, OperationOutcome.IssueType.INVALID);
        		}    	
        	}
        	else {
    			throw OperationOutcomeFactory.buildOperationOutcomeException(
    					new InvalidRequestException("Invalid timePeriod one or both of start and end date were missing"),
    					SystemCode.BAD_REQUEST, OperationOutcome.IssueType.INVALID);
        	}
        }
        else {
            throw OperationOutcomeFactory.buildOperationOutcomeException(
                    new InvalidRequestException("Missing timePeriod parameter"),
                    SystemCode.BAD_REQUEST, OperationOutcome.IssueType.INVALID);
        }
    }

    private List<Organization> convertOrganizaitonDetailsListToOrganizationList(List<OrganizationDetails> organizationDetails) {
        Map<String, Organization> map = new HashMap<>();

        for (OrganizationDetails organizationDetail : organizationDetails) {
            if (map.containsKey(organizationDetail.getOrgCode())) {
                map.get(organizationDetail.getOrgCode()).addIdentifier(new Identifier().setSystem(CareConnectSystem.ODSSiteCode).setValue(organizationDetail.getSiteCode()));
            } else {
                Organization organization = new Organization()
                        .setName(organizationDetail.getOrgName())
                        .addIdentifier(new Identifier().setSystem(CareConnectSystem.ODSOrganisationCode).setValue(organizationDetail.getOrgCode()))
                        .addIdentifier(new Identifier().setSystem(CareConnectSystem.ODSSiteCode).setValue(organizationDetail.getSiteCode()));

                organization.setId(String.valueOf(organizationDetail.getId()));

                organization.getMeta()
                        .addProfile(CareConnectProfile.Organization_1)
                        .setLastUpdated(organizationDetail.getLastUpdated())
                        .setVersionId(String.valueOf(organizationDetail.getLastUpdated().getTime()));

                map.put(organizationDetail.getOrgCode(), organization);
            }
        }

        return new ArrayList<>(map.values());
    }
}
