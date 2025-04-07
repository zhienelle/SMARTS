package controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class InvoiceAdminController {
    @GetMapping("invoiceAdmin")
    public String InvoiceAdmin(){
        return "invoiceAdmin";
    }
}
