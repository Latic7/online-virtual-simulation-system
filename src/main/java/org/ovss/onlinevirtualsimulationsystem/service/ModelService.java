package org.ovss.onlinevirtualsimulationsystem.service;

import org.ovss.onlinevirtualsimulationsystem.dto.ModelSnippetDTO;
import org.ovss.onlinevirtualsimulationsystem.dto.ModelViewDTO;
import org.ovss.onlinevirtualsimulationsystem.entity.ModelEntity;
import org.ovss.onlinevirtualsimulationsystem.entity.ModelTagEntity;
import org.ovss.onlinevirtualsimulationsystem.enumeration.AuditStatusEnum;
import org.ovss.onlinevirtualsimulationsystem.enumeration.LifecycleStatusEnum;
import org.ovss.onlinevirtualsimulationsystem.enumeration.SubmissionTypeEnum;
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
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

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

    private final Path storageRoot = Paths.get("uploads").toAbsolutePath().normalize();
    private final Path thumbnailStorageLocation = storageRoot.resolve("thumbnails");
    private final Path modelStorageLocation = storageRoot.resolve("models");

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

    // Java
    public ModelViewDTO getModelView(Long modelId, Principal principal) {
        Optional<ModelEntity> modelOpt = modelRepository.findById(modelId);
        if (modelOpt.isEmpty()) {
            return null; // Model not found
        }

        ModelEntity model = modelOpt.get();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        boolean isPubliclyVisible = model.getAuditStatus() == AuditStatusEnum.APPROVED
            && model.getLifecycleStatus() == LifecycleStatusEnum.LIVE;

        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            // Anonymous user
            if (isPubliclyVisible) {
                return convertToViewDTO(model);
            }
        } else {
            // Authenticated user
            String username = principal.getName();
            UserEntity user = userRepository.findByUserName(username)
                    .orElseThrow(() -> new RuntimeException("User not found: " + username));

            if (user.getUserAuthority() == UserAuthorityEnum.ADMIN) {
                return convertToViewDTO(model); // Admin can see everything
            }

            if (user.getUserAuthority() == UserAuthorityEnum.USER) {
                boolean isOwner = model.getUploader().getUserId().equals(user.getUserId());
                if (isPubliclyVisible || isOwner) {
                    return convertToViewDTO(model);
                }
            }
        }

        return null; // Access denied
    }


    public List<ModelSnippetDTO> getMyModels(Long userId, String search, String filterType, Sort sort) {
        ModelFilterCriteria criteria = resolveMyModelFilter(filterType);
        String normalizedSearch = StringUtils.hasText(search) ? search.trim() : "";
        List<ModelEntity> models = modelRepository.searchMyModelsByFilter(
                userId,
            normalizedSearch,
                criteria.auditStatus,
                criteria.lifecycleStatus,
                criteria.submissionType,
                criteria.excludedSubmissionType,
                sort
        );
        return models.stream().map(this::convertToSnippetDTOWithStatus).collect(Collectors.toList());
    }

    public ModelOverviewStats getMyModelOverview(Long userId) {
        return new ModelOverviewStats(
                modelRepository.countByUploader_UserId(userId),
                modelRepository.countByUploader_UserIdAndAuditStatus(userId, AuditStatusEnum.PENDING),
                modelRepository.countByUploader_UserIdAndLifecycleStatus(userId, LifecycleStatusEnum.LIVE)
        );
    }

    public ModelOverviewStats getAdminModelOverview() {
        return new ModelOverviewStats(
            modelRepository.countByAuditStatusAndLifecycleStatusAndSubmissionTypeNot(
                AuditStatusEnum.PENDING,
                LifecycleStatusEnum.STAGED,
                SubmissionTypeEnum.APPEAL
            ),
            modelRepository.countByAuditStatusAndLifecycleStatusAndSubmissionType(
                AuditStatusEnum.PENDING,
                LifecycleStatusEnum.STAGED,
                SubmissionTypeEnum.APPEAL
            ),
                modelRepository.countByAuditStatus(AuditStatusEnum.REJECTED)
        );
    }

    public List<ModelSnippetDTO> getAllModelsForAdmin(String search, String filterType, Sort sort) {
        ModelFilterCriteria criteria = resolveAdminModelFilter(filterType);
        List<ModelEntity> models = modelRepository.searchAllModelsForAdminByFilter(
                search,
                criteria.auditStatus,
                criteria.lifecycleStatus,
                criteria.submissionType,
                criteria.excludedSubmissionType,
                sort
        );
        return models.stream().map(this::convertToSnippetDTOWithStatus).collect(Collectors.toList());
    }

    private ModelSnippetDTO convertToSnippetDTOWithStatus(ModelEntity model) {
        ModelSnippetDTO dto = convertToSnippetDTO(model);
        dto.setAuditStatus(model.getAuditStatus().name());
        dto.setLifecycleStatus(model.getLifecycleStatus().name());
        dto.setSubmissionType(model.getSubmissionType().name());
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
        List<String> tags = modelTagRepository.findByModel_ModelId(model.getModelId()).stream()
                .map(modelTag -> modelTag.getTag().getTagName())
                .collect(Collectors.toList());

        return new ModelViewDTO(
                model.getModelName(),
                model.getFileAddress(),
                model.getUploader().getUserName(),
                model.getUploadTime(),
                tags
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
        newModel.setLifecycleStatus(LifecycleStatusEnum.STAGED);
        newModel.setSubmissionType(SubmissionTypeEnum.UPLOAD);
        newModel.setVersion(1);
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
    public void createUpdateRequest(Long sourceModelId,
                                    String modelName,
                                    MultipartFile thumbnailFile,
                                    MultipartFile modelFile,
                                    String tags,
                                    String username) throws IOException {
        UserEntity user = userRepository.findByUserName(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));

        ModelEntity sourceModel = modelRepository.findById(sourceModelId)
                .orElseThrow(() -> new RuntimeException("未找到ID为 " + sourceModelId + " 的模型"));

        if (!sourceModel.getUploader().getUserId().equals(user.getUserId())) {
            throw new IllegalStateException("您只能更新自己上传的模型。");
        }

        if (sourceModel.getAuditStatus() != AuditStatusEnum.APPROVED
            || sourceModel.getLifecycleStatus() != LifecycleStatusEnum.LIVE) {
            throw new IllegalStateException("只有已上线且审核通过的模型才能发起更新请求。");
        }

        if (modelRepository.existsByParentModelAndAuditStatusAndLifecycleStatus(
            sourceModel,
            AuditStatusEnum.PENDING,
            LifecycleStatusEnum.STAGED)) {
            throw new IllegalStateException("该模型已有待审核更新请求，请等待管理员审核后再提交新的更新。");
        }

        boolean hasNewName = StringUtils.hasText(modelName);
        boolean hasNewTags = StringUtils.hasText(tags);
        boolean hasNewThumbnail = thumbnailFile != null && !thumbnailFile.isEmpty();
        boolean hasNewModelFile = modelFile != null && !modelFile.isEmpty();

        if (!hasNewName && !hasNewTags && !hasNewThumbnail && !hasNewModelFile) {
            throw new IllegalStateException("请至少修改一项信息后再提交更新。\n可修改项：名称、缩略图、模型文件、标签。");
        }

        String finalModelName = sourceModel.getModelName();
        if (hasNewName) {
            String normalizedName = modelName.trim();
            if (normalizedName.equals(sourceModel.getModelName())) {
                throw new IllegalStateException("模型名称与旧版本完全相同，请输入新的名称。");
            }
            finalModelName = normalizedName;
        }

        String finalThumbnailAddress = sourceModel.getThumbnailAddress();
        if (hasNewThumbnail) {
            if (isUploadedFileSameAsStored(thumbnailFile, sourceModel.getThumbnailAddress(), this.thumbnailStorageLocation)) {
                throw new IllegalStateException("新缩略图与旧版本完全相同，请重新选择文件。");
            }
            finalThumbnailAddress = saveThumbnailFile(thumbnailFile);
        }

        String finalModelFileAddress = sourceModel.getFileAddress();
        if (hasNewModelFile) {
            validateModelFileType(modelFile);
            if (isUploadedFileSameAsStored(modelFile, sourceModel.getFileAddress(), this.modelStorageLocation)) {
                throw new IllegalStateException("新模型文件与旧版本完全相同，请重新选择文件。");
            }
            finalModelFileAddress = saveModelFile(modelFile);
        }

        Set<String> sourceTags = getTagNamesForModel(sourceModel.getModelId());
        Set<String> finalTags = sourceTags;
        if (hasNewTags) {
            finalTags = parseTagNames(tags);
            if (finalTags.equals(sourceTags)) {
                throw new IllegalStateException("新标签与旧版本完全相同，请修改后再提交。");
            }
        }

        ModelEntity updateModel = new ModelEntity();
        updateModel.setModelName(finalModelName);
        updateModel.setUploader(user);
        updateModel.setThumbnailAddress(finalThumbnailAddress);
        updateModel.setFileAddress(finalModelFileAddress);
        updateModel.setAuditStatus(AuditStatusEnum.PENDING);
        updateModel.setLifecycleStatus(LifecycleStatusEnum.STAGED);
        updateModel.setSubmissionType(SubmissionTypeEnum.UPDATE);
        updateModel.setVersion(sourceModel.getVersion() + 1);
        updateModel.setParentModel(sourceModel);

        ModelEntity savedUpdateModel = modelRepository.save(updateModel);
        saveModelTags(savedUpdateModel, finalTags);
    }

    @Transactional
    public void approveModel(Long modelId) {
        ModelEntity model = modelRepository.findById(modelId)
                .orElseThrow(() -> new RuntimeException("未找到ID为 " + modelId + " 的模型"));

        if (model.getAuditStatus() != AuditStatusEnum.PENDING) {
            throw new IllegalStateException("只有状态为“待审核”的模型才能被批准。");
        }

        ModelEntity parentModel = model.getParentModel();
        if (parentModel != null) {
            parentModel.setLifecycleStatus(LifecycleStatusEnum.LEGACY);
        }

        model.setAuditStatus(AuditStatusEnum.APPROVED);
        model.setLifecycleStatus(LifecycleStatusEnum.LIVE);
    }

    @Transactional
    public void rejectModel(Long modelId) {
        ModelEntity model = modelRepository.findById(modelId)
                .orElseThrow(() -> new RuntimeException("未找到ID为 " + modelId + " 的模型"));

        if (model.getParentModel() != null) {
            if (model.getAuditStatus() != AuditStatusEnum.PENDING) {
                throw new IllegalStateException("只有待审核的更新请求才能被驳回并删除。");
            }

            modelTagRepository.deleteByModel_ModelId(model.getModelId());
            modelRepository.delete(model);
            return;
        }

        if (model.getAuditStatus() != AuditStatusEnum.PENDING && model.getAuditStatus() != AuditStatusEnum.APPROVED) {
            throw new IllegalStateException("只有状态为“待审核”或“审核通过”的模型才能被驳回。");
        }

        model.setAuditStatus(AuditStatusEnum.REJECTED);
        model.setLifecycleStatus(LifecycleStatusEnum.STAGED);
    }

    @Transactional
    public void appealModel(Long modelId, String username) {
        UserEntity user = userRepository.findByUserName(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));

        ModelEntity model = modelRepository.findById(modelId)
                .orElseThrow(() -> new RuntimeException("未找到ID为 " + modelId + " 的模型"));

        if (!model.getUploader().getUserId().equals(user.getUserId())) {
            throw new IllegalStateException("您只能对自己上传的模型发起申诉。");
        }

        if (model.getAuditStatus() != AuditStatusEnum.REJECTED) {
            throw new IllegalStateException("只有状态为“被驳回”的模型才能申诉。当前状态：" + model.getAuditStatus());
        }

        model.setAuditStatus(AuditStatusEnum.PENDING);
        model.setLifecycleStatus(LifecycleStatusEnum.STAGED);
        model.setSubmissionType(SubmissionTypeEnum.APPEAL);
    }

    private String saveThumbnailFile(MultipartFile thumbnailFile) throws IOException {
        String extension = StringUtils.getFilenameExtension(thumbnailFile.getOriginalFilename());
        String thumbnailFilename = UUID.randomUUID() + "." + extension;

        byte[] thumbnailBytes = thumbnailFile.getBytes();
        if (thumbnailFile.getSize() > 1 * 1024 * 1024) {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            Thumbnails.of(thumbnailFile.getInputStream())
                    .size(1280, 720)
                    .outputQuality(0.5)
                    .toOutputStream(outputStream);
            thumbnailBytes = outputStream.toByteArray();
        }

        Files.copy(new ByteArrayInputStream(thumbnailBytes), this.thumbnailStorageLocation.resolve(thumbnailFilename), StandardCopyOption.REPLACE_EXISTING);
        return "/thumbnails/" + thumbnailFilename;
    }

    private String saveModelFile(MultipartFile modelFile) throws IOException {
        String extension = StringUtils.getFilenameExtension(modelFile.getOriginalFilename());
        String modelFilename = UUID.randomUUID() + "." + extension;
        Files.copy(modelFile.getInputStream(), this.modelStorageLocation.resolve(modelFilename), StandardCopyOption.REPLACE_EXISTING);
        return "/models/" + modelFilename;
    }

    private void validateModelFileType(MultipartFile modelFile) {
        String modelExtension = StringUtils.getFilenameExtension(modelFile.getOriginalFilename());
        if (!"glb".equalsIgnoreCase(modelExtension) && !"gltf".equalsIgnoreCase(modelExtension)) {
            throw new RuntimeException("Invalid model file type. Only .glb and .gltf are allowed.");
        }
    }

    private Set<String> parseTagNames(String tags) {
        return Arrays.stream(tags.split("\\s*,\\s*"))
                .map(String::trim)
                .filter(StringUtils::hasText)
                .collect(Collectors.toCollection(java.util.TreeSet::new));
    }

    private Set<String> getTagNamesForModel(Long modelId) {
        return modelTagRepository.findByModel_ModelId(modelId).stream()
                .map(modelTag -> modelTag.getTag().getTagName())
                .collect(Collectors.toCollection(java.util.TreeSet::new));
    }

    private void saveModelTags(ModelEntity model, Set<String> tagNames) {
        for (String tagName : tagNames) {
            TagEntity tag = tagRepository.findByTagName(tagName)
                    .orElseGet(() -> {
                        TagEntity newTag = new TagEntity();
                        newTag.setTagName(tagName);
                        return tagRepository.save(newTag);
                    });

            ModelTagEntity modelTag = new ModelTagEntity();
            modelTag.setModel(model);
            modelTag.setTag(tag);
            modelTagRepository.save(modelTag);
        }
    }

    private boolean isUploadedFileSameAsStored(MultipartFile uploadedFile, String oldWebPath, Path storageLocation) {
        try {
            String oldFilename = Paths.get(oldWebPath).getFileName().toString();
            Path oldFilePath = storageLocation.resolve(oldFilename).normalize();
            if (!Files.exists(oldFilePath)) {
                return false;
            }

            byte[] oldBytes = Files.readAllBytes(oldFilePath);
            byte[] newBytes = uploadedFile.getBytes();
            return MessageDigest.isEqual(sha256(oldBytes), sha256(newBytes));
        } catch (Exception ex) {
            return false;
        }
    }

    private byte[] sha256(byte[] input) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        return digest.digest(input);
    }

    private ModelFilterCriteria resolveMyModelFilter(String filterType) {
        if (!StringUtils.hasText(filterType) || "LIVE".equalsIgnoreCase(filterType)) {
            return new ModelFilterCriteria(AuditStatusEnum.APPROVED, LifecycleStatusEnum.LIVE, null, null);
        }

        if ("UNDER_REVIEW".equalsIgnoreCase(filterType)) {
            return new ModelFilterCriteria(AuditStatusEnum.PENDING, LifecycleStatusEnum.STAGED, null, SubmissionTypeEnum.APPEAL);
        }

        if ("REJECTED".equalsIgnoreCase(filterType)) {
            return new ModelFilterCriteria(AuditStatusEnum.REJECTED, LifecycleStatusEnum.STAGED, null, null);
        }

        if ("APPEALING".equalsIgnoreCase(filterType)) {
            return new ModelFilterCriteria(AuditStatusEnum.PENDING, LifecycleStatusEnum.STAGED, SubmissionTypeEnum.APPEAL, null);
        }

        if ("LEGACY".equalsIgnoreCase(filterType)) {
            return new ModelFilterCriteria(null, LifecycleStatusEnum.LEGACY, null, null);
        }

        return new ModelFilterCriteria(AuditStatusEnum.APPROVED, LifecycleStatusEnum.LIVE, null, null);
    }

    private ModelFilterCriteria resolveAdminModelFilter(String filterType) {
        if (!StringUtils.hasText(filterType) || "PENDING_REVIEW".equalsIgnoreCase(filterType)) {
            return new ModelFilterCriteria(AuditStatusEnum.PENDING, LifecycleStatusEnum.STAGED, null, SubmissionTypeEnum.APPEAL);
        }

        if ("APPEALING".equalsIgnoreCase(filterType)) {
            return new ModelFilterCriteria(AuditStatusEnum.PENDING, LifecycleStatusEnum.STAGED, SubmissionTypeEnum.APPEAL, null);
        }

        if ("REJECTED".equalsIgnoreCase(filterType)) {
            return new ModelFilterCriteria(AuditStatusEnum.REJECTED, LifecycleStatusEnum.STAGED, null, null);
        }

        return new ModelFilterCriteria(AuditStatusEnum.PENDING, LifecycleStatusEnum.STAGED, null, SubmissionTypeEnum.APPEAL);
    }

    private static class ModelFilterCriteria {
        private final AuditStatusEnum auditStatus;
        private final LifecycleStatusEnum lifecycleStatus;
        private final SubmissionTypeEnum submissionType;
        private final SubmissionTypeEnum excludedSubmissionType;

        private ModelFilterCriteria(AuditStatusEnum auditStatus,
                                    LifecycleStatusEnum lifecycleStatus,
                                    SubmissionTypeEnum submissionType,
                                    SubmissionTypeEnum excludedSubmissionType) {
            this.auditStatus = auditStatus;
            this.lifecycleStatus = lifecycleStatus;
            this.submissionType = submissionType;
            this.excludedSubmissionType = excludedSubmissionType;
        }
    }

    public static class ModelOverviewStats {
        private final long totalCount;
        private final long secondaryCount;
        private final long tertiaryCount;

        public ModelOverviewStats(long totalCount, long secondaryCount, long tertiaryCount) {
            this.totalCount = totalCount;
            this.secondaryCount = secondaryCount;
            this.tertiaryCount = tertiaryCount;
        }

        public long getTotalCount() {
            return totalCount;
        }

        public long getSecondaryCount() {
            return secondaryCount;
        }

        public long getTertiaryCount() {
            return tertiaryCount;
        }
    }

}
