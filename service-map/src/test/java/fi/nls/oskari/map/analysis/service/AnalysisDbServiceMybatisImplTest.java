package fi.nls.oskari.map.analysis.service;

import fi.nls.oskari.domain.map.analysis.Analysis;
import fi.nls.oskari.domain.map.analysis.AnalysisStyle;
import fi.nls.oskari.service.ServiceException;
import fi.nls.oskari.util.PropertyUtil;
import fi.nls.test.util.TestHelper;
import org.junit.*;

import java.util.List;

import static junit.framework.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

public class AnalysisDbServiceMybatisImplTest {

    private static AnalysisDbServiceMybatisImpl analysisDbServiceMybatis = null;
    private static AnalysisStyleDbServiceMybatisImpl analysisStyleDbServiceMybatis = null;

    private static Analysis testAnalysis = null;
    private static String testAnalysisName = "testAnalysis";
    private static String testUid = "1000";

    @BeforeClass
    public static void init() {
        assumeTrue(TestHelper.dbAvailable());
    }

    @Before
    public void setUp() throws ServiceException {
        analysisDbServiceMybatis = new AnalysisDbServiceMybatisImpl();
        analysisStyleDbServiceMybatis = new AnalysisStyleDbServiceMybatisImpl();

        AnalysisStyle analysisStyle = new AnalysisStyle();
        analysisStyle.setDot_shape("POLYGON");
        analysisStyleDbServiceMybatis.insertAnalysisStyleRow(analysisStyle);

        testAnalysis = new Analysis();
        testAnalysis.setName(testAnalysisName);
        testAnalysis.setUuid(testUid);
        testAnalysis.setStyle_id(analysisStyle.getId());
    }

    @Test
    public void testAddAndGetAnalysis() {
        analysisDbServiceMybatis.insertAnalysisRow(testAnalysis);
        Analysis analysis = analysisDbServiceMybatis.getAnalysisById(testAnalysis.getId());

        assertTrue("Analysis added and found", testAnalysis.getName().equals(analysis.getName()));
    }

    @Test
    public void testGetAnalysisListByUid() {
        analysisDbServiceMybatis.insertAnalysisRow(testAnalysis);
        List<Analysis> analysisList = analysisDbServiceMybatis.getAnalysisByUid(testAnalysis.getUuid());

        assertTrue("Analysis added and found", testAnalysis.getUuid().equals(analysisList.get(0).getUuid()));
    }

    @After
    public void tearDown() {
        try {
            analysisDbServiceMybatis.deleteAnalysis(testAnalysis);
        }
        catch (Exception e) {
        }
    }

    @AfterClass
    public static void delete() {
        PropertyUtil.clearProperties();
    }
}