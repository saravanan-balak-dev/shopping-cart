package com.shoppingcart.service;

import com.shoppingcart.exception.PriceLookupException;
import com.shoppingcart.util.CartInputValidator;
import com.shoppingcart.util.RegexPriceParser;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Objects;

public class PriceApiClient implements PriceService {

    private static final String DEFAULT_BASE_URL = System.getenv()
            .getOrDefault("PRICE_API_BASE_URL", "https://equalexperts.github.io");
    private static final String PATH_TEMPLATE = "/backend-take-home-test-data/%s.json";
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(10);

    private final HttpClient httpClient;
    private final String baseUrl;

    public PriceApiClient() {
        this(HttpClient.newHttpClient(), DEFAULT_BASE_URL);
    }

    public PriceApiClient(HttpClient httpClient, String baseUrl) {
        this.httpClient = Objects.requireNonNull(httpClient, "httpClient");
        this.baseUrl = Objects.requireNonNull(baseUrl, "baseUrl");
    }

    @Override
    public BigDecimal getPrice(String productName) {
        CartInputValidator.validateProductName(productName);
        HttpRequest request = buildRequest(productName);
        HttpResponse<String> response = send(request, productName);
        validateResponse(response, productName);
        return RegexPriceParser.parse(response.body(), productName);
    }

    private HttpRequest buildRequest(String productName) {
        URI uri = URI.create(baseUrl + String.format(PATH_TEMPLATE, URLEncoder.encode(productName, StandardCharsets.UTF_8)));
        return HttpRequest.newBuilder(uri)
                .timeout(REQUEST_TIMEOUT)
                .header("Accept", "application/json")
                .GET()
                .build();
    }

    private HttpResponse<String> send(HttpRequest request, String productName) {
        try {
            return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException e) {
            throw new PriceLookupException("Failed to communicate with price API for product '" + productName + "'", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new PriceLookupException("Interrupted while fetching price for product '" + productName + "'", e);
        }
    }

    private void validateResponse(HttpResponse<String> response, String productName) {
        if (response.statusCode() != 200) {
            throw new PriceLookupException("Price API failed with status " + response.statusCode() + " for product '" + productName + "'");
        }
    }
}
