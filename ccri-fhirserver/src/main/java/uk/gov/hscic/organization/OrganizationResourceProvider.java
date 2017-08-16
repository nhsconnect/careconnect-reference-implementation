package uk.gov.hscic.organization;

import ca.uhn.fhir.model.dstu2.composite.IdentifierDt;
import ca.uhn.fhir.model.dstu2.composite.PeriodDt;
import ca.uhn.fhir.model.dstu2.resource.Bundle;
import ca.uhn.fhir.model.dstu2.resource.OperationOutcome;
import ca.uhn.fhir.model.dstu2.resource.Organization;
import ca.uhn.fhir.model.dstu2.resource.Parameters;
import ca.uhn.fhir.model.dstu2.resource.Parameters.Parameter;
import ca.uhn.fhir.model.dstu2.valueset.BundleTypeEnum;
import ca.uhn.fhir.model.dstu2.valueset.IssueTypeEnum;
import ca.uhn.fhir.model.primitive.DateTimeDt;
import ca.uhn.fhir.model.primitive.IdDt;
import ca.uhn.fhir.rest.annotation.*;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.exceptions.InvalidRequestException;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import ca.uhn.fhir.rest.server.exceptions.UnprocessableEntityException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hscic.OperationOutcomeFactory;
import uk.gov.hscic.SystemCode;
import uk.gov.hscic.model.organization.OrganizationDetails;
import uk.org.hl7.fhir.core.dstu2.CareConnectProfile;
import uk.org.hl7.fhir.core.dstu2.CareConnectSystem;

import java.text.ParseException;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Component
public class OrganizationResourceProvider implements IResourceProvider {

    @Autowired
    private GetScheduleOperation getScheduleOperation;

    @Autowired
    private OrganizationSearch organizationSearch;

    @Override
    public Class<Organization> getResourceType() {
        return Organization.class;
    }

    @Read()
    public Organization getOrganizationById(@IdParam IdDt organizationId) {

        OrganizationDetails organizationDetails = organizationSearch.findOrganizationDetails(organizationId.getIdPart());

        if (organizationDetails == null) {
            throw OperationOutcomeFactory.buildOperationOutcomeException(
                    new ResourceNotFoundException("No organization details found for organization ID: " + organizationId.getIdPart()),
                    SystemCode.ORGANISATION_NOT_FOUND, IssueTypeEnum.INVALID_CONTENT);
        }

        return convertOrganizaitonDetailsListToOrganizationList(Collections.singletonList(organizationDetails)).get(0);
    }

    @Search
    public List<Organization> getOrganizationsByODSCode(@RequiredParam(name = Organization.SP_IDENTIFIER) TokenParam tokenParam) {
        if (StringUtils.isBlank(tokenParam.getSystem()) || StringUtils.isBlank(tokenParam.getValue())) {
            throw OperationOutcomeFactory.buildOperationOutcomeException(
                    new InvalidRequestException("Missing identifier token"),
                    SystemCode.INVALID_PARAMETER, IssueTypeEnum.INVALID_CONTENT);
        }

        switch (tokenParam.getSystem()) {
            case CareConnectSystem.ODSOrganisationCode:
                return convertOrganizaitonDetailsListToOrganizationList(organizationSearch.findOrganizationDetailsByOrgODSCode(tokenParam.getValue()));

            case CareConnectSystem.ODSSiteCode:
                return convertOrganizaitonDetailsListToOrganizationList(organizationSearch.findOrganizationDetailsBySiteODSCode(tokenParam.getValue()));

            default:
                throw OperationOutcomeFactory.buildOperationOutcomeException(
                        new InvalidRequestException("Invalid system code"),
                        SystemCode.INVALID_PARAMETER, IssueTypeEnum.INVALID_CONTENT);
        }
    }

