package org.ovss.onlinevirtualsimulationsystem.controller;

import org.ovss.onlinevirtualsimulationsystem.dto.ModelSnippetDTO;
import org.ovss.onlinevirtualsimulationsystem.dto.ModelViewDTO;
import org.ovss.onlinevirtualsimulationsystem.dto.UserDTO;
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
                          @RequestParam(required = false, defaultValue = "") String search,
                          @RequestParam(required = false, defaultValue = "LIVE") String filterType,
                          @SortDefault(sort = "uploadTime", direction = Sort.Direction.DESC) Sort sort) {
        UserDTO user = userService.findByUsername(principal.getName());
        List<ModelSnippetDTO> models = modelService.getMyModels(user.getUserId(), search, filterType, sort);
        ModelService.ModelOverviewStats stats = modelService.getMyModelOverview(user.getUserId());

        model.addAttribute("models", models);
        model.addAttribute("username", user.getUserName());
        model.addAttribute("totalModelsCount", stats.getTotalCount());
        model.addAttribute("pendingModelsCount", stats.getSecondaryCount());
        model.addAttribute("liveModelsCount", stats.getTertiaryCount());

        String sortString = sort.stream()
                .map(order -> order.getProperty() + "," + order.getDirection().name().toLowerCase())
                .findFirst()
                .orElse("uploadTime,desc");
        model.addAttribute("sort", sortString);

        model.addAttribute("search", search);
        model.addAttribute("filterType", filterType);


        return "profile";
    }

    @GetMapping("/admin/dashboard")
    public String adminDashboard(Model model, Principal principal,
                                 @RequestParam(required = false, defaultValue = "") String search,
                                 @RequestParam(required = false, defaultValue = "PENDING_REVIEW") String filterType,
                                 @SortDefault(sort = "uploadTime", direction = Sort.Direction.DESC) Sort sort) {
        model.addAttribute("username", principal.getName());
        List<ModelSnippetDTO> models = modelService.getAllModelsForAdmin(search, filterType, sort);
        ModelService.ModelOverviewStats stats = modelService.getAdminModelOverview();

        model.addAttribute("models", models);
        model.addAttribute("pendingReviewCount", stats.getTotalCount());
        model.addAttribute("appealingCount", stats.getSecondaryCount());
        model.addAttribute("rejectedModelsCount", stats.getTertiaryCount());

        String sortString = sort.stream()
                .map(order -> order.getProperty() + "," + order.getDirection().name().toLowerCase())
                .findFirst()
                .orElse("uploadTime,desc");
        model.addAttribute("sort", sortString);

        model.addAttribute("search", search);
        model.addAttribute("filterType", filterType);
        return "admin/dashboard";
    }

    @GetMapping("/model/{modelId}")
    public String modelView(@PathVariable Long modelId, Model model, Principal principal) {
        ModelViewDTO modelViewDTO = modelService.getModelView(modelId, principal);
        if (modelViewDTO == null) {
            return "error/404"; // Or a specific error page for unauthorized access
        }

        // Pass the whole DTO to the view
        model.addAttribute("modelInfo", modelViewDTO);
        model.addAttribute("modelUrl", modelViewDTO.getFileAddress()); // Keep this for the script

        // Pass username if logged in
        if (principal != null) {
            model.addAttribute("username", principal.getName());
        }

        return "model_view";
    }
}
