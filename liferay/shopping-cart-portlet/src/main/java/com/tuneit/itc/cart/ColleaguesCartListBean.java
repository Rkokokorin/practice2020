package com.tuneit.itc.cart;

import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;

import com.liferay.faces.portal.context.LiferayPortletHelperUtil;
import com.liferay.faces.util.logging.Logger;
import com.liferay.faces.util.logging.LoggerFactory;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.service.UserLocalServiceUtil;

import com.tuneit.itc.cart.model.CartWithProductsInfo;
import com.tuneit.itc.cart.model.UserCarts;
import com.tuneit.itc.commons.model.ExtendedOrganization;
import com.tuneit.itc.commons.service.cart.CartService;
import com.tuneit.itc.commons.util.LiferayUtil;

@Data
@ManagedBean
@ViewScoped
public class ColleaguesCartListBean implements Serializable {
    @ManagedProperty("#{cartService}")
    private CartService cartService;
    @ManagedProperty("#{cartUtilBean}")
    private CartUtilBean cartUtilBean;
    private List<CartService.CartWithPositionsCount> carts;
    private List<UserCarts> colleaguesCarts;
    private Logger log = LoggerFactory.getLogger(this.getClass());

    @PostConstruct
    public void init() {
        long currentUserId = LiferayPortletHelperUtil.getUserId();
        ExtendedOrganization contractor = LiferayUtil.findContractorForUser(currentUserId);
        if (contractor == null) {
            colleaguesCarts = new ArrayList<>();
            carts = new ArrayList<>();
            log.error("User has no contractor! {0}", currentUserId);
            return;
        }
        List<User> colleagues = UserLocalServiceUtil
            .getOrganizationUsers(contractor.getOrganization().getOrganizationId());

        List<Long> userIds = colleagues.stream()
            .map(User::getUserId)
            .filter(id -> id != currentUserId)
            .collect(Collectors.toList());
        carts = cartService.findForUsers(userIds);
        Map<Long, List<CartWithProductsInfo>> cartsByOwnerIds = cartUtilBean.loadCartsOrders(carts).stream()
            .collect(Collectors.groupingBy(cart -> cart.getCart().getCart().getOwnerId()));
        colleaguesCarts = colleagues.stream()
            .sorted((User u1, User u2) -> u2.getFullName().compareTo(u1.getFullName()))
            .map(u -> {
                List<CartWithProductsInfo> userCarts = cartsByOwnerIds.get(u.getUserId());
                return new UserCarts(u, userCarts);
            })
            .collect(Collectors.toList());

    }

}
