package org.ovss.onlinevirtualsimulationsystem.service;

import org.ovss.onlinevirtualsimulationsystem.dto.ModelSnippetDTO;
import org.ovss.onlinevirtualsimulationsystem.dto.ModelViewDTO;
import org.ovss.onlinevirtualsimulationsystem.entity.ModelEntity;
import org.ovss.onlinevirtualsimulationsystem.entity.ModelTagEntity;
import org.ovss.onlinevirtualsimulationsystem.repository.ModelRepository;
import org.ovss.onlinevirtualsimulationsystem.repository.ModelTagRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ModelService {

    @Autowired
    private ModelRepository modelRepository;

    @Autowired
    private ModelTagRepository modelTagRepository;

    public List<ModelSnippetDTO> getModelSnippets(String search, Sort sort) {
        List<ModelEntity> models;
        if (StringUtils.hasText(search)) {
            models = modelRepository.searchModels(search, sort);
        } else {
            models = modelRepository.findAll(sort);
        }
        return models.stream().map(this::convertToSnippetDTO).collect(Collectors.toList());
    }

    public ModelViewDTO getModelView(Long modelId) {
        ModelEntity model = modelRepository.findById(modelId).orElse(null);
        if (model == null) {
            return null;
        }
        return convertToViewDTO(model);
    }

    private ModelSnippetDTO convertToSnippetDTO(ModelEntity model) {
        List<ModelTagEntity> modelTags = modelTagRepository.findByModel_ModelId(model.getModelId());
        List<String> tags = modelTags.stream()
                .map(modelTag -> modelTag.getTag().getTagName())
                .limit(3)
                .collect(Collectors.toList());

        return new ModelSnippetDTO(
                model.getModelId(),
                model.getModelName(),
                tags,
                model.getUploadTime(),
                model.getUploader().getUserName(),
                model.getThumbnailAddress(),
                model.getFileAddress()
        );
    }

    private ModelViewDTO convertToViewDTO(ModelEntity model) {
        return new ModelViewDTO(
                model.getModelName(),
                model.getFileAddress(),
                model.getUploader().getUserName(),
                model.getUploadTime()
        );
    }
}
