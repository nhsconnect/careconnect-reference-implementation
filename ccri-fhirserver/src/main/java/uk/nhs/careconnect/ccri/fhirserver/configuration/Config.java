package uk.nhs.careconnect.ccri.fhirserver.configuration;


import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.validation.FhirValidator;


import org.hl7.fhir.dstu3.hapi.ctx.DefaultProfileValidationSupport;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.nhs.careconnect.ccri.fhirserver.stu3.provider.DatabaseBackedPagingProvider;
import uk.nhs.careconnect.ccri.fhirserver.stu3.validationSupport.CareConnectProfileValidationSupportSTU3;
import uk.nhs.careconnect.ccri.fhirserver.stu3.validationSupport.UKTerminologyValidationSupportSTU3;
import uk.nhs.careconnect.ccri.fhirserver.support.MessageInstanceValidator;


/**
 * Created by kevinmayfield on 21/07/2017.
 */



@Configuration
public class Config {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(Config.class);


    @Bean()
    public DatabaseBackedPagingProvider databaseBackedPagingProvider() {
                return new DatabaseBackedPagingProvider();
    }



    @Bean(name="fhirValidatorSTU3")
    public FhirValidator fhirValidatorSTU3 (FhirContext stu3ctx,
                                             org.hl7.fhir.dstu3.hapi.validation.ValidationSupportChain validationSupportChain,
                                            DefaultProfileValidationSupport defaultProfileValidationSupport) {
        log.info("Creating FHIR Validator STU3");
        FhirValidator val = stu3ctx.newValidator();

        val.setValidateAgainstStandardSchema(true);

        // todo reactivate once this is fixed https://github.com/nhsconnect/careconnect-reference-implementation/issues/36
        val.setValidateAgainstStandardSchematron(true);

        org.hl7.fhir.dstu3.hapi.validation.FhirInstanceValidator instanceValidator = new org.hl7.fhir.dstu3.hapi.validation.FhirInstanceValidator(defaultProfileValidationSupport);
        val.registerValidatorModule(instanceValidator);

        instanceValidator.setValidationSupport(validationSupportChain);

        return val;
    }

    @Bean("defaultProfileValidationSupport")
    public DefaultProfileValidationSupport defaultProfileValidationSupport() {
        return new DefaultProfileValidationSupport();
    }

    @Bean("validationSupportChainStu3")
    public org.hl7.fhir.dstu3.hapi.validation.ValidationSupportChain ValidationSupportChain
            (FhirContext stu3ctx,
           DefaultProfileValidationSupport defaultProfileValidationSupport
    ) {

        org.hl7.fhir.dstu3.hapi.validation.ValidationSupportChain validationSupportChain = new org.hl7.fhir.dstu3.hapi.validation.ValidationSupportChain();
        validationSupportChain.addValidationSupport(new UKTerminologyValidationSupportSTU3(stu3ctx));
        validationSupportChain.addValidationSupport(defaultProfileValidationSupport);
        // Try SNOMED and Terminology Server first

        validationSupportChain.addValidationSupport(new CareConnectProfileValidationSupportSTU3(stu3ctx));
        return  validationSupportChain;
    }

    @Bean()
    public MessageInstanceValidator messageInstanceValidator(org.hl7.fhir.dstu3.hapi.validation.ValidationSupportChain validationSupportChain) {
        return new MessageInstanceValidator(validationSupportChain);
    }

    /*
    @Bean(name="fhirValidatorR4")
    public FhirValidator fhirValidatorR4 (@Qualifier("r4ctx") FhirContext r4ctx, FhirContext stu3ctx) {

        FhirValidator val = r4ctx.newValidator();

        val.setValidateAgainstStandardSchema(true);

        // todo reactivate once this is fixed https://github.com/nhsconnect/careconnect-reference-implementation/issues/36
        val.setValidateAgainstStandardSchematron(false);

        DefaultProfileValidationSupportStu3AsR4 defaultProfileValidationSupport = new DefaultProfileValidationSupportStu3AsR4();

        FhirInstanceValidator instanceValidator = new FhirInstanceValidator(defaultProfileValidationSupport);
        val.registerValidatorModule(instanceValidator);


        ValidationSupportChain validationSupportChain = new ValidationSupportChain();

        validationSupportChain.addValidationSupport(defaultProfileValidationSupport);
        validationSupportChain.addValidationSupport(new SNOMEDUKDbValidationSupportR4(r4ctx, stu3ctx));

        validationSupportChain.addValidationSupport(new CareConnectProfileDbValidationSupportR4(r4ctx, stu3ctx, HapiProperties.getTerminologyServerSecondary()));

        instanceValidator.setValidationSupport(validationSupportChain);

        return val;
    }
*/

}
