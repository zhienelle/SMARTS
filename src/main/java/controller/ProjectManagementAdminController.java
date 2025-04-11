package controller;

import entity.Inventory;
import entity.ProjectInventory;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import entity.Project;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import repository.InventoryRepository;
import repository.ProjectInventoryRepository;
import repository.ProjectRepository;

import java.util.HashMap;
import java.util.*;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
public class ProjectManagementAdminController {

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private ProjectInventoryRepository projectInventoryRepository;

    @Autowired
    private InventoryRepository inventoryRepository;

    @GetMapping("/projectManagementAdmin")
    public String projectManagementAdmin(@RequestParam("projectId") int projectId, Model model) {
        try {
            Optional<Project> projectOpt = projectRepository.findById(projectId);
            if (projectOpt.isPresent()) {
                Project project = projectOpt.get();

                // Only load fields you want to show in the template
                model.addAttribute("projectname", project.getProjectname());
                model.addAttribute("companyname", project.getCompanyname());
                model.addAttribute("companyLocation", project.getCompanyLocation());
                model.addAttribute("companycontact", project.getCompanycontact());

                return "projectManagementAdmin";
            } else {
                System.out.println("Project not found: " + projectId);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "redirect:/projectsAdmin";
    }

    @GetMapping("/inventory/getByCategory")
    @ResponseBody
    public List<Map<String, Object>> getMaterialsByCategory(@RequestParam String category) {
        List<Inventory> materials = inventoryRepository.findByMaterialCategoryIgnoreCase(category);

        return materials.stream().map(inv -> {
            Map<String, Object> map = new HashMap<>();
            map.put("materialName", inv.getMaterialName());
            map.put("materialStock", inv.getMaterialStock());
            return map;
        }).collect(Collectors.toList());
    }

    @GetMapping("/inventory/categories")
    @ResponseBody
    public List<String> getAllMaterialCategories() {
        return inventoryRepository.findDistinctMaterialCategories();
    }


    @PostMapping("/projectManagementAdmin/addMaterials")
    @ResponseBody
    public ResponseEntity<?> addMaterialsToProject(@RequestBody Map<String, Object> payload) {
        String category = (String) payload.get("category");
        String projectName = (String) payload.get("projectName");
        List<Map<String, Object>> materials = (List<Map<String, Object>>) payload.get("materials");

        Project project = projectRepository.findByProjectname(projectName);
        if (project == null) return ResponseEntity.badRequest().body("Project not found");

        for (Map<String, Object> mat : materials) {
            String materialName = (String) mat.get("materialName");
            int quantity = (int) mat.get("quantity");

            Inventory inventory = inventoryRepository.findByMaterialName(materialName);
            if (inventory == null) continue;

            // Check stock
            if (inventory.getMaterialStock() < quantity) {
                return ResponseEntity.badRequest().body("Insufficient stock for " + materialName);
            }

            // Deduct stock
            inventory.setMaterialStock(inventory.getMaterialStock() - quantity);
            inventoryRepository.save(inventory);

            // Create or update ProjectInventory
            ProjectInventory existing = projectInventoryRepository.findByProjectAndInventory(project, inventory);
            if (existing != null) {
                existing.setQuantityAssigned(existing.getQuantityAssigned() + quantity);
                existing.setTotalPrice(existing.getQuantityAssigned() * inventory.getMaterialPrice());
                projectInventoryRepository.save(existing);
            } else {
                ProjectInventory pi = new ProjectInventory();
                pi.setProject(project);
                pi.setInventory(inventory);
                pi.setQuantityAssigned(quantity);
                pi.setMaterialPrice(inventory.getMaterialPrice());
                pi.setTotalPrice(quantity * inventory.getMaterialPrice());
                projectInventoryRepository.save(pi);
            }
        }

        return ResponseEntity.ok("Materials added and inventory updated");
    }


    @PostMapping("/projectManagementAdmin/removeMaterial")
    @ResponseBody
    public ResponseEntity<?> removeMaterial(@RequestBody Map<String, String> body) {
        String projectName = body.get("projectName");
        String materialName = body.get("materialName");
        int quantity = Integer.parseInt(body.get("quantity"));

        Project project = projectRepository.findByProjectname(projectName);
        if (project == null) return ResponseEntity.badRequest().body("Project not found");

        // Find Inventory
        Inventory inventory = inventoryRepository.findByMaterialName(materialName);
        if (inventory == null) return ResponseEntity.badRequest().body("Inventory item not found");

        // Find ProjectInventory entry
        ProjectInventory projectInventory = projectInventoryRepository.findByProjectAndInventory(project, inventory);
        if (projectInventory == null) return ResponseEntity.badRequest().body("Material not found in this project");

        // 1. Return quantity back to stock
        inventory.setMaterialStock(inventory.getMaterialStock() + quantity);
        inventoryRepository.save(inventory);

        // 2. Delete project-material link
        projectInventoryRepository.delete(projectInventory); // ✅ This removes the row from the project only

        return ResponseEntity.ok("Material unassigned from project and stock updated");
    }



    @GetMapping("/projectManagementAdmin/data")
    @ResponseBody
    public ResponseEntity<?> getProjectData(@RequestParam("projectId") int projectId) {
        Optional<Project> projectOpt = projectRepository.findById(projectId);
        if (projectOpt.isEmpty()) return ResponseEntity.badRequest().body("Not found");

        Project project = projectOpt.get();

        // Build only the fields needed
        Map<String, Object> projectInfo = new HashMap<>();
        projectInfo.put("projectname", project.getProjectname());
        projectInfo.put("companyname", project.getCompanyname());
        projectInfo.put("companyLocation", project.getCompanyLocation());
        projectInfo.put("companycontact", project.getCompanycontact());

        // Build materials list from project_inventory
        List<Map<String, Object>> materialList = new ArrayList<>();
        if (project.getProjectInventoryList() != null) {
            project.getProjectInventoryList().forEach(pi -> {
                Map<String, Object> materialData = new HashMap<>();
                materialData.put("materialName", pi.getInventory() != null ? pi.getInventory().getMaterialName() : "N/A");
                materialData.put("materialPrice", pi.getInventory() != null ? pi.getInventory().getMaterialPrice() : 0);
                materialData.put("quantityAssigned", pi.getQuantityAssigned());
                materialData.put("totalPrice", pi.getTotalPrice());
             // materialData.put("archived", pi.isArchived()); // optional if you want to use show/hide archived
                materialList.add(materialData);
            });
        }

        Map<String, Object> response = new HashMap<>();
        response.put("project", projectInfo);
        response.put("materials", materialList);

        return ResponseEntity.ok(response);
    }

}
