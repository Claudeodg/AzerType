package claudodg.customerapi.controller;

import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.security.Principal;

/**
 * Shows templates/error.html with the right HTTP status when a request fails.
 * <p>
 * Errors that happen outside our controllers (e.g. 403 from Spring Security)
 * are rendered with the same template by Spring Boot's /error endpoint,
 * which also provides the "status" attribute.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /** Unknown URL: 404 "page not found", not a server error. */
    @ExceptionHandler(NoResourceFoundException.class)
    public String handleNotFound(Principal principal, Model model, HttpServletResponse response) {
        return errorPage(HttpStatus.NOT_FOUND, principal, model, response);
    }

    /** Any other error: logged with its stack trace, shown as a generic 500 page. */
    @ExceptionHandler(Exception.class)
    public String handleError(Exception exception, Principal principal, Model model, HttpServletResponse response) {
        log.error("Unhandled error", exception);
        return errorPage(HttpStatus.INTERNAL_SERVER_ERROR, principal, model, response);
    }

    private String errorPage(HttpStatus status, Principal principal, Model model, HttpServletResponse response) {
        response.setStatus(status.value());
        model.addAttribute("status", status.value());
        // @ModelAttribute methods (GlobalModelAttributes) are not applied to exception handlers.
        model.addAttribute("loggedIn", principal != null);
        return "error"; // templates/error.html
    }
}
