package org.ovss.onlinevirtualsimulationsystem.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {

    @GetMapping("/login")
    public String login() {
        return "login"; // This will resolve to login.html in the templates directory
    }
}

