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

@Controller
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @GetMapping("/register")
    public String registerPage() {
        return "register";
    }

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

    @GetMapping("/login")
    public String loginPage() {
        return "login"; //  templates/login.html
    }

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
