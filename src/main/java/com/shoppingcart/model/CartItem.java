package com.shoppingcart.model;

import java.math.BigDecimal;

/**
 * Single line item in the cart
 * @param productName - name of the product
 * @param quantity - number of units of the product
 * @param unitPrice - price of a single unit
 */
public record CartItem(String productName, int quantity, BigDecimal unitPrice) {

    //The unrounded line total (quantity × unitPrice)
    public BigDecimal lineTotal() {
        return unitPrice.multiply(new BigDecimal(quantity));
    }
}
