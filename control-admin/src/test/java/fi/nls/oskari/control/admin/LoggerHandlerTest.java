package fi.nls.oskari.control.admin;

import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.*;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;

public class LoggerHandlerTest {

    @Test
    public void testClusterMsgDelete() {
        String loggerName = "testing";
        LoggerHandler handler = spy(LoggerHandler.class);
        // when getting remove msg
        handler.handleClusterMsg(LoggerHandler.CLUSTER_CMD_REMOVE_PREFIX + loggerName);
        // removeLogger with the logger name is called
        Mockito.verify(handler).removeLogger(loggerName);
    }

    @Test
    public void testClusterMsgSetLevel() {
        String loggerName = "testing";
        String level = "warn";
        LoggerHandler handler = spy(LoggerHandler.class);
        // when getting set level msg
        handler.handleClusterMsg(LoggerHandler.CLUSTER_CMD_SET_PREFIX + loggerName + LoggerHandler.CLUSTER_CMD_SEPARATOR + level);
        // setLevel with the correctly parsed values is called
        Mockito.verify(handler).setLevel(loggerName, level);
    }
}