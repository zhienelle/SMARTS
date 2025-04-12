package controller;

import entity.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.stereotype.Controller;
import repository.*;

import java.util.*;
import java.util.stream.Collectors;

@Controller
public class HomepageGenconController {

    @Autowired private UserRepository userRepository;
    @Autowired private ProjectRepository projectRepository;
    @Autowired private ProjectInventoryRepository projectInventoryRepository;
    @Autowired private ProjectStageRepository projectStageRepository;
    @Autowired private ProjectTaskRepository projectTaskRepository;

    @GetMapping("/homepageGencon")
    public String homepageGencon(HttpSession session, Model model) {
        User user = (User) session.getAttribute("authenticatedUser");
        if (user == null) {
            return "redirect:/";
        }

        // ✅ Check CAPTCHA was passed
        Boolean captchaPassed = (Boolean) session.getAttribute("captchaPassed");
        if (captchaPassed == null || !captchaPassed) {
            return "redirect:/?captchaRequired=true";
        }

        // ✅ Role check — use user.getRole() instead of a separate session attribute
        if (!"MAIN CONTRACTOR".equalsIgnoreCase(user.getRole())) {
            return "error/error403";
        }

        model.addAttribute("username", user.getUsername());
        return "homepageGencon";
    }
    @GetMapping("/gencon/projects")
    @ResponseBody
    public List<Project> getAssignedProjects(HttpSession session) {
        User user = (User) session.getAttribute("authenticatedUser");

        if (user == null || !"MAIN CONTRACTOR".equalsIgnoreCase(user.getRole())) {
            return Collections.emptyList();
        }

        String assigned = user.getProject();
        if (assigned == null || assigned.isEmpty()) {
            return Collections.emptyList();
        }

        List<String> names = Arrays.stream(assigned.split(","))
                .map(String::trim)
                .map(String::toLowerCase)
                .collect(Collectors.toList());

        return projectRepository.findAll().stream()
                .filter(p -> names.contains(p.getProjectname().toLowerCase()))
                .collect(Collectors.toList());
    }

    @GetMapping("/gencon/progress")
    @ResponseBody
    public List<Map<String, Object>> getProgress(@RequestParam("projectId") int projectId) {
        Project project = projectRepository.findById(projectId).orElse(null);
        if (project == null) return List.of();

        List<ProjectStage> stages = projectStageRepository.findByProject(project);
        List<Map<String, Object>> response = new ArrayList<>();

        for (ProjectStage stage : stages) {
            List<ProjectTask> tasks = projectTaskRepository.findByStage(stage);
            long completed = tasks.stream().filter(ProjectTask::isCompleted).count();

            Map<String, Object> stageMap = new HashMap<>();
            stageMap.put("stageNumber", stage.getStageNumber());
            stageMap.put("totalTasks", tasks.size());
            stageMap.put("completedTasks", completed);
            response.add(stageMap);
        }

        return response;
    }


    @GetMapping("/gencon/inventory")
    @ResponseBody
    public List<Map<String, Object>> getProjectInventory(@RequestParam("projectId") int projectId) {
        List<ProjectInventory> records = projectInventoryRepository.findByProject_ProjectIdIn(List.of(projectId));
        List<Map<String, Object>> result = new ArrayList<>();

        for (ProjectInventory pi : records) {
            Map<String, Object> item = new HashMap<>();
            Inventory inv = pi.getInventory();

            item.put("materialName", inv != null ? inv.getMaterialName() : "N/A");
            item.put("materialPrice", inv != null ? inv.getMaterialPrice() : 0.0);
            item.put("quantityAssigned", pi.getQuantityAssigned());
            item.put("totalPrice", pi.getTotalPrice());

            result.add(item);
        }

        return result;
    }


}
