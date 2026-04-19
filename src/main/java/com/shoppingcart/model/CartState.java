package com.shoppingcart.model;

import java.math.BigDecimal;

/**
 * Snapshot of the cart state, with all monetary values rounded to 2 decimal places.
 * @param subTotal - sum of price for all items
 * @param tax - tax payable
 * @param total - sum of subTotal + tax
 */
public record CartState(BigDecimal subTotal, BigDecimal tax, BigDecimal total) {}
