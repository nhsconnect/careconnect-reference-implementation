package uk.nhs.careconnect.ri.database.term;



import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.server.exceptions.InvalidRequestException;
import ca.uhn.fhir.util.TestUtil;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.Validate;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import uk.nhs.careconnect.ri.database.daointerface.CodeSystemRepository;
import uk.nhs.careconnect.ri.database.daointerface.ConceptRepository;
import uk.nhs.careconnect.ri.dao.TerminologyLoaderDao;
import uk.nhs.careconnect.ri.database.entity.Terminology.CodeSystemEntity;
import uk.nhs.careconnect.ri.database.entity.Terminology.ConceptEntity;
import uk.nhs.careconnect.ri.database.entity.Terminology.ConceptParentChildLink;
import uk.org.hl7.fhir.core.Dstu2.CareConnectSystem;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.hamcrest.Matchers.hasItem;
import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;


@RunWith(MockitoJUnitRunner.class)
public class TerminologyLoaderTestIT {
    private static final org.slf4j.Logger ourLog = org.slf4j.LoggerFactory.getLogger(TerminologyLoaderTestIT.class);

    private TerminologyLoaderDao mySvc;

    @Mock
    private CodeSystemRepository myTermSvc;

    @Mock
    private ConceptRepository codeSvc;

    Session session = null;

    @Captor
    private ArgumentCaptor<CodeSystemEntity> myCsvCaptor;

    @Before
    public void before() {
        mySvc = new TerminologyLoaderDao();
        mySvc.setTermSvcForUnitTests(myTermSvc, codeSvc);

    }

    @AfterClass
    public static void afterClassClearContext() {
        TestUtil.clearAllStaticFieldsForUnitTest();
    }

    @Test
    public void testLoadLoinc() throws Exception {
        ourLog.info("TEST = testLoadLoinc()");
        ByteArrayOutputStream bos1 = new ByteArrayOutputStream();
        ZipOutputStream zos1 = new ZipOutputStream(bos1);
        addEntry(zos1, "/loinc/", "loinc.csv");
        zos1.close();
        ourLog.info("ZIP file has {} bytes", bos1.toByteArray().length);

        ByteArrayOutputStream bos2 = new ByteArrayOutputStream();
        ZipOutputStream zos2 = new ZipOutputStream(bos2);
        addEntry(zos2, "/loinc/", "LOINC_2.54_MULTI-AXIAL_HIERARCHY.CSV");
        zos2.close();
        ourLog.info("ZIP file has {} bytes", bos2.toByteArray().length);

        RequestDetails details = mock(RequestDetails.class);
        when(codeSvc.findBySystem("http://loinc.org")).thenReturn(new CodeSystemEntity());
        mySvc.loadLoinc(list(bos1.toByteArray(), bos2.toByteArray()), details);

        verify(codeSvc).storeNewCodeSystemVersion( myCsvCaptor.capture(), any(RequestDetails.class));

        CodeSystemEntity ver = myCsvCaptor.getValue();
        ConceptEntity code = ver.getConcepts().iterator().next();
        assertEquals("10013-1", code.getCode());

    }

    @Captor
    private ArgumentCaptor<String> mySystemCaptor;


    /**
     * This is just for trying stuff, it won't run without
     * local files external to the git repo
     */
    @Ignore
    @Test
    public void testLoadSnomedCtAgainstRealFile() throws Exception {
        byte[] bytes = IOUtils.toByteArray(new FileInputStream("/Users/james/Downloads/SnomedCT_Release_INT_20160131_Full.zip"));

        RequestDetails details = mock(RequestDetails.class);
        mySvc.loadSnomedCt(list(bytes), details);
    }


