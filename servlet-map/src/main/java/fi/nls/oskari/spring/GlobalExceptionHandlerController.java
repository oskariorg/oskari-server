package fi.nls.oskari.spring;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.NoHandlerFoundException;

import javax.servlet.http.HttpServletResponse;

@Controller
public class GlobalExceptionHandlerController {
    /**
     * Catch all that didn't match
     * */
    @GetMapping("/**")
    @RequestMapping(produces = MediaType.TEXT_HTML_VALUE)
    public String handle(HttpServletResponse resp) {
        resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
        return "error/404";
    }
    @ExceptionHandler(NoHandlerFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handle(NoHandlerFoundException ex) {
        return "error/404";
    }
}
