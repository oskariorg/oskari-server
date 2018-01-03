package fi.nls.oskari.util;

import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Map;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

public class XLSXStreamerTest {

    public XLSXStreamerTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test
    @Ignore
    public void testStreamToFile() throws Exception {
        XLSXStreamer streamer = new XLSXStreamer();
        
        HSSFWorkbook wb = new HSSFWorkbook();
        FileOutputStream out = new FileOutputStream("workbook.xlsx");
        
        String[] headers = {"hello", "world"};
        Object[][] data = {
            {"100.0", "101.0"},
            {"200.0", "201.0"},            
        };
        Map<String, Object> additionalFields = new HashMap<>();
        additionalFields.put("add1", "val1");
        additionalFields.put("add2", "val2");
        
        streamer.writeToStream(headers, data, additionalFields, out);
        
        // The actual test is to see that this goes through without error,
        // and that the file opens in Excel without problems.
    }
}
