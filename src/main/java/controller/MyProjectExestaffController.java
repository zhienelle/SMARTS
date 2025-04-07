package controller;

import entity.Project;
import entity.User;
import jakarta.servlet.http.HttpSession;
import repository.ProjectRepository;
import repository.UserRepository;

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
            return "redirect:/"; // or redirect to login
        }

        List<Project> assignedProjects = new ArrayList<>();

        if (user.getProject() != null && !user.getProject().isEmpty()) {
            String[] projectNames = user.getProject().split(",\\s*");
            for (String name : projectNames) {
                Project project = projectRepository.findByProjectname(name.trim());
                if (project != null) {
                    assignedProjects.add(project);
                }
            }
        }

        model.addAttribute("projects", assignedProjects);
        return "myProjectExestaff";
    }
}
