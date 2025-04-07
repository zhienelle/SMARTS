package controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ReportsAdminController {
    @GetMapping("reportsAdmin")
    public String ReportsAdmin(){
        return "reportsAdmin";
    }
}
