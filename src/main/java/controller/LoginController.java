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
        if (!user.getPassword().equals(password)) return "invalidPassword";

        // Store in session
        session.setAttribute("authenticatedUser", user);
        session.setAttribute("captchaPassed", true); // ✅ Add this once CAPTCHA is verified
        session.setAttribute("username", user.getUsername());

        return "ROLE:" + user.getRole() + "|STATUS:" + user.getStatus(); // used by frontend to redirect
    }
}
