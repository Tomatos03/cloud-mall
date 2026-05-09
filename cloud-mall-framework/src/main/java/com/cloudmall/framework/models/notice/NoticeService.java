package com.cloudmall.framework.models.notice;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * Implementation of NoticeService.
 *
 * @author : Tomatos
 * @date : 2025/12/20
 */
@Service
public class NoticeService extends ServiceImpl<NoticeMapper, Notice> implements INoticeService {
}
