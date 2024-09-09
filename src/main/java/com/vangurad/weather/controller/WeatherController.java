package com.vangurad.weather.controller;

import com.vangurad.weather.dto.response.WeatherResponse;
import com.vangurad.weather.service.WeatherService;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("${api.base-url}/1.0/weather")
@Validated
@RequiredArgsConstructor
public class WeatherController {

    private static final String CITY_REGEX = "^[a-zA-Z\\s-]+$";
    private static final String COUNTRY_REGEX = "^[A-Za-z]{2}$";

    private final WeatherService weatherService;

    @GetMapping("/{cityName}/{countryCode}")
    public WeatherResponse getWeatherData(
            @PathVariable
            @Pattern(regexp = CITY_REGEX, message = "Invalid city name")
            @Size(max = 50, message = "City name must be at most 50 characters long")
            String cityName,
            @PathVariable
            @Pattern(regexp = COUNTRY_REGEX, message = "Country code should be 2 letters")
            String countryCode,
            @RequestParam
            @NotBlank(message = "API key must not be blank")
            String appid) {
        return weatherService.getWeather(cityName, countryCode, appid);
    }
}
