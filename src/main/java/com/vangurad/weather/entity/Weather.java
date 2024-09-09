package com.vangurad.weather.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "WEATHER", indexes = {
        @Index(name = "idx_city_country", columnList = "CITY_NAME, COUNTRY_CODE")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Weather {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Size(max = 50)
    @Column(name = "CITY_NAME", nullable = false, length = 50)
    private String cityName;

    @NotNull
    @Size(max = 2)
    @Column(name = "COUNTRY_CODE", nullable = false, length = 2)
    private String countryCode;

    @NotNull
    @Size(max = 150)
    @Column(name = "DESCRIPTION", nullable = false, length = 150)
    private String description;

    @NotNull
    @Column(name = "LAST_UPDATED", nullable = false)
    private LocalDateTime lastUpdated;
}
