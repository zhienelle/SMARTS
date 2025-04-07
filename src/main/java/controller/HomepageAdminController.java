package controller;

import entity.User;
import jakarta.servlet.http.HttpSession;
import org.springframework.ui.Model;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomepageAdminController {
    @GetMapping("homepageAdmin")
    public String HomepageAdmin(HttpSession session, Model model) {
        User user = (User) session.getAttribute("authenticatedUser");
        if (user == null) {
            return "redirect:/"; // Redirect to login if session expired
        }
        model.addAttribute("username", user.getUsername());
        return "homepageAdmin";
    }
}