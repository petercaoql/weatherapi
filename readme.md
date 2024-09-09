# Weather API Project

## Overview

This project provides a weather API service that interacts with the OpenWeatherMap API, fetching weather data for a given city and country. The project also implements rate-limiting using API keys and a caching mechanism for weather data to reduce the number of external API calls.

## Features

1. **API Key Rate Limit**
    - The API key rate limit is handled by an interceptor, which validates API keys before requests reach the controllers.
    - The rate limit is enforced using the Token-Bucket Algorithm.
    - The API key/bucket pairs are stored in a `ConcurrentHashMap`.
    - If an API key is invalid or the hourly rate limit is exceeded, appropriate exceptions are thrown.
    - Exceptions are handled by a `@RestControllerAdvice`, which returns user-friendly JSON responses.
    - If an API key is missing or invalid, a `HTTP 401 Unauthorized` response is returned.
    - If the API key has exceeded the hourly rate limit, a `HTTP 429 Too Many Requests` response is returned.

2. **Weather Data Fetch Logic**
    - To optimize the cost of external API calls, weather data is cached in the database. If the data was updated within the last 5 minutes, the database data is returned instead of making a new API call.
    - The weather description from OpenWeatherMap is validated to ensure accuracy and consistency.

## Assumptions and Trade-offs

1. **Security Implementation**:  
   Since there is no username/password or access token authentication, Spring Security is not introduced in this project. However, if Spring Security were to be used, API key validation could be placed in a `OncePerRequestFilter` within the Spring Security filter chain.

2. **Rate Limit Storage**:  
   The API key/bucket pairs are stored in a `ConcurrentHashMap`. In a production environment, a more robust key-value store like Redis would be required for better performance and persistence.

3. **Database Setup**:  
   The H2 database is initialized using `schema.sql` located in the resources folder. For production use, a database migration tool like Flyway should be used for managing schema changes.

4. **Security Error Messages**:  
   This project returns clear and descriptive error messages in cases of security failures (e.g., invalid API key). However, in production environments, it is advisable to avoid revealing detailed error messages, as they could aid potential cyberattacks.

