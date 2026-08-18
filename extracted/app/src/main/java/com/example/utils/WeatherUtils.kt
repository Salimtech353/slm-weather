package com.example.utils

import com.example.data.model.AirQualityData
import com.example.data.model.WeatherCondition
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.math.ln
import kotlin.math.roundToInt

object WeatherUtils {

    fun mapCondition(iconCode: String?, weatherId: Int?): WeatherCondition {
        val isNight = iconCode?.endsWith("n") == true

        if (weatherId != null) {
            return when (weatherId) {
                in 200..232 -> WeatherCondition.THUNDERSTORM
                in 300..321 -> WeatherCondition.SHOWER_RAIN
                in 500..504 -> if (isNight) WeatherCondition.RAIN else WeatherCondition.RAIN
                511 -> WeatherCondition.SNOW
                in 520..531 -> WeatherCondition.SHOWER_RAIN
                in 600..622 -> WeatherCondition.SNOW
                in 701..781 -> WeatherCondition.MIST
                800 -> if (isNight) WeatherCondition.CLEAR_NIGHT else WeatherCondition.CLEAR_DAY
                801 -> if (isNight) WeatherCondition.FEW_CLOUDS_NIGHT else WeatherCondition.FEW_CLOUDS_DAY
                802 -> WeatherCondition.SCATTERED_CLOUDS
                803, 804 -> WeatherCondition.BROKEN_CLOUDS
                else -> if (isNight) WeatherCondition.CLEAR_NIGHT else WeatherCondition.CLEAR_DAY
            }
        }

        return when (iconCode) {
            "01d" -> WeatherCondition.CLEAR_DAY
            "01n" -> WeatherCondition.CLEAR_NIGHT
            "02d" -> WeatherCondition.FEW_CLOUDS_DAY
            "02n" -> WeatherCondition.FEW_CLOUDS_NIGHT
            "03d", "03n" -> WeatherCondition.SCATTERED_CLOUDS
            "04d", "04n" -> WeatherCondition.BROKEN_CLOUDS
            "09d", "09n" -> WeatherCondition.SHOWER_RAIN
            "10d", "10n" -> WeatherCondition.RAIN
            "11d", "11n" -> WeatherCondition.THUNDERSTORM
            "13d", "13n" -> WeatherCondition.SNOW
            "50d", "50n" -> WeatherCondition.MIST
            else -> WeatherCondition.CLEAR_DAY
        }
    }

    fun getWindDirection(deg: Int): String {
        val directions = arrayOf("N", "NNE", "NE", "ENE", "E", "ESE", "SE", "SSE", "S", "SSW", "SW", "WSW", "W", "WNW", "NW", "NNW")
        val index = ((deg + 11.25) / 22.5).toInt() % 16
        return directions[index]
    }

    fun calculateDewPoint(tempC: Double, humidityPercent: Int): Double {
        val a = 17.27
        val b = 237.7
        val rh = (humidityPercent.coerceIn(1, 100)) / 100.0
        val alpha = ((a * tempC) / (b + tempC)) + ln(rh)
        val dewPoint = (b * alpha) / (a - alpha)
        return (dewPoint * 10.0).roundToInt() / 10.0
    }

    fun getUvCategory(uv: Double): String {
        return when {
            uv <= 2.9 -> "Low"
            uv <= 5.9 -> "Moderate"
            uv <= 7.9 -> "High"
            uv <= 10.9 -> "Very High"
            else -> "Extreme"
        }
    }

    fun mapAqiData(apiAqi: Int?, pm25: Double, pm10: Double, co: Double, no2: Double, so2: Double, o3: Double): AirQualityData {
        // Approximate standard AQI scale (0-500) from PM2.5 and OpenWeather index
        val calcValue = when {
            pm25 <= 12.0 -> (pm25 * 50.0 / 12.0).toInt().coerceIn(10, 50)
            pm25 <= 35.4 -> (50 + (pm25 - 12.0) * 50.0 / (35.4 - 12.0)).toInt().coerceIn(51, 100)
            pm25 <= 55.4 -> (100 + (pm25 - 35.4) * 50.0 / (55.4 - 35.4)).toInt().coerceIn(101, 150)
            pm25 <= 150.4 -> (150 + (pm25 - 55.4) * 50.0 / (150.4 - 55.4)).toInt().coerceIn(151, 200)
            pm25 <= 250.4 -> (200 + (pm25 - 150.4) * 100.0 / (250.4 - 150.4)).toInt().coerceIn(201, 300)
            else -> 320
        }

        val aqiIndex = when {
            calcValue <= 50 -> 1
            calcValue <= 100 -> 2
            calcValue <= 150 -> 3
            calcValue <= 200 -> 4
            calcValue <= 300 -> 5
            else -> 6
        }

        val category = when (aqiIndex) {
            1 -> "Good"
            2 -> "Moderate"
            3 -> "Unhealthy for Sensitive Groups"
            4 -> "Unhealthy"
            5 -> "Very Unhealthy"
            else -> "Hazardous"
        }

        return AirQualityData(
            aqiIndex = aqiIndex,
            aqiValue = calcValue,
            categoryName = category,
            pm2_5 = (pm25 * 10.0).roundToInt() / 10.0,
            pm10 = (pm10 * 10.0).roundToInt() / 10.0,
            co = (co * 10.0).roundToInt() / 10.0,
            no2 = (no2 * 10.0).roundToInt() / 10.0,
            so2 = (so2 * 10.0).roundToInt() / 10.0,
            o3 = (o3 * 10.0).roundToInt() / 10.0
        )
    }

    fun formatEpochTime(epochSec: Long, timezoneOffsetSec: Int = 0, pattern: String = "h:mm a"): String {
        val date = Date(epochSec * 1000L)
        val sdf = SimpleDateFormat(pattern, Locale.getDefault())
        sdf.timeZone = TimeZone.getTimeZone("UTC").apply {
            rawOffset = timezoneOffsetSec * 1000
        }
        return sdf.format(date)
    }

    fun formatDayName(epochSec: Long, timezoneOffsetSec: Int = 0): String {
        val now = System.currentTimeMillis() / 1000L
        val diffDays = ((epochSec + timezoneOffsetSec) / 86400) - ((now + timezoneOffsetSec) / 86400)
        return when (diffDays) {
            0L -> "Today"
            1L -> "Tomorrow"
            else -> formatEpochTime(epochSec, timezoneOffsetSec, "EEE")
        }
    }

    fun calculateDayProgress(sunriseEpoch: Long, sunsetEpoch: Long, timezoneOffsetSec: Int): Float {
        val nowEpoch = (System.currentTimeMillis() / 1000L)
        if (sunriseEpoch == 0L || sunsetEpoch == 0L || sunsetEpoch <= sunriseEpoch) return 0.5f
        if (nowEpoch < sunriseEpoch) return 0f
        if (nowEpoch > sunsetEpoch) return 1f
        val total = (sunsetEpoch - sunriseEpoch).toFloat()
        val current = (nowEpoch - sunriseEpoch).toFloat()
        return (current / total).coerceIn(0f, 1f)
    }

    fun isCurrentlyDaytime(sunriseEpoch: Long, sunsetEpoch: Long): Boolean {
        val nowEpoch = System.currentTimeMillis() / 1000L
        if (sunriseEpoch == 0L || sunsetEpoch == 0L) return true
        return nowEpoch in sunriseEpoch..sunsetEpoch
    }
}
