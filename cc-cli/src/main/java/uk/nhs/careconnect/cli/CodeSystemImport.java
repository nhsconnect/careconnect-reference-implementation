package uk.nhs.careconnect.cli;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.FhirVersionEnum;
import ca.uhn.fhir.validation.FhirValidator;
import ca.uhn.fhir.validation.ResultSeverityEnum;
import ca.uhn.fhir.validation.SingleValidationMessage;
import ca.uhn.fhir.validation.ValidationResult;
import org.apache.commons.cli.*;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.text.WordUtils;
import org.apache.tomcat.jdbc.pool.DataSource;
import org.flywaydb.core.Flyway;
import org.fusesource.jansi.Ansi.Color;
import org.hl7.fhir.dstu3.hapi.validation.DefaultProfileValidationSupport;
import org.hl7.fhir.dstu3.hapi.validation.FhirInstanceValidator;
import org.hl7.fhir.dstu3.hapi.validation.ValidationSupportChain;
import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Resource;
import org.hl7.fhir.dstu3.model.StructureDefinition;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import uk.org.hl7.fhir.core.Dstu2.CareConnectSystem;
import uk.org.hl7.fhir.validation.stu3.CareConnectProfileValidationSupport;
import uk.org.hl7.fhir.validation.stu3.SNOMEDUKMockValidationSupport;

import java.io.FileInputStream;
import java.io.InputStreamReader;

import static org.apache.commons.lang3.StringUtils.*;
import static org.fusesource.jansi.Ansi.ansi;

@Component
public class CodeSystemImport extends BaseCommand {

	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CodeSystemImport.class);

	@Override
	public String getCommandDescription() {
		return "Docker import CodeSystem support";
	}

	@Override
	public String getCommandName() {
		return "codesystem";
	}

	@Autowired
	Flyway flyway;

	//@Value("${flyway.locations:filesystem:/mysql_exp}")
	private String flywayLocations = "db/dataload";

	@Value("${datasource.vendor:h2}")
	private String vendor = "postgresql";

	@Value("${datasource.host:mem}")
	private String host= "//localhost";

	@Value("${datasource.path:db1}")
	private String path = "5432/careconnect";

	@Value("${datasource.username:}")
	private String username ="fhirjpa";

	@Value("${datasource.password:}")
	private String password = "fhirjpa";

	@Value("${datasource.showSql:false}")
	private boolean showSql = false;

	@Value("${datasource.showDdl:true}")
	private boolean showDdl = true;



	@Value("${datasource.driver:org.apache.derby.jdbc.EmbeddedDriver}")
	private String driverName = "org.postgresql.Driver";


	@Override
	public Options getOptions() {
		Options retVal = new Options();
		addFhirVersionOption(retVal);

		return retVal;
	}

	@Override
	public void run(CommandLine theCommandLine) throws ParseException, Exception {

		loadRun();
	}

	public void loadRun() throws ParseException, Exception {
		Flyway flyway = new Flyway();
		//  flyway.setBaselineOnMigrate(true);
		log.info("FLYWAY Locations = "+ flywayLocations);
		System.out.println("FLYWAY Locations = "+ flywayLocations);
		flyway.setLocations(flywayLocations);
		flyway.setDataSource(dataSource());
		flyway.repair();
		flyway.migrate();
	}


	public DataSource dataSource() {
		final DataSource dataSource = new DataSource();
		System.out.println("In Data Source");
		dataSource.setDriverClassName(driverName);
		dataSource.setUrl("jdbc:" + vendor + ":" + host + ":" + path);

		System.out.println(dataSource.getUrl());
		dataSource.setUsername(username);
		dataSource.setPassword(password);

		dataSource.setValidationQuery("select 1 as dbcp_connection_test");



		return dataSource;
	}

}
