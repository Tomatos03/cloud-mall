package com.cloudmall.framework.models.address;

import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 地址服务接口
 *
 * @author Tomatos
 * @date 2025/12/20
 */
public interface IAddressService extends IService<Address> {
    void setDefaultAddress(Long addressId);

    void updateAddress(Address address);

    void addAddress(AddressDTO addressDTO);

    void deleteAddress(Long id);

    List<AddressDTO> getAddressList();
}