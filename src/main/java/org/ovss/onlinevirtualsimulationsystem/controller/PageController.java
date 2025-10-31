package org.ovss.onlinevirtualsimulationsystem.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.ui.Model;
import java.security.Principal;

@Controller
public class PageController {

    @GetMapping("/login")
    public String login() {
        return "login"; // This will resolve to login.html in the templates directory
    }

    @GetMapping("/home")
    public String home(Model model, Principal principal) {
        if (principal != null) {
            model.addAttribute("username", principal.getName());
        }
        return "home";
    }
}
