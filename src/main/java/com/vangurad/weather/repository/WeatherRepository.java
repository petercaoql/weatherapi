package com.vangurad.weather.repository;

import com.vangurad.weather.entity.Weather;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WeatherRepository extends JpaRepository<Weather, Long> {
    Optional<Weather> findByCityNameAndCountryCode(String cityName, String countryCode);
}
