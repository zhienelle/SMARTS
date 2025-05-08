package controller;

import entity.MaterialRequest;
import entity.Project;
import entity.User;
import entity.ProjectStage;

import repository.MaterialRequestRepository;
import repository.ProjectRepository;
import repository.ProjectStageRepository;
import repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;

import java.util.HashMap;
import java.util.Map;
import java.util.Comparator;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
public class RequestsStatusExestaffController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MaterialRequestRepository materialRequestRepository;

    @Autowired
    private ProjectStageRepository projectStageRepository;

    @Autowired
    private ProjectRepository projectRepository;


    // ✅ Serve the HTML page without using Principal, using session instead
    @GetMapping("/requestsStatusExestaff")
    public String RequestsStatusExestaff(HttpSession session, Model model) {
        User currentUser = (User) session.getAttribute("authenticatedUser");
        if (currentUser == null) return "redirect:/";

        if (!"STAFF".equalsIgnoreCase(currentUser.getRole())) {
            return "error/error403";
        }

        model.addAttribute("username", currentUser.getUsername());
        return "requestsStatusExestaff";
    }

    // ✅ Fetch material requests for user's assigned projects
    @GetMapping("/exestaff/requests/getMaterialRequests")
    @ResponseBody
    public List<MaterialRequest> getMaterialRequests(HttpSession session) {
        User user = (User) session.getAttribute("authenticatedUser");

        if (user != null && user.getProject() != null && !user.getProject().isEmpty()) {
            List<String> assignedProjects = List.of(user.getProject().split(","))
                    .stream()
                    .map(String::trim)
                    .collect(Collectors.toList());

            List<MaterialRequest> allRequests = materialRequestRepository.findAll().stream()
                    .filter(req -> assignedProjects.contains(req.getProject().getProjectname()))
                    .collect(Collectors.toList());

            // ❗ Explicitly access nested fields while session is open
            allRequests.forEach(req -> {
                if (req.getInventory() != null) {
                    req.getInventory().getMaterialCategory(); // trigger lazy load
                    req.getInventory().getMaterialName();     // trigger lazy load
                }
                if (req.getStage() != null) {
                    req.getStage().getStageId();              // ✅ trigger lazy load
                    req.getStage().getStageNumber();          // ✅ trigger lazy load
                }
            });


            return allRequests;
        }
        return List.of();
    }

    @GetMapping("/exestaff/requests/projects")
    @ResponseBody
    public List<String> getAssignedProjects(HttpSession session) {
        User user = (User) session.getAttribute("authenticatedUser");
        if (user != null && user.getProject() != null && !user.getProject().isEmpty()) {
            return List.of(user.getProject().split(",\\s*"));
        }
        return List.of();
    }


    // ✅ Direct view rendering route (for fallback or manual navigation)
    @GetMapping("requestsStatusExestaffDirect")
    public String RequestsStatusExestaffDirect() {
        return "requestsStatusExestaff";
    }

    @GetMapping("/exestaff/requests/getStagesByProject")
    @ResponseBody
    public List<Map<String, Object>> getStagesByProject(@RequestParam String projectName) {
        Project project = projectRepository.findByProjectname(projectName);
        if (project == null) return List.of();

        List<ProjectStage> stages = projectStageRepository.findByProject(project);

        return stages.stream()
                .map(stage -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("stageId", stage.getStageId());
                    map.put("stageNumber", stage.getStageNumber()); // ✅ NEEDED FOR SORTING
                    map.put("stageName", "Stage " + stage.getStageNumber());
                    return map;
                })
                .sorted(Comparator.comparingInt(m -> (Integer) m.get("stageNumber"))) // ✅ sorted by stage number
                .collect(Collectors.toList());
    }


    @DeleteMapping("/exestaff/requests/delete/{id}")
    @ResponseBody
    public String deleteMaterialRequest(@PathVariable Long id) {
        Optional<MaterialRequest> optionalRequest = materialRequestRepository.findById(id);
        if (optionalRequest.isPresent()) {
            materialRequestRepository.deleteById(id);
            return "✅ Request deleted successfully.";
        } else {
            return "❌ Request not found.";
        }
    }

    @PostMapping("/exestaff/stages/delete")
    @ResponseBody
    public String deleteStages(@RequestBody List<Integer> stageIds) {
        projectStageRepository.deleteByStageIdIn(stageIds);
        return "✅ Deleted stage IDs: " + stageIds;
    }

}


