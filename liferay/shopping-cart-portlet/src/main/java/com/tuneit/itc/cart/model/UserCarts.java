package com.tuneit.itc.cart.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

import com.liferay.portal.kernel.model.User;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserCarts implements Serializable {
    private User owner;
    private List<CartWithProductsInfo> carts;
}
