package controller;

import entity.User;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import repository.UserRepository;

import java.util.Optional;

@RestController
public class LoginController {

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/loginCustom")
    public String login(@RequestParam String username,
                        @RequestParam String password,
                        HttpSession session) {

        Optional<User> userOptional = userRepository.findByUsername(username);

        if (userOptional.isEmpty()) return "invalidUsername";

        User user = userOptional.get();

        // Plaintext password check (no Spring Security)
        if (user.getPassword() == null || !user.getPassword().equals(password)) {
            return "invalidPassword";
        }
        System.out.println("Logging in: " + username);
        System.out.println("Fetched user: " + user);
        System.out.println("Role: " + user.getRole());
        System.out.println("Status: " + user.getStatus());

        // Store in session
        session.setAttribute("authenticatedUser", user);
        session.setAttribute("captchaPassed", true); // ✅ Add this once CAPTCHA is verified
        session.setAttribute("username", user.getUsername());

        return "ROLE:" + (user.getRole() != null ? user.getRole() : "UNKNOWN") +
                "|STATUS:" + (user.getStatus() != null ? user.getStatus() : "UNKNOWN");
    }
}
