package fi.nls.oskari.ontology;

import fi.nls.oskari.ontology.domain.Keyword;
import fi.nls.oskari.ontology.service.KeywordServiceMybatisImpl;
import fi.nls.oskari.util.PropertyUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;
import java.util.Properties;

import static junit.framework.Assert.assertTrue;

public class KeywordServiceMybatisImplTest {

    private KeywordServiceMybatisImpl keywordServiceMybatis = null;
    private String testKeyword = null;
    private String testLang = null;
    private Keyword keyword = null;

    @Before
    public void init() {

        Properties properties = new Properties();
        try {
            properties.load(new FileInputStream(new File("C:\\Omat\\Jetty\\resources\\oskari-ext.properties")));
            PropertyUtil.addProperties(properties);
        } catch (Exception e) {
            e.getStackTrace();
        }
        keywordServiceMybatis = new KeywordServiceMybatisImpl();
        testKeyword = "testKeyword";
        testLang = "FI";

        keyword = new Keyword();
        keyword.setEditable(true);
        keyword.setLang(testLang);
        keyword.setUri("testUri");
        keyword.setValue(testKeyword);
    }

    @Test
    public void testAddAndGetExactKeyword() {
        keywordServiceMybatis.addKeyword(keyword);
        Keyword exactKeyword = keywordServiceMybatis.findExactKeyword(testKeyword, testLang);

        assertTrue("Keyword added and found", keyword.getValue().equals(exactKeyword.getValue()));
    }

    @Test
    public void testGetKeywords() {
        keywordServiceMybatis.addKeyword(keyword);
        List<Keyword> keywordList = keywordServiceMybatis.findKeywordsMatching(testKeyword);

        assertTrue("Keyword added and found", keyword.getValue().equals(keywordList.get(0).getValue()));
    }

    @After
    public void delete() {
        PropertyUtil.clearProperties();
    }
}