package controller;

import entity.User;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import repository.UserRepository;
import java.util.Optional;

@Controller
public class CaptchaController {

    @GetMapping("/captcha")
    public String showCaptchaPage(HttpSession session, Model model) {
        User user = (User) session.getAttribute("authenticatedUser");

        if (user == null) {
            return "redirect:/"; // Redirect if session expired
        }
        model.addAttribute("userRole", user.getRole()); // Pass role to frontend
        return "captcha"; // Load CAPTCHA page
    }

    @PostMapping("/verifyCaptcha")
    public String verifyCaptcha(@RequestParam String captchaResponse, HttpSession session, Model model) {
        // Simulate CAPTCHA validation (Replace with actual CAPTCHA service)
        System.out.println("Verifying CAPTCHA...");
        System.out.println("Session ID: " + session.getId());
        System.out.println("Captcha Response: " + captchaResponse);

        // Retrieve authenticated user from session
        User user = (User) session.getAttribute("authenticatedUser");
        if (user == null) {
            System.out.println("User session lost! Redirecting to login.");
            return "redirect:/"; // Redirect if session expired
        }

        if (!"1234".equals(captchaResponse)) { // Placeholder CAPTCHA logic
            return "redirect:/captcha?error=true";
        }

        String username = user.getUsername();
        switch (user.getRole().toLowerCase()) {
            case "admin":
                return "redirect:/homepageAdmin?username=" + username;
            case "main contractor":
                return "redirect:/homepageGencon?username=" + username;
            case "staff":
                return "redirect:/homepageExestaff?username=" + username;
            default:
                return "redirect:/"; // Redirect to login if role is invalid
        }
    }
}
