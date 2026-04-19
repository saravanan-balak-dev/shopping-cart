package com.shoppingcart.util;

import com.shoppingcart.exception.PriceLookupException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("RegexPriceParser")
class RegexPriceParserTest {

    @Test
    @DisplayName("given JSON with a decimal price field, returns the price as BigDecimal")
    void parsesDecimalPrice() {
        assertThat(RegexPriceParser.parse("{\"title\":\"Corn Flakes\",\"price\":2.52}", "cornflakes"))
                .isEqualTo(new BigDecimal("2.52"));
    }

    @Test
    @DisplayName("given JSON with an integer price field, returns the price as BigDecimal")
    void parsesIntegerPrice() {
        assertThat(RegexPriceParser.parse("{\"title\":\"Corn Flakes\",\"price\":5}", "cornflakes"))
                .isEqualTo(new BigDecimal("5"));
    }

    @Test
    @DisplayName("given JSON with whitespace around the price colon, returns the correct price")
    void parsesWithWhitespaceAroundColon() {
        assertThat(RegexPriceParser.parse("{\"price\" : 9.99}", "weetabix"))
                .isEqualTo(new BigDecimal("9.99"));
    }

    @Test
    @DisplayName("given JSON with a negative price, throws PriceLookupException")
    void throwsForNegativePrice() {
        assertThatThrownBy(() -> RegexPriceParser.parse("{\"price\":-1.50}", "cornflakes"))
                .isInstanceOf(PriceLookupException.class);
    }

    @Test
    @DisplayName("given JSON without a price field, throws PriceLookupException with the product name")
    void throwsWhenPriceFieldAbsent() {
        assertThatThrownBy(() -> RegexPriceParser.parse("{\"title\":\"Corn Flakes\"}", "cornflakes"))
                .isInstanceOf(PriceLookupException.class)
                .hasMessageContaining("cornflakes");
    }

    @Test
    @DisplayName("given an empty response body, throws PriceLookupException")
    void throwsForEmptyBody() {
        assertThatThrownBy(() -> RegexPriceParser.parse("", "frosties"))
                .isInstanceOf(PriceLookupException.class);
    }

    @Test
    @DisplayName("given an empty JSON object, throws PriceLookupException")
    void throwsForEmptyObject() {
        assertThatThrownBy(() -> RegexPriceParser.parse("{}", "frosties"))
                .isInstanceOf(PriceLookupException.class);
    }
}