    @Test
    public void testLoadSnomedCt() throws Exception {
        ourLog.info("TEST = testLoadSnomedCt()");
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ZipOutputStream zos = new ZipOutputStream(bos);
        addEntry(zos, "/sct/", "sct2_Concept_Full_INT_20160131.txt");
        addEntry(zos, "/sct/", "sct2_Concept_Full-en_INT_20160131.txt");
        addEntry(zos, "/sct/", "sct2_Description_Full-en_INT_20160131.txt");
        addEntry(zos, "/sct/", "sct2_Identifier_Full_INT_20160131.txt");
        addEntry(zos, "/sct/", "sct2_Relationship_Full_INT_20160131.txt");
        addEntry(zos, "/sct/", "sct2_StatedRelationship_Full_INT_20160131.txt");
        addEntry(zos, "/sct/", "sct2_TextDefinition_Full-en_INT_20160131.txt");
        zos.close();

        ourLog.info("ZIP file has {} bytes", bos.toByteArray().length);

        RequestDetails details = mock(RequestDetails.class);
        when(codeSvc.findBySystem(CareConnectSystem.SNOMEDCT)).thenReturn(new CodeSystemEntity());
        when(codeSvc.getSession()).thenReturn(mock(Session.class));
        when(codeSvc.getTransaction(any())).thenReturn(mock(Transaction.class));

        mySvc.loadSnomedCt(list(bos.toByteArray()), details);

        verify(codeSvc).storeNewCodeSystemVersion( myCsvCaptor.capture(), any(RequestDetails.class));

        CodeSystemEntity csv = myCsvCaptor.getValue();
        TreeSet<String> allCodes = toCodes(csv, true);
        ourLog.info("TEST = testLoadSnomedCt() withChildren = " + allCodes.toString());

        assertThat(allCodes, hasItem("116680003"));
      //  assertThat(allCodes, not(containsInAnyOrder("207527008")));

        allCodes = toCodes(csv, false);
        ourLog.info("TEST = testLoadSnomedCt() noChildren = " +allCodes.toString());
        assertThat(allCodes, hasItem("126813005"));
    }

    private List<byte[]> list(byte[]... theByteArray) {
        return new ArrayList<byte[]>(Arrays.asList(theByteArray));
    }

    private TreeSet<String> toCodes(CodeSystemEntity theCsv, boolean theAddChildren) {
        TreeSet<String> retVal = new TreeSet<String>();
        for (ConceptEntity next : theCsv.getConcepts()) {
            toCodes(retVal, next, theAddChildren);
        }
        return retVal;
    }

    private void toCodes(TreeSet<String> theCodes, ConceptEntity theConcept, boolean theAddChildren) {
        theCodes.add(theConcept.getCode());
        if (theAddChildren) {
            for (ConceptParentChildLink next : theConcept.getChildren()) {
                toCodes(theCodes, next.getChild(), theAddChildren);
            }
        }
    }

    @Test
    public void testLoadSnomedCtBadInput() throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ZipOutputStream zos = new ZipOutputStream(bos);
        addEntry(zos, "/sct/", "sct2_StatedRelationship_Full_INT_20160131.txt");
        zos.close();

        ourLog.info("ZIP file has {} bytes", bos.toByteArray().length);
        //when(mySvc.newTx()).thenReturn(mock(EntityTransaction.class));

        RequestDetails details = mock(RequestDetails.class);
        try {
            mySvc.loadSnomedCt(Collections.singletonList(bos.toByteArray()), details);
            fail();
        } catch (InvalidRequestException e) {
            assertEquals("Invalid input zip file, expected zip to contain the following name fragments: [Terminology/sct2_Description_Full-en, Terminology/sct2_Relationship_Full, Terminology/sct2_Concept_Full_] but found: []", e.getMessage());
        }
    }

    private void addEntry(ZipOutputStream zos, String theClasspathPrefix, String theFileName) throws IOException {
        ourLog.info("Adding {} to test zip", theFileName);
        zos.putNextEntry(new ZipEntry("SnomedCT_Release_INT_20160131_Full/Terminology/" + theFileName));
        byte[] byteArray = IOUtils.toByteArray(getClass().getResourceAsStream(theClasspathPrefix + theFileName));
        Validate.notNull(byteArray);
        zos.write(byteArray);
        zos.closeEntry();
    }

}
