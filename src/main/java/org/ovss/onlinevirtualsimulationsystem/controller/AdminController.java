package org.ovss.onlinevirtualsimulationsystem.controller;

import org.ovss.onlinevirtualsimulationsystem.service.ModelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    @Autowired
    private ModelService modelService;

    @PostMapping("/models/{modelId}/approve")
    public ResponseEntity<?> approveModel(@PathVariable Long modelId) {
        try {
            modelService.approveModel(modelId);
            return ResponseEntity.ok(Map.of("message", "模型已批准"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/models/{modelId}/reject")
    public ResponseEntity<?> rejectModel(@PathVariable Long modelId) {
        try {
            modelService.rejectModel(modelId);
            return ResponseEntity.ok(Map.of("message", "模型已驳回"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}

