package uk.nhs.careconnect.ccri.fhirserver;

import ca.uhn.fhir.context.ConfigurationException;
import ca.uhn.fhir.context.FhirVersionEnum;
import ca.uhn.fhir.rest.api.EncodingEnum;
import ca.uhn.fhir.rest.server.ETagSupportEnum;
import com.google.common.annotations.VisibleForTesting;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

public class HapiProperties {

    private HapiProperties () {
        
    }
    
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(HapiProperties.class);

    static final String ALLOW_EXTERNAL_REFERENCES = "allow_external_references";
    static final String ALLOW_MULTIPLE_DELETE = "allow_multiple_delete";
    static final String ALLOW_PLACEHOLDER_REFERENCES = "allow_placeholder_references";
    static final String REUSE_CACHED_SEARCH_RESULTS_MILLIS = "reuse_cached_search_results_millis";
    static final String DATASOURCE_DRIVER = "datasource.driver";
    static final String DATASOURCE_MAX_POOL_SIZE = "datasource.max_pool_size";
    static final String DATASOURCE_PASSWORD = "datasource.password";
    static final String DATASOURCE_URL = "datasource.url";
    static final String DATASOURCE_USERNAME = "datasource.username";
    static final String DEFAULT_ENCODING = "default_encoding";
    static final String DEFAULT_PAGE_SIZE = "default_page_size";
    static final String DEFAULT_PRETTY_PRINT = "default_pretty_print";
    static final String ETAG_SUPPORT = "etag_support";
    static final String FHIR_VERSION = "fhir_version";
    static final String HAPI_PROPERTIES = "hapi.properties";
    static final String LOGGER_ERROR_FORMAT = "logger.error_format";
    static final String LOGGER_FORMAT = "logger.format";
    static final String LOGGER_LOG_EXCEPTIONS = "logger.log_exceptions";
    static final String LOGGER_NAME = "logger.name";
    static final String MAX_FETCH_SIZE = "max_fetch_size";
    static final String MAX_PAGE_SIZE = "max_page_size";
    static final String PERSISTENCE_UNIT_NAME = "persistence_unit_name";
    static final String SERVER_ADDRESS = "server_address";
    static final String SERVER_BASE = "server.base";
    static final String SERVER_ID = "server.id";
    static final String SERVER_NAME = "server.name";
    static final String SUBSCRIPTION_EMAIL_ENABLED = "subscription.email.enabled";
    static final String SUBSCRIPTION_RESTHOOK_ENABLED = "subscription.resthook.enabled";
    static final String SUBSCRIPTION_WEBSOCKET_ENABLED = "subscription.websocket.enabled";
    static final String TEST_PORT = "test.port";
    static final String TESTER_CONFIG_REFUSE_TO_FETCH_THIRD_PARTY_URLS = "tester.config.refuse_to_fetch_third_party_urls";
    static final String CORS_ENABLED = "cors.enabled";
    static final String CORS_ALLOWED_ORIGIN = "cors.allowed_origin";
    static final String ALLOW_CONTAINS_SEARCHES = "allow_contains_searches";
    static final String ALLOW_OVERRIDE_DEFAULT_SEARCH_PARAMS = "allow_override_default_search_params";
    static final String EMAIL_FROM = "email.from";

    static final String SOFTWARE_NAME = "software.name";
    static final String SOFTWARE_VERSION = "software.version";
    static final String SOFTWARE_IMPLEMENTATION_DESC = "software.implementation.desc";
    static final String SOFTWARE_IMPLEMENTATION_URL = "software.implementation.url";
    static final String SOFTWARE_IMPLEMENTATION_GUIDE = "software.implementation.guide";

    static final String VALIDATION_FLAG = "validate.flag";
    static final String VALIDATION_SERVER = "validation.server";

    static final String APP_USER = "jolokia.username";
    static final String APP_PASSWORD = "jolokia.password";

    static final String HIBERNATE_DIALECT = "hibernate.dialect";
    static final String HIBERNATE_ELASTICSEARCH_HOST = "hibernate.search.default.elasticsearch.host";
    static final String HIBERNATE_SHOW_SQL = "hibernate.show_sql";

    static final String SECURITY_OAUTH = "security.oauth2";
    static final String SECURITY_OPENID_CONFIG = "security.oauth2.configuration.server";
    static final String SECURITY_OAUTH2_AUTHORIZE = "security.oauth2.configuration.authorize";
    static final String SECURITY_OAUTH2_REGISTER = "security.oauth2.configuration.register";
    static final String SECURITY_OAUTH2_TOKEN = "security.oauth2.configuration.token";
    static final String SECURITY_OAUTH_SCOPE = "security.oauth2.scope";
    static final String SECURITY_SMART_SCOPE = "security.oauth2.smart";

    static final String GP_CONNECT_SERVER_V0 = "gpconnect.addressDSTU2";

