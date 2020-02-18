package uk.nhs.careconnect.ccri.fhirserver.support;

import ca.uhn.fhir.rest.server.exceptions.InternalErrorException;
import org.hl7.fhir.exceptions.FHIRException;
import org.hl7.fhir.utilities.cache.NpmPackage;
import org.hl7.fhir.utilities.cache.PackageCacheManager;
import org.hl7.fhir.utilities.cache.ToolsVersion;

import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

public class PackageManager {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(PackageManager.class);

    public final static NpmPackage getPackage(String packageName, String version,String url) throws Exception {

        NpmPackage npm = null;
        PackageCacheManager pcm= new PackageCacheManager(true, ToolsVersion.TOOLS_VERSION);


            try {


                if (!url.isEmpty()) {
                    // Loads the package in from a Url
                    try {
                        log.info("#1 Loading Ig Package for API Validation {} {} {}", packageName, url, version);

                        npm = getPackageFromUrl(pcm, packageName, url, version);
                    }
                    catch (Exception ex) {
                        if (ex != null) log.error(ex.getMessage());
                        //	throw new ServletException(ex);
                    }
                }
                if (npm == null) {
                    if (!version.isEmpty()) {
                        log.info("#2 Loading Ig Package for API Validation {} {}", packageName,version);
                        npm = pcm.loadPackage(packageName, version);

                    } else
                    {
                        log.info("#3 Loading Ig Package for API Validation {} {}", packageName, "No Version");
                        npm = pcm.loadPackage(packageName, null);
                    }
                }
                if (npm== null)  throw new InternalErrorException("Unable to load API Validation package");
            }
            catch (Exception ex) {
                log.error(ex.getMessage());
                throw new InternalErrorException(ex.getMessage());
            }
            return npm;
    }

    private final static NpmPackage getPackageFromUrl(PackageCacheManager pcm, String id, String url, String version) throws Exception {

        InputStream stream;
        try {
            log.info("Package Url: {}", url);
            if (url.contains(".tgz")) {
                stream = fetchFromUrlSpecific(url, true);
                if (stream != null) {
                    log.info("Packaged Returned Ok: {}", url);
                    return pcm.addPackageToCache(id,version , stream, url);

                }
            } }
        catch (Exception ex ) {
            log.error("Handled: {}",ex.getMessage());
        }
        return null;
    }

    private final static InputStream fetchFromUrlSpecific(String source, boolean optional) throws FHIRException {
        try {
            URL url = new URL(source);
            URLConnection c = url.openConnection();
            return c.getInputStream();
        } catch (Exception var5) {
            if (optional) {
                return null;
            } else {
                throw new FHIRException(var5.getMessage(), var5);
            }
        }
    }
}
