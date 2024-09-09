package com.vangurad.weather.service;

import com.vangurad.weather.dto.response.OpenWeatherMapResponse;
import com.vangurad.weather.dto.response.WeatherResponse;
import com.vangurad.weather.exception.ExternalApiException;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Slf4j
@Service
public class OpenWeatherMapService {

    private final String SPECIAL_CHARACTERS_REGEX = "[^a-zA-Z0-9\\s]";

    @Value("${openweathermap.api-url}")
    private String openWeatherMapUrl;

    private RestClient restClient;

    @PostConstruct
    private void postConstruct() {
        restClient = RestClient.builder()
                .baseUrl(openWeatherMapUrl)
                .build();
    }

    /**
     * Fetches weather data from the OpenWeatherMap API based on the provided city and country.
     * <p>
     * This method makes an API call to OpenWeatherMap using the provided city and country as the query parameter,
     * and the provided API key for authentication. It returns the weather description as a string.
     * </p>
     *
     * @param cityName The city name to retrieve the weather data.
     * @param countryCode The country code to retrieve the weather data.
     * @param appid The API key for accessing the OpenWeatherMap API.
     * @return {@link WeatherResponse} containing the weather description for the specified city and country.
     */
    public WeatherResponse getOpenWeatherData(String cityName, String countryCode, String appid) {
        String q = cityName + "," + countryCode;
        log.debug("Calling OpenWeatherMap API with query: {}", q);
        OpenWeatherMapResponse result = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .queryParam("q", q)
                        .queryParam("appid", appid)
                        .build())
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, (request, response) -> {
                    throw new ExternalApiException("OpenWeatherMap API 4xxClientError: " + response.getStatusCode());
                })
                .onStatus(HttpStatusCode::is5xxServerError, (request, response) -> {
                    throw new ExternalApiException("OpenWeatherMap API 5xxServerError: " + response.getStatusCode());
                })
                .body(OpenWeatherMapResponse.class);

        if (result == null || result.getWeather() == null || result.getWeather().get(0) == null) {
            throw new ExternalApiException("No weather data returned from OpenWeatherMap");
        }

        WeatherResponse weatherResponse = result.getWeather().get(0);
        String description = validateWeatherData(weatherResponse.getDescription());
        weatherResponse.setDescription(description);

        return weatherResponse;
    }

    /**
     * Validates and cleans up the weather description retrieved from the OpenWeatherMap API.
     * <p>
     * Assuming that API response from third party is not always valid.
     * This method processes the raw weather description string to ensure it adheres to the expected format by
     * removing any unwanted characters.
     * </p>
     *
     * @param description The raw weather description string retrieved from the OpenWeatherMap API.
     * @return A cleaned and validated weather description string.
     */
    private String validateWeatherData(String description) {
        if (StringUtils.isBlank(description)) {
            throw new ExternalApiException("No data found");
        }

        return description.replaceAll(SPECIAL_CHARACTERS_REGEX, "");
    }
}
