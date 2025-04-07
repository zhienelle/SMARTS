package controller;

import entity.Project;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import repository.ProjectRepository;

import java.util.List;
import java.util.Optional;

@Controller
public class ProjectsAdminController {

    @Autowired
    private ProjectRepository projectRepository;

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
}