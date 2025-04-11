package controller;
import entity.User;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SettingsExestaffController {
    @GetMapping("settingsExestaff")
    public String SettingsExestaff(HttpSession session, Model model) {
        User user = (User) session.getAttribute("authenticatedUser");
        if (user == null) {
            return "redirect:/"; // Redirect to login if session expired
        }

        String role = (String) session.getAttribute("role");
        if (!"staff".equalsIgnoreCase(role)) {
            return "error/error403";
        }

        model.addAttribute("username", user.getUsername());
        return "SettingsExestaff";
    }
}
