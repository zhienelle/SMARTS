package controller;

import entity.Inventory;
import entity.MaterialRequest;
import entity.Project;
import entity.ProjectInventory;
import repository.InventoryRepository;
import repository.MaterialRequestRepository;
import repository.ProjectInventoryRepository;
import repository.ProjectRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
public class RequestsAdminController {

    @Autowired
    private MaterialRequestRepository materialRequestRepository;

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private ProjectInventoryRepository projectInventoryRepository;

    @GetMapping("requestsAdmin")
    public String RequestAdmin() {
        return "requestsAdmin";
    }

    // ✅ Fetch all material requests
    @GetMapping("/admin/requests/getAll")
    @ResponseBody
    public List<MaterialRequest> getAllRequests() {
        List<MaterialRequest> pending = materialRequestRepository.findByMaterialRequestStatus("PENDING");

        // Trigger lazy-loaded fields
        pending.forEach(req -> {
            if (req.getInventory() != null) {
                req.getInventory().getMaterialName();
                req.getInventory().getMaterialCategory();
            }
            if (req.getProject() != null) {
                req.getProject().getProjectname();
            }
        });

        return pending;
    }

    @PostMapping("/admin/requests/approve/{id}")
    @ResponseBody
    public String approveRequest(@PathVariable Long id) {
        return updateRequestStatus(id, "APPROVED");
    }

    @PostMapping("/admin/requests/deny/{id}")
    @ResponseBody
    public String denyRequest(@PathVariable Long id) {
        return updateRequestStatus(id, "DENIED");
    }

    // ✅ Shared logic for approving/denying
    private String updateRequestStatus(Long requestId, String action) {
        Optional<MaterialRequest> optional = materialRequestRepository.findById(requestId);
        if (optional.isEmpty()) return "❌ Request not found.";

        MaterialRequest request = optional.get();
        request.setMaterialRequestStatus(action);

        if ("APPROVED".equalsIgnoreCase(action)) {
            Inventory inventory = request.getInventory();
            Project project = request.getProject();
            int quantity = request.getMaterialStock();

            if (inventory.getMaterialStock() < quantity) {
                return "❌ Not enough stock to approve.";
            }

            // Deduct and update stock
            inventory.setMaterialStock(inventory.getMaterialStock() - quantity);

            // ✅ Update stock status
            int newStock = inventory.getMaterialStock();
            if (newStock <= 30) {
                inventory.setMaterialStockStatus("LOW");
            } else if (newStock <= 100) {
                inventory.setMaterialStockStatus("MODERATE");
            } else {
                inventory.setMaterialStockStatus("HIGH");
            }

            inventoryRepository.save(inventory);

            // ✅ Insert/update project_inventory
            ProjectInventory existing = projectInventoryRepository.findByProjectAndInventory(project, inventory);
            if (existing != null) {
                existing.setQuantityAssigned(existing.getQuantityAssigned() + quantity);
                existing.setTotalPrice(existing.getMaterialPrice() * existing.getQuantityAssigned());
                projectInventoryRepository.save(existing);
            } else {
                ProjectInventory newEntry = new ProjectInventory();
                newEntry.setProject(project);
                newEntry.setInventory(inventory);
                newEntry.setQuantityAssigned(quantity);
                newEntry.setQuantityUsed(0);
                newEntry.setMaterialPrice(inventory.getMaterialPrice());
                newEntry.setTotalPrice(inventory.getMaterialPrice() * quantity);
                projectInventoryRepository.save(newEntry);
            }
        }

        materialRequestRepository.save(request);
        return "✅ Request status updated.";
    }

    @PostMapping("/admin/requests/bulkUpdate")
    @ResponseBody
    public String bulkUpdateRequestStatus(@RequestParam List<Long> requestIds,
                                          @RequestParam String action) {
        for (Long requestId : requestIds) {
            Optional<MaterialRequest> optional = materialRequestRepository.findById(requestId);
            if (optional.isEmpty()) continue;

            MaterialRequest request = optional.get();
            request.setMaterialRequestStatus(action);

            if ("APPROVED".equalsIgnoreCase(action)) {
                Inventory inventory = request.getInventory();
                Project project = request.getProject();
                int quantity = request.getMaterialStock();

                if (inventory.getMaterialStock() < quantity) continue;

                inventory.setMaterialStock(inventory.getMaterialStock() - quantity);

                // ✅ Update stock status
                int newStock = inventory.getMaterialStock();
                if (newStock <= 30) {
                    inventory.setMaterialStockStatus("LOW");
                } else if (newStock <= 100) {
                    inventory.setMaterialStockStatus("MODERATE");
                } else {
                    inventory.setMaterialStockStatus("HIGH");
                }

                inventoryRepository.save(inventory);

                ProjectInventory existing = projectInventoryRepository.findByProjectAndInventory(project, inventory);
                if (existing != null) {
                    existing.setQuantityAssigned(existing.getQuantityAssigned() + quantity);
                    existing.setTotalPrice(existing.getMaterialPrice() * existing.getQuantityAssigned());
                    projectInventoryRepository.save(existing);
                } else {
                    ProjectInventory newEntry = new ProjectInventory();
                    newEntry.setProject(project);
                    newEntry.setInventory(inventory);
                    newEntry.setQuantityAssigned(quantity);
                    newEntry.setQuantityUsed(0);
                    newEntry.setMaterialPrice(inventory.getMaterialPrice());
                    newEntry.setTotalPrice(inventory.getMaterialPrice() * quantity);
                    projectInventoryRepository.save(newEntry);
                }
            }

            materialRequestRepository.save(request);
        }

        return "✅ Bulk request update complete.";
    }

    @PostMapping("/admin/requests/updateMultipleStatus")
    @ResponseBody
    public String updateMultipleRequestStatus(@RequestBody BulkRequestUpdate request) {
        for (Long id : request.getRequestIds()) {
            updateRequestStatus(id, request.getAction());
        }
        return "✅ Bulk request status updated.";
    }

    public static class BulkRequestUpdate {
        private List<Long> requestIds;
        private String action;

        public List<Long> getRequestIds() {
            return requestIds;
        }

        public void setRequestIds(List<Long> requestIds) {
            this.requestIds = requestIds;
        }

        public String getAction() {
            return action;
        }

        public void setAction(String action) {
            this.action = action;
        }
    }

    @GetMapping("/admin/requests/projects")
    @ResponseBody
    public List<String> getAllProjectNames() {
        return projectRepository.findAll()
                .stream()
                .map(Project::getProjectname)
                .distinct()
                .collect(Collectors.toList());
    }
}