    static final String NHS_SERVER = "nhs.address";

    static final String NHS_ASID_FROM = "nhs.ASIDfrom";
    static final String NHS_ASID_TO = "nhs.ASIDto";

    static final String TERMINOLOGY_SERVER = "terminology.server.primary";
    static final String TERMINOLOGY_SERVER_SECONDARY = "terminology.server.secondary";

    static final String SERVER_ROLE = "ccri.role";
    static final String SERVER_CRUD_READ = "ccri.CRUD_read";
    static final String SERVER_CRUD_UPDATE = "ccri.CRUD_update";
    static final String SERVER_CRUD_CREATE = "ccri.CRUD_create";
    static final String SERVER_CRUD_DELETE = "ccri.CRUD_delete";
    private static Properties properties;

    /*
     * Force the configuration to be reloaded
     */
    public static void forceReload() {
        properties = null;
        getProperties();
    }

    /**
     * This is mostly here for unit tests. Use the actual properties file
     * to set values
     */
    @VisibleForTesting
    public static void setProperty(String theKey, String theValue) {
        getProperties().setProperty(theKey, theValue);
    }

    public static Properties getProperties() {
        if (properties == null) {
            // Load the configurable properties file
            try (InputStream in = HapiProperties.class.getClassLoader().getResourceAsStream(HAPI_PROPERTIES)){
                HapiProperties.properties = new Properties();
                HapiProperties.properties.load(in);
            } catch (Exception e) {
                throw new ConfigurationException("Could not load HAPI properties", e);
            }

            Properties overrideProps = loadOverrideProperties();
            if(overrideProps != null) {
                properties.putAll(overrideProps);
            }
        }

        return properties;
    }

    /**
     * If a configuration file path is explicitly specified via -Dhapi.properties=<path>, the properties there will
     * be used to override the entries in the default hapi.properties file (currently under WEB-INF/classes)
     * @return properties loaded from the explicitly specified configuraiton file if there is one, or null otherwise.
     */
    private static Properties loadOverrideProperties() {
        String confFile = System.getProperty(HAPI_PROPERTIES);
        if(confFile != null) {
            try {
                Properties props = new Properties();
                props.load(new FileInputStream(confFile));
                return props;
            }
            catch (Exception e) {
                throw new ConfigurationException("Could not load HAPI properties file: " + confFile, e);
            }
        }

        return null;
    }

    private static String getProperty(String propertyName) {
        Properties properties = HapiProperties.getProperties();
        log.trace("Looking for property = {}", propertyName);
        if (System.getenv(propertyName)!= null) {
            String value= System.getenv(propertyName);
            log.debug("System Environment property Found {} = {}", propertyName, value);
            return value;
        }
        if (System.getProperty(propertyName)!= null) {
            String value= System.getenv(propertyName);
            log.debug("System Property Found {} = {}" , propertyName, value);
            return value;
        }
        if (properties != null) {
            return properties.getProperty(propertyName);
        }

        return null;
    }

    private static String getProperty(String propertyName, String defaultValue) {
        Properties properties = HapiProperties.getProperties();

        if (properties != null) {

            String value = properties.getProperty(propertyName);

            if (value != null && value.length() > 0) {
                return value;
            }
        }

        return defaultValue;
    }

    private static boolean getBooleanProperty(String propertyName, boolean defaultValue) {
        String value = HapiProperties.getProperty(propertyName);

        if (value == null || value.length() == 0) {
            return defaultValue;
        }

        return Boolean.parseBoolean(value);
    }

    private static Integer getIntegerProperty(String propertyName, Integer defaultValue) {
        String value = HapiProperties.getProperty(propertyName);

        if (value == null || value.length() == 0) {
            return defaultValue;
        }

        return Integer.parseInt(value);
    }

    public static FhirVersionEnum getFhirVersion() {
        String fhirVersionString = HapiProperties.getProperty(FHIR_VERSION);

        if (fhirVersionString != null && fhirVersionString.length() > 0) {
            return FhirVersionEnum.valueOf(fhirVersionString);
        }

        return FhirVersionEnum.DSTU3;
    }

    public static ETagSupportEnum getEtagSupport() {
        String etagSupportString = HapiProperties.getProperty(ETAG_SUPPORT);

        if (etagSupportString != null && etagSupportString.length() > 0) {
            return ETagSupportEnum.valueOf(etagSupportString);
        }

        return ETagSupportEnum.ENABLED;
    }

    public static EncodingEnum getDefaultEncoding() {
        String defaultEncodingString = HapiProperties.getProperty(DEFAULT_ENCODING);

        if (defaultEncodingString != null && defaultEncodingString.length() > 0) {
            return EncodingEnum.valueOf(defaultEncodingString);
        }

        return EncodingEnum.JSON;
    }