    @Operation(name = "$gpc.getschedule")
    public Bundle getSchedule(@IdParam IdDt organizationId, @ResourceParam Parameters params) {
        Bundle bundle = null;
    	
        List<Parameter> parameter = params.getParameter();

        // there should only be 1 parameter
		if(parameter.size() == 1) {        	
        	PeriodDt timePeriod = getTimePeriod(parameter);
        	validateTimePeriod(timePeriod);
        	
        	bundle = new Bundle().setType(BundleTypeEnum.SEARCH_RESULTS);
        	
        	try {
        		getScheduleOperation.populateBundle(bundle, 
        				new OperationOutcome(), 
        				organizationId, 
        				timePeriod.getStart(),
        				getAfter(timePeriod.getEndElement()));
        	} catch (ParseException e) {
        		// TODO Auto-generated catch block
        		e.printStackTrace();
        	}
        }
        else {
            throw OperationOutcomeFactory.buildOperationOutcomeException(
                    new InvalidRequestException("Invalid number of parameters. Only one parameter named timePeriod is permitted."),
                    SystemCode.BAD_REQUEST, IssueTypeEnum.INVALID_CONTENT);
        }
        

        return bundle;
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
    
    private PeriodDt getTimePeriod(List<Parameter> parameters) {
    	PeriodDt timePeriod = null;
    	
    	// first we need a param called timePeriod. If we don't have one then we cannot proceed   
    	// similarly if there's more than one then we cannot proceed.
        timePeriod = parameters.stream()
		        	    .filter(parameter -> "timePeriod".equals(parameter.getName()))
		                .map(Parameter::getValue)
		                .map(PeriodDt.class::cast)
		        	    .reduce((a, b) -> {
		        	    	throw OperationOutcomeFactory.buildOperationOutcomeException(
		                            new InvalidRequestException("Multiple timePeriod parameters. Only one is permitted"),
		                            SystemCode.BAD_REQUEST, IssueTypeEnum.INVALID_CONTENT);
		                })
		                .get();   

        return timePeriod;   	
    }
    
    private void validateTimePeriod(PeriodDt timePeriod) {
        if(timePeriod != null) {
        	DateTimeDt startElement = timePeriod.getStartElement();
        	DateTimeDt endElement = timePeriod.getEndElement();
        	
        	String startString = startElement.getValueAsString();
        	String endString = endElement.getValueAsString();
        	
        	if(startString != null && endString != null) {
        		Date start = timePeriod.getStart();
        		Date end = timePeriod.getEnd();
        		
        		if(start != null && end != null) {
        			long period = ChronoUnit.DAYS.between(start.toInstant(), end.toInstant());
        			if(period < 0l || period > 14l) {
        				throw OperationOutcomeFactory.buildOperationOutcomeException(
        						new UnprocessableEntityException("Invalid timePeriods, was " + period + " days between (max is 14)"),
        						SystemCode.INVALID_PARAMETER, IssueTypeEnum.INVALID_CONTENT);	
        			}
        		}
        		else {
        			throw OperationOutcomeFactory.buildOperationOutcomeException(
        					new UnprocessableEntityException("Invalid timePeriod one or both of start and end date are not valid dates"),
        					SystemCode.BAD_REQUEST, IssueTypeEnum.INVALID_CONTENT);
        		}    	
        	}
        	else {
    			throw OperationOutcomeFactory.buildOperationOutcomeException(
    					new InvalidRequestException("Invalid timePeriod one or both of start and end date were missing"),
    					SystemCode.BAD_REQUEST, IssueTypeEnum.INVALID_CONTENT);
        	}
        }
        else {
            throw OperationOutcomeFactory.buildOperationOutcomeException(
                    new InvalidRequestException("Missing timePeriod parameter"),
                    SystemCode.BAD_REQUEST, IssueTypeEnum.INVALID_CONTENT);
        }
    }

    private List<Organization> convertOrganizaitonDetailsListToOrganizationList(List<OrganizationDetails> organizationDetails) {
        Map<String, Organization> map = new HashMap<>();

        for (OrganizationDetails organizationDetail : organizationDetails) {
            if (map.containsKey(organizationDetail.getOrgCode())) {
                map.get(organizationDetail.getOrgCode()).addIdentifier(new IdentifierDt(CareConnectSystem.ODSSiteCode, organizationDetail.getSiteCode()));
            } else {
                Organization organization = new Organization()
                        .setName(organizationDetail.getOrgName())
                        .addIdentifier(new IdentifierDt(CareConnectSystem.ODSOrganisationCode, organizationDetail.getOrgCode()))
                        .addIdentifier(new IdentifierDt(CareConnectSystem.ODSSiteCode, organizationDetail.getSiteCode()));

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
