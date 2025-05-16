package controller;

import entity.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import repository.*;

import java.util.*;
import java.util.stream.Collectors;

@Controller
public class MyProjectExestaffController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private ProjectInventoryRepository projectInventoryRepository;

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private ProjectStageRepository stageRepo;

    @Autowired
    private ProjectTaskRepository taskRepo;

    @GetMapping("/myProjectExestaff")
    public String myProjectExestaff(HttpSession session, Model model) {
        User user = (User) session.getAttribute("authenticatedUser");
        if (user == null) return "redirect:/";

        if (!"STAFF".equalsIgnoreCase(user.getRole())) {
            return "error/error403";
        }

        List<Project> assignedProjects = new ArrayList<>();

        if (user.getProject() != null && !user.getProject().isEmpty()) {
            String[] projectNames = user.getProject().split(",\\s*");
            for (String name : projectNames) {
                Project project = projectRepository.findByProjectname(name.trim());
                if (project != null) {
                    project.getCompanyname();
                    project.getCompanyLocation();
                    project.getCompanycontact();

                    if (project.getProjectInventoryList() != null) {
                        project.getProjectInventoryList().size();
                        for (ProjectInventory pi : project.getProjectInventoryList()) {
                            if (pi.getInventory() != null) {
                                pi.getInventory().getMaterialName();
                                pi.getInventory().getMaterialCategory();
                                pi.getInventory().getMaterialStock();
                                pi.getInventory().getMaterialPrice();
                            }
                        }
                    }

                    assignedProjects.add(project);
                }
            }
        }

        model.addAttribute("projects", assignedProjects);
        return "myProjectExestaff";
    }

    @GetMapping("/myProjectExestaff/materials")
    @ResponseBody
    public List<ProjectInventory> getProjectMaterials(HttpSession session, String projectName) {
        User user = (User) session.getAttribute("authenticatedUser");

        if (user == null || user.getProject() == null || user.getProject().isEmpty() || projectName == null) {
            return new ArrayList<>();
        }

        Project selectedProject = projectRepository.findByProjectname(projectName.trim());
        if (selectedProject == null) return new ArrayList<>();

        List<ProjectInventory> list = selectedProject.getProjectInventoryList();
        list.forEach(pi -> {
            if (pi.getInventory() != null) {
                pi.getInventory().getMaterialName();
                pi.getInventory().getMaterialCategory();
                pi.getInventory().getMaterialStock();
                pi.getInventory().getMaterialPrice();
            }
        });

        return list;
    }

    @PostMapping("/myProjectExestaff/removeMaterial")
    @ResponseBody
    public ResponseEntity<?> removeMaterialFromProject(@RequestBody Map<String, String> body) {
        String projectName = body.get("projectName");
        String materialName = body.get("materialName");
        int quantity = Integer.parseInt(body.get("quantity"));

        Project project = projectRepository.findByProjectname(projectName);
        if (project == null) return ResponseEntity.badRequest().body("Project not found");

        ProjectInventory target = null;
        for (ProjectInventory pi : project.getProjectInventoryList()) {
            if (pi.getInventory().getMaterialName().equalsIgnoreCase(materialName)) {
                target = pi;
                break;
            }
        }

        if (target == null) return ResponseEntity.badRequest().body("Material not found");

        target.getInventory().setMaterialStock(target.getInventory().getMaterialStock() + quantity);
        inventoryRepository.save(target.getInventory());

        project.getProjectInventoryList().remove(target);
        projectInventoryRepository.delete(target);

        return ResponseEntity.ok("Removed");
    }

    @PostMapping("/projectsAdmin/saveStagesByName")
    public ResponseEntity<String> saveStagesByName(@RequestParam String projectName,
                                                   @RequestBody List<Map<String, Object>> stages) {
        try {
            Project project = projectRepository.findByProjectname(projectName);
            if (project == null) {
                return ResponseEntity.badRequest().body("Project not found.");
            }

            List<ProjectStage> existingStages = stageRepo.findByProject(project);
            Map<Integer, ProjectStage> stageMap = existingStages.stream()
                    .collect(Collectors.toMap(ProjectStage::getStageNumber, s -> s));

            for (Map<String, Object> stageData : stages) {
                Integer stageNumber = (Integer) stageData.get("stageNumber");
                ProjectStage stage = stageMap.getOrDefault(stageNumber, new ProjectStage());
                stage.setProject(project);
                stage.setStageNumber(stageNumber);

                List<Map<String, Object>> tasksRaw = (List<Map<String, Object>>) stageData.get("tasks");
                List<Map<String, Object>> tasks = tasksRaw != null ? tasksRaw : new ArrayList<>();

                Set<Integer> frontendTaskIds = new HashSet<>();
                boolean allCompleted = !tasks.isEmpty() && tasks.stream().allMatch(t -> Boolean.TRUE.equals(t.get("completed")));

                stage = stageRepo.save(stage); // Save or update stage first

                for (Map<String, Object> taskData : tasks) {
                    String taskName = (String) taskData.get("taskName");
                    Boolean completed = (Boolean) taskData.get("completed");

                    if (taskName == null || taskName.trim().isEmpty()) continue;

                    Integer taskId = taskData.get("taskId") != null ? ((Number) taskData.get("taskId")).intValue() : null;

                    ProjectTask task;
                    if (taskId != null && taskRepo.existsById(taskId)) {
                        task = taskRepo.findById(taskId).orElse(new ProjectTask());
                    } else {
                        task = new ProjectTask();
                        task.setStage(stage);
                    }

                    task.setTaskName(taskName.trim());
                    task.setCompleted(completed != null && completed);
                    task = taskRepo.save(task);
                    frontendTaskIds.add(task.getTaskId());
                }

                // Clean up old tasks
                List<ProjectTask> existingTasks = taskRepo.findByStage(stage);
                for (ProjectTask existing : existingTasks) {
                    if (!frontendTaskIds.contains(existing.getTaskId())) {
                        taskRepo.delete(existing);
                    }
                }

                stage.setStatus(allCompleted ? "Complete" : "Incomplete");
                stageRepo.save(stage);
            }

            return ResponseEntity.ok("Stages saved successfully.");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }


    @GetMapping("/myProjectExestaff/getStagesByName")
    @ResponseBody
    public ResponseEntity<List<ProjectStage>> getStagesByProjectName(@RequestParam String projectName) {
        Project project = projectRepository.findByProjectname(projectName);
        if (project == null) return ResponseEntity.badRequest().body(Collections.emptyList());

        List<ProjectStage> stages = stageRepo.findByProject(project);
        stages.sort(Comparator.comparingInt(ProjectStage::getStageNumber)); // ✅ sort here
        return ResponseEntity.ok(stages);
    }

    @GetMapping("/myProjectExestaff/getMaterialsByStage")
    @ResponseBody
    public List<ProjectInventory> getMaterialsByStage(@RequestParam String projectName,
                                                      @RequestParam int stageNumber) {
        Project project = projectRepository.findByProjectname(projectName);
        if (project == null) return new ArrayList<>();

        Optional<ProjectStage> optionalStage = stageRepo.findByProjectAndStageNumber(project, stageNumber);
        if (!optionalStage.isPresent()) return new ArrayList<>();

        ProjectStage stage = optionalStage.get();

        List<ProjectInventory> materials = stage.getProjectInventoryList();
        materials.forEach(pi -> {
            if (pi.getInventory() != null) {
                pi.getInventory().getMaterialName();
                pi.getInventory().getMaterialCategory();
                pi.getInventory().getMaterialStock();
                pi.getInventory().getMaterialPrice();
            }
        });

        return materials;
    }
}
