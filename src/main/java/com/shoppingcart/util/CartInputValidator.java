package com.shoppingcart.util;

import com.shoppingcart.exception.PriceLookupException;

import java.math.BigDecimal;

public class CartInputValidator {

    private CartInputValidator() {}

    public static void validateProductName(String productName) {
        if (productName == null || productName.isBlank()) {
            throw new IllegalArgumentException("Product name must not be blank");
        }
    }

    public static void validateQuantity(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive, got " + quantity);
        }
    }

    public static void validatePrice(BigDecimal price, String productName) {
        if (price.compareTo(BigDecimal.ZERO) < 0) {
            throw new PriceLookupException("Negative price " + price + " is not valid for product '" + productName + "'");
        }
    }
}
