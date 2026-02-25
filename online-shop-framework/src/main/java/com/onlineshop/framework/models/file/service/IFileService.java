package com.onlineshop.framework.models.file.service;

import com.onlineshop.framework.models.file.FileMeta;
import org.springframework.web.multipart.MultipartFile;

public interface IFileService {
    FileMeta upload(MultipartFile file) throws Exception;
}