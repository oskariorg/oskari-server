package fi.nls.oskari.spring;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.NoHandlerFoundException;

@Controller
public class GlobalExceptionHandlerController {
    /**
     * Catch all that didn't match
     * */
/*
    This prevents resources configured in SpringConfig.addResourceHandlers() to be called.
    We should find another way to do this.
    @GetMapping("/**")
    public String handle(HttpServletResponse resp) {
        resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
        return "error/404";
    }
*/
    @ExceptionHandler(NoHandlerFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handle(NoHandlerFoundException ex) {
        return "error/404";
    }
}
