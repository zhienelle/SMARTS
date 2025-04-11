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

import java.util.*;
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

        pending.forEach(req -> {
            if (req.getInventory() != null) {
                req.setMaterialName(req.getInventory().getMaterialName());
                req.setMaterialCategory(req.getInventory().getMaterialCategory());
            }
            if (req.getProject() != null) {
                req.getProject().getProjectname();
            }
        });

        return pending;
    }
//        pending.forEach(req -> {
//            Inventory inv = req.getInventory();
//            if (inv != null) {
//                req.setMaterialName(inv.getMaterialName());
//                req.setMaterialCategory(inv.getMaterialCategory());
//            }
//
//            Project project = req.getProject();
//            if (project != null) {
//                project.getProjectname(); // Optional: force load
//            }
//
//            if (req.getUser() != null) {
//                req.getUser().getUsername(); // Optional: force load
//            }
//        });
//
//
//        return pending;
//    }

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
                return "❌ Cannot approve request: Requested quantity (" + quantity + ") exceeds current stock (" + inventory.getMaterialStock() + ") of " + inventory.getMaterialName() + ".";
            }

            inventory.setMaterialStock(inventory.getMaterialStock() - quantity);

            int newStock = inventory.getMaterialStock();
            inventory.setMaterialStockStatus(newStock <= 30 ? "LOW" : newStock <= 100 ? "MODERATE" : "HIGH");

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
        return "✅ Request status updated.";
    }


    @PostMapping("/admin/requests/bulkUpdate")
    @ResponseBody
    public String bulkUpdateRequestStatus(@RequestParam List<Long> requestIds,
                                          @RequestParam String action) {
        if (!"APPROVED".equalsIgnoreCase(action)) {
            for (Long id : requestIds) {
                updateRequestStatus(id, action);
            }
            return "✅ Bulk request status updated.";
        }

        // Step 1: Collect and aggregate request quantities per inventory item
        Map<Long, Integer> inventoryRequestSums = new HashMap<>();
        Map<Long, String> inventoryNames = new HashMap<>();

        for (Long id : requestIds) {
            Optional<MaterialRequest> optional = materialRequestRepository.findById(id);
            if (optional.isEmpty()) continue;

            MaterialRequest req = optional.get();
            Inventory inv = req.getInventory();

            if (inv != null) {
                long invId = inv.getMaterialId();
                inventoryRequestSums.put(invId, inventoryRequestSums.getOrDefault(invId, 0) + req.getMaterialStock());
                inventoryNames.put(invId, inv.getMaterialName());
            }
        }

        // Step 2: Validate total quantity per inventory item
        List<String> errors = new ArrayList<>();

        for (Map.Entry<Long, Integer> entry : inventoryRequestSums.entrySet()) {
            Long invId = entry.getKey();
            int requestedQty = entry.getValue();

            Optional<Inventory> invOpt = inventoryRepository.findById(Math.toIntExact(invId));
            if (invOpt.isEmpty()) continue;

            Inventory inv = invOpt.get();

            if (inv.getMaterialStock() < requestedQty) {
                String materialName = inventoryNames.get(invId);
                errors.add("❌ Total requested quantity for '" + materialName + "' (" + requestedQty +
                        ") exceeds available stock (" + inv.getMaterialStock() + ").");
            }
        }

// If there were any errors, return them all
        if (!errors.isEmpty()) {
            return String.join("\n", errors); // returns all error messages as a single string separated by newlines
        }



        // Step 3: Proceed with approvals if all validations passed
        for (Long id : requestIds) {
            updateRequestStatus(id, "APPROVED");
        }

        return "✅ Bulk request approval successful.";
    }

    @GetMapping("/admin/inventory/getAll")
    @ResponseBody
    public List<Inventory> getAllInventory() {
        return inventoryRepository.findAll();
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
