package controller;

import entity.Inventory;
import entity.ProjectInventory;
import entity.ProjectStage;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import entity.Project;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import repository.InventoryRepository;
import repository.ProjectInventoryRepository;
import repository.ProjectRepository;
import repository.ProjectStageRepository;

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

    @Autowired
    private ProjectStageRepository projectStageRepository;


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
        Integer stageId = (payload.get("stageId") instanceof Integer)
                ? (Integer) payload.get("stageId")
                : Integer.parseInt(payload.get("stageId").toString());

        Optional<ProjectStage> stageOpt = projectStageRepository.findById(stageId);
        if (stageOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("Invalid stage selected");
        }
        ProjectStage stage = stageOpt.get();


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

// Update stock status
            int newStock = inventory.getMaterialStock();
            if (newStock <= 30) {
                inventory.setMaterialStockStatus("LOW");
            } else if (newStock <= 100) {
                inventory.setMaterialStockStatus("MODERATE");
            } else {
                inventory.setMaterialStockStatus("HIGH");
            }

// Save inventory
            inventoryRepository.save(inventory);

            // Create or update ProjectInventory
            ProjectInventory existing = projectInventoryRepository.findByProjectAndInventoryAndStage(project, inventory, stage);

            if (existing != null) {
                existing.setQuantityAssigned(existing.getQuantityAssigned() + quantity);
                existing.setTotalPrice(existing.getQuantityAssigned() * inventory.getMaterialPrice());
                existing.setStage(stage);
                projectInventoryRepository.save(existing);
            } else {
                ProjectInventory pi = new ProjectInventory();
                pi.setProject(project);
                pi.setInventory(inventory);
                pi.setQuantityAssigned(quantity);
                pi.setMaterialPrice(inventory.getMaterialPrice());
                pi.setTotalPrice(quantity * inventory.getMaterialPrice());
                pi.setStage(stage); // ✅ ADD THIS
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

        Inventory inventory = inventoryRepository.findByMaterialName(materialName);
        if (inventory == null) return ResponseEntity.badRequest().body("Inventory item not found");

        ProjectInventory projectInventory = projectInventoryRepository.findByProjectAndInventory(project, inventory);
        if (projectInventory == null) return ResponseEntity.badRequest().body("Material not found in this project");

        // ✅ Return quantity to stock
        int newStock = inventory.getMaterialStock() + quantity;
        inventory.setMaterialStock(newStock);

        // ✅ Update stock status
        if (newStock <= 30) {
            inventory.setMaterialStockStatus("LOW");
        } else if (newStock <= 100) {
            inventory.setMaterialStockStatus("MODERATE");
        } else {
            inventory.setMaterialStockStatus("HIGH");
        }

        // ✅ Save inventory changes
        inventoryRepository.save(inventory);

        // ✅ Remove material from the project
        projectInventoryRepository.delete(projectInventory);

        return ResponseEntity.ok("Material unassigned from project, stock and status updated.");
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
                materialData.put("totalPrice", pi.getQuantityAssigned() * pi.getInventory().getMaterialPrice());


// ✅ NEW: Include stage info
                if (pi.getStage() != null) {
                    materialData.put("stageId", pi.getStage().getStageId());
                    materialData.put("stageName", "Stage " + pi.getStage().getStageNumber());
                } else {
                    materialData.put("stageId", -1);
                    materialData.put("stageName", "Unassigned");
                }

                // materialData.put("archived", pi.isArchived()); // optional if you want to use show/hide archived
                materialList.add(materialData);
            });
        }

        Map<String, Object> response = new HashMap<>();
        response.put("project", projectInfo);
        response.put("materials", materialList);

        return ResponseEntity.ok(response);
    }

//    @GetMapping("/projectManagementAdmin/stages")
//    @ResponseBody
//    public List<Map<String, Object>> getStagesByProject(@RequestParam("projectId") int projectId) {
//        Optional<Project> projectOpt = projectRepository.findById(projectId);
//        if (projectOpt.isEmpty()) return Collections.emptyList();
//
//        List<ProjectStage> stages = projectStageRepository.findByProject(projectOpt.get());
//
//        return stages.stream().map(stage -> {
//            Map<String, Object> map = new HashMap<>();
//            map.put("stageId", stage.getStageId());
//            map.put("stageName", "Stage " + stage.getStageNumber());
//            return map;
//        }).collect(Collectors.toList());
//    }

    @GetMapping("/projectManagementAdmin/stages")
    @ResponseBody
    public ResponseEntity<?> getStagesByProjectId(@RequestParam("projectId") int projectId) {
        Optional<Project> projectOpt = projectRepository.findById(projectId);
        if (projectOpt.isEmpty()) return ResponseEntity.badRequest().body("Project not found");

        Project project = projectOpt.get();

        List<ProjectStage> stages = projectStageRepository.findByProject(project);
        List<Map<String, Object>> response = new ArrayList<>();

        for (ProjectStage stage : stages) {
            Map<String, Object> stageInfo = new HashMap<>();
            stageInfo.put("stageId", stage.getStageId());
            stageInfo.put("stageName", "Stage " + stage.getStageNumber());
            response.add(stageInfo);
        }

        System.out.println("✅ Returning " + response.size() + " stages for project " + projectId); // For debugging

        return ResponseEntity.ok(response);
    }


}
