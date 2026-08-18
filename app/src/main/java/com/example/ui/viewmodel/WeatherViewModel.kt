package com.example.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.SettingsManager
import com.example.data.model.PressureUnit
import com.example.data.model.SavedCity
import com.example.data.model.TempUnit
import com.example.data.model.ThemeMode
import com.example.data.model.UnitsConfig
import com.example.data.model.WindUnit
import com.example.data.repository.CompleteWeatherResult
import com.example.data.repository.SampleWeatherData
import com.example.data.repository.WeatherRepository
import com.example.utils.LocationHelper
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class WeatherUiState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val weatherData: CompleteWeatherResult? = null,
    val selectedCity: SavedCity? = null,
    val errorMessage: String? = null,
    val searchQuery: String = "",
    val searchResults: List<SavedCity> = emptyList(),
    val isSearching: Boolean = false,
    val isGpsActive: Boolean = false
)

class WeatherViewModel(
    private val repository: WeatherRepository,
    private val settingsManager: SettingsManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(WeatherUiState())
    val uiState: StateFlow<WeatherUiState> = _uiState.asStateFlow()

    val settingsState: StateFlow<UnitsConfig> = settingsManager.settingsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UnitsConfig())

    val savedCities: StateFlow<List<SavedCity>> = repository.savedCitiesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private var searchJob: Job? = null

    init {
        viewModelScope.launch {
            repository.initializeDefaultCitiesIfNeeded()
            // Default initial load: Dhaka / first saved city
            val initialCity = SavedCity(
                name = "Dhaka",
                state = "Dhaka Division",
                country = "BD",
                lat = 23.8103,
                lon = 90.4125,
                isCurrentLocation = true
            )
            loadWeatherForCity(initialCity)
        }
    }

    fun loadWeatherForCity(city: SavedCity, forceRefresh: Boolean = false) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = !forceRefresh && _uiState.value.weatherData == null,
                isRefreshing = forceRefresh,
                selectedCity = city,
                errorMessage = null
            )

            val result = repository.getCompleteWeather(
                lat = city.lat,
                lon = city.lon,
                cityNameFallback = city.name,
                countryFallback = city.country,
                forceRefresh = forceRefresh
            )

            result.onSuccess { data ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isRefreshing = false,
                    weatherData = data,
                    errorMessage = null
                )
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isRefreshing = false,
                    errorMessage = error.localizedMessage ?: "Unable to fetch weather data. Please check your internet connection."
                )
            }
        }
    }

    fun refreshCurrent() {
        val currentCity = _uiState.value.selectedCity ?: SavedCity(
            name = "Dhaka",
            country = "BD",
            lat = 23.8103,
            lon = 90.4125
        )
        loadWeatherForCity(currentCity, forceRefresh = true)
    }

    fun fetchDeviceLocation(context: Context) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, isGpsActive = true, errorMessage = null)
            val location = LocationHelper.getCurrentLocation(context)
            if (location != null) {
                // Reverse geocode to get real human-readable city & country name
                val (resolvedCity, resolvedState, resolvedCountry) = LocationHelper.getCityInfoFromCoordinates(
                    context = context,
                    lat = location.latitude,
                    lon = location.longitude
                )

                val gpsCity = SavedCity(
                    name = resolvedCity.ifBlank { "Current Location" },
                    state = resolvedState,
                    country = resolvedCountry.ifBlank { "GPS" },
                    lat = location.latitude,
                    lon = location.longitude,
                    isCurrentLocation = true
                )

                // Save or update as current location
                repository.saveCity(gpsCity)
                loadWeatherForCity(gpsCity, forceRefresh = true)
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isGpsActive = false,
                    errorMessage = "Location unavailable. Please check GPS / permissions."
                )
                // Fallback to Dhaka if no weather data loaded yet
                if (_uiState.value.weatherData == null) {
                    val fallback = SavedCity(
                        name = "Dhaka",
                        state = "Dhaka Division",
                        country = "BD",
                        lat = 23.8103,
                        lon = 90.4125
                    )
                    loadWeatherForCity(fallback)
                }
            }
        }
    }

    fun onSearchQueryChanged(query: String, context: Context? = null) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        searchJob?.cancel()

        if (query.trim().isEmpty()) {
            _uiState.value = _uiState.value.copy(
                searchResults = emptyList(),
                isSearching = false
            )
            return
        }

        searchJob = viewModelScope.launch {
            delay(250) // debounce
            _uiState.value = _uiState.value.copy(isSearching = true)
            val results = repository.searchCities(query, context)
            _uiState.value = _uiState.value.copy(
                searchResults = results,
                isSearching = false
            )
        }
    }

    fun setAutoDetectLocation(enabled: Boolean) {
        settingsManager.setAutoDetectLocation(enabled)
    }

    fun saveLocation(city: SavedCity) {
        viewModelScope.launch {
            repository.saveCity(city)
        }
    }

    fun removeLocation(cityId: Long) {
        viewModelScope.launch {
            repository.removeCity(cityId)
        }
    }

    fun setTempUnit(unit: TempUnit) {
        settingsManager.setTempUnit(unit)
    }

    fun setWindUnit(unit: WindUnit) {
        settingsManager.setWindUnit(unit)
    }

    fun setPressureUnit(unit: PressureUnit) {
        settingsManager.setPressureUnit(unit)
    }

    fun setThemeMode(mode: ThemeMode) {
        settingsManager.setThemeMode(mode)
    }

    fun setNotifications(enabled: Boolean) {
        settingsManager.setNotificationsEnabled(enabled)
    }

    fun setAnimationsEnabled(enabled: Boolean) {
        settingsManager.setAnimationsEnabled(enabled)
    }

    fun setCustomApiKey(key: String) {
        settingsManager.setCustomApiKey(key)
        refreshCurrent()
    }

    fun clearCache() {
        viewModelScope.launch {
            repository.clearCache()
            refreshCurrent()
        }
    }
}

class WeatherViewModelFactory(
    private val repository: WeatherRepository,
    private val settingsManager: SettingsManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WeatherViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return WeatherViewModel(repository, settingsManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
