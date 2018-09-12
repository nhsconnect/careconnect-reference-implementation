package uk.nhs.careconnect.ccri.fhirserver.provider;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.nhs.careconnect.ri.database.daointerface.TerminologyLoader;

@Component
public class TerminologyUploaderProvider extends BaseProvider  {

    public static final String UPLOAD_EXTERNAL_CODE_SYSTEM = "$upload-external-code-system";

    private static final org.slf4j.Logger ourLog = org.slf4j.LoggerFactory.getLogger(TerminologyUploaderProvider.class);


    @Autowired
    private TerminologyLoader myTerminologyLoaderSvc;

    //@formatter:off

   /*

   Not fully tested

    @Operation(name = UPLOAD_EXTERNAL_CODE_SYSTEM, idempotent = false, returnParameters= {
            @OperationParam(name="conceptCount", type=IntegerType.class, min=1)
    })
    public Parameters uploadExternalCodeSystem(
            HttpServletRequest theServletRequest,
            @OperationParam(name="url", min=1) UriType theUrl,
            @OperationParam(name="package", min=0) Attachment thePackage,
            @OperationParam(name="localfile", min=0, max=OperationParam.MAX_UNLIMITED) List<StringType> theLocalFile,
            RequestDetails theRequestDetails
    ) {
        //@formatter:on

       startRequest(theServletRequest);
        try {
            List<byte[]> data = new ArrayList<>();
            if (theLocalFile != null && theLocalFile.size() > 0) {
                for (StringType nextLocalFile : theLocalFile) {
                    if (isNotBlank(nextLocalFile.getValue())) {
                        ourLog.info("Reading in local file: {}", nextLocalFile.getValue());
                        try {
                            byte[] nextData = IOUtils.toByteArray(new FileInputStream(nextLocalFile.getValue()));
                            data.add(nextData);
                        } catch (IOException e) {
                            throw new InternalErrorException(e);
                        }
                    }
                }
            } else if (thePackage == null || thePackage.getData() == null || thePackage.getData().length == 0) {
                throw new InvalidRequestException("No 'localfile' or 'package' parameter, or package had no data");
            } else {
                data = new ArrayList<byte[]>();
                data.add(thePackage.getData());
                thePackage.setData(null);
            }

            String url = theUrl != null ? theUrl.getValueAsString() : null;
            url = defaultString(url);


            TerminologyLoader.UploadStatistics stats;
            if (TerminologyLoader.SCT_URL.equals(url)) {
                stats = myTerminologyLoaderSvc.loadSnomedCt((data), theRequestDetails);
            } else if (TerminologyLoader.LOINC_URL.equals(url)) {
                stats = myTerminologyLoaderSvc.loadLoinc((data), theRequestDetails);
            } else {
                throw new InvalidRequestException("Unknown URL: " + url);
            }

            Parameters retVal = new Parameters();
            retVal.addParameter().setName("conceptCount").setValue(new IntegerType(stats.getConceptCount()));
            return retVal;
        } finally {
           endRequest(theServletRequest);
        }
    }
    */
}
