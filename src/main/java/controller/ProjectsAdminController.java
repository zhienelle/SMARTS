package controller;

import entity.Project;
import entity.ProjectStage;
import entity.ProjectTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import repository.ProjectRepository;
import repository.ProjectStageRepository;
import repository.ProjectTaskRepository;

import java.util.List;
import java.util.Optional;

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
    @ResponseBody
    public String saveStages(@PathVariable int projectId, @RequestBody List<ProjectStage> stages) {
        try {
            Project project = projectRepository.findById(projectId).orElse(null);
            if (project == null) return "Project not found";

            for (ProjectStage stage : stages) {
                stage.setProject(project);

                if (stage.getTasks() != null) {
                    for (ProjectTask task : stage.getTasks()) {
                        task.setStage(stage);

                        if (task.getTaskName() == null || task.getTaskName().trim().isEmpty()) {
                            task.setTaskName("Untitled Task");
                        }

                        if (task.getTaskId() != null) {
                            Optional<ProjectTask> existingTask = taskRepo.findById(task.getTaskId());
                            if (existingTask.isPresent()) {
                                ProjectTask updateTask = existingTask.get();
                                updateTask.setTaskName(task.getTaskName());
                                updateTask.setCompleted(task.isCompleted());
                                updateTask.setStage(stage);
                            }
                        }
                    }
                }
            }

            stageRepo.saveAll(stages);
            return "Progress saved successfully";

        } catch (Exception e) {
            e.printStackTrace();
            return "Error saving progress: " + e.getMessage();
        }
    }
}
