package controller;

import entity.User;
import jakarta.servlet.http.HttpSession;
import repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
public class LoginController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/loginCustom")
    @ResponseBody
    public String login(@RequestParam String username, @RequestParam String password, HttpSession session) {
        Optional<User> userOptional = userRepository.findByUsername(username);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            String storedPassword = user.getPassword();

            boolean matchesEncrypted = passwordEncoder.matches(password, storedPassword);
            boolean matchesPlain = password.equals(storedPassword); // fallback for legacy passwords

            if (matchesEncrypted || matchesPlain) {
                session.setAttribute("authenticatedUser", user);
                return "ROLE:" + user.getRole();
            } else {
                return "invalidPassword";
            }
        } else {
            return "invalidUsername";
        }
    }

}
