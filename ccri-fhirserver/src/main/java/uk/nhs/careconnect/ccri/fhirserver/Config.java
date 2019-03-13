package uk.nhs.careconnect.ccri.fhirserver;


import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.validation.FhirValidator;


import org.hl7.fhir.dstu3.hapi.validation.DefaultProfileValidationSupport;
import org.hl7.fhir.dstu3.hapi.validation.FhirInstanceValidator;
import org.hl7.fhir.dstu3.hapi.validation.ValidationSupportChain;
import org.springframework.beans.factory.annotation.Autowire;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.nhs.careconnect.ccri.fhirserver.provider.DatabaseBackedPagingProvider;
import uk.org.hl7.fhir.validation.stu3.CareConnectProfileValidationSupport;
import uk.org.hl7.fhir.validation.stu3.SNOMEDUKMockValidationSupport;


/**
 * Created by kevinmayfield on 21/07/2017.
 */



@Configuration
public class Config {


    @Bean(autowire = Autowire.BY_TYPE)
		public DatabaseBackedPagingProvider databaseBackedPagingProvider() {
        			DatabaseBackedPagingProvider retVal = new DatabaseBackedPagingProvider();
        			return retVal;
        }

    @Autowired()
    FhirContext ctx;

    @Value("${ccri.server.base}")
    private String serverBase;

    @Value("${server.port}")
    private String serverPort;

    @Value("${server.servlet.context-path}")
    private String serverPath;


    @Bean(name="fhirValidator")
    public FhirValidator fhirValidator () {

       // FhirContext r4ctx = FhirContext.forR4();

        FhirValidator val = ctx.newValidator();

        val.setValidateAgainstStandardSchema(true);

        // todo reactivate once this is fixed https://github.com/nhsconnect/careconnect-reference-implementation/issues/36
        val.setValidateAgainstStandardSchematron(false);

        DefaultProfileValidationSupport defaultProfileValidationSupport = new DefaultProfileValidationSupport();

        FhirInstanceValidator instanceValidator = new FhirInstanceValidator(defaultProfileValidationSupport);
        val.registerValidatorModule(instanceValidator);


        ValidationSupportChain validationSupportChain = new ValidationSupportChain();

        validationSupportChain.addValidationSupport(new DefaultProfileValidationSupport());
        validationSupportChain.addValidationSupport(new CareConnectProfileValidationSupport(ctx, "http://localhost:"+serverPort+serverPath+"/STU3"));
        validationSupportChain.addValidationSupport(new SNOMEDUKMockValidationSupport());

        instanceValidator.setValidationSupport(validationSupportChain);



        return val;
    }


}
