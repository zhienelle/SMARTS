package controller;

import entity.User;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomepageExestaffController {
    @GetMapping("homepageExestaff")
    public String HomepageExestaff(HttpSession session, Model model) {
        User user = (User) session.getAttribute("authenticatedUser");
        if (user == null) {
            return "redirect:/";
        }

        // ✅ Check CAPTCHA was passed
        Boolean captchaPassed = (Boolean) session.getAttribute("captchaPassed");
        if (captchaPassed == null || !captchaPassed) {
            return "redirect:/?captchaRequired=true";
        }

        // ✅ Role check — use user.getRole() instead of a separate session attribute
        if (!"STAFF".equalsIgnoreCase(user.getRole())) {
            return "error/error403";
        }


        model.addAttribute("username", user.getUsername());
        return "homepageExestaff";
    }
}
