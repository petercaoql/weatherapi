package com.vangurad.weather.service;

import com.vangurad.weather.dto.response.WeatherResponse;
import com.vangurad.weather.entity.Weather;
import com.vangurad.weather.repository.WeatherRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class WeatherService {

    @Value("${api.weather-data-ttl-minutes}")
    private long weatherDataExpiration;

    private final WeatherRepository weatherRepository;
    private final OpenWeatherMapService openWeatherMapService;

    /**
     * Retrieve weather data from the database based on city and country.
     * <p>
     * If a weather data entry exists and it was updated within the last 5 minutes, the method returns that data.
     * If the data is outdated, it calls the OpenWeatherMap API to update the data and then returns the updated data.
     * If no weather data exists, it fetches new data from the OpenWeatherMap API, saves it, and returns the new data.
     * </p>
     *
     * @param cityName The city name to retrieve the weather data.
     * @param countryCode The country code to retrieve the weather data.
     * @param appid The API key for accessing the OpenWeatherMap API.
     * @return A {@link WeatherResponse} containing the weather description for the specified city and country.
     */
    public WeatherResponse getWeather(String cityName, String countryCode, String appid) {
        log.debug("Fetching weather data for city: {} and country: {}", cityName, countryCode);
        Optional<Weather> optionalWeather = weatherRepository.findByCityNameAndCountryCode(cityName, countryCode);
        if (optionalWeather.isPresent()) {
            Weather existingWeather = optionalWeather.get();
            Duration duration = Duration.between(existingWeather.getLastUpdated(), LocalDateTime.now());
            // Return the existing weather if it's updated within 5 minutes
            if (duration.toMinutes() <= weatherDataExpiration) {
                return new WeatherResponse(existingWeather.getDescription());
            }

            // Update the existing weather if it's outdated
            WeatherResponse response = openWeatherMapService.getOpenWeatherData(cityName, countryCode, appid);
            existingWeather.setDescription(response.getDescription());
            existingWeather.setLastUpdated(LocalDateTime.now());
            weatherRepository.save(existingWeather);
            return response;
        }

        // No existing weather data, fetch new data and save it
        WeatherResponse response = openWeatherMapService.getOpenWeatherData(cityName, countryCode, appid);
        Weather newWeather = Weather.builder()
                .cityName(cityName)
                .countryCode(countryCode)
                .description(response.getDescription())
                .lastUpdated(LocalDateTime.now())
                .build();
        weatherRepository.save(newWeather);
        return response;
    }

}
