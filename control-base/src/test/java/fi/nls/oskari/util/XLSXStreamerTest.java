package fi.nls.oskari.util;

import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Map;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class XLSXStreamerTest {

    public XLSXStreamerTest() {
    }

    @BeforeAll
    public static void setUpClass() {
    }

    @AfterAll
    public static void tearDownClass() {
    }

    @BeforeEach
    public void setUp() {
    }

    @AfterEach
    public void tearDown() {
    }

    @Test
    @Disabled
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
