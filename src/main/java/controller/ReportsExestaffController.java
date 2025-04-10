package controller;

import entity.Project;
import entity.ProjectInventory;
import entity.User;
import repository.ProjectInventoryRepository;
import repository.ProjectRepository;
import repository.UserRepository;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Controller
public class ReportsExestaffController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private ProjectInventoryRepository projectInventoryRepository;

    @GetMapping("/reportsExestaff")
    public String loadReportsPage(HttpSession session, Model model) {
        User user = (User) session.getAttribute("authenticatedUser");

        if (user == null) {
            return "redirect:/";
        }

        System.out.println("✅ Logged in as: " + user.getUsername());

        List<Project> assignedProjects = getAssignedProjects(user);
        model.addAttribute("projects", assignedProjects);
        return "reportsExestaff";
    }

    @GetMapping("/exestaff/reports/getProjects")
    @ResponseBody
    public List<Project> getAssignedProjectsList(HttpSession session) {
        User user = (User) session.getAttribute("authenticatedUser");

        if (user == null) return List.of();

        return getAssignedProjects(user);
    }

    @GetMapping("/exestaff/reports/metrics")
    @ResponseBody
    public Map<String, Object> getKeyMetrics(
            @RequestParam(required = false) String projectName,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            HttpSession session) {

        User user = (User) session.getAttribute("authenticatedUser");
        Map<String, Object> response = new HashMap<>();

        if (user == null) {
            response.put("error", "Not authenticated");
            return response;
        }

        List<Project> assignedProjects = getAssignedProjects(user);

        if (projectName != null && !projectName.equalsIgnoreCase("All Projects")) {
            assignedProjects = assignedProjects.stream()
                    .filter(p -> p.getProjectname().equalsIgnoreCase(projectName))
                    .collect(Collectors.toList());
        }

        if (startDate != null && endDate != null && !endDate.isBefore(startDate)) {
            assignedProjects = assignedProjects.stream()
                    .filter(p -> !p.getProjectstart().toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate().isBefore(startDate) &&
                            !p.getProjectend().toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate().isAfter(endDate))
                    .collect(Collectors.toList());
        }

        int totalProjects = assignedProjects.size();
        int ongoingProjects = (int) assignedProjects.stream()
                .filter(p -> "IN PROGRESS".equalsIgnoreCase(p.getProjectstatus()))
                .count();
        int completedProjects = totalProjects - ongoingProjects;
        double totalRevenue = assignedProjects.stream()
                .mapToDouble(p -> Optional.ofNullable(p.getContractamount()).orElse(0.0))
                .sum();

        List<Integer> projectIds = assignedProjects.stream()
                .map(Project::getProjectId)
                .collect(Collectors.toList());

        List<ProjectInventory> projectMaterials = projectInventoryRepository.findByProject_ProjectIdIn(projectIds);

        double totalExpense = projectMaterials.stream()
                .mapToDouble(pi -> Optional.ofNullable(pi.getTotalPrice()).orElse(0.0))
                .sum();

        double netProfit = totalRevenue - totalExpense;

        response.put("totalProjects", totalProjects);
        response.put("ongoingProjects", ongoingProjects);
        response.put("completedProjects", completedProjects);
        response.put("totalRevenue", totalRevenue);
        response.put("totalExpense", totalExpense);
        response.put("netProfit", netProfit);

        return response;
    }

    @GetMapping("/exestaff/reports/materialPreview")
    @ResponseBody
    public List<Map<String, Object>> getMaterialPreview(
            @RequestParam(required = false) String projectName,
            HttpSession session) {

        User user = (User) session.getAttribute("authenticatedUser");

        if (user == null) return List.of();

        List<Project> assignedProjects = getAssignedProjects(user);

        if (projectName != null && !projectName.equalsIgnoreCase("All Projects")) {
            assignedProjects = assignedProjects.stream()
                    .filter(p -> p.getProjectname().equalsIgnoreCase(projectName))
                    .collect(Collectors.toList());
        }

        List<Integer> projectIds = assignedProjects.stream()
                .map(Project::getProjectId)
                .collect(Collectors.toList());

        List<ProjectInventory> materials = projectInventoryRepository.findByProject_ProjectIdIn(projectIds);

        return materials.stream().map(pi -> {
            Map<String, Object> entry = new HashMap<>();
            entry.put("materialName", pi.getInventory().getMaterialName());
            entry.put("quantity", pi.getQuantityAssigned());
            return entry;
        }).collect(Collectors.toList());
    }

    private List<Project> getAssignedProjects(User user) {
        if (user.getProject() == null || user.getProject().isEmpty()) return List.of();

        return Arrays.stream(user.getProject().split(","))
                .map(String::trim)
                .map(projectRepository::findByProjectname)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
}
