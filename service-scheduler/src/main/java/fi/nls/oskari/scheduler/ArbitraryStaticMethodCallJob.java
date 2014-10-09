package fi.nls.oskari.scheduler;

import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
* A configurable Quartz job wrapper for calling an arbitrary static method.
 *
 * Requires JobData entries for "className" and "methodName", which contain the obvious parameters.
*/
public class ArbitraryStaticMethodCallJob implements Job {

    private static final Logger log = LogFactory.getLogger(ArbitraryStaticMethodCallJob.class);

    private static final String CLASS_NAME = "className";

    private static final String METHOD_NAME = "methodName";

    @Override
    public void execute(final JobExecutionContext context) throws JobExecutionException {
        final JobDataMap data = context.getMergedJobDataMap();
        try {
            final Class<?> clazz = Class.forName(data.getString(CLASS_NAME));
            final Method method = clazz.getMethod(data.getString(METHOD_NAME));
            method.invoke(null);
            log.info("calling method", data.getString(CLASS_NAME), data.getString(METHOD_NAME));
        } catch (final IllegalAccessException e) {
            log.error(e, "illegal access in method call");
        } catch (final ClassNotFoundException e) {
            log.error(e, "class not found", data.getString(CLASS_NAME));
        } catch (final NoSuchMethodException e) {
            log.error(e, "no such method", data.getString(CLASS_NAME), data.getString(METHOD_NAME));
        } catch (final InvocationTargetException e) {
            log.error(e, "invocation failed", data.getString(CLASS_NAME), data.getString(METHOD_NAME));
        }
    }
}
