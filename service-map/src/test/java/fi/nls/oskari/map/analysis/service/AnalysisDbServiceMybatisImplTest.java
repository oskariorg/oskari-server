package fi.nls.oskari.map.analysis.service;

import fi.nls.oskari.domain.map.UserDataStyle;
import fi.nls.oskari.domain.map.analysis.Analysis;
import fi.nls.oskari.service.OskariComponentManager;
import fi.nls.oskari.service.ServiceException;
import fi.nls.oskari.util.PropertyUtil;
import fi.nls.test.util.TestHelper;
import org.junit.*;

import java.util.List;

import static junit.framework.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

public class AnalysisDbServiceMybatisImplTest {

    private static AnalysisDbService analysisDbService;

    private static Analysis testAnalysis = null;
    private static String testAnalysisName = "testAnalysis";
    private static String testUid = "1000";

    @BeforeClass
    public static void init() {
        assumeTrue(TestHelper.dbAvailable());
    }

    @Before
    public void setUp() throws ServiceException {
        analysisDbService = OskariComponentManager.getComponentOfType(AnalysisDbService.class);
        testAnalysis = new Analysis();
        testAnalysis.setName(testAnalysisName);
        testAnalysis.setUuid(testUid);
    }

    @Test
    public void testAddAndGetAnalysis() {
        analysisDbService.insertAnalysisRow(testAnalysis);
        Analysis analysis = analysisDbService.getAnalysisById(testAnalysis.getId());

        assertTrue("Analysis added and found", testAnalysis.getName().equals(analysis.getName()));
    }

    @Test
    public void testGetAnalysisListByUid() {
        analysisDbService.insertAnalysisRow(testAnalysis);
        List<Analysis> analysisList = analysisDbService.getAnalysisByUid(testAnalysis.getUuid());

        assertTrue("Analysis added and found", testAnalysis.getUuid().equals(analysisList.get(0).getUuid()));
    }

    @After
    public void tearDown() {
        try {
            analysisDbService.deleteAnalysis(testAnalysis);
        }
        catch (Exception e) {
        }
    }

    @AfterClass
    public static void delete() {
        PropertyUtil.clearProperties();
    }
}