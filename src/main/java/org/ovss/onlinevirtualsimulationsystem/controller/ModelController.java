package org.ovss.onlinevirtualsimulationsystem.controller;

import org.ovss.onlinevirtualsimulationsystem.service.ModelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/models")
public class ModelController {

    @Autowired
    private ModelService modelService;

    @PostMapping("/upload")
    public ResponseEntity<?> uploadModel(@RequestParam("modelName") String modelName,
                                         @RequestParam("thumbnailFile") MultipartFile thumbnailFile,
                                         @RequestParam("modelFile") MultipartFile modelFile,
                                         @RequestParam(value = "tags", required = false) String tags,
                                         @AuthenticationPrincipal UserDetails userDetails) {
        try {
            modelService.uploadModel(modelName, thumbnailFile, modelFile, tags, userDetails.getUsername());
            return ResponseEntity.ok(Map.of("message", "模型上传成功，正在等待审核。"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", e.getMessage()));
        }
    }
}

