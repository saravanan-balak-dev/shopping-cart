package com.shoppingcart.service;

import com.shoppingcart.exception.PriceLookupException;
import com.shoppingcart.model.CartItem;
import com.shoppingcart.model.CartState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.net.http.HttpClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@DisplayName("ShoppingCartService")
class ShoppingCartServiceIT {

    private static final String BASE_URL = "https://equalexperts.github.io";

    private final PriceService priceService = spy(new PriceApiClient(HttpClient.newHttpClient(), BASE_URL));
    private ShoppingCartService shoppingCartService;

    @BeforeEach
    void setUp() {
        shoppingCartService = new ShoppingCartService(priceService);
    }

    @Nested
    @DisplayName("when items() is called")
    class Items {

        @Test
        @DisplayName("given an empty cart, returns no items")
        void emptyCartReturnsNoItems() {
            assertThat(shoppingCartService.items()).isEmpty();
        }

        @Test
        @DisplayName("given 2×cornflakes added, records name, quantity, and unit price")
        void singleAddRecordsItem() {
            shoppingCartService.add("cornflakes", 2);

            assertThat(shoppingCartService.items())
                    .hasSize(1)
                    .first()
                    .returns("cornflakes", CartItem::productName)
                    .returns(2, CartItem::quantity)
                    .returns(new BigDecimal("2.52"), CartItem::unitPrice);

            verify(priceService).getPrice("cornflakes");
        }

        @Test
        @DisplayName("given cornflakes added with qty 1 then 3, merges into one item with qty 4")
        void sameProductMergesQuantities() {
            shoppingCartService.add("cornflakes", 1);
            shoppingCartService.add("cornflakes", 3);

            assertThat(shoppingCartService.items())
                    .hasSize(1)
                    .first()
                    .returns("cornflakes", CartItem::productName)
                    .returns(4, CartItem::quantity)
                    .returns(new BigDecimal("2.52"), CartItem::unitPrice);

            verify(priceService, times(2)).getPrice("cornflakes");
        }

        @Test
        @DisplayName("given products added as weetabix → cornflakes → frosties, preserves that insertion order")
        void itemsPreserveInsertionOrder() {
            shoppingCartService.add("weetabix", 1);
            shoppingCartService.add("cornflakes", 1);
            shoppingCartService.add("frosties", 1);

            assertThat(shoppingCartService.items())
                    .extracting(CartItem::productName)
                    .containsExactly("weetabix", "cornflakes", "frosties");
        }

        @Test
        @DisplayName("given weetabix, then cornflakes, then weetabix again, weetabix stays first with merged qty 3")
        void reAddKeepsInsertionOrder() {
            shoppingCartService.add("weetabix", 1);
            shoppingCartService.add("cornflakes", 1);
            shoppingCartService.add("weetabix", 2);

            assertThat(shoppingCartService.items())
                    .extracting(CartItem::productName)
                    .containsExactly("weetabix", "cornflakes");

            assertThat(shoppingCartService.items().getFirst())
                    .returns(3, CartItem::quantity);
        }

        @Test
        @DisplayName("given a non-empty cart, mutating the returned list throws UnsupportedOperationException")
        void itemsIsUnmodifiable() {
            shoppingCartService.add("cornflakes", 1);

            assertThatThrownBy(() -> shoppingCartService.items().clear())
                    .isInstanceOf(UnsupportedOperationException.class);
        }
    }

    @Nested
    @DisplayName("when state() is called")
    class CartStateTests {

        @Test
        @DisplayName("given an empty cart, subtotal, tax, and total are all zero")
        void emptyCartHasZeroState() {
            assertThat(shoppingCartService.state())
                    .returns(new BigDecimal("0.00"), CartState::subTotal)
                    .returns(new BigDecimal("0.00"), CartState::tax)
                    .returns(new BigDecimal("0.00"), CartState::total);
        }

        @Test
        @DisplayName("given a single product in the cart, calculates correct subtotal, 12.5% tax, and total")
        void singleProductState() {
            shoppingCartService.add("cornflakes", 2);

            assertThat(shoppingCartService.state())
                    .returns(new BigDecimal("5.04"), CartState::subTotal)
                    .returns(new BigDecimal("0.63"), CartState::tax)
                    .returns(new BigDecimal("5.67"), CartState::total);
        }

        @Test
        @DisplayName("given multiple different products in the cart, calculates correct subtotal, 12.5% tax, and total")
        void multipleProductsState() {
            shoppingCartService.add("cornflakes", 2);
            shoppingCartService.add("weetabix", 1);

            assertThat(shoppingCartService.state())
                    .returns(new BigDecimal("15.02"), CartState::subTotal)
                    .returns(new BigDecimal("1.88"), CartState::tax)
                    .returns(new BigDecimal("16.90"), CartState::total);
        }

        @Test
        @DisplayName("given a tax amount with a fractional half-penny, rounds the tax up using HALF_UP")
        void taxRoundsHalfUp() {
            shoppingCartService.add("cornflakes", 1);

            assertThat(shoppingCartService.state())
                    .returns(new BigDecimal("2.52"), CartState::subTotal)
                    .returns(new BigDecimal("0.32"), CartState::tax)
                    .returns(new BigDecimal("2.84"), CartState::total);
        }

        @Test
        @DisplayName("given the same product added in separate calls, state reflects the merged quantities")
        void stateReflectsMergedQuantities() {
            shoppingCartService.add("cornflakes", 1);
            shoppingCartService.add("cornflakes", 2);

            assertThat(shoppingCartService.state())
                    .returns(new BigDecimal("7.56"), CartState::subTotal)
                    .returns(new BigDecimal("0.95"), CartState::tax)
                    .returns(new BigDecimal("8.51"), CartState::total);
        }
    }

    @Nested
    @DisplayName("when validation fails or price service throws")
    class ErrorHandling {

        @Test
        @DisplayName("given an unknown product name, throws PriceLookupException containing the product name")
        void propagatesExceptionForUnknownProduct() {
            assertThatThrownBy(() -> shoppingCartService.add("unknown", 1))
                    .isInstanceOf(PriceLookupException.class)
                    .hasMessageContaining("unknown");
        }

        @Test
        @DisplayName("given a successful add followed by a failing add, the cart retains only the successful item")
        void priceServiceFailureLeavesCartUnchanged() {
            shoppingCartService.add("cornflakes", 1);

            assertThatThrownBy(() -> shoppingCartService.add("unknown", 1))
                    .isInstanceOf(PriceLookupException.class);

            assertThat(shoppingCartService.items()).hasSize(1);
            assertThat(shoppingCartService.items().getFirst().productName()).isEqualTo("cornflakes");
        }

        @Test
        @DisplayName("given a blank product name, throws IllegalArgumentException without contacting the price service")
        void rejectsBlankProductName() {
            assertThatThrownBy(() -> shoppingCartService.add("", 1))
                    .isInstanceOf(IllegalArgumentException.class);
            verifyNoInteractions(priceService);
        }

        @Test
        @DisplayName("given quantity 0, throws IllegalArgumentException without contacting the price service")
        void rejectsNonPositiveQuantity() {
            assertThatThrownBy(() -> shoppingCartService.add("cornflakes", 0))
                    .isInstanceOf(IllegalArgumentException.class);
            verifyNoInteractions(priceService);
        }
    }
}
