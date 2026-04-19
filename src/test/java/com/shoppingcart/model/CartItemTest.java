package com.shoppingcart.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("CartItem")
class CartItemTest {

    @Test
    @DisplayName("rejects null or blank product name")
    void rejectsBlankProductName() {
        assertThrows(IllegalArgumentException.class,
                () -> new CartItem("", 1, BigDecimal.ONE));
        assertThrows(IllegalArgumentException.class,
                () -> new CartItem(null, 1, BigDecimal.ONE));
    }

    @Test
    @DisplayName("rejects non-positive quantity")
    void rejectsNonPositiveQuantity() {
        assertThrows(IllegalArgumentException.class,
                () -> new CartItem("cornflakes", 0, BigDecimal.ONE));
        assertThrows(IllegalArgumentException.class,
                () -> new CartItem("cornflakes", -1, BigDecimal.ONE));
    }

    @Test
    @DisplayName("rejects negative unit price")
    void rejectsNegativeUnitPrice() {
        assertThrows(IllegalArgumentException.class,
                () -> new CartItem("cornflakes", 1, new BigDecimal("-1")));
        assertThrows(IllegalArgumentException.class,
                () -> new CartItem("cornflakes", 1, null));
    }

    @Test
    @DisplayName("compute line total by quantity * unitPrice")
    void lineTotalMultipliesQuantityByUnitPrice() {
        CartItem item = new CartItem("cornflakes", 3, new BigDecimal("2.52"));
        assertEquals(new BigDecimal("7.56"), item.lineTotal());
    }

}
