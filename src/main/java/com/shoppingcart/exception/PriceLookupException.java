package com.shoppingcart.exception;

/**
 * Thrown when the price for a product cannot be obtained from the price source.
 */
public class PriceLookupException extends RuntimeException {

    public PriceLookupException(String message) {
        super(message);
    }

    public PriceLookupException(String message, Throwable cause) {
        super(message, cause);
    }
}
