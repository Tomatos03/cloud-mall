package com.onlineshop.controller;

import com.onlineshop.framework.models.file.Avatar;
import com.onlineshop.framework.models.notice.Notice;
import com.onlineshop.framework.models.file.IFileUploadService;
import com.onlineshop.framework.models.notice.INoticeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/web/common")
public class CommonController {
    @Autowired
    private IFileUploadService fileUploadService;
    @Autowired
    private INoticeService  noticeService;

    @PostMapping("/upload/image")
    public Avatar uploadImage(@RequestParam("file") MultipartFile file) throws Exception {
        return fileUploadService.uploadImage(file);
    }

    @GetMapping("/notice")
    public List<Notice> getNotice() {
        return noticeService.list();
    }
}