    public static boolean getDefaultPrettyPrint() {
        return HapiProperties.getBooleanProperty(DEFAULT_PRETTY_PRINT, true);
    }

    public static String getServerAddress() {
        return HapiProperties.getProperty(SERVER_ADDRESS);
    }

    public static Integer getDefaultPageSize() {
        return HapiProperties.getIntegerProperty(DEFAULT_PAGE_SIZE, 20);
    }

    public static Integer getMaximumPageSize() {
        return HapiProperties.getIntegerProperty(MAX_PAGE_SIZE, 200);
    }

    public static Integer getMaximumFetchSize() {
        return HapiProperties.getIntegerProperty(MAX_FETCH_SIZE, Integer.MAX_VALUE);
    }

    public static String getPersistenceUnitName() {
        return HapiProperties.getProperty(PERSISTENCE_UNIT_NAME, "HAPI_PU");
    }

    public static String getLoggerName() {
        return HapiProperties.getProperty(LOGGER_NAME, "fhirtest.access");
    }

    public static String getLoggerFormat() {
        return HapiProperties.getProperty(LOGGER_FORMAT, "Path[${servletPath}] Source[${requestHeader.x-forwarded-for}] Operation[${operationType} ${operationName} ${idOrResourceName}] UA[${requestHeader.user-agent}] Params[${requestParameters}] ResponseEncoding[${responseEncodingNoDefault}]");
    }

    public static String getLoggerErrorFormat() {
        return HapiProperties.getProperty(LOGGER_ERROR_FORMAT, "ERROR - ${requestVerb} ${requestUrl}");
    }

    public static boolean getLoggerLogExceptions() {
        return HapiProperties.getBooleanProperty(LOGGER_LOG_EXCEPTIONS, true);
    }

    public static String getDataSourceDriver() {
        return HapiProperties.getProperty(DATASOURCE_DRIVER);
    }

    public static Integer getDataSourceMaxPoolSize() {
        return HapiProperties.getIntegerProperty(DATASOURCE_MAX_POOL_SIZE, 10);
    }

    public static String getDataSourceUrl() {
        return HapiProperties.getProperty(DATASOURCE_URL);
    }

    public static String getDataSourceUsername() {
        return HapiProperties.getProperty(DATASOURCE_USERNAME);
    }

    public static String getDataSourcePassword() {
        return HapiProperties.getProperty(DATASOURCE_PASSWORD);
    }

    public static boolean getAllowMultipleDelete() {
        return HapiProperties.getBooleanProperty(ALLOW_MULTIPLE_DELETE, false);
    }

    public static boolean getAllowExternalReferences() {
        return HapiProperties.getBooleanProperty(ALLOW_EXTERNAL_REFERENCES, false);
    }

    public static boolean getExpungeEnabled() {
        return HapiProperties.getBooleanProperty("expunge_enabled", true);
    }

    public static Integer getTestPort() {
        return HapiProperties.getIntegerProperty(TEST_PORT, 0);
    }

    public static boolean getTesterConfigRefustToFetchThirdPartyUrls() {
        return HapiProperties.getBooleanProperty(TESTER_CONFIG_REFUSE_TO_FETCH_THIRD_PARTY_URLS, false);
    }

    public static boolean getCorsEnabled() {
        return HapiProperties.getBooleanProperty(CORS_ENABLED, true);
    }

    public static String getCorsAllowedOrigin() {
        return HapiProperties.getProperty(CORS_ALLOWED_ORIGIN, "*");
    }

    public static String getServerBase() {
        return HapiProperties.getProperty(SERVER_BASE, "/fhir");
    }

    public static String getServerName() {
        return HapiProperties.getProperty(SERVER_NAME);
    }

    public static String getServerId() {
        return HapiProperties.getProperty(SERVER_ID, "home");
    }

    public static boolean getAllowPlaceholderReferences() {
        return HapiProperties.getBooleanProperty(ALLOW_PLACEHOLDER_REFERENCES, true);
    }

    public static boolean getSubscriptionEmailEnabled() {
        return HapiProperties.getBooleanProperty(SUBSCRIPTION_EMAIL_ENABLED, false);
    }

    public static boolean getSubscriptionRestHookEnabled() {
        return HapiProperties.getBooleanProperty(SUBSCRIPTION_RESTHOOK_ENABLED, false);
    }

    public static boolean getSubscriptionWebsocketEnabled() {
        return HapiProperties.getBooleanProperty(SUBSCRIPTION_WEBSOCKET_ENABLED, false);
    }

    public static boolean getAllowContainsSearches() {
        return HapiProperties.getBooleanProperty(ALLOW_CONTAINS_SEARCHES, true);
    }

    public static boolean getAllowOverrideDefaultSearchParams() {
        return HapiProperties.getBooleanProperty(ALLOW_OVERRIDE_DEFAULT_SEARCH_PARAMS, true);
    }

