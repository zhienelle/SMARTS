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
public class  UserAdminController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @GetMapping("/userAdmin/getUsers")
    @ResponseBody
    public List<User> getAllUsers() {
        return userRepository.findAll(); // Return both ACTIVE and INACTIVE users
    }

    @PostMapping("/userAdmin/addUser")
    @ResponseBody
    public User addUser(@RequestBody User user) {
        return userRepository.save(user);
    }

    @PutMapping("/userAdmin/updateUser/{user_id}")
    @ResponseBody
    public User updateUser(@PathVariable int user_id, @RequestBody User updatedUser) {
        Optional<User> existingUser = userRepository.findById(user_id);
        if (existingUser.isPresent()) {
            User user = existingUser.get();
            user.setUsername(updatedUser.getUsername());
            user.setPassword(updatedUser.getPassword());
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
    public String userAdminPage(Model model) {
        model.addAttribute("users", userRepository.findAll());
        return "userAdmin"; // sure "userAdmin.html" exists
    }

    @PutMapping("/userAdmin/archiveUser/{user_id}")
    @ResponseBody
    public User archiveUser(@PathVariable int user_id) {
        Optional<User> userOptional = userRepository.findById(user_id);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            user.setStatus("INACTIVE"); // Mark as archived
            return userRepository.save(user);
        }
        return null;
    }
}
