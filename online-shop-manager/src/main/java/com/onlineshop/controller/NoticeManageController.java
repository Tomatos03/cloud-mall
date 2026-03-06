package com.onlineshop.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.onlineshop.framework.models.notice.INoticeService;
import com.onlineshop.framework.models.notice.Notice;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for managing notices.
 *
 * @author : Tomatos
 * @since : 2025/12/20
 */
@RestController
@RequestMapping("/notice")
@PreAuthorize("hasAuthority('notice:view')")
public class NoticeManageController {
    @Autowired
    private INoticeService noticeService;

    @GetMapping("/page")
    public IPage<Notice> fetchNoticePage(@RequestParam("page") int page,
                                         @RequestParam("pageSize") int size) {
        return noticeService.page(new Page<>(page, size));
    }

    @PostMapping("/add")
    @PreAuthorize("hasAuthority('notice:add')")
    public boolean addNotice(@RequestBody Notice notice) {
        return noticeService.save(notice);
    }

    @PostMapping("/update/{id}")
    @PreAuthorize("hasAuthority('notice:edit')")
    public boolean updateNotice(@PathVariable Long id, @RequestBody Notice notice) {
        notice.setId(id);
        return noticeService.updateById(notice);
    }

    @PostMapping("/delete/{id}")
    @PreAuthorize("hasAuthority('notice:delete')")
    public boolean deleteNotice(@PathVariable Long id) {
        return noticeService.removeById(id);
    }

    @PostMapping("/batch/del")
    @PreAuthorize("hasAuthority('notice:delete')")
    public boolean batchDeleteNotice(@RequestBody List<Long> ids) {
        return noticeService.removeByIds(ids);
    }
}