package controller;

import entity.Inventory;
import entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import repository.InventoryRepository;
import repository.UserRepository;
// import repository.MaterialRequestRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
public class InventoryExestaffController {

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private UserRepository userRepository;

    // @Autowired
    // private MaterialRequestRepository materialRequestRepository;

    // Load executive staff inventory page only if role is EXESTAFF
    @GetMapping("/exestaff/inventory")
    public String loadExeStaffInventory(@RequestParam("username") String username, Model model) {
        Optional<User> optionalUser = userRepository.findByUsername(username);

        if (optionalUser.isPresent()) {
            User currentUser = optionalUser.get();

            if ("EXESTAFF".equalsIgnoreCase(currentUser.getRole())) {
                List<Inventory> unarchivedInventory = inventoryRepository.findAll().stream()
                        .filter(item -> item.getMaterialArchived() == null || !item.getMaterialArchived())
                        .collect(Collectors.toList());

                model.addAttribute("inventory", unarchivedInventory);
                return "inventoryExestaff";
            }
        }

        return "error"; // fallback
    }

    // Fetch inventory items as JSON (REST)
    @GetMapping("/exestaff/inventory/getInventory")
    @ResponseBody
    public List<Inventory> getInventory() {
        return inventoryRepository.findAll().stream()
                .filter(item -> item.getMaterialArchived() == null || !item.getMaterialArchived())
                .collect(Collectors.toList());
    }

    @GetMapping("inventoryExestaff")
    public String InventoryExestaff(){
        return "inventoryExestaff";
    }
    /*
    // Submit request for materials (for future use)
    @PostMapping("/exestaff/inventory/requestMaterial")
    @ResponseBody
    public String submitMaterialRequest(@RequestBody MaterialRequest request) {
        materialRequestRepository.save(request);
        return "Request submitted successfully";
    }
    */
}
