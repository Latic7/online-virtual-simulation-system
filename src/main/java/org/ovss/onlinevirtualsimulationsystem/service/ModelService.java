package org.ovss.onlinevirtualsimulationsystem.service;

import org.ovss.onlinevirtualsimulationsystem.dto.ModelSnippetDTO;
import org.ovss.onlinevirtualsimulationsystem.dto.ModelViewDTO;
import org.ovss.onlinevirtualsimulationsystem.entity.ModelEntity;
import org.ovss.onlinevirtualsimulationsystem.entity.ModelTagEntity;
import org.ovss.onlinevirtualsimulationsystem.enumeration.AuditStatusEnum;
import org.ovss.onlinevirtualsimulationsystem.enumeration.UserAuthorityEnum;
import org.ovss.onlinevirtualsimulationsystem.repository.ModelRepository;
import org.ovss.onlinevirtualsimulationsystem.repository.ModelTagRepository;
import org.ovss.onlinevirtualsimulationsystem.repository.TagRepository;
import org.ovss.onlinevirtualsimulationsystem.repository.UserRepository;
import org.ovss.onlinevirtualsimulationsystem.entity.UserEntity;
import org.ovss.onlinevirtualsimulationsystem.entity.TagEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import net.coobird.thumbnailator.Thumbnails;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ModelService {

    @Autowired
    private ModelRepository modelRepository;

    @Autowired
    private ModelTagRepository modelTagRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TagRepository tagRepository;

    private final Path thumbnailStorageLocation = Paths.get("src/main/resources/static/thumbnails");
    private final Path modelStorageLocation = Paths.get("src/main/resources/static/models");

    public ModelService() {
        try {
            Files.createDirectories(this.thumbnailStorageLocation);
            Files.createDirectories(this.modelStorageLocation);
        } catch (Exception ex) {
            throw new RuntimeException("Could not create the directory where the uploaded files will be stored.", ex);
        }
    }

    public List<ModelSnippetDTO> getModelSnippets(String search, Sort sort) {
        List<ModelEntity> models;
        if (StringUtils.hasText(search)) {
            models = modelRepository.searchModels(search, sort);
        } else {
            models = modelRepository.findAll(sort);
        }
        return models.stream().map(this::convertToSnippetDTO).collect(Collectors.toList());
    }

    public ModelViewDTO getModelView(Long modelId, Principal principal) {
        ModelEntity model = modelRepository.findById(modelId).orElse(null);
        if (model == null) {
            return null;
        }

        if (principal == null) { // Anonymous user
            if (model.getAuditStatus() == AuditStatusEnum.APPROVED && model.getIsLive()) {
                return convertToViewDTO(model);
            } else {
                return null;
            }
        } else { // Authenticated user
            UserEntity user = userRepository.findByUserName(principal.getName()).orElse(null);
            if (user == null) {
                return null;
            }

            if (user.getUserAuthority() == UserAuthorityEnum.ADMIN) {
                return convertToViewDTO(model);
            } else { // USER
                if ((model.getAuditStatus() == AuditStatusEnum.APPROVED && model.getIsLive()) || model.getUploader().getUserId().equals(user.getUserId())) {
                    return convertToViewDTO(model);
                } else {
                    return null;
                }
            }
        }
    }

    public List<ModelSnippetDTO> getMyModels(Long userId, String search, AuditStatusEnum status, Sort sort) {
        List<ModelEntity> models;
        if (StringUtils.hasText(search)) {
            models = modelRepository.searchMyModels(userId, search, status, sort);
        } else {
            models = modelRepository.findByUploader_UserIdAndAuditStatus(userId, status, sort);
        }
        return models.stream().map(this::convertToSnippetDTOWithStatus).collect(Collectors.toList());
    }

    public List<ModelSnippetDTO> getAllModelsForAdmin(String search, AuditStatusEnum status, Sort sort) {
        List<ModelEntity> models = modelRepository.searchAllModelsForAdmin(search, status, sort);
        return models.stream().map(this::convertToSnippetDTOWithStatus).collect(Collectors.toList());
    }

    private ModelSnippetDTO convertToSnippetDTOWithStatus(ModelEntity model) {
        ModelSnippetDTO dto = convertToSnippetDTO(model);
        dto.setAuditStatus(model.getAuditStatus().name());
        return dto;
    }

    private ModelSnippetDTO convertToSnippetDTO(ModelEntity model) {
        List<ModelTagEntity> modelTags = modelTagRepository.findByModel_ModelId(model.getModelId());
        List<String> tags = modelTags.stream()
                .map(modelTag -> modelTag.getTag().getTagName())
                .sorted()
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

    @Transactional
    public void uploadModel(String modelName, MultipartFile thumbnailFile, MultipartFile modelFile, String tags, String username) throws IOException {
        UserEntity uploader = userRepository.findByUserName(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));

        if (modelRepository.existsByUploaderAndModelName(uploader, modelName)) {
            throw new RuntimeException("You have already uploaded a model with the same name.");
        }

        String modelExtension = StringUtils.getFilenameExtension(modelFile.getOriginalFilename());
        if (!"glb".equalsIgnoreCase(modelExtension) && !"gltf".equalsIgnoreCase(modelExtension)) {
            throw new RuntimeException("Invalid model file type. Only .glb and .gltf are allowed.");
        }

        // Generate unique filenames to prevent collisions
        String thumbnailFilename = UUID.randomUUID() + "." + StringUtils.getFilenameExtension(thumbnailFile.getOriginalFilename());
        String modelFilename = UUID.randomUUID() + "." + StringUtils.getFilenameExtension(modelFile.getOriginalFilename());

        byte[] thumbnailBytes = thumbnailFile.getBytes();
        if (thumbnailFile.getSize() > 1 * 1024 * 1024) { // If > 1MB
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            Thumbnails.of(thumbnailFile.getInputStream())
                    .size(1280, 720)
                    .outputQuality(0.5)
                    .toOutputStream(outputStream);
            thumbnailBytes = outputStream.toByteArray();
        }

        Files.copy(new ByteArrayInputStream(thumbnailBytes), this.thumbnailStorageLocation.resolve(thumbnailFilename), StandardCopyOption.REPLACE_EXISTING);
        Files.copy(modelFile.getInputStream(), this.modelStorageLocation.resolve(modelFilename), StandardCopyOption.REPLACE_EXISTING);

        ModelEntity newModel = new ModelEntity();
        newModel.setModelName(modelName);
        newModel.setUploader(uploader);
        newModel.setThumbnailAddress("/thumbnails/" + thumbnailFilename);
        newModel.setFileAddress("/models/" + modelFilename);
        newModel.setAuditStatus(AuditStatusEnum.PENDING);
        newModel.setVersion(1);
        newModel.setIsLive(false);
        newModel.setParentModel(null);

        ModelEntity savedModel = modelRepository.save(newModel);

        if (StringUtils.hasText(tags)) {
            Set<String> tagNames = new HashSet<>(Arrays.asList(tags.split("\\s*,\\s*")));
            for (String tagName : tagNames) {
                TagEntity tag = tagRepository.findByTagName(tagName)
                        .orElseGet(() -> {
                            TagEntity newTag = new TagEntity();
                            newTag.setTagName(tagName);
                            return tagRepository.save(newTag);
                        });
                ModelTagEntity modelTag = new ModelTagEntity();
                modelTag.setModel(savedModel);
                modelTag.setTag(tag);
                modelTagRepository.save(modelTag);
            }
        }
    }

    @Transactional
    public void approveModel(Long modelId) {
        ModelEntity model = modelRepository.findById(modelId)
                .orElseThrow(() -> new RuntimeException("未找到ID为 " + modelId + " 的模型"));

        if (model.getAuditStatus() != AuditStatusEnum.PENDING) {
            throw new IllegalStateException("只有状态为“待审核”的模型才能被批准。");
        }

        model.setAuditStatus(AuditStatusEnum.APPROVED);
        model.setIsLive(true);
    }

    @Transactional
    public void rejectModel(Long modelId) {
        ModelEntity model = modelRepository.findById(modelId)
                .orElseThrow(() -> new RuntimeException("未找到ID为 " + modelId + " 的模型"));

        if (model.getAuditStatus() != AuditStatusEnum.PENDING && model.getAuditStatus() != AuditStatusEnum.APPROVED) {
            throw new IllegalStateException("只有状态为“待审核”或“审核通过”的模型才能被驳回。");
        }

        model.setAuditStatus(AuditStatusEnum.REJECTED);
        model.setIsLive(false);
    }

}
