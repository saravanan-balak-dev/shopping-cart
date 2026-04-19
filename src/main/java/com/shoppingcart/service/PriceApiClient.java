package com.shoppingcart.service;

import java.math.BigDecimal;
import java.net.http.HttpClient;

public class PriceApiClient implements PriceService {

    private final HttpClient httpClient;

    public PriceApiClient(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    @Override
    public BigDecimal getUnitPrice(String productName) {
        return null;
    }

}
