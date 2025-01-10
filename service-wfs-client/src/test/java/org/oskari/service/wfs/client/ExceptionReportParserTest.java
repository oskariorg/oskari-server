package org.oskari.service.wfs.client;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.Assert.assertEquals;

import java.io.InputStream;

public class ExceptionReportParserTest {

    @Test
    public void happyCase() throws Exception {
        try (InputStream in = getClass().getClassLoader().getResourceAsStream("exceptionReport.xml")) {
            OWSException exception = OWSExceptionReportParser.parse(in);
            Assertions.assertEquals("InvalidParameterValue", exception.getExceptionCode());
            Assertions.assertEquals("outputFormat", exception.getLocator());
            Assertions.assertEquals("Failed to find response for output format application/json11", exception.getExceptionText());
        }
    }

}
