package controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.boot.web.servlet.error.ErrorController;

@Controller
public class CustomErrorController implements ErrorController {

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request) {
        Integer statusCode = (Integer) request.getAttribute("jakarta.servlet.error.status_code");

        if (statusCode != null) {
            if (statusCode == 404) {
                return "error/error404"; // must be in templates/error/error404.html
            } else if (statusCode == 403) {
                return "error/error403";
            } else if (statusCode == 500) {
                return "error/error500";
            }
        }

        return "error/error"; // fallback
    }

    @RequestMapping("/errorSessionDestroyed")
    public String handleSessionDestroyed() {
        return "error/errorSessionDestroyed"; // Must be in templates/error/errorSessionDestroyed.html
    }

    // Optional: Spring Boot 2.3+ doesn't need this override, but safe to include
    public String getErrorPath() {
        return "/error";
    }
}