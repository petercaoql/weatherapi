# Weather API Project

## 1. API Key Rate Limit

The API key rate limit is managed by an interceptor that validates API keys before requests reach the controllers. The rate limiting mechanism is implemented using the **Token-Bucket Algorithm**, and API key/bucket pairs are stored in a `ConcurrentHashMap`. If the API key is invalid or the hourly rate limit is exceeded, relevant exceptions are thrown.

- **Exception Handling:** Managed by `@RestControllerAdvice`, which provides user-friendly JSON responses.
- **Error Responses:**
    - **401 Unauthorized:** If the API key is missing or invalid.
    - **429 Too Many Requests:** If the API key exceeds its hourly limit.

## 2. Weather Data Fetch Logic

To optimize the system and reduce costs associated with external API calls, the service minimizes calls to OpenWeatherMap. If the weather data in our database was updated within the last 5 minutes, the service will return the stored data instead of making a new API request.

- **Data Validation:** Basic validation is applied to the weather description from the OpenWeatherMap API to ensure the data’s accuracy and format.

## Assumptions and Trade-offs

1. **Authentication:**
    - No username/password or access token authentication is implemented, so Spring Security is not used.
    - If Spring Security were introduced, API key validation could be handled by a `OncePerRequestFilter` in the security filter chain.

2. **Data Storage:**
    - The API key/bucket pairs are currently stored in a `ConcurrentHashMap`.
    - In a production environment, a more scalable data store like **Redis** should be used.

3. **Database Management:**
    - The project uses an H2 database, initialized via `schema.sql`.
    - In production, tools like **Flyway** are recommended for managing database migrations and versioning.

