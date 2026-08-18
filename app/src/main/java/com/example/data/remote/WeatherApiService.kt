package com.example.data.remote

import com.example.data.model.AirPollutionResponse
import com.example.data.model.CurrentWeatherResponse
import com.example.data.model.ForecastResponse
import com.example.data.model.GeocodingItemDto
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApiService {

    @GET("data/2.5/weather")
    suspend fun getCurrentWeatherByCoords(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("units") units: String = "metric",
        @Query("appid") apiKey: String
    ): CurrentWeatherResponse

    @GET("data/2.5/weather")
    suspend fun getCurrentWeatherByCity(
        @Query("q") query: String,
        @Query("units") units: String = "metric",
        @Query("appid") apiKey: String
    ): CurrentWeatherResponse

    @GET("data/2.5/forecast")
    suspend fun getForecastByCoords(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("units") units: String = "metric",
        @Query("appid") apiKey: String
    ): ForecastResponse

    @GET("data/2.5/air_pollution")
    suspend fun getAirPollution(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("appid") apiKey: String
    ): AirPollutionResponse

    @GET("geo/1.0/direct")
    suspend fun searchLocations(
        @Query("q") query: String,
        @Query("limit") limit: Int = 8,
        @Query("appid") apiKey: String
    ): List<GeocodingItemDto>
}
