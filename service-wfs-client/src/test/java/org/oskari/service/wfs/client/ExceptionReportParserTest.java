package org.oskari.service.wfs.client;

import static org.junit.Assert.assertEquals;

import java.io.InputStream;

import org.junit.Test;

public class ExceptionReportParserTest {

    @Test
    public void happyCase() throws Exception {
        try (InputStream in = getClass().getClassLoader().getResourceAsStream("exceptionReport.xml")) {
            OWSException exception = ExceptionReportParser.parse(in);
            assertEquals("InvalidParameterValue", exception.getExceptionCode());
            assertEquals("outputFormat", exception.getLocator());
            assertEquals("Failed to find response for output format application/json11", exception.getExceptionText());
        }
    }

}
