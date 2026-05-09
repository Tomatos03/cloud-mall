package com.cloudmall.framework.models.file.service.impl;

import com.cloudmall.framework.common.enums.BizErrorCode;
import com.cloudmall.framework.exception.BizException;
import com.cloudmall.framework.models.file.FileMeta;
import com.cloudmall.framework.models.file.FileProperties;
import com.cloudmall.framework.models.file.service.IFileService;
import com.cloudmall.framework.utils.AssertUtils;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.http.Method;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

/**
 * MinIO 文件服务实现
 * 当 file.storage-type=minio 时此服务生效
 *
 * @author : Tomatos
 * @date : 2026/03/03
 */
@Service
@ConditionalOnProperty(name = "file.storage-type", havingValue = "MINIO")
public class MinIOFileService implements IFileService {
    @Autowired
    private FileProperties fileProperties;
    
    @Autowired
    private MinioClient minioClient;
    
    @Override
    public FileMeta upload(MultipartFile file) throws Exception {
        validateFile(file);
        String ext = getExtension(file.getOriginalFilename());
        String fileName = generateUniqueFileName(ext);
        
        try {
            FileProperties.Minio minioConfig = fileProperties.getMinio();
            
            // 构建上传参数
            PutObjectArgs args = PutObjectArgs.builder()
                    .bucket(minioConfig.getBucketName())
                    .object(fileName)
                    .stream(file.getInputStream(), file.getSize(), -1)
                    .contentType(file.getContentType())
                    .build();
            
            // 上传文件到MinIO
            minioClient.putObject(args);
            
            // 生成预签名URL（长期有效）
            String url = minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(minioConfig.getBucketName())
                            .object(fileName)
                            .build()
            );
            
            return new FileMeta(url);
        } catch (Exception e) {
            throw new BizException(BizErrorCode.FILE_SAVE_FAILED);
        }
    }
    
    /**
     * 验证文件
     */
    private void validateFile(MultipartFile file) {
        AssertUtils.notNull(file, BizErrorCode.FILE_EMPTY);
        AssertUtils.isFalse(file.isEmpty(), BizErrorCode.FILE_EMPTY);
        
        FileProperties.Upload uploadConfig = fileProperties.getUpload();
        AssertUtils.isTrue(file.getSize() <= uploadConfig.getMaxSize(), BizErrorCode.FILE_SIZE_EXCEEDS_LIMIT);
        validateFileExtension(file);
        validateFileMimeType(file);
    }
    
    /**
     * 验证文件扩展名
     */
    private void validateFileExtension(MultipartFile file) {
        String originalName = file.getOriginalFilename();
        AssertUtils.assertNotBlank(originalName, BizErrorCode.FILE_EMPTY);
        String ext = getExtension(originalName);
        
        FileProperties.Upload uploadConfig = fileProperties.getUpload();
        AssertUtils.isIn(ext, uploadConfig.getAllowedExtensions().toArray(new String[0]), BizErrorCode.FILE_EXTENSION_NOT_ALLOWED);
    }
    
    /**
     * 验证文件MIME类型
     */
    private void validateFileMimeType(MultipartFile file) {
        String mime = file.getContentType();
        
        FileProperties.Upload uploadConfig = fileProperties.getUpload();
        AssertUtils.isIn(mime, uploadConfig.getAllowedMimeTypes().toArray(new String[0]), BizErrorCode.FILE_MIME_TYPE_INVALID);
    }
    
    /**
     * 生成唯一的文件名
     */
    private String generateUniqueFileName(String ext) {
        return UUID.randomUUID().toString().replace("-", "") + "." + ext;
    }
    
    /**
     * 获取文件扩展名
     */
    private String getExtension(String filename) {
        int idx = filename.lastIndexOf('.');
        return (idx > 0) ? filename.substring(idx + 1).toLowerCase() : "";
    }
}
