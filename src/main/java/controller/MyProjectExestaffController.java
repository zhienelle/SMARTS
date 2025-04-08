package controller;

import entity.Project;
import entity.User;
import jakarta.servlet.http.HttpSession;
import repository.ProjectRepository;
import repository.UserRepository;
import entity.ProjectInventory;
import org.springframework.web.bind.annotation.ResponseBody;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
public class MyProjectExestaffController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @GetMapping("/myProjectExestaff")
    public String myProjectExestaff(HttpSession session, Model model) {
        User user = (User) session.getAttribute("authenticatedUser");

        if (user == null) {
            return "redirect:/";
        }

        List<Project> assignedProjects = new ArrayList<>();

        if (user.getProject() != null && !user.getProject().isEmpty()) {
            String[] projectNames = user.getProject().split(",\\s*");

            for (String name : projectNames) {
                Project project = projectRepository.findByProjectname(name.trim());
                if (project != null) {
                    // ✅ Force load required fields while session is open
                    project.getCompanyname();
                    project.getCompanyLocation();
                    project.getCompanycontact();

                    if (project.getProjectInventoryList() != null) {
                        project.getProjectInventoryList().size(); // preload list
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


}
