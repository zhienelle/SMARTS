package controller;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = {"config", "controller", "entity", "repository", "service"})
@EntityScan("entity")
@EnableJpaRepositories(basePackages = "repository")

public class SmartsApplication {
    public static void main(String[] args) {
        SpringApplication.run(SmartsApplication.class, args);
    }
}

//SE SOFTWARE ENGINEERING:
//
//-Change the date to MM/DD/YR. BAYLON ✅
//
//-Add the logo of the company. On the website. On the top right FRANCISCO ✅
//
//-They should be right justified. And if it exceeds 999 there should be commas in the stock available and cost MAGTANONG AND BAYLON ❌
//
//-Ask the client on what is the right number on each material when low on stocks. (We should know kung anong stocks yung may minimum na low stock di sila pare parehas) TABANGAY ✅
//
//-The checkbox design. I don't think that's good. Make it a drop-down sa categories. FRANCISCO ✅
//
//-Account creation should have a dropdown ❌
//It's better if your message includes the project name where the staff is already assigned. (on error page if creating a new account) MANGALI ❌
//
//-Ipop up a warning message if it is still ongoing project bawal maarchive MAGTANONG ❌
//
//-Add additional column instead of add delete edit progress BAYLON
//
//-should be a notification, “Are you sure you want to archive?” Magtanong ❌
//
//-Your way to add additional products should be different, not manually editing the quantity. There should be a editable column wherein the user will be able to add or edit when adding a material pcs LO ✅
//
//-Dapat isang project pili nalang siya ng materials, then after requesting the materials it should have a checkout receipt summarizing the request. MANGALI
//
//-Also, if nag doble yung request, dapat editable and it should have a remove request. And it should have been prompted earlier that I already requested it (if doble). DAVID ✅
//
//-The invoice should be restricted if the stages is not complete. But if small diretso invoice na. MARC ✅
//
//-Also the invoice filename number should be the date today or date duration of the project, not just random numbers. LO ✅
//
//-Per stages ng project dapat nandiyan so di manual input yung dates. DAVID ✅