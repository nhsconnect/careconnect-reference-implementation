package uk.nhs.careconnect.ccri.fhirserver;


import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.validation.FhirValidator;

import org.hl7.fhir.dstu3.hapi.validation.DefaultProfileValidationSupport;
import org.hl7.fhir.r4.hapi.validation.FhirInstanceValidator;
import org.hl7.fhir.r4.hapi.validation.ValidationSupportChain;
import org.springframework.beans.factory.annotation.Autowire;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.nhs.careconnect.ccri.fhirserver.stu3.provider.DatabaseBackedPagingProvider;
import uk.nhs.careconnect.ccri.fhirserver.validationSupport.CareConnectProfileDbValidationSupportR4;
import uk.nhs.careconnect.ccri.fhirserver.stu3.validationSupport.CareConnectProfileDbValidationSupportSTU3;
import uk.nhs.careconnect.ccri.fhirserver.validationSupport.SNOMEDUKDbValidationSupportR4;
import uk.nhs.careconnect.ccri.fhirserver.stu3.validationSupport.SNOMEDUKDbValidationSupportSTU3;
import uk.org.hl7.fhir.validation.r4.DefaultProfileValidationSupportStu3AsR4;


/**
 * Created by kevinmayfield on 21/07/2017.
 */



@Configuration
public class Config {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(Config.class);


    @Bean(autowire = Autowire.BY_TYPE)
		public DatabaseBackedPagingProvider databaseBackedPagingProvider() {
        			DatabaseBackedPagingProvider retVal = new DatabaseBackedPagingProvider();
        			return retVal;
        }



    @Value("${server.port}")
    private String serverPort;

    @Value("${server.servlet.context-path}")
    private String serverPath;


    @Qualifier("r4ctx")
    @Autowired()
    FhirContext r4ctx;

    @Autowired()
    FhirContext stu3ctx;


    @Bean(name="fhirValidatorSTU3")
    public FhirValidator fhirValidatorSTU3 () {

        log.info("Creating FHIR Validator STU3");
        FhirValidator val = stu3ctx.newValidator();

        val.setValidateAgainstStandardSchema(true);

        // todo reactivate once this is fixed https://github.com/nhsconnect/careconnect-reference-implementation/issues/36
        val.setValidateAgainstStandardSchematron(false);

        DefaultProfileValidationSupport defaultProfileValidationSupport = new DefaultProfileValidationSupport();

        org.hl7.fhir.dstu3.hapi.validation.FhirInstanceValidator instanceValidator = new org.hl7.fhir.dstu3.hapi.validation.FhirInstanceValidator(defaultProfileValidationSupport);
        val.registerValidatorModule(instanceValidator);

        org.hl7.fhir.dstu3.hapi.validation.ValidationSupportChain validationSupportChain = new org.hl7.fhir.dstu3.hapi.validation.ValidationSupportChain();

        validationSupportChain.addValidationSupport(defaultProfileValidationSupport);
        validationSupportChain.addValidationSupport(new CareConnectProfileDbValidationSupportSTU3(stu3ctx));
        validationSupportChain.addValidationSupport(new SNOMEDUKDbValidationSupportSTU3(stu3ctx));

        instanceValidator.setValidationSupport(validationSupportChain);


        return val;
    }

    @Bean(name="fhirValidatorR4")
    public FhirValidator fhirValidatorR4 () {



        FhirValidator val = r4ctx.newValidator();

        val.setValidateAgainstStandardSchema(true);

        // todo reactivate once this is fixed https://github.com/nhsconnect/careconnect-reference-implementation/issues/36
        val.setValidateAgainstStandardSchematron(false);

        DefaultProfileValidationSupportStu3AsR4 defaultProfileValidationSupport = new DefaultProfileValidationSupportStu3AsR4();

        FhirInstanceValidator instanceValidator = new FhirInstanceValidator(defaultProfileValidationSupport);
        val.registerValidatorModule(instanceValidator);


        ValidationSupportChain validationSupportChain = new ValidationSupportChain();

        validationSupportChain.addValidationSupport(defaultProfileValidationSupport);
        validationSupportChain.addValidationSupport(new CareConnectProfileDbValidationSupportR4(r4ctx, stu3ctx,HapiProperties.getTerminologyServerSecondary()));
        validationSupportChain.addValidationSupport(new SNOMEDUKDbValidationSupportR4(r4ctx, stu3ctx));

        instanceValidator.setValidationSupport(validationSupportChain);


        return val;
    }


}
