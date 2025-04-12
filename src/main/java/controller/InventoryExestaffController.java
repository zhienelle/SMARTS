package controller;
//FIXED BY JED
import entity.Inventory;
import entity.MaterialRequest;
import entity.Project;
import entity.User;
import repository.InventoryRepository;
import repository.MaterialRequestRepository;
import repository.ProjectRepository;
import repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import jakarta.servlet.http.HttpSession; // ✅ Changed: Added for session handling

@Controller
public class InventoryExestaffController {

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private MaterialRequestRepository materialRequestRepository;

    // ✅ CHANGED: Replaced Principal with HttpSession to match your session-based login system
    @GetMapping("/exestaff/inventory")
    public String loadExeStaffInventory(HttpSession session, Model model) {
        User currentUser = (User) session.getAttribute("authenticatedUser"); // ✅ CHANGED: get user from session

        if (currentUser == null) {
            return "redirect:/"; // or "error"
        }

        if (!"STAFF".equalsIgnoreCase(currentUser.getRole())) {
            return "error/error403";
        }

        String username = currentUser.getUsername(); // ✅ CHANGED: retrieve username from User object
        System.out.println("this is the username:" + username);

        if ("EXESTAFF".equalsIgnoreCase(currentUser.getRole())) {
            List<Inventory> unarchivedInventory = inventoryRepository.findAll().stream()
                    .filter(item -> item.getMaterialArchived() == null || !item.getMaterialArchived())
                    .collect(Collectors.toList());

            List<Project> assignedProjects = List.of();
            System.out.println("this is the exestaff project:" + currentUser.getProject());
            if (currentUser.getProject() != null && !currentUser.getProject().isEmpty()) {
                assignedProjects = List.of(currentUser.getProject().split(",")).stream()
                        .map(String::trim)
                        .map(projectRepository::findByProjectname)
                        .filter(p -> p != null)
                        .collect(Collectors.toList());
            }

            model.addAttribute("inventory", unarchivedInventory);
            model.addAttribute("projects", assignedProjects);
            model.addAttribute("username", username);
            return "inventoryExestaff";
        }

        return "error";
    }

    // ✅ UNCHANGED
    @GetMapping("/exestaff/inventory/getInventory")
    @ResponseBody
    public List<Inventory> getInventory() {
        return inventoryRepository.findAll().stream()
                .filter(item -> item.getMaterialArchived() == null || !item.getMaterialArchived())
                .collect(Collectors.toList());
    }

    // ✅ UNCHANGED
    // ✅ Submit material request
    @PostMapping("/exestaff/inventory/requestMaterial")
    @ResponseBody
    public String submitMaterialRequest(@RequestParam("projectName") String projectName,
                                        @RequestParam("category") String category,
                                        @RequestParam("material") String material,
                                        @RequestParam("quantity") Integer quantity,
                                        HttpSession session) {
        System.out.println("Received request: projectName=" + projectName + ", category=" + category + ", material=" + material + ", quantity=" + quantity);

        User user = (User) session.getAttribute("authenticatedUser");
        if (user == null) {
            return "❌ Not authenticated.";
        }

        if (quantity == null || quantity <= 0) {
            return "❌ Quantity must be atleast 1.";
        }

        Inventory inventory = inventoryRepository.findByMaterialCategoryIgnoreCaseAndMaterialNameIgnoreCase(
                category.trim(), material.trim());

        Project project = projectRepository.findByProjectname(projectName.trim());

        if (inventory == null || project == null) {
            return "❌ Invalid material or project.";
        }

        if (quantity > inventory.getMaterialStock()) {
            return "❌ Not enough stock available.";
        }

        MaterialRequest request = new MaterialRequest();
        request.setProject(project);
        request.setInventory(inventory);
        request.setMaterialName(material);
        request.setMaterialCategory(category);
        request.setMaterialStock(quantity);
        request.setMaterialRequestStatus("PENDING");
        request.setUser(user);

        materialRequestRepository.save(request);
        return "✅ Request submitted successfully!";
    }





    // ✅ UNCHANGED
    @GetMapping("inventoryExestaff")
    public String InventoryExestaff() {
        return "inventoryExestaff";
    }

    // ✅ CHANGED: get username from session instead of Principal
    @GetMapping("/exestaff/inventory/getProjects")
    @ResponseBody
    public List<String> getAssignedProjects(HttpSession session) {
        User user = (User) session.getAttribute("authenticatedUser"); // ✅ CHANGED

        if (user != null && user.getProject() != null && !user.getProject().isEmpty()) {
            return List.of(user.getProject().split(","))
                    .stream()
                    .map(String::trim)
                    .collect(Collectors.toList());
        }

        return List.of(); // Return empty if not found or no projects
    }
}
