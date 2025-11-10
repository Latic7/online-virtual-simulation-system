package org.ovss.onlinevirtualsimulationsystem.controller;

import org.ovss.onlinevirtualsimulationsystem.dto.ModelSnippetDTO;
import org.ovss.onlinevirtualsimulationsystem.dto.ModelViewDTO;
import org.ovss.onlinevirtualsimulationsystem.dto.UserDTO;
import org.ovss.onlinevirtualsimulationsystem.enumeration.AuditStatusEnum;
import org.ovss.onlinevirtualsimulationsystem.enumeration.UserAuthorityEnum;
import org.ovss.onlinevirtualsimulationsystem.service.ModelService;
import org.ovss.onlinevirtualsimulationsystem.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.SortDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;
import java.util.List;

@Controller
public class PageController {

    @Autowired
    private ModelService modelService;

    @Autowired
    private UserService userService;

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

    @GetMapping("/profile")
    public String profile(Model model, Principal principal,
                          @RequestParam(required = false) String search,
                          @RequestParam(required = false) AuditStatusEnum status,
                          @SortDefault(sort = "uploadTime", direction = Sort.Direction.DESC) Sort sort) {
        UserDTO user = userService.findByUsername(principal.getName());
        List<ModelSnippetDTO> models = modelService.getMyModels(user.getUserId(), search, status, sort);
        model.addAttribute("models", models);
        model.addAttribute("username", user.getUserName());

        String sortString = sort.stream()
                .map(order -> order.getProperty() + "," + order.getDirection().name().toLowerCase())
                .findFirst()
                .orElse("uploadTime,desc");
        model.addAttribute("sort", sortString);

        model.addAttribute("search", search);
        model.addAttribute("status", status != null ? status.name() : "");


        return "profile";
    }

    @GetMapping("/admin/dashboard")
    public String adminDashboard(Model model, Principal principal,
                                 @RequestParam(required = false, defaultValue = "") String search,
                                 @RequestParam(required = false) AuditStatusEnum status,
                                 @SortDefault(sort = "uploadTime", direction = Sort.Direction.DESC) Sort sort) {
        model.addAttribute("username", principal.getName());
        List<ModelSnippetDTO> models = modelService.getAllModelsForAdmin(search, status, sort);
        model.addAttribute("models", models);

        String sortString = sort.stream()
                .map(order -> order.getProperty() + "," + order.getDirection().name().toLowerCase())
                .findFirst()
                .orElse("uploadTime,desc");
        model.addAttribute("sort", sortString);

        model.addAttribute("search", search);
        model.addAttribute("status", status != null ? status.name() : null);
        return "admin/dashboard";
    }

    @GetMapping("/models/{modelId}")
    public String modelView(@PathVariable Long modelId, Model model, Principal principal) {
        if (principal != null) {
            model.addAttribute("username", principal.getName());
        }
        ModelViewDTO modelViewDTO = modelService.getModelView(modelId, principal);
        if (modelViewDTO == null) {
            return "error/404";
        }
        model.addAttribute("model", modelViewDTO);
        return "model_view";
    }
}
