package controller;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SettingsExestaffController {
    @GetMapping("settingsExestaff")
    public String SettingsExestaff(){
        return "settingsExestaff";
    }
}
