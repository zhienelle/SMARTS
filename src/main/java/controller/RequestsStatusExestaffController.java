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

import java.util.*;

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
    // ✅ Updated to explicitly map all nested fields as primitives
    @GetMapping("/exestaff/requests/getMaterialRequests")
    @ResponseBody
    public List<Map<String, Object>> getMaterialRequests(HttpSession session) {
        User user = (User) session.getAttribute("authenticatedUser");

        if (user != null && user.getProject() != null && !user.getProject().isEmpty()) {
            List<String> assignedProjects = List.of(user.getProject().split(","))
                    .stream()
                    .map(String::trim)
                    .collect(Collectors.toList());

            List<MaterialRequest> allRequests = materialRequestRepository.findAll().stream()
                    .filter(req -> assignedProjects.contains(req.getProject().getProjectname()))
                    .collect(Collectors.toList());

            List<Map<String, Object>> mappedRequests = new ArrayList<>();

            for (MaterialRequest req : allRequests) {
                Map<String, Object> map = new HashMap<>();
                map.put("materialRequestId", req.getMaterialRequestId());
                map.put("materialStock", req.getMaterialStock());
                map.put("materialCategory", req.getMaterialCategory());
                map.put("materialName", req.getMaterialName());
                map.put("materialRequestStatus", req.getMaterialRequestStatus());
                map.put("requestDate", req.getRequestDate());

                if (req.getStage() != null) {
                    Map<String, Object> stageMap = new HashMap<>();
                    stageMap.put("stageId", req.getStage().getStageId());
                    stageMap.put("stageNumber", req.getStage().getStageNumber());
                    map.put("stage", stageMap);
                }

                if (req.getInventory() != null) {
                    map.put("inventory", Map.of(
                            "materialCategory", req.getInventory().getMaterialCategory(),
                            "materialName", req.getInventory().getMaterialName()
                    ));
                }

                map.put("project", Map.of("projectname", req.getProject().getProjectname()));

                mappedRequests.add(map);
            }

            return mappedRequests;
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


