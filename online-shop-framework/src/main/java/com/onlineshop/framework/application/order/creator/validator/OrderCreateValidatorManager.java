package com.onlineshop.framework.application.order.creator.validator;

import com.onlineshop.framework.models.cart.PurchaseMode;
import com.onlineshop.framework.application.order.context.TradeContext;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Component
public class OrderCreateValidatorManager implements ApplicationContextAware {

    private List<IOrderCreateValidator> validators;

    @Override
    public void setApplicationContext(ApplicationContext ctx) {
        Map<String, IOrderCreateValidator> map = ctx.getBeansOfType(IOrderCreateValidator.class);
        this.validators = map.values()
                .stream()
                .sorted(Comparator.comparingInt(IOrderCreateValidator::getOrder))
                .toList();
    }

    public void validate(TradeContext context, PurchaseMode mode) {
        for (IOrderCreateValidator validator : validators) {
            if (validator.support(mode)) {
                validator.validate(context);
            }
        }
    }
}