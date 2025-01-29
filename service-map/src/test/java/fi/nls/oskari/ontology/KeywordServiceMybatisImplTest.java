package fi.nls.oskari.ontology;

import fi.nls.oskari.ontology.domain.Keyword;
import fi.nls.oskari.ontology.service.KeywordServiceMybatisImpl;
import fi.nls.oskari.util.PropertyUtil;
import fi.nls.test.util.TestHelper;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

public class KeywordServiceMybatisImplTest {

    private static KeywordServiceMybatisImpl keywordServiceMybatis = null;
    private static String testKeyword = null;
    private static String testLang = null;
    private static Keyword keyword = null;

    @BeforeAll
    public static void init() {
        Assumptions.assumeTrue(TestHelper.dbAvailable());
    }

    @BeforeEach
    public void setUp() {
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

        Assertions.assertTrue(keyword.getValue().equals(exactKeyword.getValue()), "Keyword added and found");
    }

    @Test
    public void testGetKeywords() {
        keywordServiceMybatis.addKeyword(keyword);
        List<Keyword> keywordList = keywordServiceMybatis.findKeywordsMatching(testKeyword);

        Assertions.assertTrue(keyword.getValue().equals(keywordList.get(0).getValue()), "Keyword added and found");
    }

    @AfterAll
    public static void delete() {
        PropertyUtil.clearProperties();
    }
}