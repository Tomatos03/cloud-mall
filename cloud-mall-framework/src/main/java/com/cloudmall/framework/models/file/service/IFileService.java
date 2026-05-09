package com.cloudmall.framework.models.file.service;

import com.cloudmall.framework.models.file.FileMeta;
import org.springframework.web.multipart.MultipartFile;

public interface IFileService {
    FileMeta upload(MultipartFile file) throws Exception;
}