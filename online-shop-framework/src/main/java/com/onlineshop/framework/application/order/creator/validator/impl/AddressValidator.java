package com.onlineshop.framework.application.order.creator.validator.impl;

import com.onlineshop.framework.common.enums.BizErrorCode;
import com.onlineshop.framework.models.address.Address;
import com.onlineshop.framework.models.address.IAddressService;
import com.onlineshop.framework.application.order.context.TradeContext;
import com.onlineshop.framework.application.order.creator.validator.IOrderCreateValidator;
import com.onlineshop.framework.utils.AssertUtils;
import com.onlineshop.framework.utils.AuthUserUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AddressValidator implements IOrderCreateValidator {

    private final IAddressService addressService;

    @Autowired
    public AddressValidator(IAddressService addressService) {
        this.addressService = addressService;
    }

    @Override
    public void validate(TradeContext context) {
        Address address = addressService.lambdaQuery()
                                        .eq(Address::getId, context.getTradeDTO()
                                                                   .getAddressId())
                                        .eq(Address::getUserId, AuthUserUtils.getUserId())
                                        .one();
        AssertUtils.notNull(address, BizErrorCode.ORDER_CREATE_ADDRESS_NOT_EXIST);
        context.setAddress(address);
    }

    @Override
    public int getOrder() {
        return 10;
    }
}