package fi.nls.oskari.ontology;

import fi.nls.oskari.ontology.domain.Keyword;
import fi.nls.oskari.ontology.service.KeywordServiceMybatisImpl;
import fi.nls.oskari.util.PropertyUtil;
import fi.nls.test.util.TestHelper;
import org.junit.*;

import java.util.List;

import static junit.framework.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

@Ignore
public class KeywordServiceMybatisImplTest {

    private static KeywordServiceMybatisImpl keywordServiceMybatis = null;
    private static String testKeyword = null;
    private static String testLang = null;
    private static Keyword keyword = null;

    @BeforeClass
    public static void init() {
        assumeTrue(TestHelper.dbAvailable());
    }

    @Before
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

        assertTrue("Keyword added and found", keyword.getValue().equals(exactKeyword.getValue()));
    }

    @Test
    public void testGetKeywords() {
        keywordServiceMybatis.addKeyword(keyword);
        List<Keyword> keywordList = keywordServiceMybatis.findKeywordsMatching(testKeyword);

        assertTrue("Keyword added and found", keyword.getValue().equals(keywordList.get(0).getValue()));
    }

    @AfterClass
    public static void delete() {
        PropertyUtil.clearProperties();
    }
}