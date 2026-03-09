package claudodg.customerapi.controller;


import claudodg.customerapi.entity.User;
import claudodg.customerapi.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
/**
 * Controller responsible for handling user authentication.
 * Manages registration and login operations.
 *
 * @author claudodg
 */
@Controller
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    /**
     * Displays the registration page.
     *
     * @return the name of the register template
     */
    @GetMapping("/register")
    public String registerPage() {
        return "register";
    }

    /**
     * Handles user registration form submission.
     * Validates that the password and confirmation match before registering.
     *
     * @param email           the user's email address
     * @param password        the chosen password
     * @param confirmPassword the password confirmation
     * @return redirect to login page on success, or back to register page with error
     */
    @PostMapping("/register")
    public String register(@RequestParam String email,
                           @RequestParam String password,
                           @RequestParam String confirmPassword) {

        if (!password.equals(confirmPassword)) {
            return "redirect:/auth/register?error=passwordMismatch";
        }

        userService.register(email, password);
        return "redirect:/login";
    }
    /**
     * Displays the login page.
     *
     * @return the name of the login template
     */
    @GetMapping("/login")
    public String loginPage() {
        return "login"; //  templates/login.html
    }


    /**
     * Handles user login form submission.
     * On success, stores the authenticated user in the HTTP session.
     * On failure, redirects back to the login page with an error message.
     *
     * @param email    the user's email address
     * @param password the user's password
     * @param session  the current HTTP session
     * @return redirect to the game page on success, or back to login page with error
     * @throws RuntimeException if the credentials are invalid
     */
    @PostMapping("/login")
    public String login(@RequestParam String email,
                        @RequestParam String password,
                        HttpSession session) {
        try {
            User user = userService.login(email, password);
            session.setAttribute("user", user);
            return "redirect:/azerType";
        } catch (RuntimeException e) {
            return "redirect:/auth/login?error=" + e.getMessage();
        }
    }
}
