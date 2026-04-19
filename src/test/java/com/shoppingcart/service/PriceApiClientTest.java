package com.shoppingcart.service;

import com.shoppingcart.exception.PriceLookupException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("PriceApiClient")
@SuppressWarnings("unchecked")
class PriceApiClientTest {

    @Mock
    private HttpClient httpClient;

    @Mock
    private HttpResponse<String> httpResponse;

    private PriceApiClient client;

    @BeforeEach
    void setUp() {
        client = new PriceApiClient(httpClient, "http://mock-price-server:8080");
    }

    @Test
    @DisplayName("given default construction, creates a usable PriceService")
    void defaultConstructorCreatesInstance() {
        assertThat(new PriceApiClient()).isInstanceOf(PriceService.class);
    }

    @Test
    @DisplayName("given a 200 response with valid price JSON, returns the parsed BigDecimal price")
    void getPriceForValidProduct() throws Exception {
        when(httpResponse.statusCode()).thenReturn(200);
        when(httpResponse.body()).thenReturn("{\"title\":\"Corn Flakes\",\"price\":2.52}");
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(httpResponse);

        assertThat(client.getPrice("cornflakes"))
                .isEqualTo(new BigDecimal("2.52"));
    }

    @Test
    @DisplayName("given product name cornflakes, sends GET to /backend-take-home-test-data/cornflakes.json with Accept: application/json")
    void usesCorrectUrl() throws Exception {
        when(httpResponse.statusCode()).thenReturn(200);
        when(httpResponse.body()).thenReturn("{\"price\":1.00}");
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(httpResponse);

        client.getPrice("cornflakes");

        ArgumentCaptor<HttpRequest> captor = ArgumentCaptor.forClass(HttpRequest.class);
        verify(httpClient).send(captor.capture(), any(HttpResponse.BodyHandler.class));
        assertThat(captor.getValue().uri().getPath())
                .endsWith("/backend-take-home-test-data/cornflakes.json");
        assertThat(captor.getValue().method())
                .isEqualTo("GET");
        assertThat(captor.getValue().headers().firstValue("Accept"))
                .hasValue("application/json");
    }

    @Test
    @DisplayName("given a 404 response, throws PriceLookupException containing the status code and product name")
    void throwsOnNon200() throws Exception {
        when(httpResponse.statusCode()).thenReturn(404);
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(httpResponse);

        assertThatThrownBy(() -> client.getPrice("unknown"))
                .isInstanceOf(PriceLookupException.class)
                .hasMessageContaining("404")
                .hasMessageContaining("unknown");
    }

    @Test
    @DisplayName("given a 200 response with a null body, throws PriceLookupException")
    void throwsOnEmptyPriceResponse() throws Exception {
        when(httpResponse.statusCode()).thenReturn(200);
        when(httpResponse.body()).thenReturn(null);
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(httpResponse);

        assertThatThrownBy(() -> client.getPrice("cornflakes"))
                .isInstanceOf(PriceLookupException.class);
    }

    @Test
    @DisplayName("given a 200 response with no price field in the body, throws PriceLookupException")
    void throwsOnMissingPriceField() throws Exception {
        when(httpResponse.statusCode()).thenReturn(200);
        when(httpResponse.body()).thenReturn("{\"title\":\"Corn Flakes\"}");
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(httpResponse);

        assertThatThrownBy(() -> client.getPrice("cornflakes"))
                .isInstanceOf(PriceLookupException.class);
    }

    @Test
    @DisplayName("given the HTTP client throws IOException, wraps it as PriceLookupException")
    void wrapsIOException() throws Exception {
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenThrow(new IOException("Communication failure"));

        assertThatThrownBy(() -> client.getPrice("cornflakes"))
                .isInstanceOf(PriceLookupException.class)
                .cause().isInstanceOf(IOException.class);
    }

    @Test
    @SuppressWarnings("ResultOfMethodCallIgnored")
    @DisplayName("given the HTTP client throws InterruptedException, wraps it as PriceLookupException and restores the thread interrupt flag")
    void wrapsInterruptedException() throws Exception {
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenThrow(new InterruptedException("stop"));

        Thread.interrupted(); // clear any stale flag from prior tests

        assertThatThrownBy(() -> client.getPrice("cornflakes"))
                .isInstanceOf(PriceLookupException.class)
                .cause().isInstanceOf(InterruptedException.class);

        assertThat(Thread.currentThread().isInterrupted())
                .as("thread interrupt flag must be restored after InterruptedException")
                .isTrue();
        Thread.interrupted(); // restore clean state for subsequent tests
    }

    @Test
    @DisplayName("given a blank or null product name, throws IllegalArgumentException")
    void rejectsBlankProductName() {
        assertThatThrownBy(() -> client.getPrice("")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> client.getPrice(null)).isInstanceOf(IllegalArgumentException.class);
    }

}
