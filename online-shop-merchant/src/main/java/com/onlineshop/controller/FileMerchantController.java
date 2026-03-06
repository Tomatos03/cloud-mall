package com.onlineshop.controller;

import com.onlineshop.framework.models.file.FileMeta;
import com.onlineshop.framework.models.file.service.IFileService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/files")
@RequiredArgsConstructor
public class FileMerchantController {
    private final IFileService fileService;

    @PostMapping("/upload")
    public FileMeta uploadImage(@RequestParam("file") MultipartFile file) throws Exception {
        return fileService.upload(file);
    }
}
