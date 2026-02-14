package com.onlineshop.controller;

import com.onlineshop.framework.models.file.Avatar;
import com.onlineshop.framework.models.file.IFileUploadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/manage/file")
public class FileManageController {
    @Autowired
    private IFileUploadService fileUploadService;

    @PostMapping("/upload/image")
    public Avatar uploadImage(@RequestParam("file") MultipartFile file) throws Exception {
        return fileUploadService.uploadImage(file);
    }
}