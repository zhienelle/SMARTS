package controller;

import entity.Project;
import entity.ProjectInventory;
import entity.User;
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

    @GetMapping("/invoiceExestaff")
    public String invoiceExestaff() {
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
    public List<ProjectInventory> getProjectMaterials(@RequestParam("projectId") int projectId) {
        Project project = projectRepository.findById(projectId).orElse(null);
        if (project == null) return List.of();

        return projectInventoryRepository.findByProject_ProjectIdIn(List.of(project.getProjectId()));
    }



}
