package com.onlineshop.framework.models.order.application.creator.validator.impl;

import com.onlineshop.framework.common.enums.BizErrorCode;
import com.onlineshop.framework.models.order.application.context.TradeContext;
import com.onlineshop.framework.models.order.application.creator.validator.IOrderCreateValidator;
import com.onlineshop.framework.models.order.dto.TradeShopDTO;
import com.onlineshop.framework.models.store.IStoreService;
import com.onlineshop.framework.models.store.Store;
import com.onlineshop.framework.utils.AssertUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

@Component
public class StoreValidator implements IOrderCreateValidator {

    private final IStoreService storeService;

    @Autowired
    public StoreValidator(IStoreService storeService) {
        this.storeService = storeService;
    }

    @Override
    public void validate(TradeContext context) {
        Set<Long> storeIdSet = context.getTradeDTO()
                                      .getTradeItems()
                                      .stream()
                                      .map(TradeShopDTO::getStoreId)
                                      .collect(Collectors.toSet());

        Long count = storeService.lambdaQuery()
                                 .in(Store::getId, storeIdSet)
                                 .count();
        AssertUtils.isTrue(count == storeIdSet.size(), BizErrorCode.ORDER_CREATE_STORE_NOT_EXIST);
    }

    @Override
    public int getOrder() {
        return 20;
    }
}