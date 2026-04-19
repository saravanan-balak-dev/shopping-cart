package com.shoppingcart.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("CartItem")
class CartItemTest {

    @Test
    @DisplayName("given cart item, line total is quantity multiplied by unit price")
    void lineTotalMultipliesQuantityByUnitPrice() {
        CartItem item = new CartItem("cornflakes", 3, new BigDecimal("2.52"));
        assertThat(item.lineTotal()).isEqualTo(new BigDecimal("7.56"));
    }

     @Test
     @DisplayName("given cart item with zero quantity, line total is zero")
     void lineTotalIsZeroForZeroQuantity() {
         CartItem item = new CartItem("cornflakes", 0, new BigDecimal("2.52"));
         assertThat(item.lineTotal()).isEqualTo(new BigDecimal("0.00"));
     }

     @Test
     @DisplayName("given cart item with zero unit price, line total is zero")
     void lineTotalIsZeroForZeroUnitPrice() {
         CartItem item = new CartItem("cornflakes", 3, BigDecimal.ZERO);
         assertThat(item.lineTotal()).isEqualTo(BigDecimal.ZERO);
     }

}
