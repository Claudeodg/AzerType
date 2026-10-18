package claudodg.customerapi.controller;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.security.Principal;

/**
 * Model attributes available in every Thymeleaf template.
 */
@ControllerAdvice
public class GlobalModelAttributes {

    /** Used by the top bar to show "Log in / Sign up" or "Play / Log out". */
    @ModelAttribute("loggedIn")
    public boolean loggedIn(Principal principal) {
        return principal != null;
    }
}
