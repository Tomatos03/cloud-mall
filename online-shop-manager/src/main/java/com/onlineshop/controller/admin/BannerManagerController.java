package com.onlineshop.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.onlineshop.framework.models.banner.IBannerService;
import com.onlineshop.framework.models.banner.dto.BannerDTO;
import com.onlineshop.framework.models.banner.vo.BannerVO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 轮播图控制器
 *
 * @author Tomatos
 * @date 2025/12/17
 */
@RestController
@RequestMapping("/manager/admin/banner")
public class BannerManagerController {

    @Autowired
    private IBannerService bannerService;

    /**
     * 创建或更新轮播图
     *
     * @param dto 轮播图创建/更新DTO
     * @return 是否成功
     */
    @PostMapping
    public boolean save(@Valid @RequestBody BannerDTO dto) {
        return bannerService.saveOrUpdateBanner(dto);
    }

    /**
     * 批量删除轮播图
     *
     * @param ids 轮播图ID列表
     * @return 是否成功
     */
    @DeleteMapping("/batch/del")
    public boolean deleteBatch(@RequestParam("ids")List<Long> ids) {
        return bannerService.removeByIds(ids);
    }

    /**
     * 分页查询轮播图
     *
     * @param pageNum  页码
     * @param pageSize 每页大小
     * @return 分页结果
     */
    @GetMapping("/page")
    public IPage<BannerVO> page(
            @RequestParam("page") Integer pageNum,
            @RequestParam("pageSize") Integer pageSize,
            @RequestParam(required = false, name = "status") Boolean status
    ) {
        return bannerService.pageBannerVO(pageNum, pageSize, status);
    }

    /**
     * 切换轮播图推荐状态
     *
     * @param id 轮播图ID
     * @return 是否成功
     */
    @PostMapping("/recommend/{id}/{status}")
    public boolean toggleRecommend(
            @NotNull @PathVariable Long id,
            @NotNull @PathVariable Boolean status
    ) {
        return bannerService.toggleRecommend(id, status);
    }
}