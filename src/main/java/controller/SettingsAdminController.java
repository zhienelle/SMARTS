package controller;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SettingsAdminController {
    @GetMapping("settingsAdmin")
    public String SettingsAdmin(){
        return "settingsAdmin";
    }
}
