package com.cloudmall.framework.models.address;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cloudmall.framework.common.enums.BizErrorCode;
import com.cloudmall.framework.exception.BizException;
import com.cloudmall.framework.utils.AuthUserUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 地址服务实现类
 *
 * @author Tomatos
 * @date 2025/12/20
 */
@Service
public class AddressService extends ServiceImpl<AddressMapper, Address> implements IAddressService {
    @Override
    public void setDefaultAddress(Long addressId) {
        Long userId = AuthUserUtils.getUserId();
        Address address = this.lambdaQuery()
                              // TODO: 待处理
                              //                              .eq(
                              //                                      UserRole.NORMAL.getCode()
                              //                                                     .equals
                              //                                                     (AuthUserUtils.getRole()),
                              //                                      Address::getUserId,
                              //                                      userId
                              //                              )
                              .eq(Address::getId, addressId)
                              .one();

        if (address == null) {
            throw new BizException(BizErrorCode.ADDRESS_NOT_EXIST);
        }
        cancelDefaultAddress(address.getUserId());
        address.setIsDefault(true);
        this.updateById(address);
    }

    @Override
    public void updateAddress(Address address) {
        if (address.getIsDefault()) {
            // 先取消已有默认地址
            cancelDefaultAddress(AuthUserUtils.getUserId());
        }
        this.updateById(address);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void addAddress(AddressDTO addressDTO) {
        Long userId = AuthUserUtils.getUserId();
        if (addressDTO.getIsDefault()) {
            cancelDefaultAddress(userId);
        }
        Address address = BeanUtil.copyProperties(addressDTO, Address.class);
        address.setUserId(userId);
        this.save(address);
    }

    @Override
    public void deleteAddress(Long id) {
        this.removeById(
                Address.builder()
                       .id(id)
                       .userId(AuthUserUtils.getUserId())
                       .build()
        );
    }

    @Override
    public List<AddressDTO> getAddressList() {
        return lambdaQuery().eq(Address::getUserId, AuthUserUtils.getUserId())
                            .list()
                            .stream()
                            .map(address -> BeanUtil.copyProperties(address, AddressDTO.class))
                            .toList();
    }

    private void cancelDefaultAddress(Long userId) {
        this.lambdaUpdate()
            .eq(Address::getUserId, userId)
            .eq(Address::getIsDefault, true)
            .set(Address::getIsDefault, false)
            .update();
    }
}
