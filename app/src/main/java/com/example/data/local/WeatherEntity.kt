package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "saved_cities")
data class SavedCityEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val state: String = "",
    val country: String,
    val lat: Double,
    val lon: Double,
    val isCurrentLocation: Boolean = false,
    val cachedTempC: Double? = null,
    val cachedCondition: String? = null,
    val cachedIcon: String? = null,
    val orderIndex: Int = 0
)

@Entity(tableName = "cached_weather")
data class CachedWeatherEntity(
    @PrimaryKey
    val locationKey: String, // e.g. "lat,lon" or "cityName"
    val cityName: String,
    val country: String,
    val lat: Double,
    val lon: Double,
    val currentTempC: Double,
    val feelsLikeC: Double,
    val tempMinC: Double,
    val tempMaxC: Double,
    val conditionName: String,
    val description: String,
    val iconCode: String,
    val humidity: Int,
    val windSpeedKmh: Double,
    val windDeg: Int,
    val pressureHpa: Double,
    val visibilityKm: Double,
    val cloudiness: Int,
    val uvIndex: Double,
    val dewPointC: Double,
    val sunriseEpoch: Long,
    val sunsetEpoch: Long,
    val timezoneOffsetSec: Int,
    val aqiValue: Int,
    val aqiCategory: String,
    val pm2_5: Double,
    val pm10: Double,
    val co: Double,
    val no2: Double,
    val so2: Double,
    val o3: Double,
    val updatedTimestampEpoch: Long
)
