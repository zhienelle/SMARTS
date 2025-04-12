package controller;

import entity.User;
import jakarta.servlet.http.HttpSession;
import org.springframework.ui.Model;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomepageAdminController {

    @GetMapping("/homepageAdmin")
    public String HomepageAdmin(HttpSession session, Model model) {
        // ✅ Check user is logged in
        User user = (User) session.getAttribute("authenticatedUser");
        if (user == null || !"ADMIN".equalsIgnoreCase(user.getRole())) {
            return "error/error403";
        }

        // ✅ Check CAPTCHA was passed
        Boolean captchaPassed = (Boolean) session.getAttribute("captchaPassed");
        if (captchaPassed == null || !captchaPassed) {
            return "redirect:/?captchaRequired=true";
        }

        // ✅ Role check — use user.getRole() instead of a separate session attribute
        if (!"ADMIN".equalsIgnoreCase(user.getRole())) {
            return "error/error403";
        }

        model.addAttribute("username", user.getUsername());
        return "homepageAdmin"; // HTML: templates/homepageAdmin.html
    }
}
