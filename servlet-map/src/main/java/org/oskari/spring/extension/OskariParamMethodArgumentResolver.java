package org.oskari.spring.extension;

import fi.nls.oskari.control.ActionParameters;
import org.oskari.user.User;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebArgumentResolver;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * Handler for @OskariParam annotation on Spring webmvc RequestMapping methods.
 */
@Component
public class OskariParamMethodArgumentResolver implements
        HandlerMethodArgumentResolver {

    private Logger log = LogFactory.getLogger(OskariParamMethodArgumentResolver.class);

    @Override
    public boolean supportsParameter(MethodParameter methodParameter) {
        if (methodParameter.getParameterAnnotation(OskariParam.class) == null) {
            return false;
        }
        return methodParameter.getParameterType().equals(ActionParameters.class);
    }

    @Override
    public Object resolveArgument(MethodParameter methodParameter,
                                  ModelAndViewContainer mavContainer, NativeWebRequest webRequest,
                                  WebDataBinderFactory binderFactory) throws Exception {
        log.debug("Resolving argument for", methodParameter.getParameterType().getName());
        if (!this.supportsParameter(methodParameter)) {
            return WebArgumentResolver.UNRESOLVED;
        } else if (methodParameter.getParameterType().equals(ActionParameters.class)) {
            return handleActionParameters(methodParameter, mavContainer, webRequest, binderFactory);
        }
        return WebArgumentResolver.UNRESOLVED;
    }

    private Object handleActionParameters(MethodParameter methodParameter,
                                          ModelAndViewContainer mavContainer, NativeWebRequest webRequest,
                                          WebDataBinderFactory binderFactory) {

        log.debug("Resolving ActionParameters");
        HttpServletRequest request = (HttpServletRequest) webRequest.getNativeRequest();
        HttpServletResponse response = (HttpServletResponse) webRequest.getNativeResponse();

        final ActionParameters params = new ActionParameters();
        params.setRequest(request);
        params.setResponse(response);
        params.setLocale(LocaleContextHolder.getLocale());
        HttpSession session = request.getSession(false);
        if (session != null) {
            params.setUser((User) session.getAttribute(User.class.getName()));
        }
        return params;
    }
}
