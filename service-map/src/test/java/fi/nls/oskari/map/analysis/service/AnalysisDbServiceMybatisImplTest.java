package fi.nls.oskari.map.analysis.service;

import fi.nls.oskari.domain.map.analysis.Analysis;
import fi.nls.oskari.domain.map.analysis.AnalysisStyle;
import fi.nls.oskari.util.PropertyUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;
import java.util.Properties;

import static junit.framework.Assert.assertTrue;

public class AnalysisDbServiceMybatisImplTest {

    private AnalysisDbServiceMybatisImpl analysisDbServiceMybatis = null;
    private AnalysisStyleDbServiceMybatisImpl analysisStyleDbServiceMybatis = null;

    private Analysis testAnalysis = null;
    private String testAnalysisName = "testAnalysis";
    private String testUid = "1000";

    @Before
    public void init() {
        Properties properties = new Properties();
        try {
            properties.load(new FileInputStream(new File("C:\\Omat\\Jetty\\resources\\oskari-ext.properties")));
            PropertyUtil.addProperties(properties);
        } catch (Exception e) {
            e.getStackTrace();
        }
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
    public void delete() {
        try {
            analysisDbServiceMybatis.deleteAnalysis(testAnalysis);
        }
        catch (Exception e) {
            //TODO handle error
        }
        PropertyUtil.clearProperties();
    }
}