package com.tuneit.itc.commons.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Currency {
    /**
     * Internal id of currency, same as ISO 4217 number code.
     * Use it in ITC services.
     */
    private String id;
    /**
     * ISO 4217 string code of currency.
     */
    private String code;
    /**
     * Sign to show in interfaces.
     */
    private String sign;

    public String getSign() {
        if (sign == null || sign.isBlank()) {
            return code;
        }
        return sign;
    }

    public boolean isHasSign() {
        return sign != null && !sign.isBlank();
    }
}
