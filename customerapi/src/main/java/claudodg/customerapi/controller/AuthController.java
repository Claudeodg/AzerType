package claudodg.customerapi.controller;


import claudodg.customerapi.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;
/**
 * Controller responsible for handling user authentication pages:
 * registration, login page and password reset.
 * <p>
 * Messages are passed to the templates as message keys
 * (see messages.properties / messages_de.properties), never as plain text.
 *
 * @author claudodg
 */
@Controller
@RequestMapping("/auth")
public class AuthController {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
    private static final int MIN_PASSWORD_LENGTH = 8;

    /** Maps the "?error=..." values of the register page to message keys. */
    private static final Map<String, String> REGISTER_ERRORS = Map.of(
            "invalidEmail", "error.invalidEmail",
            "weakPassword", "error.weakPassword",
            "passwordMismatch", "error.passwordMismatch"
    );

    @Autowired
    private UserService userService;

    /**
     * Displays the registration page.
     *
     * @return the name of the register template
     */
    @GetMapping("/register")
    public String registerPage(@RequestParam(required = false) String error, Model model) {
        if (error != null && REGISTER_ERRORS.containsKey(error)) {
            model.addAttribute("error", REGISTER_ERRORS.get(error));
        }
        return "register";
    }

    /**
     * Handles user registration form submission.
     * Validates the email and that the password and confirmation match before registering.
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

        if (email == null || !EMAIL_PATTERN.matcher(email.trim()).matches()) {
            return "redirect:/auth/register?error=invalidEmail";
        }

        if (password == null || password.length() < MIN_PASSWORD_LENGTH) {
            return "redirect:/auth/register?error=weakPassword";
        }

        if (!password.equals(confirmPassword)) {
            return "redirect:/auth/register?error=passwordMismatch";
        }

        userService.register(email.trim(), password);
        return "redirect:/auth/login";
    }

    /**
     * Displays the login page.
     * The form submission (POST /auth/login) and logout (POST /auth/logout)
     * are handled by Spring Security, see {@code SecurityConfig}.
     *
     * @return the name of the login template
     */
    @GetMapping("/login")
    public String loginPage(@RequestParam(required = false) String error,
                            @RequestParam(required = false) String reset,
                            @RequestParam(required = false) String logout,
                            @RequestParam(required = false) String expired,
                            Model model) {
        if (error != null) {
            // Generic message on purpose: distinguishing "unknown email" from
            // "wrong password" lets an attacker enumerate registered accounts.
            model.addAttribute("error", "error.badCredentials");
        }
        if ("success".equals(reset)) {
            model.addAttribute("message", "msg.resetSuccess");
        }
        if (logout != null) {
            model.addAttribute("message", "msg.logout");
        }
        if (expired != null) {
            model.addAttribute("message", "msg.expired");
        }
        return "login"; //  templates/login.html
    }

    /**
     * Displays the "forgot password" request form.
     */
    @GetMapping("/forgot-password")
    public String forgotPasswordPage(@RequestParam(required = false) String sent, Model model) {
        if (sent != null) {
            model.addAttribute("message", "msg.resetSent");
        }
        return "forgot-password";
    }

    /**
     * Handles the "forgot password" form submission. Always redirects with the
     * same generic confirmation, whether or not the email is registered, to
     * avoid leaking which accounts exist. The work runs asynchronously, so the
     * response time and outcome never depend on the email either.
     * The email is written in the language currently chosen by the user.
     */
    @PostMapping("/forgot-password")
    public String forgotPassword(@RequestParam String email, Locale locale) {
        userService.createPasswordResetToken(email.trim(), locale);
        return "redirect:/auth/forgot-password?sent=true";
    }

    /**
     * Displays the "choose a new password" form for a given reset token.
     */
    @GetMapping("/reset-password")
    public String resetPasswordPage(@RequestParam(required = false) String token, Model model) {
        if (token == null || token.isBlank()) {
            model.addAttribute("error", "error.invalidResetLink");
            return "reset-password";
        }
        model.addAttribute("token", token);
        return "reset-password";
    }

    /**
     * Handles the "choose a new password" form submission.
     */
    @PostMapping("/reset-password")
    public String resetPassword(@RequestParam String token,
                                @RequestParam String newPassword,
                                @RequestParam String confirmNewPassword,
                                Model model) {
        if (newPassword == null || newPassword.length() < MIN_PASSWORD_LENGTH) {
            model.addAttribute("token", token);
            model.addAttribute("error", "error.weakPassword");
            return "reset-password";
        }

        if (!newPassword.equals(confirmNewPassword)) {
            model.addAttribute("token", token);
            model.addAttribute("error", "error.passwordMismatch");
            return "reset-password";
        }

        try {
            userService.resetPassword(token, newPassword);
            return "redirect:/auth/login?reset=success";
        } catch (RuntimeException e) {
            model.addAttribute("error", "error.expiredResetLink");
            return "reset-password";
        }
    }
}
