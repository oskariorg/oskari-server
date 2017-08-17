package fi.nls.oskari.ontology;

import fi.nls.oskari.ontology.domain.Keyword;
import fi.nls.oskari.ontology.service.KeywordServiceMybatisImpl;
import fi.nls.oskari.util.PropertyUtil;
import org.junit.Before;
import org.junit.Test;

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
            properties.load(KeywordServiceMybatisImplTest.class.getResourceAsStream("test.properties"));
            PropertyUtil.addProperties(properties);
        } catch (Exception e) {
            //fail("Should not throw exception" + e.getStackTrace());
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
}