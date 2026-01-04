package com.onlineshop.framework.models.menu;

import com.onlineshop.framework.models.user.UserRole;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class MenuService implements IMenuService {

    @Override
    public List<Menu> getMenusByRole(UserRole role) {
        if (role == UserRole.ADMIN) {
            return getAdminMenus();
        } else if (role == UserRole.MERCHANT) {
            return getMerchantMenus();
        } else {
            // 普通用户返回空菜单
            return new ArrayList<>();
        }
    }

    /**
     * 获取管理员菜单（全部菜单）
     */
    private List<Menu> getAdminMenus() {
        List<Menu> menus = new ArrayList<>();

        // 统计菜单
        Menu statistics = new Menu();
        statistics.setName("Statistics");
        statistics.setPath("statistics");
        statistics.setComponent("statistics");
        statistics.setRouteRecordRawType(RoueRecordRawType.VIEW.getCode());
        statistics.setMeta(new Menu.Meta("数据嵌板", "data-analysis"));
        menus.add(statistics);

        // 公告菜单
        Menu notice = new Menu();
        notice.setName("Notice");
        notice.setPath("notice");
        notice.setComponent("notice");
        notice.setRouteRecordRawType(RoueRecordRawType.VIEW.getCode());
        notice.setMeta(new Menu.Meta("公告管理", "notification"));
        menus.add(notice);

        // 轮播图菜单
        Menu banner = new Menu();
        banner.setName("Banner");
        banner.setPath("banner");
        banner.setComponent("banner");
        banner.setRouteRecordRawType(RoueRecordRawType.VIEW.getCode());
        banner.setMeta(new Menu.Meta("轮播图管理", "picture"));
        menus.add(banner);

        // 分类菜单
        Menu category = new Menu();
        category.setName("Category");
        category.setPath("category");
        category.setComponent("category");
        category.setRouteRecordRawType(RoueRecordRawType.VIEW.getCode());
        category.setMeta(new Menu.Meta("分类管理", "menu"));
        menus.add(category);

        // 商品菜单
        Menu goods = new Menu();
        goods.setName("Goods");
        goods.setPath("goods");
        goods.setComponent("goods");
        goods.setRouteRecordRawType(RoueRecordRawType.VIEW.getCode());
        goods.setMeta(new Menu.Meta("商品管理", "shop"));
        menus.add(goods);

        // 订单菜单
        Menu order = new Menu();
        order.setName("Order");
        order.setPath("order");
        order.setComponent("order");
        order.setRouteRecordRawType(RoueRecordRawType.VIEW.getCode());
        order.setMeta(new Menu.Meta("订单管理", "wallet"));
        menus.add(order);

        // 收货地址菜单
        Menu address = new Menu();
        address.setName("Address");
        address.setPath("address");
        address.setComponent("address");
        address.setRouteRecordRawType(RoueRecordRawType.VIEW.getCode());
        address.setMeta(new Menu.Meta("地址管理", "location"));
        menus.add(address);

        // 用户菜单
        Menu user = new Menu();
        user.setName("User");
        user.setPath("user");
        user.setComponent("user");
        user.setRouteRecordRawType(RoueRecordRawType.VIEW.getCode());
        user.setMeta(new Menu.Meta("用户管理", "user"));
        menus.add(user);

        // wrap into a 'home' layout for frontend (vue-router): a single root layout with children
        Menu home = new Menu();
        home.setPath("/");
        home.setName("Home");
        home.setRedirect("/statistics");
        home.setRouteRecordRawType(RoueRecordRawType.LAYOUT.getCode());
        home.setMeta(new Menu.Meta("首页", "home"));
        home.setChildren(menus);

        List<Menu> wrapped = new ArrayList<>();
        wrapped.add(home);
        return wrapped;
    }

    /**
     * 获取商家菜单（仅商品和订单）
     */
    private List<Menu> getMerchantMenus() {
        List<Menu> menus = new ArrayList<>();

        Menu statistics = new Menu();
        statistics.setName("Statistics");
        statistics.setPath("statistics");
        statistics.setComponent("statistics");
        statistics.setRouteRecordRawType(RoueRecordRawType.VIEW.getCode());
        statistics.setMeta(new Menu.Meta("数据嵌板", "data-analysis"));
        menus.add(statistics);

        // 商品菜单
        Menu store = new Menu();
        store.setName("Store");
        store.setPath("store");
        store.setComponent("store");
        store.setRouteRecordRawType(RoueRecordRawType.VIEW.getCode());
        store.setMeta(new Menu.Meta("店铺管理", "setting"));
        menus.add(store);

        // 商品菜单
        Menu goods = new Menu();
        goods.setName("Goods");
        goods.setPath("goods");
        goods.setComponent("goods");
        goods.setRouteRecordRawType(RoueRecordRawType.VIEW.getCode());
        goods.setMeta(new Menu.Meta("商品管理", "shop"));
        menus.add(goods);

        Menu comment = new Menu();
        comment.setName("Comment");
        comment.setPath("comment");
        comment.setComponent("comment");
        comment.setRouteRecordRawType(RoueRecordRawType.VIEW.getCode());
        comment.setMeta(new Menu.Meta("商品评论", "chat-round"));
        menus.add(comment);

        // 订单菜单
        Menu order = new Menu();
        order.setName("Order");
        order.setPath("order");
        order.setComponent("order");
        order.setRouteRecordRawType(RoueRecordRawType.VIEW.getCode());
        order.setMeta(new Menu.Meta("订单管理", "wallet"));
        menus.add(order);

        // wrap into a 'home' layout for frontend (vue-router): a single root layout with children
        Menu home = new Menu();
        home.setName("Home");
        home.setPath("/");
        home.setRedirect("/statistics");
        home.setRouteRecordRawType(RoueRecordRawType.LAYOUT.getCode());
        home.setMeta(new Menu.Meta("首页", "home"));
        home.setChildren(menus);

        List<Menu> wrapped = new ArrayList<>();
        wrapped.add(home);
        return wrapped;
    }
}
