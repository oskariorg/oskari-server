package fi.nls.oskari.spring.extension;

import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.domain.User;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.servlet.WebLocaleResolver;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebArgumentResolver;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Handler for @OskariParam annotation on Spring webmvc RequestMapping methods.
 */
@Component
public class OskariParamMethodArgumentResolver implements
        HandlerMethodArgumentResolver {

    private Logger log = LogFactory.getLogger(OskariParamMethodArgumentResolver.class);
    private WebLocaleResolver localeResolver = new WebLocaleResolver();

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
        // localeResolver validates the locale to supported one, but can result in
        // spring components using different locale than Oskari components.
        // TODO: replace with custom validating spring based localeResolver and use request.getLocale() here.
        params.setLocale(localeResolver.resolveLocale(request, response));
        HttpSession session = request.getSession(false);
        if (session != null) {
            params.setUser((User) session.getAttribute(User.class.getName()));
        }
        return params;
    }
}
