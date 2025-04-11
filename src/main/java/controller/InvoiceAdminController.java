package controller;

import entity.Project;
import entity.ProjectInventory;
import entity.User;
import org.springframework.format.annotation.DateTimeFormat;
import repository.ProjectRepository;
import repository.ProjectInventoryRepository;
import repository.UserRepository;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Controller
public class InvoiceAdminController {

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private ProjectInventoryRepository projectInventoryRepository;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/invoiceAdmin")
    public String loadInvoicePage(HttpSession session, Model model) {
        User user = (User) session.getAttribute("authenticatedUser");
        if (user == null || !"ADMIN".equalsIgnoreCase(user.getRole())) {
            return "redirect:/";
        }

        List<Project> allProjects = projectRepository.findAll();
        model.addAttribute("projects", allProjects);
        return "invoiceAdmin";
    }

    @GetMapping("/admin/invoice/getProjects")
    @ResponseBody
    public List<Project> getAllProjects(HttpSession session) {
        User user = (User) session.getAttribute("authenticatedUser");
        if (user == null || !"ADMIN".equalsIgnoreCase(user.getRole())) {
            return List.of();
        }
        return projectRepository.findAll();
    }

    @GetMapping("/admin/invoice/getInventory")
    @ResponseBody
    public List<Map<String, Object>> getProjectInventory(@RequestParam("projectName") String projectName, HttpSession session) {
        User user = (User) session.getAttribute("authenticatedUser");
        if (user == null || !"ADMIN".equalsIgnoreCase(user.getRole())) {
            return List.of();
        }

        Project project = projectRepository.findByProjectname(projectName);
        if (project == null) return List.of();

        List<ProjectInventory> materials = projectInventoryRepository.findByProject_ProjectIdIn(Collections.singletonList(project.getProjectId()));


        return materials.stream().map(pi -> {
            Map<String, Object> entry = new HashMap<>();
            entry.put("materialName", pi.getInventory().getMaterialName());
            entry.put("materialCategory", pi.getInventory().getMaterialCategory());
            entry.put("quantity", pi.getQuantityAssigned());
            entry.put("unitPrice", pi.getMaterialPrice());
            entry.put("totalPrice", pi.getTotalPrice());
            return entry;
        }).collect(Collectors.toList());
    }



}
