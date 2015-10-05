package fi.nls.oskari.util;

import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by SMAKINEN on 15.4.2015.
 */
public class FileHelperTest {

    // list of: original path, filename, file basename, extension, "safe name"
    private static final String[][] TEST_FILE_NAMES = new String[1][0];

    @BeforeClass
    public static void setup() {
        // note! in windows Java uses both / and \ path separators, but on linux \ stay as part of the filename
        TEST_FILE_NAMES[0] = new String[] {",sdag/..SADG/jyt_-7_/sadg/moikka.moi.txt", "moikka.moi.txt", "moikka.moi", "txt", "moikka_moi.txt"};
    }

    @Test
    public void testFileHandling() {
        for(String[] entry : TEST_FILE_NAMES) {
            FileHelper file = new FileHelper(entry[0]);
            assertEquals("getFilename should be parsed correctly", entry[1], file.getFilename());
            assertEquals("getBaseName should be parsed correctly", entry[2], file.getBaseName());
            assertEquals("getExtension should be parsed correctly", entry[3], file.getExtension());
            assertEquals("getSafeName should be parsed correctly", entry[4], file.getSafeName());
        }
    }
}