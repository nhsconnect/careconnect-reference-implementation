package uk.nhs.careconnect.ri.fhirserver;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.FhirVersionEnum;
import ca.uhn.fhir.rest.server.RestfulServer;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.WebApplicationContext;
import uk.nhs.careconnect.ri.provider.location.LocationResourceProvider;
import uk.nhs.careconnect.ri.provider.organization.OrganizationResourceProvider;
import uk.nhs.careconnect.ri.provider.PatientResourceProvider;
import uk.nhs.careconnect.ri.provider.PractitionerResourceProvider;

import javax.servlet.ServletException;
import java.util.Arrays;


public class HAPIRestfulConfig extends RestfulServer {

	private static final long serialVersionUID = 1L;

	private WebApplicationContext myAppCtx;

	@SuppressWarnings("unchecked")
	@Override
	protected void initialize() throws ServletException {
		super.initialize();

		/* 
		 * We want to support FHIR DSTU2 format. This means that the server
		 * will use the DSTU2 bundle format and other DSTU2 encoding changes.
		 *
		 * If you want to use DSTU1 instead, change the following line, and change the 2 occurrences of dstu2 in web.xml to dstu1
		 */
		FhirVersionEnum fhirVersion = FhirVersionEnum.DSTU2_HL7ORG;
		setFhirContext(new FhirContext(fhirVersion));

		// Get the spring context from the web container (it's declared in web.xml)
		myAppCtx = ContextLoaderListener.getCurrentWebApplicationContext();

		/* 
		 * The BaseJavaConfigDstu2.java class is a spring configuration
		 * file which is automatically generated as a part of hapi-fhir-jpaserver-base and
		 * contains bean definitions for a resource provider for each resource type
		 */
		setResourceProviders(Arrays.asList(
				myAppCtx.getBean(PatientResourceProvider.class),
				myAppCtx.getBean(OrganizationResourceProvider.class),
				myAppCtx.getBean(PractitionerResourceProvider.class),
				myAppCtx.getBean(LocationResourceProvider.class)
			//	myAppCtx.getBean(MedicationResourceProvider.class),
			//	myAppCtx.getBean(MedicationOrderResourceProvider.class),
			//	myAppCtx.getBean(MedicationDispenseResourceProvider.class),
			//	myAppCtx.getBean(MedicationAdministrationResourceProvider.class),

			//	myAppCtx.getBean(AppointmentResourceProvider.class),
			//	myAppCtx.getBean(ScheduleResourceProvider.class),
			//	myAppCtx.getBean(SlotResourceProvider.class),
			//	myAppCtx.getBean(OrderResourceProvider.class),

		));


	}

}
