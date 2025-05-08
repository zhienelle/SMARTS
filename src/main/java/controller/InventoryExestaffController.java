package controller;
//FIXED BY JED

import entity.*;
import repository.InventoryRepository;
import repository.MaterialRequestRepository;
import repository.ProjectRepository;
import repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;

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

        if ("STAFF".equalsIgnoreCase(currentUser.getRole())) {
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
    @PostMapping("/exestaff/inventory/requestMaterials")
    @ResponseBody
    public String submitMultipleMaterialRequests(@RequestBody Map<String, Object> payload, HttpSession session) {
        User user = (User) session.getAttribute("authenticatedUser");
        if (user == null) return "❌ Not authenticated.";

        String projectName = (String) payload.get("projectName");
        Integer stageId = Integer.parseInt(payload.get("stageId").toString());  // ✅ you're already extracting this
        List<Map<String, Object>> materials = (List<Map<String, Object>>) payload.get("materials");

        if (projectName == null || materials == null || materials.isEmpty()) {
            return "❌ Missing project name or material list.";
        }

        Project project = projectRepository.findByProjectname(projectName.trim());
        if (project == null) return "❌ Project not found.";

        Optional<ProjectStage> stageOpt = project.getStages()
                .stream()
                .filter(s -> s.getStageId() == stageId)
                .findFirst();

        if (stageOpt.isEmpty()) return "❌ Stage not found for this project.";

        ProjectStage stage = stageOpt.get();

        for (Map<String, Object> item : materials) {
            String name = (String) item.get("materialName");
            String category = (String) item.get("category");
            Integer quantity = Integer.parseInt(item.get("quantity").toString());

            if (quantity == null || quantity <= 0) return "❌ Quantity must be valid for all materials.";

            Inventory inventory = inventoryRepository.findByMaterialCategoryIgnoreCaseAndMaterialNameIgnoreCase(
                    category.trim(), name.trim());

            if (inventory == null) return "❌ Material not found: " + name;

            if (quantity > inventory.getMaterialStock()) {
                return "❌ Not enough stock for " + name;
            }

            MaterialRequest request = new MaterialRequest();
            request.setUser(user);
            request.setProject(project);
            request.setStage(stage); // ✅ SET STAGE HERE
            request.setInventory(inventory);
            request.setMaterialName(name);
            request.setMaterialCategory(category);
            request.setMaterialStock(quantity);
            request.setMaterialRequestStatus("PENDING");

            materialRequestRepository.save(request);
        }

        return "✅ Material request submitted successfully!";
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

    @GetMapping("/exestaff/inventory/getStagesByProject")
    @ResponseBody
    public List<Map<String, Object>> getStagesByProject(@RequestParam String projectName) {
        Project project = projectRepository.findByProjectname(projectName.trim());
        if (project == null) return new ArrayList<>();

        List<ProjectStage> stages = project.getStages(); // assuming you added getStages() in Project.java
        List<Map<String, Object>> result = new ArrayList<>();

        for (ProjectStage stage : stages) {
            Map<String, Object> stageInfo = new HashMap<>();
            stageInfo.put("stageId", stage.getStageId());
            stageInfo.put("stageName", "Stage " + stage.getStageNumber());
            stageInfo.put("stageNumber", stage.getStageNumber()); // <— ADD THIS
            result.add(stageInfo);
        }

        return result;
    }

}
