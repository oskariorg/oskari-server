package fi.nls.oskari.ontology;

import fi.nls.oskari.ontology.service.KeywordServiceMybatisImpl;
import org.junit.Test;

import static junit.framework.Assert.assertTrue;

public class KeywordServiceMybatisImplTest {

    private KeywordServiceMybatisImpl service = new KeywordServiceMybatisImpl();

    @Test
    public void testGetKeywordTemplateByName() {
        assertTrue("This will succeed.", true);
    }

    @Test
    public void testAddKeywordTemplate() {
        assertTrue("This will succeed.", true);
    }

}
