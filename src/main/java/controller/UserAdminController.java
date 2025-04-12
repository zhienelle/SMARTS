package controller;

import entity.Project;
import entity.User;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.*;
import repository.ProjectRepository;
import repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

import java.util.List;
import java.util.Optional;

@Controller
public class UserAdminController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProjectRepository projectRepository;

    // ✅ Utility method: check if project already assigned to another STAFF
    private boolean isProjectAlreadyAssignedToOtherStaff(String projectName, Integer currentUserId) {
        List<User> assignedStaffs = userRepository.findByRoleAndProjectContainingIgnoreCase("STAFF", projectName);
        return assignedStaffs.stream().anyMatch(user -> currentUserId == null || user.getUserId() != currentUserId);
    }

    @GetMapping("/userAdmin/getUsers")
    @ResponseBody
    public List<User> getAllUsers() {
        return userRepository.findAll(); // Return both ACTIVE and INACTIVE users
    }

    @PostMapping("/userAdmin/addUser")
    @ResponseBody
    public User addUser(@RequestBody User user) {
        // Store plain password (or replace with your custom hash if needed)
        String rawPassword = user.getPassword();
        user.setPassword(rawPassword); // ❗Password stored as plain text for simplicity (NO ENCRYPTION)

        if ("STAFF".equalsIgnoreCase(user.getRole()) && user.getProject() != null) {
            if (isProjectAlreadyAssignedToOtherStaff(user.getProject(), null)) {
                throw new RuntimeException("❌ This project is already assigned to another executive staff.");
            }
        }
        return userRepository.save(user);
    }

    @PutMapping("/userAdmin/updateUser/{user_id}")
    @ResponseBody
    public User updateUser(@PathVariable int user_id, @RequestBody User updatedUser) {
        Optional<User> existingUser = userRepository.findById(user_id);
        if (existingUser.isPresent()) {

            if ("STAFF".equalsIgnoreCase(updatedUser.getRole()) && updatedUser.getProject() != null) {
                if (isProjectAlreadyAssignedToOtherStaff(updatedUser.getProject(), user_id)) {
                    throw new RuntimeException("❌ This project is already assigned to another executive staff.");
                }
            }

            User user = existingUser.get();
            user.setUsername(updatedUser.getUsername());

            // If password changed, update it (no encryption)
            if (!user.getPassword().equals(updatedUser.getPassword())) {
                user.setPassword(updatedUser.getPassword());
            }

            user.setRole(updatedUser.getRole());
            user.setPermissions(updatedUser.getPermissions());
            user.setProject(updatedUser.getProject());
            user.setStatus(updatedUser.getStatus());
            user.setFirstName(updatedUser.getFirstName());
            user.setLastName(updatedUser.getLastName());
            user.setPhone(updatedUser.getPhone());
            user.setEmail(updatedUser.getEmail());

            return userRepository.save(user);
        }
        return null;
    }

    @GetMapping("/getCurrentUser")
    @ResponseBody
    public User getCurrentUser(HttpSession session) {
        String username = (String) session.getAttribute("username");
        return userRepository.findByUsername(username).orElse(null);
    }

    @GetMapping("/userAdmin")
    public String userAdminPage(HttpSession session, Model model) {
        User user = (User) session.getAttribute("authenticatedUser");
        if (user == null) {
            return "redirect:/"; // Redirect to login if session expired
        }

        if (!"ADMIN".equalsIgnoreCase(user.getRole())) {
            return "error/error403";
        }

        model.addAttribute("users", userRepository.findAll());
        return "userAdmin";
    }

    @PutMapping("/userAdmin/archiveUser/{user_id}")
    @ResponseBody
    public User archiveUser(@PathVariable int user_id) {
        Optional<User> userOptional = userRepository.findById(user_id);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            user.setStatus("INACTIVE");
            return userRepository.save(user);
        }
        return null;
    }
}
