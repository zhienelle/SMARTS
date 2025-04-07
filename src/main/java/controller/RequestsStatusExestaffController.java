package controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class RequestsStatusExestaffController {
    @GetMapping("requestsStatusExestaff")
    public String RequestStatusExestaff(){
        return "requestsStatusExestaff";
    }
}
