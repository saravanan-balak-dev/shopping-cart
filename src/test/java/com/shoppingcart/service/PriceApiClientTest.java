package com.shoppingcart.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("PriceApiClient")
class PriceApiClientTest {

    @Mock
    private HttpClient httpClient;

    @Mock
    private HttpResponse<String> httpResponse;

    private PriceApiClient client;

    @BeforeEach
    void setUp() {
        client = new PriceApiClient(httpClient);
    }

    @Test
    @DisplayName("")
    void getPriceForValidProduct() throws Exception {
        when(httpResponse.statusCode()).thenReturn(200);
        when(httpResponse.body()).thenReturn("{\"title\":\"Corn Flakes\",\"price\":2.52}");
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(httpResponse);

        BigDecimal price = client.getUnitPrice("cornflakes");

        assertEquals(new BigDecimal("2.52"), price);
    }

    @Test
    @DisplayName("builds the expected URL from base URL + product name")
    void usesCorrectUrl() throws Exception {
        when(httpResponse.statusCode()).thenReturn(200);
        when(httpResponse.body()).thenReturn("{\"price\":1.00}");
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(httpResponse);

        client.getUnitPrice("cornflakes");

        ArgumentCaptor<HttpRequest> captor = ArgumentCaptor.forClass(HttpRequest.class);
        verify(httpClient).send(captor.capture(), any(HttpResponse.BodyHandler.class));
        assertEquals(
                "/backend-take-home-test-data/weetabix.json",
                captor.getValue().uri().toString());
        assertEquals("GET", captor.getValue().method());
    }

    @Test
    @DisplayName("throws exception when product not found")
    void throwsOnNon200() throws Exception {
        when(httpResponse.statusCode()).thenReturn(404);
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(httpResponse);

        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> client.getUnitPrice("unknown"));
        assertTrue(ex.getMessage().contains("404"));
        assertTrue(ex.getMessage().contains("unknown"));
    }

    @Test
    @DisplayName("throws exception when response body has no price field")
    void throwsOnMissingPriceField() throws Exception {
        when(httpResponse.statusCode()).thenReturn(200);
        when(httpResponse.body()).thenReturn("{\"title\":\"Corn Flakes\"}");
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(httpResponse);

        assertThrows(RuntimeException.class, () -> client.getUnitPrice("cornflakes"));
    }

    @Test
    @DisplayName("rejects blank product name")
    void rejectsBlankProductName() {
        assertThrows(IllegalArgumentException.class, () -> client.getUnitPrice(""));
        assertThrows(IllegalArgumentException.class, () -> client.getUnitPrice(null));
    }
}
