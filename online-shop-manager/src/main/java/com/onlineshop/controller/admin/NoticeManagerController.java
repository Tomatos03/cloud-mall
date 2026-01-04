package com.onlineshop.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.onlineshop.framework.models.notice.Notice;
import com.onlineshop.framework.models.notice.INoticeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for managing notices.
 *
 * @author : Tomatos
 * @since : 2025/12/20
 */
@RestController
@RequestMapping("/manager/admin/notice")
public class NoticeManagerController {

    @Autowired
    private INoticeService noticeService;

    @GetMapping("/page")
    public IPage<Notice> fetchNoticePage(@RequestParam("page") int page,
                                         @RequestParam("pageSize") int size) {
        return noticeService.page(new Page<>(page, size));
    }

    @PostMapping("/add")
    public boolean addNotice(@RequestBody Notice notice) {
        return noticeService.save(notice);
    }

    @PostMapping("/update/{id}")
    public boolean updateNotice(@PathVariable Long id, @RequestBody Notice notice) {
        notice.setId(id);
        return noticeService.updateById(notice);
    }

    @PostMapping("/delete/{id}")
    public boolean deleteNotice(@PathVariable Long id) {
        return noticeService.removeById(id);
    }

    @PostMapping("/batch/del")
    public boolean batchDeleteNotice(@RequestBody List<Long> ids) {
        return noticeService.removeByIds(ids);
    }
}
