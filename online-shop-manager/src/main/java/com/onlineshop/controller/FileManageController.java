package com.onlineshop.controller;

import com.onlineshop.framework.models.file.FileMeta;
import com.onlineshop.framework.models.file.service.IFileService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/manage/files")
@RequiredArgsConstructor
public class FileManageController {
    private final IFileService fileService;

    @PostMapping("/upload")
    public FileMeta upload(@RequestParam("file") MultipartFile file) throws Exception {
        return fileService.upload(file);
    }
}