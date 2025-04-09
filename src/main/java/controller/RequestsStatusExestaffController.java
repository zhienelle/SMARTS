package controller;

import entity.MaterialRequest;
import entity.User;
import repository.MaterialRequestRepository;
import repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
public class RequestsStatusExestaffController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MaterialRequestRepository materialRequestRepository;

    // ✅ Serve the HTML page without using Principal, using session instead
    @GetMapping("/requestsStatusExestaff")
    public String RequestsStatusExestaff(HttpSession session, Model model) {
        User currentUser = (User) session.getAttribute("authenticatedUser");
        if (currentUser == null) return "redirect:/";

        model.addAttribute("username", currentUser.getUsername());
        return "requestsStatusExestaff";
    }

    // ✅ Fetch material requests for user's assigned projects
    @GetMapping("/exestaff/requests/getMaterialRequests")
    @ResponseBody
    public List<MaterialRequest> getMaterialRequests(HttpSession session) {
        User user = (User) session.getAttribute("authenticatedUser");

        if (user != null && user.getProject() != null && !user.getProject().isEmpty()) {
            List<String> assignedProjects = List.of(user.getProject().split(","))
                    .stream()
                    .map(String::trim)
                    .collect(Collectors.toList());

            List<MaterialRequest> allRequests = materialRequestRepository.findAll().stream()
                    .filter(req -> assignedProjects.contains(req.getProject().getProjectname()))
                    .collect(Collectors.toList());

            // ❗ Explicitly access nested fields while session is open
            allRequests.forEach(req -> {
                if (req.getInventory() != null) {
                    req.getInventory().getMaterialCategory(); // triggers lazy load
                    req.getInventory().getMaterialName();     // triggers lazy load
                }
            });

            return allRequests;
        }
        return List.of();
    }

    @GetMapping("/exestaff/requests/projects")
    @ResponseBody
    public List<String> getAssignedProjects(HttpSession session) {
        User user = (User) session.getAttribute("authenticatedUser");
        if (user != null && user.getProject() != null && !user.getProject().isEmpty()) {
            return List.of(user.getProject().split(",\\s*"));
        }
        return List.of();
    }



    // ✅ Direct view rendering route (for fallback or manual navigation)
    @GetMapping("requestsStatusExestaffDirect")
    public String RequestsStatusExestaffDirect() {
        return "requestsStatusExestaff";
    }
}
