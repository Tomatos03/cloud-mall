package com.onlineshop.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.onlineshop.framework.models.address.Address;
import com.onlineshop.framework.models.address.IAddressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 *
 *
 * @author : Tomatos
 * @date : 2025/12/22
 */
@RestController
@RequestMapping("/manage/address")
@PreAuthorize("hasAuthority('address:view')")
public class AddressManageController {
    @Autowired
    private IAddressService addressService;

    /**
     * 分页查询用户地址列表
     *
     * @param pageNum  当前页码
     * @param pageSize 每页数量
     * @return 分页地址列表
     */
    @GetMapping("/page")
    public IPage<Address> getAddressPage(
            @RequestParam(name = "page", defaultValue = "1") int pageNum,
            @RequestParam(name = "pageSize", defaultValue = "10") int pageSize) {
        Page<Address> page = new Page<>(pageNum, pageSize);
        return addressService.lambdaQuery()
                             .page(page);
    }

    /**
     * 查询单个地址
     */
    @GetMapping("/{id}")
    public Address getById(@PathVariable Long id) {
        return addressService.getById(id);
    }

    /**
     * 新增地址
     */
    @PostMapping
    @PreAuthorize("hasAuthority('address:add')")
    public boolean add(@RequestBody Address address) {
        return addressService.save(address);
    }

    /**
     * 修改地址
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('address:edit')")
    public boolean update(@PathVariable Long id, @RequestBody Address address) {
        address.setId(id);
        return addressService.updateById(address);
    }

    /**
     * 删除地址
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('address:delete')")
    public boolean delete(@PathVariable Long id) {
        return addressService.removeById(id);
    }

    /**
     * 更新默认地址
     *
     */
    @PutMapping("/{id}/default")
    @PreAuthorize("hasAuthority('address:edit')")
    public void updateStatus(@PathVariable Long id) {
        addressService.setDefaultAddress(id);
    }
}