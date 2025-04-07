package controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomepageGenconController {
    @GetMapping("homepageGencon")
    public String HomepageGencon() {
        return "homepageGencon";
    }
}
