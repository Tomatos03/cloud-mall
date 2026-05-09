package com.cloudmall.framework.models.file.service.impl;

import com.cloudmall.framework.common.enums.BizErrorCode;
import com.cloudmall.framework.exception.BizException;
import com.cloudmall.framework.models.file.FileMeta;
import com.cloudmall.framework.models.file.FileProperties;
import com.cloudmall.framework.models.file.service.IFileService;
import com.cloudmall.framework.utils.AssertUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

/**
 * 本地文件服务实现
 * 当 file.storage-type=local 时此服务生效
 *
 * @author : Tomatos
 * @date : 2025/12/21
 */
@Service
@ConditionalOnProperty(name = "file.storage-type", havingValue = "LOCAL", matchIfMissing = true)
public class LocalFileService implements IFileService {
    @Autowired
    private FileProperties fileProperties;

    public void ensureUploadDirExist(String uploadDirPath) {
        File dir = new File(uploadDirPath);
        if (!dir.exists() && !dir.mkdirs()) {
            throw new BizException(BizErrorCode.UPLOAD_DIR_CREATE_FAILED);
        }
    }

    @Override
    public FileMeta upload(MultipartFile file) {
        validateFile(file);
        String ext = getExtension(file.getOriginalFilename());
        String newName = generateUniqueFileName(ext);
        
        FileProperties.Local localConfig = fileProperties.getLocal();
        ensureUploadDirExist(localConfig.getUploadDir());
        
        File dest = new File(localConfig.getUploadDir(), newName);
        saveFile(file, dest);
        return buildFileMeta(newName, localConfig.getEndpoint());
    }

    private void validateFile(MultipartFile file) {
        AssertUtils.notNull(file, BizErrorCode.FILE_EMPTY);
        AssertUtils.isFalse(file.isEmpty(), BizErrorCode.FILE_EMPTY);
        
        FileProperties.Upload uploadConfig = fileProperties.getUpload();
        AssertUtils.isTrue(file.getSize() <= uploadConfig.getMaxSize(), BizErrorCode.FILE_SIZE_EXCEEDS_LIMIT);
        validateFileExtension(file);
        validateFileMimeType(file);
    }

    private void validateFileExtension(MultipartFile file) {
        String originalName = file.getOriginalFilename();
        AssertUtils.assertNotBlank(originalName, BizErrorCode.FILE_EMPTY);
        String ext = getExtension(originalName);
        
        FileProperties.Upload uploadConfig = fileProperties.getUpload();
        AssertUtils.isIn(ext, uploadConfig.getAllowedExtensions().toArray(new String[0]), BizErrorCode.FILE_EXTENSION_NOT_ALLOWED);
    }

    private void validateFileMimeType(MultipartFile file) {
        String mime = file.getContentType();
        
        FileProperties.Upload uploadConfig = fileProperties.getUpload();
        AssertUtils.isIn(mime, uploadConfig.getAllowedMimeTypes().toArray(new String[0]), BizErrorCode.FILE_MIME_TYPE_INVALID);
    }

    private String generateUniqueFileName(String ext) {
        String newName = generateUniqueName(ext);
        FileProperties.Local localConfig = fileProperties.getLocal();
        File dest = new File(localConfig.getUploadDir(), newName);
        while (dest.exists()) {
            newName = generateUniqueName(ext);
            dest = new File(localConfig.getUploadDir(), newName);
        }
        return newName;
    }

    private void saveFile(MultipartFile file, File dest) {
        try {
            Files.copy(file.getInputStream(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new BizException(BizErrorCode.FILE_SAVE_FAILED);
        }
    }

    private FileMeta buildFileMeta(String fileName, String endpoint) {
        return new FileMeta(normalizationEndPoint(endpoint) + fileName);
    }

    private String normalizationEndPoint(String prefix) {
        return prefix.endsWith("/") ? prefix : prefix + "/";
    }

    private String getExtension(String filename) {
        int idx = filename.lastIndexOf('.');
        return (idx > 0) ? filename.substring(idx + 1).toLowerCase() : "";
    }

    private String generateUniqueName(String ext) {
        return UUID.randomUUID().toString().replace("-", "") + "." + ext;
    }
}
