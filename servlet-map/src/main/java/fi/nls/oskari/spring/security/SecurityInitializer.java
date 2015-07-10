package fi.nls.oskari.spring.security;

import org.springframework.security.web.context.AbstractSecurityWebApplicationInitializer;

/**
 * This does nothing but having a class that extends
 * AbstractSecurityWebApplicationInitializer will load the springSecurityFilterChain automatically.
 *
 * The equivalent of Spring Security in web.xml file :

 <filter>
 <filter-name>springSecurityFilterChain</filter-name>
 <filter-class>org.springframework.web.filter.DelegatingFilterProxy
 </filter-class>
 </filter>

 <filter-mapping>
 <filter-name>springSecurityFilterChain</filter-name>
 <url-pattern>/*</url-pattern>
 </filter-mapping>

 * http://www.mkyong.com/spring-security/spring-security-hello-world-annotation-example/
 */
public class SecurityInitializer extends AbstractSecurityWebApplicationInitializer {
        //do nothing
}
