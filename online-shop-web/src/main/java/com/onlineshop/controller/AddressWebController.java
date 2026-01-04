package com.onlineshop.controller;

import com.onlineshop.framework.models.address.Address;
import com.onlineshop.framework.models.address.AddressDTO;
import com.onlineshop.framework.models.address.IAddressService;
import com.onlineshop.framework.utils.context.UserContextHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 地址控制器
 *
 * @author Tomatos
 * @date 2025/12/20
 */
@RestController
@RequestMapping("/web/address")
public class AddressWebController {

    @Autowired
    private IAddressService addressService;

    /**
     * 添加地址
     *
     * @return 是否成功
     */
    @Transactional(rollbackFor = Exception.class)
    @PostMapping("/add")
    public void addAddress(@RequestBody AddressDTO addressDTO) {
        addressService.addAddress(addressDTO);
    }

    /**
     * 删除地址
     *
     * @param id 地址ID
     * @return 是否成功
     */
    @DeleteMapping("/{id}")
    public void deleteAddress(@PathVariable Long id) {
        addressService.deleteAddress(id);
    }

    /**
     * 更新地址
     *
     * @param id      地址ID
     * @param address 地址信息
     * @return 是否成功
     */
    @Transactional(rollbackFor = Exception.class)
    @PutMapping("/{id}")
    public void updateAddress(@PathVariable Long id, @RequestBody Address address) {
        address.setId(id);
        addressService.updateAddress(address);
    }

    /**
     * 查询用户地址列表
     *
     * @return 地址列表
     */
    @GetMapping
    public List<AddressDTO> getAddressList() {
        return addressService.getAddressList();
    }

    /**
     * 获取默认地址
     *
     * @return 默认地址
     */
    @GetMapping("/default")
    public Address getDefaultAddress() {
        Long userId = UserContextHolder.getUserId();
        return addressService.lambdaQuery()
                             .eq(Address::getUserId, userId)
                             .eq(Address::getIsDefault, true)
                             .one();
    }

    /**
     * 设置默认地址
     *
     * @param id 地址ID
     * @return 是否成功
     */
    @PutMapping("/setDefault/{id}")
    public void setDefaultAddress(@PathVariable Long id) {
        addressService.setDefaultAddress(id);
    }
}