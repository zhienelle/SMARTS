package controller;

import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import entity.Project;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import repository.ProjectRepository;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Controller
public class ProjectManagementAdminController {

    @Autowired
    private ProjectRepository projectRepository;

    @GetMapping("/projectManagementAdmin")
    public String projectManagementAdmin(@RequestParam("projectId") int projectId, Model model) {
        Optional<Project> projectOpt = projectRepository.findById(projectId);
        if (projectOpt.isPresent()) {
            Project project = projectOpt.get();

            // Force lazy loading of inventory and its related fields
            if (project.getProjectInventoryList() != null) {
                project.getProjectInventoryList().forEach(pi -> {
                    if (pi.getInventory() != null) {
                        pi.getInventory().getMaterialName(); // safely trigger lazy load
                        pi.getInventory().getMaterialPrice();
                        pi.getInventory().getMaterialCategory();
                    }
                });
            }

            model.addAttribute("project", project);
            return "projectManagementAdmin";
        }

        return "redirect:/projectsAdmin";
    }


    @GetMapping("/projectManagementAdmin/data")
    @ResponseBody
    public ResponseEntity<?> getProjectData(@RequestParam("projectId") int projectId) {
        Optional<Project> projectOpt = projectRepository.findById(projectId);
        if (projectOpt.isEmpty()) return ResponseEntity.badRequest().body("Not found");

        Project project = projectOpt.get();

        // Force loading related inventory list
        project.getProjectInventoryList().forEach(pi -> {
            pi.getInventory().getMaterialName(); // lazy load
        });

        Map<String, Object> response = new HashMap<>();
        response.put("project", project);
        response.put("materials", project.getProjectInventoryList());

        return ResponseEntity.ok(response);
    }

}
