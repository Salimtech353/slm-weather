package com.example.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

// OpenWeatherMap Current Weather DTO
@JsonClass(generateAdapter = true)
data class CurrentWeatherResponse(
    @Json(name = "coord") val coord: CoordDto? = null,
    @Json(name = "weather") val weather: List<WeatherDescriptionDto>? = null,
    @Json(name = "base") val base: String? = null,
    @Json(name = "main") val main: MainDto? = null,
    @Json(name = "visibility") val visibility: Int? = null,
    @Json(name = "wind") val wind: WindDto? = null,
    @Json(name = "clouds") val clouds: CloudsDto? = null,
    @Json(name = "dt") val dt: Long? = null,
    @Json(name = "sys") val sys: SysDto? = null,
    @Json(name = "timezone") val timezone: Int? = null,
    @Json(name = "id") val id: Long? = null,
    @Json(name = "name") val name: String? = null,
    @Json(name = "cod") val cod: Int? = null
)

@JsonClass(generateAdapter = true)
data class CoordDto(
    @Json(name = "lon") val lon: Double? = null,
    @Json(name = "lat") val lat: Double? = null
)

@JsonClass(generateAdapter = true)
data class WeatherDescriptionDto(
    @Json(name = "id") val id: Int? = null,
    @Json(name = "main") val main: String? = null,
    @Json(name = "description") val description: String? = null,
    @Json(name = "icon") val icon: String? = null
)

@JsonClass(generateAdapter = true)
data class MainDto(
    @Json(name = "temp") val temp: Double? = null,
    @Json(name = "feels_like") val feelsLike: Double? = null,
    @Json(name = "temp_min") val tempMin: Double? = null,
    @Json(name = "temp_max") val tempMax: Double? = null,
    @Json(name = "pressure") val pressure: Double? = null,
    @Json(name = "humidity") val humidity: Int? = null,
    @Json(name = "sea_level") val seaLevel: Double? = null,
    @Json(name = "grnd_level") val grndLevel: Double? = null
)

@JsonClass(generateAdapter = true)
data class WindDto(
    @Json(name = "speed") val speed: Double? = null,
    @Json(name = "deg") val deg: Int? = null,
    @Json(name = "gust") val gust: Double? = null
)

@JsonClass(generateAdapter = true)
data class CloudsDto(
    @Json(name = "all") val all: Int? = null
)

@JsonClass(generateAdapter = true)
data class SysDto(
    @Json(name = "type") val type: Int? = null,
    @Json(name = "id") val id: Long? = null,
    @Json(name = "country") val country: String? = null,
    @Json(name = "sunrise") val sunrise: Long? = null,
    @Json(name = "sunset") val sunset: Long? = null
)

// OpenWeatherMap 5-Day / 3-Hour Forecast DTO
@JsonClass(generateAdapter = true)
data class ForecastResponse(
    @Json(name = "cod") val cod: String? = null,
    @Json(name = "message") val message: Int? = null,
    @Json(name = "cnt") val cnt: Int? = null,
    @Json(name = "list") val list: List<ForecastItemDto>? = null,
    @Json(name = "city") val city: CityDto? = null
)

@JsonClass(generateAdapter = true)
data class ForecastItemDto(
    @Json(name = "dt") val dt: Long? = null,
    @Json(name = "main") val main: MainDto? = null,
    @Json(name = "weather") val weather: List<WeatherDescriptionDto>? = null,
    @Json(name = "clouds") val clouds: CloudsDto? = null,
    @Json(name = "wind") val wind: WindDto? = null,
    @Json(name = "visibility") val visibility: Int? = null,
    @Json(name = "pop") val pop: Double? = null, // Probability of precipitation (0 to 1)
    @Json(name = "dt_txt") val dtTxt: String? = null
)

@JsonClass(generateAdapter = true)
data class CityDto(
    @Json(name = "id") val id: Long? = null,
    @Json(name = "name") val name: String? = null,
    @Json(name = "coord") val coord: CoordDto? = null,
    @Json(name = "country") val country: String? = null,
    @Json(name = "population") val population: Long? = null,
    @Json(name = "timezone") val timezone: Int? = null,
    @Json(name = "sunrise") val sunrise: Long? = null,
    @Json(name = "sunset") val sunset: Long? = null
)

// OpenWeatherMap Air Pollution DTO
@JsonClass(generateAdapter = true)
data class AirPollutionResponse(
    @Json(name = "coord") val coord: CoordDto? = null,
    @Json(name = "list") val list: List<AirPollutionItemDto>? = null
)

@JsonClass(generateAdapter = true)
data class AirPollutionItemDto(
    @Json(name = "main") val main: AirMainDto? = null,
    @Json(name = "components") val components: AirComponentsDto? = null,
    @Json(name = "dt") val dt: Long? = null
)

@JsonClass(generateAdapter = true)
data class AirMainDto(
    @Json(name = "aqi") val aqi: Int? = null // AQI scale: 1 = Good, 2 = Fair, 3 = Moderate, 4 = Poor, 5 = Very Poor
)

@JsonClass(generateAdapter = true)
data class AirComponentsDto(
    @Json(name = "co") val co: Double? = null,
    @Json(name = "no") val no: Double? = null,
    @Json(name = "no2") val no2: Double? = null,
    @Json(name = "o3") val o3: Double? = null,
    @Json(name = "so2") val so2: Double? = null,
    @Json(name = "pm2_5") val pm2_5: Double? = null,
    @Json(name = "pm10") val pm10: Double? = null,
    @Json(name = "nh3") val nh3: Double? = null
)

// OpenWeatherMap Geocoding Direct DTO
@JsonClass(generateAdapter = true)
data class GeocodingItemDto(
    @Json(name = "name") val name: String,
    @Json(name = "lat") val lat: Double,
    @Json(name = "lon") val lon: Double,
    @Json(name = "country") val country: String? = null,
    @Json(name = "state") val state: String? = null
)
