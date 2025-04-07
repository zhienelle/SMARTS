package controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ReportsExestaffController {
    @GetMapping("reportsExestaff")
    public String ReportsExestaff(){
        return "ReportsExestaff";
    }
}
