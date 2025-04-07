package controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class RequestsAdminController {
    @GetMapping("requestsAdmin")
    public String RequestAdmin(){
        return "requestsAdmin";
    }
}
