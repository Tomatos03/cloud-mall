package com.cloudmall.controller;

import com.cloudmall.framework.models.file.FileMeta;
import com.cloudmall.framework.models.file.service.IFileService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/web/files")
@RequiredArgsConstructor
public class FileWebController {
    private final IFileService fileService;

    @PostMapping("/upload")
    public FileMeta upload(@RequestParam("file") MultipartFile file) throws Exception {
        return fileService.upload(file);
    }
}