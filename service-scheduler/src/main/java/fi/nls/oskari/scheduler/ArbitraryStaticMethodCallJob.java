package fi.nls.oskari.scheduler;

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
    @Override
    public void execute(final JobExecutionContext context) throws JobExecutionException {
        try {
            final JobDataMap data = context.getMergedJobDataMap();
            final Class<?> clazz = Class.forName(data.getString("className"));
            final Method method = clazz.getMethod(data.getString("methodName"));
            method.invoke(null);
        } catch (IllegalAccessException e) {
        } catch (ClassNotFoundException e) {
        } catch (NoSuchMethodException e) {
        } catch (InvocationTargetException e) {
        }
    }
}
