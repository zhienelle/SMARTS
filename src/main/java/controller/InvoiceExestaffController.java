package controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class InvoiceExestaffController {
    @GetMapping("invoiceExestaff")
    public String InvoiceExestaff(){
        return "invoiceExestaff";
    }
}
