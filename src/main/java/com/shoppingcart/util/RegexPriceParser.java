package com.shoppingcart.util;

import com.shoppingcart.exception.PriceLookupException;

import java.math.BigDecimal;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Extracts the {@code "price"} field from a JSON response body using a regex.
 */
public class RegexPriceParser {

    private static final Pattern PRICE_PATTERN = Pattern.compile("\"price\"\\s*:\\s*(-?\\d+(?:\\.\\d+)?)");

    private RegexPriceParser() {}

    public static BigDecimal parse(String body, String productName) {
        if (body == null || body.isEmpty()) {
            throw new PriceLookupException("Empty price response body for product '" + productName + "'");
        }
        Matcher matcher = PRICE_PATTERN.matcher(body);
        if (!matcher.find()) {
            throw new PriceLookupException("No 'price' field found in response for product '" + productName + "'");
        }
        BigDecimal price = new BigDecimal(matcher.group(1));
        CartInputValidator.validatePrice(price, productName);
        return price;
    }
}
