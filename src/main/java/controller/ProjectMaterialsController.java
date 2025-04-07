package controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ProjectMaterialsController {
    @GetMapping("projectMaterials")
    public String ProjectMaterials(){
        return "projectMaterials";
    }
    //test
}
