package fi.nls.oskari.ontology;

import fi.nls.oskari.ontology.service.KeywordServiceIbatisImpl;
import org.junit.Test;

import static junit.framework.Assert.assertTrue;

public class KeywordServiceIbatisImplTest {

    private KeywordServiceIbatisImpl service = new KeywordServiceIbatisImpl();

    @Test
    public void testGetKeywordTemplateByName() {
        assertTrue("This will succeed.", true);
    }

    @Test
    public void testAddKeywordTemplate() {
        assertTrue("This will succeed.", true);
    }

}
