package com.example.data.repository

import com.example.data.model.AirQualityData
import com.example.data.model.DailyForecast
import com.example.data.model.HourlyForecast
import com.example.data.model.SavedCity
import com.example.data.model.WeatherCondition
import com.example.data.model.WeatherData
import com.example.utils.WeatherUtils
import java.util.Calendar
import java.util.TimeZone
import kotlin.math.sin

object SampleWeatherData {

    val popularCities = listOf(
        // Bangladesh Divisions & Cities
        SavedCity(name = "Dhaka", state = "Dhaka Division", country = "BD", lat = 23.8103, lon = 90.4125),
        SavedCity(name = "Chittagong", state = "Chittagong Division", country = "BD", lat = 22.3569, lon = 91.7832),
        SavedCity(name = "Sylhet", state = "Sylhet Division", country = "BD", lat = 24.8949, lon = 91.8687),
        SavedCity(name = "Rajshahi", state = "Rajshahi Division", country = "BD", lat = 24.3745, lon = 88.6042),
        SavedCity(name = "Khulna", state = "Khulna Division", country = "BD", lat = 22.8456, lon = 89.5403),
        SavedCity(name = "Barisal", state = "Barisal Division", country = "BD", lat = 22.7010, lon = 90.3535),
        SavedCity(name = "Rangpur", state = "Rangpur Division", country = "BD", lat = 25.7439, lon = 89.2752),
        SavedCity(name = "Mymensingh", state = "Mymensingh Division", country = "BD", lat = 24.7471, lon = 90.4203),
        SavedCity(name = "Comilla", state = "Chittagong Division", country = "BD", lat = 23.4682, lon = 91.1788),
        SavedCity(name = "Cox's Bazar", state = "Chittagong Division", country = "BD", lat = 21.4272, lon = 92.0058),
        SavedCity(name = "Bogura", state = "Rajshahi Division", country = "BD", lat = 24.8465, lon = 89.3777),
        SavedCity(name = "Gazipur", state = "Dhaka Division", country = "BD", lat = 23.9999, lon = 90.4203),
        SavedCity(name = "Narayanganj", state = "Dhaka Division", country = "BD", lat = 23.6238, lon = 90.5000),

        // Global Metropolises
        SavedCity(name = "London", state = "England", country = "GB", lat = 51.5074, lon = -0.1278),
        SavedCity(name = "New York", state = "New York", country = "US", lat = 40.7128, lon = -74.0060),
        SavedCity(name = "Tokyo", state = "Kanto", country = "JP", lat = 35.6762, lon = 139.6503),
        SavedCity(name = "Dubai", state = "Dubai", country = "AE", lat = 25.2048, lon = 55.2708),
        SavedCity(name = "Paris", state = "Ile-de-France", country = "FR", lat = 48.8566, lon = 2.3522),
        SavedCity(name = "Singapore", state = "Singapore", country = "SG", lat = 1.3521, lon = 103.8198),
        SavedCity(name = "Sydney", state = "New South Wales", country = "AU", lat = -33.8688, lon = 151.2093),
        SavedCity(name = "Toronto", state = "Ontario", country = "CA", lat = 43.6532, lon = -79.3832),
        SavedCity(name = "Kolkata", state = "West Bengal", country = "IN", lat = 22.5726, lon = 88.3639),
        SavedCity(name = "Mumbai", state = "Maharashtra", country = "IN", lat = 19.0760, lon = 72.8777),
        SavedCity(name = "Delhi", state = "Delhi", country = "IN", lat = 28.6139, lon = 77.2090),
        SavedCity(name = "Bangkok", state = "Bangkok", country = "TH", lat = 13.7563, lon = 100.5018),
        SavedCity(name = "Kuala Lumpur", state = "Federal Territory", country = "MY", lat = 3.1390, lon = 101.6869),
        SavedCity(name = "Seoul", state = "Seoul", country = "KR", lat = 37.5665, lon = 126.9780),
        SavedCity(name = "Riyadh", state = "Riyadh", country = "SA", lat = 24.7136, lon = 46.6753),
        SavedCity(name = "Berlin", state = "Berlin", country = "DE", lat = 52.5200, lon = 13.4050),
        SavedCity(name = "Rome", state = "Lazio", country = "IT", lat = 41.9028, lon = 12.4964),
        SavedCity(name = "Madrid", state = "Madrid", country = "ES", lat = 40.4168, lon = -3.7038),
        SavedCity(name = "Los Angeles", state = "California", country = "US", lat = 34.0522, lon = -118.2437),
        SavedCity(name = "San Francisco", state = "California", country = "US", lat = 37.7749, lon = -122.4194)
    )

