package com.example.data.model

// Main Weather Condition Enum
enum class WeatherCondition(val displayName: String, val iconRes: String) {
    CLEAR_DAY("Clear Sky", "01d"),
    CLEAR_NIGHT("Clear Night", "01n"),
    FEW_CLOUDS_DAY("Few Clouds", "02d"),
    FEW_CLOUDS_NIGHT("Few Clouds", "02n"),
    SCATTERED_CLOUDS("Scattered Clouds", "03d"),
    BROKEN_CLOUDS("Broken Clouds", "04d"),
    OVERCAST_CLOUDS("Overcast Clouds", "04d"),
    SHOWER_RAIN("Shower Rain", "09d"),
    RAIN("Rain", "10d"),
    THUNDERSTORM("Thunderstorm", "11d"),
    SNOW("Snow", "13d"),
    MIST("Mist / Fog", "50d"),
    UNKNOWN("Clear", "01d");

    val isDay: Boolean
        get() = this in listOf(CLEAR_DAY, FEW_CLOUDS_DAY, SCATTERED_CLOUDS, BROKEN_CLOUDS, OVERCAST_CLOUDS, SHOWER_RAIN, RAIN, THUNDERSTORM, SNOW, MIST)

    val isRainy: Boolean
        get() = this in listOf(SHOWER_RAIN, RAIN, THUNDERSTORM)

    val isSnowy: Boolean
        get() = this == SNOW

    val isStormy: Boolean
        get() = this == THUNDERSTORM

    val isCloudy: Boolean
        get() = this in listOf(FEW_CLOUDS_DAY, FEW_CLOUDS_NIGHT, SCATTERED_CLOUDS, BROKEN_CLOUDS, OVERCAST_CLOUDS)
}

// Complete Weather State for UI
data class WeatherData(
    val cityName: String,
    val country: String,
    val lat: Double,
    val lon: Double,
    val currentTempC: Double,
    val feelsLikeC: Double,
    val tempMinC: Double,
    val tempMaxC: Double,
    val condition: WeatherCondition,
    val description: String,
    val iconCode: String,
    val humidity: Int, // %
    val windSpeedKmh: Double, // km/h
    val windDeg: Int, // degrees 0-360
    val pressureHpa: Double, // hPa
    val visibilityKm: Double, // km
    val cloudiness: Int, // %
    val uvIndex: Double = 0.0, // 0-12
    val dewPointC: Double = 0.0,
    val sunriseTimeEpoch: Long,
    val sunsetTimeEpoch: Long,
    val timezoneOffsetSec: Int = 0,
    val lastUpdatedEpoch: Long = System.currentTimeMillis()
)

// Hourly forecast item
data class HourlyForecast(
    val timeLabel: String, // e.g. "Now", "11 AM", "12 PM"
    val epochTime: Long,
    val tempC: Double,
    val feelsLikeC: Double,
    val popPercentage: Int, // Probability of precipitation 0-100%
    val condition: WeatherCondition,
    val iconCode: String,
    val windSpeedKmh: Double,
    val isCurrentHour: Boolean = false
)

// Daily forecast item (7-day)
data class DailyForecast(
    val dayName: String, // e.g. "Today", "Tomorrow", "Mon", "Tue"
    val dateLabel: String, // e.g. "Aug 14"
    val epochTime: Long,
    val minTempC: Double,
    val maxTempC: Double,
    val popPercentage: Int,
    val condition: WeatherCondition,
    val description: String,
    val iconCode: String,
    val humidity: Int,
    val windSpeedKmh: Double
)

// Air Quality Data Model
data class AirQualityData(
    val aqiIndex: Int, // 1=Good, 2=Moderate, 3=Unhealthy for sensitive, 4=Unhealthy, 5=Very Unhealthy, 6=Hazardous
    val aqiValue: Int, // 0-500 standard scale approximation
    val categoryName: String, // "Good", "Moderate", "Unhealthy for Sensitive Groups", etc.
    val pm2_5: Double,
    val pm10: Double,
    val co: Double,
    val no2: Double,
    val so2: Double,
    val o3: Double
)

// Saved City for Local DB & Multi-City selector
data class SavedCity(
    val id: Long = 0,
    val name: String,
    val state: String = "",
    val country: String,
    val lat: Double,
    val lon: Double,
    val isCurrentLocation: Boolean = false,
    val cachedTempC: Double? = null,
    val cachedCondition: String? = null,
    val cachedIcon: String? = null
)

// Units configuration
enum class TempUnit(val symbol: String) {
    CELSIUS("°C"),
    FAHRENHEIT("°F");

    fun convert(tempC: Double): Double = when (this) {
        CELSIUS -> tempC
        FAHRENHEIT -> (tempC * 9.0 / 5.0) + 32.0
    }
}

enum class WindUnit(val label: String) {
    KMH("km/h"),
    MS("m/s"),
    MPH("mph");

    fun convert(speedKmh: Double): Double = when (this) {
        KMH -> speedKmh
        MS -> speedKmh / 3.6
        MPH -> speedKmh * 0.621371
    }
}

enum class PressureUnit(val label: String) {
    HPA("hPa"),
    INHG("inHg");

    fun convert(pressureHpa: Double): Double = when (this) {
        HPA -> pressureHpa
        INHG -> pressureHpa * 0.02953
    }
}

enum class ThemeMode {
    SYSTEM,
    DARK,
    LIGHT
}

data class UnitsConfig(
    val tempUnit: TempUnit = TempUnit.CELSIUS,
    val windUnit: WindUnit = WindUnit.KMH,
    val pressureUnit: PressureUnit = PressureUnit.HPA,
    val themeMode: ThemeMode = ThemeMode.DARK,
    val notificationsEnabled: Boolean = true,
    val animationsEnabled: Boolean = true,
    val autoDetectLocation: Boolean = true,
    val customApiKey: String = ""
)
