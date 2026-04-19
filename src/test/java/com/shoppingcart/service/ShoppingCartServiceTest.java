package com.shoppingcart.service;

import com.shoppingcart.model.CartItem;
import com.shoppingcart.model.CartState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ShoppingCartServiceTest {

    @Mock
    private PriceService priceService;

    private ShoppingCartService shoppingCartService;

    @BeforeEach
    void setUp() {
        shoppingCartService = new ShoppingCartService(priceService);
    }

    @Test
    @DisplayName("adds a single product with the quantity specified")
    void addsSingleProduct() {
        when(priceService.getUnitPrice("cornflakes")).thenReturn(BigDecimal.valueOf(4.00));

        shoppingCartService.add("cornflakes", 1);

        List<CartItem> cartItems = shoppingCartService.items();
        assertEquals(1, cartItems.size());
        var item = cartItems.getFirst();
        assertEquals("cornflakes", item.productName());
        assertEquals(1, item.quantity());
        assertEquals(BigDecimal.valueOf(4.00), item.unitPrice());
    }

    @Test
    @DisplayName("merges quantities when the same product is added twice")
    void mergesQuantitiesForSameProduct() {
        when(priceService.getUnitPrice("cornflakes")).thenReturn(BigDecimal.valueOf(4.00));

        shoppingCartService.add("cornflakes", 1);
        shoppingCartService.add("cornflakes", 1);

        List<CartItem> cartItems = shoppingCartService.items();
        assertEquals(1, cartItems.size());
        var item = cartItems.getFirst();
        assertEquals("cornflakes", item.productName());
        assertEquals(2, item.quantity());
        assertEquals(BigDecimal.valueOf(4.00), item.unitPrice());
    }

    @Test
    @DisplayName("calls the pricing service on every add")
    void callsPricingServiceOnEveryAdd() {
        when(priceService.getUnitPrice("frosties")).thenReturn(BigDecimal.valueOf(4.99));

        shoppingCartService.add("frosties", 1);
        shoppingCartService.add("frosties", 2);

        verify(priceService, times(2)).getUnitPrice("frosties");
    }

    @Test
    @DisplayName("state reflects quantity-weighted subtotal with multiple products")
    void multipleProductsSubtotal() {
        when(priceService.getUnitPrice("cheerios")).thenReturn(BigDecimal.valueOf(8.43));
        when(priceService.getUnitPrice("frosties")).thenReturn(BigDecimal.valueOf(4.99));

        shoppingCartService.add("cheerios", 2);
        shoppingCartService.add("frosties", 3);

        CartState state = shoppingCartService.state();
        assertEquals(BigDecimal.valueOf(31.83), state.subTotal()); //16.86 + 14.97 = 31.83
        assertEquals(BigDecimal.valueOf(3.98),  state.tax()); //31.83 * 0.125 = 3.97875 -> 3.98
        assertEquals(BigDecimal.valueOf(35.81), state.total()); //35.81
    }

    @Test
    @DisplayName("rounds tax to 2dp using HALF_UP (0.125 rounds up to 0.13)")
    void roundsTaxHalfUp() {
        when(priceService.getUnitPrice("cornflakes")).thenReturn(BigDecimal.valueOf(1.00));

        shoppingCartService.add("cornflakes", 1);

        CartState state = shoppingCartService.state();
        assertEquals(new BigDecimal("1.00"), state.subTotal());
        assertEquals(BigDecimal.valueOf(0.13), state.tax());
        assertEquals(BigDecimal.valueOf(1.13), state.total());
    }

    @Test
    @DisplayName("rejects null or blank product name")
    void rejectsNullOrBlankProductName() {
        assertThrows(IllegalArgumentException.class, () -> shoppingCartService.add("", 1));
        assertThrows(IllegalArgumentException.class, () -> shoppingCartService.add("   ", 1));
        assertThrows(IllegalArgumentException.class, () -> shoppingCartService.add(null, 1));
    }

    @Test
    @DisplayName("rejects non-positive quantity")
    void rejectsNonPositiveQuantity() {
        assertThrows(IllegalArgumentException.class, () -> shoppingCartService.add("cornflakes", 0));
        assertThrows(IllegalArgumentException.class, () -> shoppingCartService.add("cornflakes", -1));
    }

    @Test
    @DisplayName("items() returns the products in insertion order")
    void itemsInInsertionOrder() {
        when(priceService.getUnitPrice("cheerios")).thenReturn(BigDecimal.valueOf(1.00));
        when(priceService.getUnitPrice("weetabix")).thenReturn(BigDecimal.valueOf(2.00));
        when(priceService.getUnitPrice("frosties")).thenReturn(BigDecimal.valueOf(3.00));

        shoppingCartService.add("weetabix", 1);
        shoppingCartService.add("cheerios", 1);
        shoppingCartService.add("frosties", 1);

        List<CartItem> items = shoppingCartService.items();
        assertEquals("weetabix", items.get(0).productName());
        assertEquals("cheerios", items.get(1).productName());
        assertEquals("frosties", items.get(2).productName());
    }

    @Test
    @DisplayName("propagates exception from the price service")
    void propagatesPriceServiceFailure() {
        when(priceService.getUnitPrice("cornflakes"))
                .thenThrow(new RuntimeException("boom"));

        assertThrows(RuntimeException.class, () -> shoppingCartService.add("cornflakes", 1));
    }

}