    public static String getEmailFrom() {
        return HapiProperties.getProperty(EMAIL_FROM, "some@test.com");
    }

    public static boolean getEmailEnabled() {
        return HapiProperties.getBooleanProperty("email.enabled", false);
    }

    public static String getEmailHost() {
        return HapiProperties.getProperty("email.host");
    }

    public static Integer getEmailPort() {
        return HapiProperties.getIntegerProperty("email.port", 0);
    }

    public static String getEmailUsername() {
        return HapiProperties.getProperty("email.username");
    }

    public static String getEmailPassword() {
        return HapiProperties.getProperty("email.password");
    }

    public static Long getReuseCachedSearchResultsMillis() {
        String value = HapiProperties.getProperty(REUSE_CACHED_SEARCH_RESULTS_MILLIS, "-1");
        return Long.valueOf(value);
    }

    public static String getSoftwareName() {
        return HapiProperties.getProperty(SOFTWARE_NAME);
    }

    public static String getSoftwareVersion() {
        return HapiProperties.getProperty(SOFTWARE_VERSION);
    }

    public static String getSoftwareImplementationDesc() {
        return HapiProperties.getProperty(SOFTWARE_IMPLEMENTATION_DESC);
    }

    public static String getSoftwareImplementationUrl() {
        return HapiProperties.getProperty(SOFTWARE_IMPLEMENTATION_URL);
    }

    public static String getSoftwareImplementationGuide() {
        return HapiProperties.getProperty(SOFTWARE_IMPLEMENTATION_GUIDE);
    }

    public static boolean getValidationFlag() {
        return HapiProperties.getBooleanProperty(VALIDATION_FLAG, false);
    }

    public static String getValidationServer() {
        return HapiProperties.getProperty(VALIDATION_SERVER);
    }

    public static String getAppUser() {
        return HapiProperties.getProperty(APP_USER);
    }

    public static String getAppPassword() {
        return HapiProperties.getProperty(APP_PASSWORD);
    }

    public static String getHibernateDialect() {
        return HapiProperties.getProperty(HIBERNATE_DIALECT);
    }

    public static String getHibernateElasticsearchHost() {
        return HapiProperties.getProperty(HIBERNATE_ELASTICSEARCH_HOST);
    }

    public static String getHibernateShowSql() {
        return HapiProperties.getProperty(HIBERNATE_SHOW_SQL);
    }

    public static boolean getSecurityOauth() {
        return HapiProperties.getBooleanProperty(SECURITY_OAUTH, false);
    }


    public static String getSecurityOpenidConfig() {
        return HapiProperties.getProperty(SECURITY_OPENID_CONFIG);
    }

    public static String getSecurityOauth2Authorize() {
        return HapiProperties.getProperty(SECURITY_OAUTH2_AUTHORIZE);
    }

    public static String getSecurityOauth2Register() {
        return HapiProperties.getProperty(SECURITY_OAUTH2_REGISTER);
    }

    public static String getSecurityOauth2Token() {
        return HapiProperties.getProperty(SECURITY_OAUTH2_TOKEN);
    }

    public static boolean getSecuritySmartScope() {
        return HapiProperties.getBooleanProperty(SECURITY_SMART_SCOPE, false);
    }

    public static String getSecurityOauthScope() {
        return HapiProperties.getProperty(SECURITY_OAUTH_SCOPE);
    }


    public static String getNHSServerV0() {
        return HapiProperties.getProperty(GP_CONNECT_SERVER_V0);
    }

    public static String getNhsServer() {
        return HapiProperties.getProperty(NHS_SERVER);
    }

    public static String getNhsAsidFrom() {
        return HapiProperties.getProperty(NHS_ASID_FROM);
    }

    public static String getNhsAsidTo() {
        return HapiProperties.getProperty(NHS_ASID_TO);
    }

    public static String getTerminologyServer() {
        return HapiProperties.getProperty(TERMINOLOGY_SERVER);
    }

    public static String getTerminologyServerSecondary() {
        return HapiProperties.getProperty(TERMINOLOGY_SERVER_SECONDARY);
    }
    public static String getServerRole() {
        return HapiProperties.getProperty(SERVER_ROLE);
    }

    public static boolean getServerCrudRead() {
        return HapiProperties.getBooleanProperty(SERVER_CRUD_READ, false);
    }

    public static boolean getServerCrudUpdate() {
        return HapiProperties.getBooleanProperty(SERVER_CRUD_UPDATE, false);
    }

    public static boolean getServerCrudCreate() {
        return HapiProperties.getBooleanProperty(SERVER_CRUD_CREATE, false);
    }

    public static boolean getServerCrudDelete() {
        return HapiProperties.getBooleanProperty(SERVER_CRUD_DELETE, false);
    }
}
