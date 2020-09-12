package com.tuneit.itc.cart;

import lombok.Data;

import java.io.Serializable;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;

import com.liferay.faces.util.logging.Logger;
import com.liferay.faces.util.logging.LoggerFactory;

import com.tuneit.itc.cart.model.CartWithProductsInfo;
import com.tuneit.itc.commons.Pair;
import com.tuneit.itc.commons.model.cart.CartPosition;
import com.tuneit.itc.commons.model.rest.ProductResponse;
import com.tuneit.itc.commons.service.cart.CartPositionService;
import com.tuneit.itc.commons.service.cart.CartService;
import com.tuneit.itc.commons.service.rest.Requester;
import com.tuneit.itc.commons.util.CartUtil;


@ApplicationScoped
@ManagedBean
@Data
public class CartUtilBean implements Serializable {

    private final Logger log = LoggerFactory.getLogger(CartUtilBean.class);

    @ManagedProperty("#{cartPositionService}")
    private CartPositionService cartPositionService;
    @ManagedProperty("#{requester.productsService}")
    private Requester.ProductsService productsService;

    public List<CartWithProductsInfo> loadCartsOrders(List<CartService.CartWithPositionsCount> carts) {
        List<Pair<CartService.CartWithPositionsCount, List<CartPosition>>> cartWithPositions = carts.stream()
            .map(cartWithPositionsCount -> {
                var productsInCart = cartPositionService.findForCart(cartWithPositionsCount.getCart(), 5);
                return Pair.of(cartWithPositionsCount, productsInCart);
            })
            .collect(Collectors.toList());
        List<CartPosition> positions = cartWithPositions.stream()
            .map(Pair::getSecond)
            .flatMap(List::stream)
            .collect(Collectors.toList());
        var sources = CartUtil.fromCartPositions(productsService, positions);
        log.debug("Sources size: {0}", sources.size());
        return cartWithPositions.stream()
            .sorted(Comparator
                .comparing((Pair<CartService.CartWithPositionsCount, ?> cart) ->
                    cart.getFirst().getCart().getCreationDate())
                .reversed()
            )
            .map(cart -> {
                List<ProductResponse.ProductHitSource> cartProductsInfo = cart.getSecond().stream()
                    .map(CartPosition::getExternalProductId)
                    .map(sources::get)
                    .filter(Objects::nonNull)
                    .sorted(Comparator.comparing(ProductResponse.ProductHitSource::getCode))
                    .collect(Collectors.toList());
                return new CartWithProductsInfo(cart.getFirst(), cartProductsInfo);
            })
            .collect(Collectors.toList());
    }
}
