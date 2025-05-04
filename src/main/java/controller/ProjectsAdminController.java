package controller;

import entity.Project;
import entity.ProjectStage;
import entity.ProjectTask;
import entity.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import repository.ProjectRepository;
import repository.ProjectStageRepository;
import repository.ProjectTaskRepository;

import java.util.*;
import java.util.stream.Collectors;

@Controller
public class ProjectsAdminController {

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private ProjectStageRepository stageRepo;

    @Autowired
    private ProjectTaskRepository taskRepo;

    @GetMapping("/projectsAdmin/getProjects")
    @ResponseBody
    public List<Project> getAllProjects() {
        return projectRepository.findAll();
    }

    @PostMapping("/projectsAdmin/addProject")
    @ResponseBody
    public ResponseEntity<?> addProject(@RequestBody Project project) {
        // Duplicate name check
        Project existing = projectRepository.findByProjectname(project.getProjectname());
        if (existing != null) {
            return ResponseEntity.badRequest().body("Project with the same name already exists.");
        }

        // Start and end date should not be the same
        if (project.getProjectstart() != null && project.getProjectend() != null &&
                project.getProjectstart().equals(project.getProjectend())) {
            return ResponseEntity.badRequest().body("Start and end date cannot be the same.");
        }
        Project saved = projectRepository.save(project);
        return ResponseEntity.ok(saved);
    }


    @PutMapping("/projectsAdmin/updateProject/{project_id}")
    @ResponseBody
    public Project updateProject(@PathVariable int project_id, @RequestBody Project updatedProject) {
        Optional<Project> existingProject = projectRepository.findById(project_id);
        if (existingProject.isPresent()) {
            Project project = existingProject.get();
            project.setProjectname(updatedProject.getProjectname());
            project.setProjectstatus(updatedProject.getProjectstatus());
            project.setProjectstart(updatedProject.getProjectstart());
            project.setProjectend(updatedProject.getProjectend());
            project.setClientname(updatedProject.getClientname());
            project.setContractamount(updatedProject.getContractamount());
            project.setDownpayment(updatedProject.getDownpayment());
            return projectRepository.save(project);
        }
        return null;
    }

    @GetMapping("/projectsAdmin")
    public String projectAdminPage(HttpSession session, Model model) {
        User user = (User) session.getAttribute("authenticatedUser");
        if (user == null) {
            return "redirect:/"; // Redirect to login if session expired
        }

        if (!"ADMIN".equalsIgnoreCase(user.getRole())) {
            return "error/error403";
        }

        model.addAttribute("projects", projectRepository.findAll());
        return "projectsAdmin";
    }


    @GetMapping("/projectsAdmin/getStages/{projectId}")
    @ResponseBody
    public List<ProjectStage> getStages(@PathVariable int projectId) {
        Project project = projectRepository.findById(projectId).orElse(null);
        return stageRepo.findByProject(project);
    }

    @PostMapping("/projectsAdmin/saveStages/{projectId}")
    public ResponseEntity<String> saveStages(@PathVariable Integer projectId,
                                             @RequestBody List<Map<String, Object>> stages) {
        Optional<Project> projectOpt = projectRepository.findById(projectId);
        if (projectOpt.isEmpty()) return ResponseEntity.badRequest().body("Project not found.");

        Project project = projectOpt.get();
        List<ProjectStage> existingStages = stageRepo.findByProject(project);

        Map<Integer, ProjectStage> stageMap = existingStages.stream()
                .collect(Collectors.toMap(ProjectStage::getStageNumber, s -> s));

        for (Map<String, Object> stageData : stages) {
            Integer stageNumber = (Integer) stageData.get("stageNumber");
            ProjectStage stage = stageMap.getOrDefault(stageNumber, new ProjectStage());
            stage.setProject(project);
            stage.setStageNumber(stageNumber);

            stage = stageRepo.save(stage); // Save or update stage
            List<Map<String, Object>> tasks = (List<Map<String, Object>>) stageData.get("tasks");

            Set<Integer> taskIdsFromFrontend = new HashSet<>();
            boolean allCompleted = true; // ✅ Track task completion for this stage

            for (Map<String, Object> taskData : tasks) {
                Integer taskId = taskData.get("taskId") != null ? (Integer) taskData.get("taskId") : null;
                String taskName = (String) taskData.get("taskName");
                Boolean completed = (Boolean) taskData.get("completed");

                if (completed == null || !completed) {
                    allCompleted = false; // ✅ Any incomplete task makes the stage incomplete
                }

                ProjectTask task;
                if (taskId != null && taskRepo.existsById(taskId)) {
                    task = taskRepo.findById(taskId).get();
                } else {
                    task = new ProjectTask();
                    task.setStage(stage);
                }

                task.setTaskName(taskName);
                task.setCompleted(completed != null && completed);
                task = taskRepo.save(task);
                taskIdsFromFrontend.add(task.getTaskId());
            }

            // ✅ Delete removed tasks
            List<ProjectTask> existingTasks = taskRepo.findByStage(stage);
            for (ProjectTask existing : existingTasks) {
                if (!taskIdsFromFrontend.contains(existing.getTaskId())) {
                    taskRepo.delete(existing);
                }
            }

            // ✅ Set stage status based on tasks
            stage.setStatus(allCompleted ? "Complete" : "Incomplete");
            stageRepo.save(stage); // ✅ Save the updated status
        }

        return ResponseEntity.ok("Project progress saved successfully.");
    }


    @GetMapping("/projectsAdmin/getStagesByName")
    @ResponseBody
    public List<ProjectStage> getStagesByProjectName(@RequestParam String projectName) {
        Project project = projectRepository.findByProjectname(projectName);
        return stageRepo.findByProject(project);
    }


}