    fun getSampleWeather(cityName: String = "Dhaka", country: String = "BD", lat: Double = 23.8103, lon: Double = 90.4125): WeatherData {
        val nowSec = System.currentTimeMillis() / 1000L
        val cal = Calendar.getInstance()
        val hour = cal.get(Calendar.HOUR_OF_DAY)
        val isDay = hour in 6..18

        // City-specific base temperatures
        val baseTemp = when (cityName.lowercase()) {
            "dhaka" -> 29.0
            "chittagong" -> 28.5
            "sylhet" -> 27.0
            "rajshahi" -> 30.0
            "london" -> 19.0
            "new york" -> 23.0
            "tokyo" -> 25.0
            "dubai" -> 38.0
            "paris" -> 21.0
            "singapore" -> 31.0
            "sydney" -> 20.0
            else -> 26.0
        }

        val condition = when {
            cityName.contains("london", ignoreCase = true) -> WeatherCondition.RAIN
            cityName.contains("sylhet", ignoreCase = true) -> WeatherCondition.SHOWER_RAIN
            cityName.contains("dubai", ignoreCase = true) -> if (isDay) WeatherCondition.CLEAR_DAY else WeatherCondition.CLEAR_NIGHT
            isDay -> WeatherCondition.FEW_CLOUDS_DAY
            else -> WeatherCondition.FEW_CLOUDS_NIGHT
        }

        val humidity = if (condition.isRainy) 82 else 64
        val windSpeed = 14.5
        val dewPoint = WeatherUtils.calculateDewPoint(baseTemp, humidity)

        // Sunrise ~ 5:32 AM, Sunset ~ 6:42 PM
        val calSunrise = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 5)
            set(Calendar.MINUTE, 32)
            set(Calendar.SECOND, 0)
        }
        val calSunset = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 18)
            set(Calendar.MINUTE, 42)
            set(Calendar.SECOND, 0)
        }

        return WeatherData(
            cityName = cityName,
            country = country,
            lat = lat,
            lon = lon,
            currentTempC = baseTemp,
            feelsLikeC = baseTemp + 2.5,
            tempMinC = baseTemp - 4.0,
            tempMaxC = baseTemp + 3.0,
            condition = condition,
            description = when (condition) {
                WeatherCondition.CLEAR_DAY -> "Clear Sky"
                WeatherCondition.CLEAR_NIGHT -> "Clear Night"
                WeatherCondition.FEW_CLOUDS_DAY -> "Partly Cloudy"
                WeatherCondition.FEW_CLOUDS_NIGHT -> "Partly Cloudy"
                WeatherCondition.RAIN -> "Moderate Rain"
                WeatherCondition.SHOWER_RAIN -> "Passing Showers"
                WeatherCondition.THUNDERSTORM -> "Thunderstorm with Rain"
                WeatherCondition.SNOW -> "Light Snow"
                WeatherCondition.MIST -> "Misty Atmosphere"
                else -> "Scattered Clouds"
            },
            iconCode = condition.iconRes,
            humidity = humidity,
            windSpeedKmh = windSpeed,
            windDeg = 135, // SE
            pressureHpa = 1012.0,
            visibilityKm = 9.5,
            cloudiness = if (condition.isCloudy) 45 else 15,
            uvIndex = if (isDay) 6.8 else 0.0,
            dewPointC = dewPoint,
            sunriseTimeEpoch = calSunrise.timeInMillis / 1000L,
            sunsetTimeEpoch = calSunset.timeInMillis / 1000L,
            timezoneOffsetSec = 21600, // +6 hrs
            lastUpdatedEpoch = System.currentTimeMillis()
        )
    }

    fun getSampleHourlyForecast(baseTemp: Double = 29.0): List<HourlyForecast> {
        val list = mutableListOf<HourlyForecast>()
        val cal = Calendar.getInstance()
        val currentHour = cal.get(Calendar.HOUR_OF_DAY)

        for (i in 0 until 24) {
            val hourEpoch = (cal.timeInMillis / 1000L) + (i * 3600L)
            val h = (currentHour + i) % 24
            val isDay = h in 6..18
            val timeLabel = when (i) {
                0 -> "Now"
                else -> {
                    val displayH = if (h == 0) 12 else if (h > 12) h - 12 else h
                    val amPm = if (h < 12) "AM" else "PM"
                    "$displayH $amPm"
                }
            }

            // Diurnal temp curve
            val tempVariation = sin((h - 6) * Math.PI / 12.0) * 4.0
            val temp = (baseTemp + tempVariation * 0.8).coerceIn(15.0, 42.0)
            val pop = if (i in 4..9) 40 else if (i in 10..15) 15 else 5

            val condition = when {
                pop > 35 -> WeatherCondition.SHOWER_RAIN
                !isDay -> if (i % 2 == 0) WeatherCondition.CLEAR_NIGHT else WeatherCondition.FEW_CLOUDS_NIGHT
                else -> if (i % 3 == 0) WeatherCondition.FEW_CLOUDS_DAY else WeatherCondition.CLEAR_DAY
            }

            list.add(
                HourlyForecast(
                    timeLabel = timeLabel,
                    epochTime = hourEpoch,
                    tempC = (temp * 10.0).toInt() / 10.0,
                    feelsLikeC = ((temp + 1.8) * 10.0).toInt() / 10.0,
                    popPercentage = pop,
                    condition = condition,
                    iconCode = condition.iconRes,
                    windSpeedKmh = 10.0 + (i % 5) * 2.0,
                    isCurrentHour = i == 0
                )
            )
        }
        return list
    }

    fun getSample7DayForecast(baseTemp: Double = 29.0): List<DailyForecast> {
        val list = mutableListOf<DailyForecast>()
        val cal = Calendar.getInstance()
        val dayNames = arrayOf("Today", "Tomorrow", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")

        for (i in 0 until 7) {
            val dayEpoch = (cal.timeInMillis / 1000L) + (i * 86400L)
            val dayName = when (i) {
                0 -> "Today"
                1 -> "Tomorrow"
                else -> WeatherUtils.formatEpochTime(dayEpoch, pattern = "EEE")
            }
            val dateLabel = WeatherUtils.formatEpochTime(dayEpoch, pattern = "MMM d")
            val pop = when (i) {
                0 -> 15
                1 -> 45
                2 -> 70
                3 -> 30
                4 -> 10
                5 -> 5
                else -> 20
            }

            val condition = when {
                pop >= 60 -> WeatherCondition.RAIN
                pop >= 40 -> WeatherCondition.SHOWER_RAIN
                pop >= 20 -> WeatherCondition.FEW_CLOUDS_DAY
                else -> WeatherCondition.CLEAR_DAY
            }

            val minT = baseTemp - 4.5 + (i % 2) * 0.5
            val maxT = baseTemp + 2.5 + ((i + 1) % 3) * 0.7

            list.add(
                DailyForecast(
                    dayName = dayName,
                    dateLabel = dateLabel,
                    epochTime = dayEpoch,
                    minTempC = (minT * 10.0).toInt() / 10.0,
                    maxTempC = (maxT * 10.0).toInt() / 10.0,
                    popPercentage = pop,
                    condition = condition,
                    description = when (condition) {
                        WeatherCondition.RAIN -> "Moderate Rain"
                        WeatherCondition.SHOWER_RAIN -> "Passing Showers"
                        WeatherCondition.FEW_CLOUDS_DAY -> "Partly Sunny"
                        else -> "Mostly Clear"
                    },
                    iconCode = condition.iconRes,
                    humidity = 60 + (i * 3) % 25,
                    windSpeedKmh = 12.0 + (i % 4) * 2.5
                )
            )
        }
        return list
    }

    fun getSampleAirQuality(cityName: String = "Dhaka"): AirQualityData {
        val (aqiVal, cat) = when (cityName.lowercase()) {
            "dhaka" -> Pair(142, "Unhealthy for Sensitive Groups")
            "london" -> Pair(35, "Good")
            "tokyo" -> Pair(28, "Good")
            "dubai" -> Pair(88, "Moderate")
            "new york" -> Pair(46, "Good")
            else -> Pair(65, "Moderate")
        }

        return AirQualityData(
            aqiIndex = when {
                aqiVal <= 50 -> 1
                aqiVal <= 100 -> 2
                aqiVal <= 150 -> 3
                aqiVal <= 200 -> 4
                aqiVal <= 300 -> 5
                else -> 6
            },
            aqiValue = aqiVal,
            categoryName = cat,
            pm2_5 = if (aqiVal > 100) 52.4 else 18.2,
            pm10 = if (aqiVal > 100) 84.1 else 32.0,
            co = 420.5,
            no2 = 24.3,
            so2 = 8.1,
            o3 = 45.2
        )
    }
}
