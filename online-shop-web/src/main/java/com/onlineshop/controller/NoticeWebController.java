package com.onlineshop.controller;

import com.onlineshop.framework.models.notice.Notice;
import com.onlineshop.framework.models.notice.NoticeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 *
 *
 * @author : Tomatos
 * @date : 2026/3/3
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/notice")
public class NoticeWebController {
    private final NoticeService noticeService;

    @GetMapping
    public List<Notice> getNotice() {
        return noticeService.list();
    }
}