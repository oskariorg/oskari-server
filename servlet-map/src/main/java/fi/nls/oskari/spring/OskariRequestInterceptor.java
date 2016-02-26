package fi.nls.oskari.spring;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
/**
 * Created by SMAKINEN on 15.2.2016.
 */
public class OskariRequestInterceptor implements HandlerInterceptor {
    @Override
    public void afterCompletion(HttpServletRequest request,
                                HttpServletResponse response, Object arg2, Exception ex)
            throws Exception {
        // NOOP
    }

    @Override
    public void postHandle(HttpServletRequest arg0,
                           HttpServletResponse response, Object handler,
                           ModelAndView modelAndView) throws Exception {
        // NOOP
    }

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response, Object handler) throws Exception {

        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        return true;
    }

}
