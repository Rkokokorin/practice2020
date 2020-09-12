package com.tuneit.itc.stocks.beans;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.tuneit.itc.commons.model.JsonConvertible;
import com.tuneit.itc.stocks.model.SpecialProduct;

@Data
@NoArgsConstructor
public class StocksPortletConfig implements JsonConvertible {
    private List<SpecialProduct> specialProducts;

    public StocksPortletConfig(List<SpecialProduct> specialProducts) {
        this.specialProducts = specialProducts.stream().distinct().collect(Collectors.toList());
    }

    @Override
    public String toJsonString() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(this);
    }
}
