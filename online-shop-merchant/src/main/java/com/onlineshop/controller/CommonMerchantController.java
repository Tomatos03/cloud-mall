package com.onlineshop.controller;

import com.onlineshop.framework.models.file.Avatar;
import com.onlineshop.framework.models.file.IFileUploadService;
import com.onlineshop.framework.models.notice.INoticeService;
import com.onlineshop.framework.models.notice.Notice;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/merchant/file")
@RequiredArgsConstructor
public class CommonMerchantController {
    private final IFileUploadService fileUploadService;

    @PostMapping("/upload/image")
    public Avatar uploadImage(@RequestParam("file") MultipartFile file) throws Exception {
        return fileUploadService.uploadImage(file);
    }
}
