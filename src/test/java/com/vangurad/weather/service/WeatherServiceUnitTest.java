package com.vangurad.weather.service;

import com.vangurad.weather.dto.response.WeatherResponse;
import com.vangurad.weather.entity.Weather;
import com.vangurad.weather.repository.WeatherRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@Tag("unit")
public class WeatherServiceUnitTest {

    @Mock
    private WeatherRepository weatherRepository;

    @Mock
    private OpenWeatherMapService openWeatherMapService;

    @InjectMocks
    private WeatherService weatherService;

    private String cityName = "London";
    private String countryCode = "uk";
    private String appid = "test-api-key";

    private Weather existingWeather;
    private WeatherResponse apiResponse;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        weatherService = new WeatherService(weatherRepository, openWeatherMapService);
        ReflectionTestUtils.setField(weatherService, "weatherDataExpiration", 5L);

        existingWeather = Weather.builder()
                .cityName(cityName)
                .countryCode(countryCode)
                .description("Sunny")
                .lastUpdated(LocalDateTime.now().minusMinutes(2)) // within expiration
                .build();

        apiResponse = new WeatherResponse("Cloudy");
    }

    @Test
    void testGetWeather_ReturnsExistingWeather() {
        // Given existing weather data within the expiration time
        when(weatherRepository.findByCityNameAndCountryCode(cityName, countryCode)).thenReturn(Optional.of(existingWeather));

        WeatherResponse result = weatherService.getWeather(cityName, countryCode, appid);

        verify(weatherRepository, times(1)).findByCityNameAndCountryCode(cityName, countryCode);
        verify(openWeatherMapService, never()).getOpenWeatherData(anyString(), anyString(), anyString());
        assertEquals("Sunny", result.getDescription());
    }

    @Test
    void testGetWeather_UpdatesExpiredWeather() {
        // Given expired weather data (last updated more than 5 minutes ago)
        existingWeather.setLastUpdated(LocalDateTime.now().minusMinutes(10));
        when(weatherRepository.findByCityNameAndCountryCode(cityName, countryCode)).thenReturn(Optional.of(existingWeather));
        when(openWeatherMapService.getOpenWeatherData(cityName, countryCode, appid)).thenReturn(apiResponse);

        WeatherResponse result = weatherService.getWeather(cityName, countryCode, appid);

        verify(openWeatherMapService, times(1)).getOpenWeatherData(cityName, countryCode, appid);
        verify(weatherRepository, times(1)).save(existingWeather);
        assertEquals("Cloudy", result.getDescription());
        assertEquals("Cloudy", existingWeather.getDescription()); // Weather data updated
    }

    @Test
    void testGetWeather_FetchesNewWeatherData() {
        // Given no existing weather data
        when(weatherRepository.findByCityNameAndCountryCode(cityName, countryCode)).thenReturn(Optional.empty());
        when(openWeatherMapService.getOpenWeatherData(cityName, countryCode, appid)).thenReturn(apiResponse);

        WeatherResponse result = weatherService.getWeather(cityName, countryCode, appid);

        verify(openWeatherMapService, times(1)).getOpenWeatherData(cityName, countryCode, appid);
        verify(weatherRepository, times(1)).save(any(Weather.class));
        assertEquals("Cloudy", result.getDescription());
    }
}

