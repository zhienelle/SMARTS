package controller;
//FIXED BY JED

import entity.Inventory;
import entity.User;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.ui.Model;
import org.springframework.stereotype.Controller;
import repository.InventoryRepository;
import repository.UserRepository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
public class GeneralInventoryController {

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/generalInventory")
    public String loadInventoryPage(@RequestParam("username") String username, Model model) {
        Optional<User> optionalUser = userRepository.findByUsername(username);

        if (optionalUser.isPresent()) {
            User currentUser = optionalUser.get();
            List<Inventory> inventoryList = inventoryRepository.findAll();
            model.addAttribute("inventory", inventoryList);

            String role = currentUser.getRole();
            if ("ADMIN".equalsIgnoreCase(role)) {
                return "inventoryAdmin";
            } else if ("EXESTAFF".equalsIgnoreCase(role)) {
                return "inventoryExestaff";
            }
        }

        return "error";
    }

    @GetMapping("inventoryAdmin")
    public String AdminInventory(HttpSession session, Model model) {
        User currentUser = (User) session.getAttribute("authenticatedUser");

        if (currentUser == null) {
            return "redirect:/"; // or "error"
        }

        if (!"ADMIN".equalsIgnoreCase(currentUser.getRole())) {
            return "error/error403";
        }

        return "inventoryAdmin";
    }

    @GetMapping("/generalInventory/getInventory")
    @ResponseBody
    public List<Inventory> getInventory() {
        return inventoryRepository.findAll();
    }


    @PostMapping("/generalInventory/addInventory")
    @ResponseBody
    public ResponseEntity<?> addInventory(@RequestBody Inventory inventory) {
        try {
            // Validate required fields
            if (inventory.getMaterialCategory() == null || inventory.getMaterialCategory().isEmpty()) {
                return ResponseEntity.badRequest().body("Material category is required");
            }
            if (inventory.getMaterialName() == null || inventory.getMaterialName().isEmpty()) {
                return ResponseEntity.badRequest().body("Material name is required");
            }
            if (inventory.getMaterialStock() <= 0) {
                return ResponseEntity.badRequest().body("Material stock must be greater than zero");
            }
            if (inventory.getMaterialPrice() <= 0) {
                return ResponseEntity.badRequest().body("Material price must be greater than zero");
            }

            // Set stock status based on stock value
            inventory.updateStockStatus();

            // Save the inventory item
            Inventory savedInventory = inventoryRepository.save(inventory);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedInventory); // return created inventory

        } catch (Exception e) {
            System.err.println("Error saving inventory item: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error saving inventory");
        }
    }


