package controller;

import entity.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;


import java.util.List;
import java.util.Optional;

@Controller
public class LandingPageController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JavaMailSender mailSender; // ✅ Inject mail sender

    @GetMapping("/")
    public String showLoginPage() {
        return "landingPage";
    }

    @PostMapping("/api/sendForgotEmail")
    @ResponseBody
    public ResponseEntity<String> sendForgotPasswordEmail(@RequestParam String email) {
        List<User> users = userRepository.findByEmail(email); // ✅ list now, not optional

        if (users.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }

        try {
            String adminEmail = "marc.tabangay.cics@ust.edu.ph";

            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(adminEmail);
            message.setSubject("Password Reset Request");
            message.setText("A user with email " + email + " has requested a password reset.");

            mailSender.send(message);
            return ResponseEntity.ok("Email sent successfully!");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to send email: " + e.getMessage());
        }
    }

    @PostMapping("/login")
    public String loginUser(@RequestParam String username,
                            @RequestParam String password,
                            HttpSession session) {
        Optional<User> userOpt = userRepository.findByUsername(username);

        if (userOpt.isEmpty()) {
            return "redirect:/invalidUsername";
        }

        User user = userOpt.get();

        if (!user.getPassword().equals(password)) {
            return "redirect:/invalidPassword";
        }

        if ("inactive".equalsIgnoreCase(user.getStatus())) {
            return "redirect:/?inactive=true";
        }

        session.setAttribute("authenticatedUser", user);
        session.setAttribute("username", user.getUsername());
        session.setAttribute("role", user.getRole());

        return "redirect:/captcha";
    }

    @GetMapping("/invalidUsername")
    public String showInvalidUsernamePage() {
        return "invalidUsername"; // redirection
    }

    @GetMapping("/invalidPassword")
    public String showInvalidPasswordPage() {
        return "invalidPassword"; // redirection
    }

    @GetMapping("/confirmLogout")
    public String confirmLogout(HttpSession session, HttpServletResponse response, Model model) {
        User user = (User) session.getAttribute("authenticatedUser");

        // Prevent browser from caching this page
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);

        if (user == null) {
            return "redirect:/"; // Redirect to login
        }

        model.addAttribute("userAuthenticated", true);
        return "confirmLogout";
    }

    @GetMapping("/logout")
    public String logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false); // don't create if it doesn't exist
        if (session != null) {
            session.invalidate(); // properly invalidate the session
        }
        return "redirect:/";
    }
}
