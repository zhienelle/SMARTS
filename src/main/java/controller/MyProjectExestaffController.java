package controller;

import entity.Project;
import entity.User;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import repository.InventoryRepository;
import repository.ProjectInventoryRepository;
import repository.ProjectRepository;
import repository.UserRepository;
import entity.ProjectInventory;
import org.springframework.web.bind.annotation.ResponseBody;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
public class MyProjectExestaffController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private ProjectInventoryRepository projectInventoryRepository;

    @Autowired
    private InventoryRepository inventoryRepository;


    @GetMapping("/myProjectExestaff")
    public String myProjectExestaff(HttpSession session, Model model) {
        User user = (User) session.getAttribute("authenticatedUser");

        if (user == null) {
            return "redirect:/";
        }

        String role = (String) session.getAttribute("role");
        if (!"staff".equalsIgnoreCase(role)) {
            return "error/error403";
        }

        List<Project> assignedProjects = new ArrayList<>();

        if (user.getProject() != null && !user.getProject().isEmpty()) {
            String[] projectNames = user.getProject().split(",\\s*");

            for (String name : projectNames) {
                Project project = projectRepository.findByProjectname(name.trim());
                if (project != null) {
                    // ✅ Force load required fields while session is open
                    project.getCompanyname();
                    project.getCompanyLocation();
                    project.getCompanycontact();

                    if (project.getProjectInventoryList() != null) {
                        project.getProjectInventoryList().size(); // preload list
                        for (ProjectInventory pi : project.getProjectInventoryList()) {
                            if (pi.getInventory() != null) {
                                pi.getInventory().getMaterialName();
                                pi.getInventory().getMaterialCategory();
                                pi.getInventory().getMaterialStock();
                                pi.getInventory().getMaterialPrice();
                            }
                        }
                    }

                    assignedProjects.add(project);
                }
            }
        }

        model.addAttribute("projects", assignedProjects);
        return "myProjectExestaff";
    }

    @GetMapping("/myProjectExestaff/materials")
    @ResponseBody
    public List<ProjectInventory> getProjectMaterials(HttpSession session, String projectName) {
        User user = (User) session.getAttribute("authenticatedUser");

        if (user == null || user.getProject() == null || user.getProject().isEmpty() || projectName == null) {
            return new ArrayList<>();
        }

        Project selectedProject = projectRepository.findByProjectname(projectName.trim());
        if (selectedProject == null) return new ArrayList<>();

        List<ProjectInventory> list = selectedProject.getProjectInventoryList();
        list.forEach(pi -> {
            if (pi.getInventory() != null) {
                pi.getInventory().getMaterialName();
                pi.getInventory().getMaterialCategory();
                pi.getInventory().getMaterialStock();
                pi.getInventory().getMaterialPrice();
            }
        });

        return list;
    }

    @PostMapping("/myProjectExestaff/removeMaterial")
    @ResponseBody
    public ResponseEntity<?> removeMaterialFromProject(@RequestBody Map<String, String> body) {
        String projectName = body.get("projectName");
        String materialName = body.get("materialName");
        int quantity = Integer.parseInt(body.get("quantity"));

        Project project = projectRepository.findByProjectname(projectName);
        if (project == null) return ResponseEntity.badRequest().body("Project not found");

        ProjectInventory target = null;
        for (ProjectInventory pi : project.getProjectInventoryList()) {
            if (pi.getInventory().getMaterialName().equalsIgnoreCase(materialName)) {
                target = pi;
                break;
            }
        }

        if (target == null) return ResponseEntity.badRequest().body("Material not found");

        // Return quantity to main inventory
        target.getInventory().setMaterialStock(target.getInventory().getMaterialStock() + quantity);
        inventoryRepository.save(target.getInventory());

        // Remove from project_inventory and delete it
        project.getProjectInventoryList().remove(target);
        projectInventoryRepository.delete(target); // ✅ hard-delete

        return ResponseEntity.ok("Removed");
    }

}
