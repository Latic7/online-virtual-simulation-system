package org.ovss.onlinevirtualsimulationsystem.controller;

import org.ovss.onlinevirtualsimulationsystem.dto.ModelSnippetDTO;
import org.ovss.onlinevirtualsimulationsystem.service.ModelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.SortDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;
import java.util.List;

@Controller
public class PageController {

    @Autowired
    private ModelService modelService;

    @GetMapping("/login")
    public String login() {
        return "login"; // This will resolve to login.html in the templates directory
    }

    @GetMapping("/register")
    public String register() {
        return "register";
    }

    @GetMapping("/home")
    public String home(Model model, Principal principal,
                       @RequestParam(required = false) String search,
                       @SortDefault(sort = "uploadTime", direction = Sort.Direction.DESC) Sort sort) {
        if (principal != null) {
            model.addAttribute("username", principal.getName());
        }
        List<ModelSnippetDTO> models = modelService.getModelSnippets(search, sort);
        model.addAttribute("models", models);

        String sortString = sort.stream()
                .map(order -> order.getProperty() + "," + order.getDirection().name().toLowerCase())
                .findFirst()
                .orElse("uploadTime,desc");
        model.addAttribute("sort", sortString);

        model.addAttribute("search", search);
        return "home";
    }
}
