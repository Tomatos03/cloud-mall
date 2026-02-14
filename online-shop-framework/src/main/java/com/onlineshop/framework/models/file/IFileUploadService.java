package com.onlineshop.framework.models.file;

import org.springframework.web.multipart.MultipartFile;

public interface IFileUploadService {
    Avatar uploadImage(MultipartFile file) throws Exception;
}