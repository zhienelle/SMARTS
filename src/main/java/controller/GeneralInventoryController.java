package controller;

import entity.Inventory;
import entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.ui.Model;
import org.springframework.stereotype.Controller;
import repository.InventoryRepository;
import repository.UserRepository;

import java.util.List;
import java.util.Optional;

@Controller
public class GeneralInventoryController {

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private UserRepository userRepository;

    // Load page based on user's role
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

        return "error"; // fallback if user not found or invalid role
    }

    @GetMapping("inventoryAdmin")
    public String AdminInventory() {
        return "inventoryAdmin";
    }
    // REST: Get inventory as JSON
    @GetMapping("/generalInventory/getInventory")
    @ResponseBody
    public List<Inventory> getInventory() {
        return inventoryRepository.findAll();
    }

    // ✅ REST: Add new inventory item
    @PostMapping("/generalInventory/addInventory")
    @ResponseBody
    public Inventory addInventory(@RequestBody Inventory inventory) {
        try {
            // Log incoming data
            System.out.println("Received new inventory item:");
            System.out.println("ID: " + inventory.getMaterialId());
            System.out.println("Category: " + inventory.getMaterialCategory());
            System.out.println("Name: " + inventory.getMaterialName());
            System.out.println("Stock: " + inventory.getMaterialStock());
            System.out.println("Price: " + inventory.getMaterialPrice());

            // Ensure Hibernate treats it as new
            inventory.setMaterialId(null);

            // Save and return the saved item
            Inventory savedInventory = inventoryRepository.save(inventory);
            System.out.println("✅ Saved item ID: " + savedInventory.getMaterialId());
            return savedInventory;

        } catch (Exception e) {
            System.err.println("❌ Error saving new inventory item: " + e.getMessage());
            throw new RuntimeException("Add failed: " + e.getMessage());
        }
    }

    // ✅ REST: Update inventory item
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

            Optional<Inventory> optionalInventory = inventoryRepository.findById(id);
            if (optionalInventory.isPresent()) {
                Inventory inventory = optionalInventory.get();
                inventory.setMaterialCategory(updatedInventory.getMaterialCategory());
                inventory.setMaterialName(updatedInventory.getMaterialName());
                inventory.setMaterialStock(updatedInventory.getMaterialStock());
                inventory.setMaterialPrice(updatedInventory.getMaterialPrice());
                inventory.setMaterialArchived(updatedInventory.getMaterialArchived()); // ✅ Apply archive status

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


    // ✅ REST: Archive or Unarchive inventory item
    @PutMapping("/generalInventory/archiveInventory/{id}")
    @ResponseBody
    public String archiveInventory(@PathVariable int id) {
        Optional<Inventory> optionalInventory = inventoryRepository.findById(id);
        if (optionalInventory.isPresent()) {
            Inventory inventory = optionalInventory.get();
            Boolean currentStatus = inventory.getMaterialArchived() != null ? inventory.getMaterialArchived() : false;
            inventory.setMaterialArchived(!currentStatus); // Toggle archive status
            inventoryRepository.save(inventory);
            System.out.println("✔️ Toggled archive for ID: " + id + " → Now: " + !currentStatus);
            return "Archived toggled for ID: " + id;
        } else {
            throw new RuntimeException("Inventory item not found with ID: " + id);
        }
    }
//test
}
