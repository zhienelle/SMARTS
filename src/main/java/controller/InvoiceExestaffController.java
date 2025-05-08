package controller;

import entity.Project;
import entity.ProjectInventory;
import entity.ProjectStage;
import entity.User;
import org.springframework.ui.Model;
import repository.ProjectInventoryRepository;
import repository.ProjectRepository;
import repository.ProjectStageRepository;
import repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.stereotype.Controller;

import java.util.*;
import java.util.stream.Collectors;

@Controller
public class InvoiceExestaffController {

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private ProjectInventoryRepository projectInventoryRepository; // ✅ you were missing this

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProjectStageRepository projectStageRepository;

    @GetMapping("/invoiceExestaff")
    public String invoiceExestaff(HttpSession session, Model model) {
        User user = (User) session.getAttribute("authenticatedUser");
        if (user == null) return "redirect:/";
        if (!"STAFF".equalsIgnoreCase(user.getRole())) return "error/error403";

        List<Project> assigned = getAssignedProjects(session);
        model.addAttribute("projects", assigned);

        Map<Long, Boolean> stageCompletionMap = new HashMap<>();
        Long selectedProjectId = null;

        for (Project p : assigned) {
            if (selectedProjectId == null) {
                selectedProjectId = (long) p.getProjectId(); // First project as default
            }

            List<ProjectStage> stages = projectStageRepository.findByProject(p);

            if (stages == null || stages.isEmpty()) {
                stageCompletionMap.put((long) p.getProjectId(), true);
            } else {
                boolean hasIncomplete = stages.stream().anyMatch(stage ->
                        stage.getStatus() == null || !stage.getStatus().equalsIgnoreCase("Complete")
                );
                stageCompletionMap.put((long) p.getProjectId(), hasIncomplete);
            }
        }

        model.addAttribute("selectedProjectId", selectedProjectId);
        model.addAttribute("incompleteStagesMap", stageCompletionMap);

        return "invoiceExestaff";
    }


    @GetMapping("/exestaff/invoice/projects")
    @ResponseBody
    public List<Project> getAssignedProjects(HttpSession session) {
        User user = (User) session.getAttribute("authenticatedUser");

        // Always print these logs first
        System.out.println("User from session: " + user);
        System.out.println("Assigned Projects: " + (user != null ? user.getProject() : "null"));
        System.out.println("User Role: " + (user != null ? user.getRole() : "null"));

        // Only now return early if user is invalid
        if (user == null || (!"EXECUTIVE STAFF".equalsIgnoreCase(user.getRole()) && !"STAFF".equalsIgnoreCase(user.getRole()))) {

            return List.of();
        }

        String assigned = user.getProject();
        if (assigned == null || assigned.isEmpty()) return List.of();

        List<String> names = Arrays.stream(assigned.split(","))
                .map(String::trim)
                .map(String::toLowerCase)
                .collect(Collectors.toList());

        System.out.println("== All projects in DB ==");
        projectRepository.findAll().forEach(p -> System.out.println("• " + p.getProjectname()));
        System.out.println("== Comparing against: " + names);

        return projectRepository.findAll().stream()
                .filter(p -> names.contains(p.getProjectname().toLowerCase()))
                .collect(Collectors.toList());
    }


    @GetMapping("/exestaff/invoice/materials")
    @ResponseBody
    public List<Map<String, Object>> getProjectMaterials(@RequestParam("projectId") int projectId) {
        Project project = projectRepository.findById(projectId).orElse(null);
        if (project == null) return List.of();

        List<ProjectInventory> inventories = projectInventoryRepository.findByProject_ProjectIdIn(List.of(project.getProjectId()));

        return inventories.stream().map(pi -> {
            Map<String, Object> entry = new HashMap<>();
            entry.put("materialName", pi.getInventory().getMaterialName());
            entry.put("materialPrice", pi.getInventory().getMaterialPrice());
            entry.put("quantityAssigned", pi.getQuantityAssigned());
            entry.put("unitPrice", pi.getInventory().getMaterialPrice());
            entry.put("totalPrice", pi.getTotalPrice());
            return entry;
        }).collect(Collectors.toList());
    }

    @GetMapping("/exestaff/invoice/stages")
    @ResponseBody
    public List<Map<String, Object>> getProjectStages(@RequestParam("projectId") int projectId) {
        Project project = projectRepository.findById(projectId).orElse(null);
        if (project == null) return List.of();

        List<ProjectStage> stages = projectStageRepository.findByProject(project);

        return stages.stream().map(stage -> {
            Map<String, Object> map = new HashMap<>();
            map.put("stageId", stage.getStageId());
            map.put("stageName", stage.getStageName());
            map.put("stageNumber", stage.getStageNumber()); // ✅ add this line if not present
            return map;
        }).collect(Collectors.toList());
    }


    @GetMapping("/exestaff/invoice/materials-by-stage")
    @ResponseBody
    public List<Map<String, Object>> getMaterialsByStage(@RequestParam("stageId") int stageId) {
        ProjectStage stage = projectStageRepository.findById(stageId).orElse(null);
        if (stage == null) return List.of();

        List<ProjectInventory> inventories = projectInventoryRepository.findByProject_ProjectIdAndStage_StageId(
                stage.getProject().getProjectId(), Long.valueOf(stage.getStageId()));


        return inventories.stream().map(pi -> {
            Map<String, Object> entry = new HashMap<>();
            entry.put("materialName", pi.getInventory().getMaterialName());
            entry.put("materialPrice", pi.getInventory().getMaterialPrice());
            entry.put("quantityAssigned", pi.getQuantityAssigned());
            entry.put("unitPrice", pi.getInventory().getMaterialPrice());
            entry.put("totalPrice", pi.getTotalPrice());
            return entry;
        }).collect(Collectors.toList());
    }
}
