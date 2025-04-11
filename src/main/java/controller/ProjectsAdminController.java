package controller;

import entity.Project;
import entity.ProjectStage;
import entity.ProjectTask;
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
    public Project addProject(@RequestBody Project project) {
        return projectRepository.save(project);
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

    @GetMapping("projectsAdmin")
    public String projectAdminPage(Model model) {
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

            stage = stageRepo.save(stage); // save new stage or update existing
            List<Map<String, Object>> tasks = (List<Map<String, Object>>) stageData.get("tasks");

            Set<Integer> taskIdsFromFrontend = new HashSet<>();
            for (Map<String, Object> taskData : tasks) {
                Integer taskId = taskData.get("taskId") != null ? (Integer) taskData.get("taskId") : null;
                String taskName = (String) taskData.get("taskName");
                Boolean completed = (Boolean) taskData.get("completed");

                ProjectTask task;
                if (taskId != null && taskRepo.existsById(taskId)) {
                    task = taskRepo.findById(taskId).get();
                } else {
                    task = new ProjectTask();
                    task.setStage(stage);
                }
                task.setTaskName(taskName);
                task.setCompleted(completed);
                task = taskRepo.save(task);
                taskIdsFromFrontend.add(task.getTaskId());
            }

            // ✅ Delete removed tasks (existing in DB but not in frontend)
            List<ProjectTask> existingTasks = taskRepo.findByStage(stage);
            for (ProjectTask existing : existingTasks) {
                if (!taskIdsFromFrontend.contains(existing.getTaskId())) {
                    taskRepo.delete(existing);
                }
            }
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

