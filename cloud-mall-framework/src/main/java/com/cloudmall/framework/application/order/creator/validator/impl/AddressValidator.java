package com.cloudmall.framework.application.order.creator.validator.impl;

import com.cloudmall.framework.common.enums.BizErrorCode;
import com.cloudmall.framework.models.address.Address;
import com.cloudmall.framework.models.address.IAddressService;
import com.cloudmall.framework.application.order.context.TradeContext;
import com.cloudmall.framework.application.order.creator.validator.IOrderCreateValidator;
import com.cloudmall.framework.utils.AssertUtils;
import com.cloudmall.framework.context.AuthUserContext;
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
                                        .eq(Address::getUserId, AuthUserContext.getUserId())
                                        .one();
        AssertUtils.notNull(address, BizErrorCode.ORDER_CREATE_ADDRESS_NOT_EXIST);
        context.setAddress(address);
    }

    @Override
    public int getOrder() {
        return 10;
    }
}