//    @PostMapping("/generalInventory/addInventory")
//    @ResponseBody
//    public Inventory addInventory(@RequestBody Inventory inventory) {
//        try {
//            System.out.println("Received new inventory item:");
//            System.out.println("ID: " + inventory.getMaterialId());
//            System.out.println("Category: " + inventory.getMaterialCategory());
//            System.out.println("Name: " + inventory.getMaterialName());
//            System.out.println("Stock: " + inventory.getMaterialStock());
//            System.out.println("Price: " + inventory.getMaterialPrice());
//
//            // ✅ Validate inputs
//            String invalidPattern = ".*[^a-zA-Z0-9\\s].*";
//            if (inventory.getMaterialStock() < 0 || inventory.getMaterialPrice() < 0) {
//                throw new RuntimeException("❌ Stock and price must be non-negative.");
//            }
//            if (inventory.getMaterialCategory().matches(invalidPattern) || inventory.getMaterialName().matches(invalidPattern)) {
//                throw new RuntimeException("❌ Category and Material Name must not contain special characters.");
//            }
//
//            inventory.setMaterialId(null);
//
//            int stock = inventory.getMaterialStock();
//            if (stock <= 30) {
//                inventory.setMaterialStockStatus("LOW");
//            } else if (stock <= 100) {
//                inventory.setMaterialStockStatus("MODERATE");
//            } else {
//                inventory.setMaterialStockStatus("HIGH");
//            }
//
//            Inventory savedInventory = inventoryRepository.save(inventory);
//            System.out.println("✅ Saved item ID: " + savedInventory.getMaterialId());
//            return savedInventory;
//
//        } catch (Exception e) {
//            System.err.println("❌ Error saving new inventory item: " + e.getMessage());
//            throw new RuntimeException("Add failed: " + e.getMessage());
//        }
//    }

    @PutMapping("/generalInventory/updateInventory/{id}")
    @ResponseBody
    public Inventory updateInventory(@PathVariable int id, @RequestBody Inventory updatedInventory) {
        try {
            if (updatedInventory.getMaterialCategory() == null || updatedInventory.getMaterialCategory().trim().isEmpty()) {
                throw new RuntimeException("Material category must not be null or empty.");
            }
            if (updatedInventory.getMaterialName() == null || updatedInventory.getMaterialName().trim().isEmpty()) {
                throw new RuntimeException("Material name must not be null or empty.");
            }

            // ✅ Validate inputs
            String invalidPattern = ".*[^a-zA-Z0-9\\s].*";
            if (updatedInventory.getMaterialStock() < 0 || updatedInventory.getMaterialPrice() < 0) {
                throw new RuntimeException("❌ Value must be non-negative.");
            }
            if (updatedInventory.getMaterialCategory().matches(invalidPattern) || updatedInventory.getMaterialName().matches(invalidPattern)) {
                throw new RuntimeException("❌ Input must not contain special characters.");
            }

            Optional<Inventory> optionalInventory = inventoryRepository.findById(id);
            if (optionalInventory.isPresent()) {
                Inventory inventory = optionalInventory.get();
                inventory.setMaterialCategory(updatedInventory.getMaterialCategory());
                inventory.setMaterialName(updatedInventory.getMaterialName());
                inventory.setMaterialStock(updatedInventory.getMaterialStock());
                inventory.setMaterialPrice(updatedInventory.getMaterialPrice());
                inventory.setMaterialArchived(updatedInventory.getMaterialArchived());

                int stock = updatedInventory.getMaterialStock();
                if (stock <= 30) {
                    inventory.setMaterialStockStatus("LOW");
                } else if (stock <= 100) {
                    inventory.setMaterialStockStatus("MODERATE");
                } else {
                    inventory.setMaterialStockStatus("HIGH");
                }

                System.out.println("Updating inventory ID: " + id);
                return inventoryRepository.save(inventory);
            } else {
                throw new RuntimeException("Inventory item not found with ID: " + id);
            }
        } catch (Exception e) {
            System.err.println("Error updating inventory with ID " + id + ": " + e.getMessage());
            throw new RuntimeException("Update failed: " + e.getMessage());
        }
    }

    @PutMapping("/generalInventory/archiveInventory/{id}")
    @ResponseBody
    public String archiveInventory(@PathVariable int id) {
        Optional<Inventory> optionalInventory = inventoryRepository.findById(id);
        if (optionalInventory.isPresent()) {
            Inventory inventory = optionalInventory.get();
            Boolean currentStatus = inventory.getMaterialArchived() != null ? inventory.getMaterialArchived() : false;
            inventory.setMaterialArchived(!currentStatus);
            inventoryRepository.save(inventory);
            System.out.println("✔️ Toggled archive for ID: " + id + " → Now: " + !currentStatus);
            return "Archived toggled for ID: " + id;
        } else {
            throw new RuntimeException("Inventory item not found with ID: " + id);
        }
    }

    @PutMapping("/generalInventory/adjustStockMultiple")
    @ResponseBody
    public ResponseEntity<String> adjustStockMultiple(@RequestBody List<Map<String, Integer>> adjustments) {
        for (Map<String, Integer> adjustment : adjustments) {
            int materialId = adjustment.get("materialId");
            int adjustBy = adjustment.get("adjustBy");

            Optional<Inventory> optionalInventory = inventoryRepository.findById(materialId);
            if (optionalInventory.isPresent()) {
                Inventory inventory = optionalInventory.get();
                int newStock = inventory.getMaterialStock() + adjustBy;

                if (newStock < 0) {
                    return ResponseEntity.badRequest().body("Stock cannot be negative for material ID: " + materialId);
                }

                inventory.setMaterialStock(newStock);
                inventoryRepository.save(inventory);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Material not found for ID: " + materialId);
            }
        }
        return ResponseEntity.ok("Stocks updated successfully for all items.");
    }


}
