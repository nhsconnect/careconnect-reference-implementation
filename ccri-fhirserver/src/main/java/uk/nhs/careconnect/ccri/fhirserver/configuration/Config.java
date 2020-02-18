package uk.nhs.careconnect.ccri.fhirserver.configuration;


import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.server.exceptions.InternalErrorException;
import ca.uhn.fhir.validation.FhirValidator;


import org.hl7.fhir.dstu3.hapi.ctx.DefaultProfileValidationSupport;
import org.hl7.fhir.r4.hapi.validation.FhirInstanceValidator;
import org.hl7.fhir.r4.hapi.validation.ValidationSupportChain;
import org.hl7.fhir.utilities.cache.NpmPackage;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.nhs.careconnect.ccri.fhirserver.HapiProperties;
import uk.nhs.careconnect.ccri.fhirserver.r4.validationsupport.IGValidationSupportR4;
import uk.nhs.careconnect.ccri.fhirserver.r4.validationsupport.SNOMEDUKDbValidationSupportR4;
import uk.nhs.careconnect.ccri.fhirserver.stu3.provider.DatabaseBackedPagingProvider;
import uk.nhs.careconnect.ccri.fhirserver.stu3.validationSupport.IGValidationSupport;
import uk.nhs.careconnect.ccri.fhirserver.stu3.validationSupport.NHSDigitalProfileValidationSupportSTU3;
import uk.nhs.careconnect.ccri.fhirserver.stu3.validationSupport.TerminologyServerValidationSupport;
import uk.nhs.careconnect.ccri.fhirserver.support.MessageInstanceValidator;
import uk.nhs.careconnect.ccri.fhirserver.support.PackageManager;
import uk.org.hl7.fhir.validation.r4.DefaultProfileValidationSupportStu3AsR4;


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

    @Bean("validationPackageSTU3")
    public NpmPackage validationPackageSTU3() {
        NpmPackage validationIgPackage = null;
        if (!HapiProperties.getValidationIgPackage().isEmpty()) {
            try {
                validationIgPackage = PackageManager.getPackage(HapiProperties.getValidationIgPackage(), HapiProperties.getValidationIgVersion(), HapiProperties.getValidationIgUrl());
                if (validationIgPackage== null)  throw new InternalErrorException("Unable to load API Validation package");
            }
            catch (Exception ex) {
                log.error(ex.getMessage());
                throw new InternalErrorException(ex.getMessage());
            }
        }
        return validationIgPackage;
    }

    @Bean("validationUKCoreIg")
    public NpmPackage validationUKCoreIg() {
        NpmPackage validationIgPackage = null;

            try {
                validationIgPackage = PackageManager.getPackage("uk.testcore.r4", "4.0.0","https://project-wildfyre.github.io/uk-testcore-r4/package.tgz");
                if (validationIgPackage== null)  throw new InternalErrorException("Unable to load API Validation package");
            }
            catch (Exception ex) {
                log.error(ex.getMessage());
                throw new InternalErrorException(ex.getMessage());
            }

        return validationIgPackage;
    }

    @Bean("validationSupportChainStu3")
    public org.hl7.fhir.dstu3.hapi.validation.ValidationSupportChain ValidationSupportChain
            (FhirContext stu3ctx,
           DefaultProfileValidationSupport defaultProfileValidationSupport,
             @Qualifier("validationPackageSTU3")  NpmPackage validationIgPackage
    ) throws Exception {

        org.hl7.fhir.dstu3.hapi.validation.ValidationSupportChain validationSupportChain = new org.hl7.fhir.dstu3.hapi.validation.ValidationSupportChain();
        if (HapiProperties.getValidateTerminologyEnabled() && !HapiProperties.getTerminologyServer().isEmpty()) {
            validationSupportChain.addValidationSupport(new TerminologyServerValidationSupport(stu3ctx, HapiProperties.getTerminologyServer()));
        }

        validationSupportChain.addValidationSupport(defaultProfileValidationSupport);

        if (validationIgPackage != null) {
            validationSupportChain.addValidationSupport(new IGValidationSupport(stu3ctx, validationIgPackage));
        }
        validationSupportChain.addValidationSupport(new NHSDigitalProfileValidationSupportSTU3(stu3ctx));

        return  validationSupportChain;
    }

    @Bean()
    public MessageInstanceValidator messageInstanceValidator(org.hl7.fhir.dstu3.hapi.validation.ValidationSupportChain validationSupportChain) {
        return new MessageInstanceValidator(validationSupportChain);
    }


    @Bean(name="fhirValidatorR4")
    public FhirValidator fhirValidatorR4 (@Qualifier("r4ctx") FhirContext r4ctx, FhirContext stu3ctx,
                                          @Qualifier("validationUKCoreIg") NpmPackage validationUKCoreIg) throws Exception {

        FhirValidator val = r4ctx.newValidator();

        val.setValidateAgainstStandardSchema(true);

        // todo reactivate once this is fixed https://github.com/nhsconnect/careconnect-reference-implementation/issues/36
        val.setValidateAgainstStandardSchematron(false);

        org.hl7.fhir.r4.hapi.ctx.DefaultProfileValidationSupport defaultProfileValidationSupport = new org.hl7.fhir.r4.hapi.ctx.DefaultProfileValidationSupport();

        FhirInstanceValidator instanceValidator = new FhirInstanceValidator();
        val.registerValidatorModule(instanceValidator);


        ValidationSupportChain validationSupportChain = new ValidationSupportChain();

        validationSupportChain.addValidationSupport(new SNOMEDUKDbValidationSupportR4(r4ctx, stu3ctx));

        validationSupportChain.addValidationSupport(new IGValidationSupportR4(r4ctx, validationUKCoreIg));
        validationSupportChain.addValidationSupport(defaultProfileValidationSupport);

        instanceValidator.setValidationSupport(validationSupportChain);

        return val;
    }


}
