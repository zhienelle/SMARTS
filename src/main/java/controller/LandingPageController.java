package controller;

import entity.User;
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
            return "redirect:/invalidUsername"; // Redirect if username is incorrect
        }

        User user = userOpt.get();

        if (!user.getPassword().equals(password)) {
            return "redirect:/invalidPassword"; // Redirect if password is incorrect
        }

        // Store everything needed
        session.setAttribute("authenticatedUser", user);  // for CAPTCHA (if still used)
        session.setAttribute("username", user.getUsername()); // Needed by getCurrentUser
        session.setAttribute("role", user.getRole()); // For conditional redirect or display

        // Redirect to CAPTCHA verification page
        return "redirect:/captcha";
    }

    @GetMapping("/projectManagementAdmin")
    public String projectManagementAdminPage() {
        return "projectManagementAdmin"; // redirection
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
    public String ConfirmLogout(){
            return "confirmLogout";
        }
}