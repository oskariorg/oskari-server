package fi.nls.oskari.spring;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * A way to get references to spring beans from non-spring initialized code.
 * Notice there are timing issues to consider. Trying to get a bean ref before spring has
 * done it's magic will result in a null value from getBean().
 */
@Component
public class SpringContextHolder implements ApplicationContextAware {
    private static ApplicationContext context;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        context = applicationContext;
    }

    public static <T> T getBean(Class<T> cls) {
        return context.getBean(cls);
    }
}
