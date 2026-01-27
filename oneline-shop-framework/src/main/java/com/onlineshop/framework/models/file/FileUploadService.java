package com.onlineshop.framework.models.file;

import com.onlineshop.framework.common.enums.BizErrorCode;
import com.onlineshop.framework.exception.BusinessException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Objects;
import java.util.UUID;

@Service
public class FileUploadService implements IFileUploadService {
    @Value("${file.upload-dir:/home/Tomatos/Pictures/upload/image}")
    private String uploadDir;

    private static final long MAX_SIZE_5MB = 5 * 1024 * 1024; // 5MB

    private static final String[] ALLOWED_EXT = {"jpg", "jpeg", "png"};
    private static final String[] ALLOWED_MIME = {"image/jpeg", "image/png"};

    @PostConstruct
    public void init() {
        File dir = new File(uploadDir);
        if (!dir.exists()) {
            boolean created = dir.mkdirs();
            if (!created) {
                throw new BusinessException(BizErrorCode.UPLOAD_DIR_CREATE_FAILED);
            }
        }
    }

    @Override
    public Avatar uploadImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(BizErrorCode.FILE_EMPTY);
        }
        if (file.getSize() > MAX_SIZE_5MB) {
            throw new BusinessException(BizErrorCode.FILE_SIZE_EXCEEDS_LIMIT);
        }
        String originalName = Objects.requireNonNull(file.getOriginalFilename());
        String ext = getExtension(originalName);
        if (!isAllowedExt(ext)) {
            throw new BusinessException(BizErrorCode.FILE_EXTENSION_NOT_ALLOWED);
        }
        String mime = file.getContentType();
        if (!isAllowedMime(mime)) {
            throw new BusinessException(BizErrorCode.FILE_MIME_TYPE_INVALID);
        }
        String newName = generateUniqueName(ext);
        File dest = new File(uploadDir, newName);
        // 防止极端并发下冲突
        while (dest.exists()) {
            newName = generateUniqueName(ext);
            dest = new File(uploadDir, newName);
        }
        try {
            Files.copy(file.getInputStream(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new BusinessException(BizErrorCode.FILE_SAVE_FAILED);
        }
        return new Avatar("/uploads/image/" + newName);
    }

    private String getExtension(String filename) {
        int idx = filename.lastIndexOf('.');
        return (idx > 0) ? filename.substring(idx + 1).toLowerCase() : "";
    }

    private boolean isAllowedExt(String ext) {
        for (String allow : ALLOWED_EXT) {
            if (allow.equals(ext)) return true;
        }
        return false;
    }

    private boolean isAllowedMime(String mime) {
        for (String allow : ALLOWED_MIME) {
            if (allow.equals(mime)) return true;
        }
        return false;
    }

    private String generateUniqueName(String ext) {
        return UUID.randomUUID().toString().replace("-", "") + "." + ext;
    }
}