package com.example.data.repository

import android.content.Context
import com.example.data.local.CachedWeatherEntity
import com.example.data.local.SavedCityEntity
import com.example.data.local.SettingsManager
import com.example.data.local.WeatherDao
import com.example.data.model.AirQualityData
import com.example.data.model.DailyForecast
import com.example.data.model.HourlyForecast
import com.example.data.model.SavedCity
import com.example.data.model.WeatherCondition
import com.example.data.model.WeatherData
import com.example.data.remote.NetworkClient
import com.example.data.remote.WeatherApiService
import com.example.utils.LocationHelper
import com.example.utils.WeatherUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.util.Calendar
import kotlin.math.max
import kotlin.math.min

data class CompleteWeatherResult(
    val current: WeatherData,
    val hourly: List<HourlyForecast>,
    val daily: List<DailyForecast>,
    val airQuality: AirQualityData,
    val isFromCache: Boolean = false,
    val isSampleFallback: Boolean = false
)

class WeatherRepository(
    private val weatherDao: WeatherDao,
    private val settingsManager: SettingsManager,
    private val apiService: WeatherApiService = NetworkClient.apiService
) {

    val savedCitiesFlow: Flow<List<SavedCity>> = weatherDao.getAllSavedCities().map { entities ->
        entities.map { it.toDomain() }
    }

    suspend fun getCompleteWeather(
        lat: Double,
        lon: Double,
        cityNameFallback: String = "Dhaka",
        countryFallback: String = "BD",
        forceRefresh: Boolean = false
    ): Result<CompleteWeatherResult> = withContext(Dispatchers.IO) {
        val locationKey = "${(lat * 100).toInt()},${(lon * 100).toInt()}"
        val apiKey = settingsManager.getEffectiveApiKey()

        if (apiKey.isBlank() || apiKey == "YOUR_OPENWEATHERMAP_API_KEY") {
            // No API key configured: Return rich realistic sample dataset for the location
            val sampleCurrent = SampleWeatherData.getSampleWeather(cityNameFallback, countryFallback, lat, lon)
            val sampleHourly = SampleWeatherData.getSampleHourlyForecast(sampleCurrent.currentTempC)
            val sampleDaily = SampleWeatherData.getSample7DayForecast(sampleCurrent.currentTempC)
            val sampleAqi = SampleWeatherData.getSampleAirQuality(cityNameFallback)
            return@withContext Result.success(
                CompleteWeatherResult(
                    current = sampleCurrent,
                    hourly = sampleHourly,
                    daily = sampleDaily,
                    airQuality = sampleAqi,
                    isSampleFallback = true
                )
            )
        }

        try {
            coroutineScope {
                val currentWeatherDeferred = async {
                    apiService.getCurrentWeatherByCoords(lat, lon, "metric", apiKey)
                }
                val forecastDeferred = async {
                    apiService.getForecastByCoords(lat, lon, "metric", apiKey)
                }
                val airPollutionDeferred = async {
                    try {
                        apiService.getAirPollution(lat, lon, apiKey)
                    } catch (e: Exception) {
                        null
                    }
                }

                val currDto = currentWeatherDeferred.await()
                val forecastDto = forecastDeferred.await()
                val airDto = airPollutionDeferred.await()

                val firstWeather = currDto.weather?.firstOrNull()
                val condition = WeatherUtils.mapCondition(firstWeather?.icon, firstWeather?.id)
                val city = currDto.name ?: cityNameFallback
                val country = currDto.sys?.country ?: countryFallback
                val tempC = currDto.main?.temp ?: 25.0
                val feelsLikeC = currDto.main?.feelsLike ?: tempC
                val tempMin = currDto.main?.tempMin ?: (tempC - 3.0)
                val tempMax = currDto.main?.tempMax ?: (tempC + 3.0)
                val humidity = currDto.main?.humidity ?: 60
                val windSpeedKmh = (currDto.wind?.speed ?: 3.0) * 3.6
                val windDeg = currDto.wind?.deg ?: 0
                val pressure = currDto.main?.pressure ?: 1013.0
                val visibilityKm = (currDto.visibility ?: 10000) / 1000.0
                val clouds = currDto.clouds?.all ?: 20
                val dewPoint = WeatherUtils.calculateDewPoint(tempC, humidity)
                val sunrise = currDto.sys?.sunrise ?: 0L
                val sunset = currDto.sys?.sunset ?: 0L
                val tzOffset = currDto.timezone ?: 0

                val weatherData = WeatherData(
                    cityName = city,
                    country = country,
                    lat = lat,
                    lon = lon,
                    currentTempC = tempC,
                    feelsLikeC = feelsLikeC,
                    tempMinC = tempMin,
                    tempMaxC = tempMax,
                    condition = condition,
                    description = firstWeather?.description?.replaceFirstChar { it.uppercase() } ?: condition.displayName,
                    iconCode = firstWeather?.icon ?: condition.iconRes,
                    humidity = humidity,
                    windSpeedKmh = windSpeedKmh,
                    windDeg = windDeg,
                    pressureHpa = pressure,
                    visibilityKm = visibilityKm,
                    cloudiness = clouds,
                    uvIndex = if (WeatherUtils.isCurrentlyDaytime(sunrise, sunset)) 5.5 else 0.0,
                    dewPointC = dewPoint,
                    sunriseTimeEpoch = sunrise,
                    sunsetTimeEpoch = sunset,
                    timezoneOffsetSec = tzOffset,
                    lastUpdatedEpoch = System.currentTimeMillis()
                )

                // Parse 24-hour hourly list from 3-hour forecasts
                val hourlyList = mutableListOf<HourlyForecast>()
                val rawForecastItems = forecastDto.list.orEmpty()
                val nowEpoch = System.currentTimeMillis() / 1000L

                // First item is current
                hourlyList.add(
                    HourlyForecast(
                        timeLabel = "Now",
                        epochTime = nowEpoch,
                        tempC = tempC,
                        feelsLikeC = feelsLikeC,
                        popPercentage = ((rawForecastItems.firstOrNull()?.pop ?: 0.0) * 100).toInt(),
                        condition = condition,
                        iconCode = firstWeather?.icon ?: condition.iconRes,
                        windSpeedKmh = windSpeedKmh,
                        isCurrentHour = true
                    )
                )

                // Fill up to 24 hours
                for (item in rawForecastItems.take(8)) {
                    val itemEpoch = item.dt ?: continue
                    val itemWeather = item.weather?.firstOrNull()
                    val itemCond = WeatherUtils.mapCondition(itemWeather?.icon, itemWeather?.id)
                    val label = WeatherUtils.formatEpochTime(itemEpoch, tzOffset, "h a")
                    hourlyList.add(
                        HourlyForecast(
                            timeLabel = label,
                            epochTime = itemEpoch,
                            tempC = item.main?.temp ?: tempC,
                            feelsLikeC = item.main?.feelsLike ?: tempC,
                            popPercentage = ((item.pop ?: 0.0) * 100).toInt(),
                            condition = itemCond,
                            iconCode = itemWeather?.icon ?: itemCond.iconRes,
                            windSpeedKmh = (item.wind?.speed ?: 3.0) * 3.6,
                            isCurrentHour = false
                        )
                    )
                }

                // Aggregate daily 7-day forecast
                val dailyMap = mutableMapOf<String, MutableList<com.example.data.model.ForecastItemDto>>()
                for (item in rawForecastItems) {
                    val epoch = item.dt ?: continue
                    val dayKey = WeatherUtils.formatEpochTime(epoch, tzOffset, "yyyy-MM-dd")
                    dailyMap.getOrPut(dayKey) { mutableListOf() }.add(item)
                }

                val dailyList = mutableListOf<DailyForecast>()
                var dayIdx = 0
                for ((_, items) in dailyMap) {
                    if (dayIdx >= 7) break
                    val representative = items[items.size / 2]
                    val repEpoch = representative.dt ?: continue
                    val repWeather = representative.weather?.firstOrNull()
                    val repCond = WeatherUtils.mapCondition(repWeather?.icon, repWeather?.id)
                    var minT = 999.0
                    var maxT = -999.0
                    var maxPop = 0.0
                    for (it in items) {
                        it.main?.tempMin?.let { minT = min(minT, it) }
                        it.main?.tempMax?.let { maxT = max(maxT, it) }
                        it.pop?.let { maxPop = max(maxPop, it) }
                    }
                    if (minT > 900.0) minT = tempMin
                    if (maxT < -900.0) maxT = tempMax

                    dailyList.add(
                        DailyForecast(
                            dayName = when (dayIdx) {
                                0 -> "Today"
                                1 -> "Tomorrow"
                                else -> WeatherUtils.formatDayName(repEpoch, tzOffset)
                            },
                            dateLabel = WeatherUtils.formatEpochTime(repEpoch, tzOffset, "MMM d"),
                            epochTime = repEpoch,
                            minTempC = minT,
                            maxTempC = maxT,
                            popPercentage = (maxPop * 100).toInt(),
                            condition = repCond,
                            description = repWeather?.description?.replaceFirstChar { it.uppercase() } ?: repCond.displayName,
                            iconCode = repWeather?.icon ?: repCond.iconRes,
                            humidity = representative.main?.humidity ?: 60,
                            windSpeedKmh = (representative.wind?.speed ?: 3.0) * 3.6
                        )
                    )
                    dayIdx++
                }

                // If daily list is less than 7 days, append synthetic continuation from sample
                if (dailyList.size < 7) {
                    val sampleDaily = SampleWeatherData.getSample7DayForecast(tempC)
                    for (i in dailyList.size until 7) {
                        dailyList.add(sampleDaily[i])
                    }
                }

                // Parse Air Quality
                val airPollutionItem = airDto?.list?.firstOrNull()
                val comps = airPollutionItem?.components
                val aqiData = if (comps != null) {
                    WeatherUtils.mapAqiData(
                        apiAqi = airPollutionItem.main?.aqi,
                        pm25 = comps.pm2_5 ?: 15.0,
                        pm10 = comps.pm10 ?: 25.0,
                        co = comps.co ?: 300.0,
                        no2 = comps.no2 ?: 15.0,
                        so2 = comps.so2 ?: 5.0,
                        o3 = comps.o3 ?: 40.0
                    )
                } else {
                    SampleWeatherData.getSampleAirQuality(city)
                }

                // Cache in Room DB
                val cacheEntity = CachedWeatherEntity(
                    locationKey = locationKey,
                    cityName = city,
                    country = country,
                    lat = lat,
                    lon = lon,
                    currentTempC = tempC,
                    feelsLikeC = feelsLikeC,
                    tempMinC = tempMin,
                    tempMaxC = tempMax,
                    conditionName = condition.name,
                    description = weatherData.description,
                    iconCode = weatherData.iconCode,
                    humidity = humidity,
                    windSpeedKmh = windSpeedKmh,
                    windDeg = windDeg,
                    pressureHpa = pressure,
                    visibilityKm = visibilityKm,
                    cloudiness = clouds,
                    uvIndex = weatherData.uvIndex,
                    dewPointC = dewPoint,
                    sunriseEpoch = sunrise,
                    sunsetEpoch = sunset,
                    timezoneOffsetSec = tzOffset,
                    aqiValue = aqiData.aqiValue,
                    aqiCategory = aqiData.categoryName,
                    pm2_5 = aqiData.pm2_5,
                    pm10 = aqiData.pm10,
                    co = aqiData.co,
                    no2 = aqiData.no2,
                    so2 = aqiData.so2,
                    o3 = aqiData.o3,
                    updatedTimestampEpoch = System.currentTimeMillis()
                )
                weatherDao.cacheWeather(cacheEntity)

                Result.success(
                    CompleteWeatherResult(
                        current = weatherData,
                        hourly = hourlyList,
                        daily = dailyList,
                        airQuality = aqiData,
                        isFromCache = false
                    )
                )
            }
        } catch (e: Exception) {
            // Check Room cache on error
            val cached = weatherDao.getCachedWeather(locationKey)
            if (cached != null) {
                val cachedCond = try { WeatherCondition.valueOf(cached.conditionName) } catch (ex: Exception) { WeatherCondition.CLEAR_DAY }
                val current = WeatherData(
                    cityName = cached.cityName,
                    country = cached.country,
                    lat = cached.lat,
                    lon = cached.lon,
                    currentTempC = cached.currentTempC,
                    feelsLikeC = cached.feelsLikeC,
                    tempMinC = cached.tempMinC,
                    tempMaxC = cached.tempMaxC,
                    condition = cachedCond,
                    description = cached.description,
                    iconCode = cached.iconCode,
                    humidity = cached.humidity,
                    windSpeedKmh = cached.windSpeedKmh,
                    windDeg = cached.windDeg,
                    pressureHpa = cached.pressureHpa,
                    visibilityKm = cached.visibilityKm,
                    cloudiness = cached.cloudiness,
                    uvIndex = cached.uvIndex,
                    dewPointC = cached.dewPointC,
                    sunriseTimeEpoch = cached.sunriseEpoch,
                    sunsetTimeEpoch = cached.sunsetEpoch,
                    timezoneOffsetSec = cached.timezoneOffsetSec,
                    lastUpdatedEpoch = cached.updatedTimestampEpoch
                )
                val hourly = SampleWeatherData.getSampleHourlyForecast(cached.currentTempC)
                val daily = SampleWeatherData.getSample7DayForecast(cached.currentTempC)
                val aqi = AirQualityData(
                    aqiIndex = 2,
                    aqiValue = cached.aqiValue,
                    categoryName = cached.aqiCategory,
                    pm2_5 = cached.pm2_5,
                    pm10 = cached.pm10,
                    co = cached.co,
                    no2 = cached.no2,
                    so2 = cached.so2,
                    o3 = cached.o3
                )
                Result.success(
                    CompleteWeatherResult(
                        current = current,
                        hourly = hourly,
                        daily = daily,
                        airQuality = aqi,
                        isFromCache = true
                    )
                )
            } else {
                // Fallback to sample data for smooth experience with error note
                val sampleCurrent = SampleWeatherData.getSampleWeather(cityNameFallback, countryFallback, lat, lon)
                Result.success(
                    CompleteWeatherResult(
                        current = sampleCurrent,
                        hourly = SampleWeatherData.getSampleHourlyForecast(sampleCurrent.currentTempC),
                        daily = SampleWeatherData.getSample7DayForecast(sampleCurrent.currentTempC),
                        airQuality = SampleWeatherData.getSampleAirQuality(cityNameFallback),
                        isSampleFallback = true
                    )
                )
            }
        }
    }

    suspend fun searchCities(query: String, context: Context? = null): List<SavedCity> = withContext(Dispatchers.IO) {
        val q = query.trim()
        if (q.isBlank()) return@withContext emptyList()

        val results = mutableListOf<SavedCity>()

        // 1. Android Geocoder search (if context provided) - supports any city worldwide
        if (context != null) {
            try {
                val geocoderResults = LocationHelper.searchLocationsViaGeocoder(context, q)
                results.addAll(geocoderResults)
            } catch (e: Exception) {
                // Ignore geocoder error and fallback
            }
        }

        // 2. OpenWeatherMap Geocoding API if key configured
        val apiKey = settingsManager.getEffectiveApiKey()
        if (apiKey.isNotBlank() && apiKey != "YOUR_OPENWEATHERMAP_API_KEY") {
            try {
                val apiResults = apiService.searchLocations(q, 8, apiKey)
                if (apiResults.isNotEmpty()) {
                    results.addAll(apiResults.map { dto ->
                        SavedCity(
                            name = dto.name,
                            state = dto.state ?: "",
                            country = dto.country ?: "",
                            lat = dto.lat,
                            lon = dto.lon
                        )
                    })
                }
            } catch (e: Exception) {
                // fallback
            }
        }

        // 3. Search curated city catalog
        val localMatches = SampleWeatherData.popularCities.filter {
            it.name.contains(q, ignoreCase = true) ||
            it.state.contains(q, ignoreCase = true) ||
            it.country.contains(q, ignoreCase = true)
        }
        results.addAll(localMatches)

        // Deduplicate results by name and country
        results.distinctBy { "${it.name.lowercase().trim()}_${it.country.lowercase().trim()}" }
    }

    suspend fun saveCity(city: SavedCity) = withContext(Dispatchers.IO) {
        weatherDao.insertCity(
            SavedCityEntity(
                name = city.name,
                state = city.state,
                country = city.country,
                lat = city.lat,
                lon = city.lon,
                isCurrentLocation = city.isCurrentLocation,
                cachedTempC = city.cachedTempC,
                cachedCondition = city.cachedCondition,
                cachedIcon = city.cachedIcon
            )
        )
    }

    suspend fun removeCity(cityId: Long) = withContext(Dispatchers.IO) {
        weatherDao.deleteCityById(cityId)
    }

    suspend fun clearCache() = withContext(Dispatchers.IO) {
        weatherDao.clearWeatherCache()
    }

    suspend fun initializeDefaultCitiesIfNeeded() = withContext(Dispatchers.IO) {
        val existing = weatherDao.getCurrentLocationCity()
        if (existing == null) {
            val defaults = listOf(
                SavedCityEntity(
                    name = "Dhaka",
                    state = "Dhaka Division",
                    country = "BD",
                    lat = 23.8103,
                    lon = 90.4125,
                    isCurrentLocation = true,
                    cachedTempC = 29.0,
                    cachedCondition = "Partly Cloudy",
                    cachedIcon = "02d",
                    orderIndex = 0
                ),
                SavedCityEntity(
                    name = "London",
                    state = "England",
                    country = "GB",
                    lat = 51.5074,
                    lon = -0.1278,
                    isCurrentLocation = false,
                    cachedTempC = 19.0,
                    cachedCondition = "Rain",
                    cachedIcon = "10d",
                    orderIndex = 1
                ),
                SavedCityEntity(
                    name = "New York",
                    state = "New York",
                    country = "US",
                    lat = 40.7128,
                    lon = -74.0060,
                    isCurrentLocation = false,
                    cachedTempC = 23.0,
                    cachedCondition = "Clear",
                    cachedIcon = "01d",
                    orderIndex = 2
                ),
                SavedCityEntity(
                    name = "Tokyo",
                    state = "Kanto",
                    country = "JP",
                    lat = 35.6762,
                    lon = 139.6503,
                    isCurrentLocation = false,
                    cachedTempC = 25.0,
                    cachedCondition = "Partly Cloudy",
                    cachedIcon = "02d",
                    orderIndex = 3
                )
            )
            weatherDao.insertCities(defaults)
        }
    }

    private fun SavedCityEntity.toDomain(): SavedCity = SavedCity(
        id = this.id,
        name = this.name,
        state = this.state,
        country = this.country,
        lat = this.lat,
        lon = this.lon,
        isCurrentLocation = this.isCurrentLocation,
        cachedTempC = this.cachedTempC,
        cachedCondition = this.cachedCondition,
        cachedIcon = this.cachedIcon
    )
}
