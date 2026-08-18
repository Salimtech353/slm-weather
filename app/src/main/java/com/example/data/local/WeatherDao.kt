package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface WeatherDao {

    @Query("SELECT * FROM saved_cities ORDER BY isCurrentLocation DESC, orderIndex ASC, id ASC")
    fun getAllSavedCities(): Flow<List<SavedCityEntity>>

    @Query("SELECT * FROM saved_cities WHERE isCurrentLocation = 1 LIMIT 1")
    suspend fun getCurrentLocationCity(): SavedCityEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCity(city: SavedCityEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCities(cities: List<SavedCityEntity>)

    @Update
    suspend fun updateCity(city: SavedCityEntity)

    @Delete
    suspend fun deleteCity(city: SavedCityEntity)

    @Query("DELETE FROM saved_cities WHERE id = :id")
    suspend fun deleteCityById(id: Long)

    @Query("DELETE FROM saved_cities WHERE name = :name AND country = :country")
    suspend fun deleteCityByNameAndCountry(name: String, country: String)

    @Query("SELECT * FROM cached_weather WHERE locationKey = :key LIMIT 1")
    suspend fun getCachedWeather(key: String): CachedWeatherEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun cacheWeather(weather: CachedWeatherEntity)

    @Query("DELETE FROM cached_weather")
    suspend fun clearWeatherCache()
}
