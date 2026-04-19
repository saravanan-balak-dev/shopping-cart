package com.shoppingcart.model;

import java.math.BigDecimal;

/**
 * Single line item in the cart
 * @param productName - name of the product
 * @param quantity - number of units of the product
 * @param unitPrice - price of a single unit
 */
public record CartItem(String productName, int quantity, BigDecimal unitPrice) {

    public CartItem {
        if (productName == null || productName.isBlank()) {
            throw new IllegalArgumentException("productName must not be blank");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("quantity must be positive");
        }
        if (unitPrice == null || unitPrice.signum() < 0) {
            throw new IllegalArgumentException("unitPrice must be non-negative");
        }
    }

    //The unrounded line total (quantity × unitPrice)
    public BigDecimal lineTotal() {
        return BigDecimal.valueOf(quantity).multiply(unitPrice);
    }
}
