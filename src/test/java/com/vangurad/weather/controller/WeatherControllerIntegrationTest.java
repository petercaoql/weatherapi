package com.vangurad.weather.controller;

import com.vangurad.weather.dto.response.WeatherResponse;
import com.vangurad.weather.service.ApiKeyService;
import com.vangurad.weather.service.WeatherService;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.mockito.ArgumentMatchers.anyString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@Tag("integration")
@SpringBootTest
@AutoConfigureMockMvc
class WeatherControllerIntegrationTest {

    private static final String WEATHER_API_URL = "/data/1.0/weather/{cityName}/{countryCode}";
    private static final String VALID_API_KEY = "25c7a83616ae8632d5b9df84cfea4114";
    private static final String INVALID_API_KEY = "invalid-api-key";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ApiKeyService apiKeyService;

    @MockBean
    private WeatherService weatherService;

    @Test
    void shouldReturnUnauthorizedForMissingAppId() throws Exception {
        // Perform GET request without the api key
        mockMvc.perform(get(WEATHER_API_URL, "London", "uk")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(MockMvcResultMatchers.jsonPath("$.error").value("Invalid API Key"));
    }

    @Test
    void shouldReturnUnauthorizedForInvalidAppId() throws Exception {
        // Perform GET request with invalid api key
        mockMvc.perform(get(WEATHER_API_URL, "London", "uk")
                        .param("appid", INVALID_API_KEY)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(MockMvcResultMatchers.jsonPath("$.error").value("Invalid API Key"));
    }

    @Test
    void shouldReturnWeatherDataForValidCityAndCountry() throws Exception {
        WeatherResponse mockResponse = new WeatherResponse("Clear sky");
        Mockito.when(weatherService.getWeather(anyString(), anyString(), anyString()))
                .thenReturn(mockResponse);

        // Perform GET request with valid cityName, countryCode, and appid
        mockMvc.perform(get(WEATHER_API_URL, "London", "uk")
                        .param("appid", VALID_API_KEY)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("Clear sky"));
    }

    @Test
    void shouldReturnBadRequestForInvalidCity() throws Exception {
        // Perform GET request with invalid city name
        mockMvc.perform(get(WEATHER_API_URL, "London123", "uk")
                        .param("appid", VALID_API_KEY)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

//    @Test
    @Disabled("This test is disabled due to rate limit issues")
    public void shouldReturnRooManyRequestsForRateLimitExceeded() throws Exception {
        // Make 5 API calls
        for (int i = 0; i < 5; i++) {
            mockMvc.perform(get(WEATHER_API_URL, "London", "uk")
                            .param("appid", VALID_API_KEY)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.status().isOk());
        }

        // Test exceeding the rate limit
        mockMvc.perform(get(WEATHER_API_URL, "London", "uk")
                        .param("appid", VALID_API_KEY)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isTooManyRequests());
    }

}
