package com.shoppingcart.service;

import java.math.BigDecimal;

/**
 * Get the unit price of a product by name.
 * <p>
 * Kept as an interface so the shopping cart can depend on an abstraction
 * and tests can mock the price source without hitting the real HTTP server.
 */
public interface PriceService {

    /**
     * @param productName the product identifier (e.g. "cornflakes")
     * @return the unit price
     */
    BigDecimal getPrice(String productName);

}
