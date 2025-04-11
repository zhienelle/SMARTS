package controller;

import entity.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
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
    public String homepageGencon() {
        return "homepageGencon";
    }

    @GetMapping("/gencon/projects")
    @ResponseBody
    public List<Project> getAssignedProjects(HttpSession session) {
        User user = (User) session.getAttribute("authenticatedUser");
        if (user == null || !"GENERAL CONTRACTOR".equalsIgnoreCase(user.getRole())) return List.of();

        List<String> assignedNames = Arrays.stream(user.getProject().split(","))
                .map(String::trim)
                .collect(Collectors.toList());

        return projectRepository.findAll().stream()
                .filter(p -> assignedNames.contains(p.getProjectname()))
                .collect(Collectors.toList());
    }

    @GetMapping("/gencon/inventory")
    @ResponseBody
    public List<ProjectInventory> getProjectInventory(@RequestParam("projectId") int projectId) {
        return projectInventoryRepository.findByProject_ProjectIdIn(List.of(projectId));
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
            long completed = tasks.stream().filter(t -> t.getStatus().equalsIgnoreCase("completed")).count();

            Map<String, Object> stageMap = new HashMap<>();
            stageMap.put("stageNumber", stage.getStageNumber());
            stageMap.put("totalTasks", tasks.size());
            stageMap.put("completedTasks", completed);
            response.add(stageMap);
        }

        return response;
    }
}
