package com.shoppingcart.service;

import com.shoppingcart.model.CartItem;
import com.shoppingcart.model.CartState;
import com.shoppingcart.util.CartInputValidator;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * In-memory shopping cart.
 * <p>
 * Responsibilities:
 * <ul>
 *   <li>Add products (name + quantity), looking up the unit price via a {@link PriceService}</li>
 *   <li>Expose the current items and calculate subtotal, tax (12.5%), and total</li>
 * </ul>
 * <p>
 * Monetary values are returned rounded to 2 decimal places using {@link RoundingMode#HALF_UP}.
 * <p>
 */
public class ShoppingCartService {

    private static final BigDecimal TAX_RATE = new BigDecimal("0.125");
    private static final int MONEY_SCALE = 2;
    private static final RoundingMode MONEY_ROUNDING = RoundingMode.HALF_UP;

    private final PriceService priceService;

    private final Map<String, CartItem> itemsByProductName = new LinkedHashMap<>();

    public ShoppingCartService(PriceService priceService) {
        this.priceService = priceService;
    }

    /**
     * Add {@code quantity} of {@code productName} to the cart.
     * If the product is already in the cart, the quantities are summed.
     * The unit price is fetched from the {@link PriceService} on every add call
     */
    public void add(String productName, int quantity) {
        validateInput(productName, quantity);
        BigDecimal unitPrice = priceService.getPrice(productName);
        CartItem incoming = new CartItem(productName, quantity, unitPrice);
        itemsByProductName.merge(productName, incoming, this::mergeQuantities);
    }

    /** @return an unmodifiable view of the items in insertion order. */
    public List<CartItem> items() {
        return List.copyOf(itemsByProductName.values());
    }

    /**
     * Calculate the cart's current state: subtotal, tax (12.5% of subtotal), and total.
     * All values are rounded to 2dp.
     */
    public CartState state() {
        BigDecimal rawSubtotal = itemsByProductName.values().stream()
                .map(CartItem::lineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal subtotal = round(rawSubtotal);
        BigDecimal tax = round(rawSubtotal.multiply(TAX_RATE));
        BigDecimal total = subtotal.add(tax);

        return new CartState(subtotal, tax, total);
    }

    private void validateInput(String productName, int quantity) {
        CartInputValidator.validateProductName(productName);
        CartInputValidator.validateQuantity(quantity);
    }

    private CartItem mergeQuantities(CartItem existing, CartItem incoming) {
        return new CartItem(existing.productName(), existing.quantity() + incoming.quantity(), incoming.unitPrice());
    }

    private static BigDecimal round(BigDecimal value) {
        return value.setScale(MONEY_SCALE, MONEY_ROUNDING);
    }
}
