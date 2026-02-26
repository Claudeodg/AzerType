package claudodg.customerapi.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {

    @GetMapping("/")
    public String home() {
        return "redirect:/auth/register";
    }

    @GetMapping("/azerType")
    public String azertype(HttpSession session) {
        if (session.getAttribute("user") == null) {
            return "redirect:/auth/login";
        }
        return "azerType";
    }
}
