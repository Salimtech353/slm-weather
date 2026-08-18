package com.example.data.local

import android.content.Context
import android.content.SharedPreferences
import com.example.BuildConfig
import com.example.data.model.PressureUnit
import com.example.data.model.TempUnit
import com.example.data.model.ThemeMode
import com.example.data.model.UnitsConfig
import com.example.data.model.WindUnit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SettingsManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("slm_weather_settings", Context.MODE_PRIVATE)

    private val _settingsFlow = MutableStateFlow(loadSettings())
    val settingsFlow: StateFlow<UnitsConfig> = _settingsFlow.asStateFlow()

    private fun loadSettings(): UnitsConfig {
        val tempUnitName = prefs.getString(KEY_TEMP_UNIT, TempUnit.CELSIUS.name) ?: TempUnit.CELSIUS.name
        val windUnitName = prefs.getString(KEY_WIND_UNIT, WindUnit.KMH.name) ?: WindUnit.KMH.name
        val pressureUnitName = prefs.getString(KEY_PRESSURE_UNIT, PressureUnit.HPA.name) ?: PressureUnit.HPA.name
        val themeModeName = prefs.getString(KEY_THEME_MODE, ThemeMode.DARK.name) ?: ThemeMode.DARK.name
        val notifications = prefs.getBoolean(KEY_NOTIFICATIONS, true)
        val animations = prefs.getBoolean(KEY_ANIMATIONS, true)
        val autoLocation = prefs.getBoolean(KEY_AUTO_LOCATION, true)
        val customKey = prefs.getString(KEY_CUSTOM_API_KEY, "") ?: ""

        val tempUnit = try { TempUnit.valueOf(tempUnitName) } catch (e: Exception) { TempUnit.CELSIUS }
        val windUnit = try { WindUnit.valueOf(windUnitName) } catch (e: Exception) { WindUnit.KMH }
        val pressureUnit = try { PressureUnit.valueOf(pressureUnitName) } catch (e: Exception) { PressureUnit.HPA }
        val themeMode = try { ThemeMode.valueOf(themeModeName) } catch (e: Exception) { ThemeMode.DARK }

        return UnitsConfig(
            tempUnit = tempUnit,
            windUnit = windUnit,
            pressureUnit = pressureUnit,
            themeMode = themeMode,
            notificationsEnabled = notifications,
            animationsEnabled = animations,
            autoDetectLocation = autoLocation,
            customApiKey = customKey
        )
    }

    fun setTempUnit(unit: TempUnit) {
        prefs.edit().putString(KEY_TEMP_UNIT, unit.name).apply()
        _settingsFlow.value = _settingsFlow.value.copy(tempUnit = unit)
    }

    fun setWindUnit(unit: WindUnit) {
        prefs.edit().putString(KEY_WIND_UNIT, unit.name).apply()
        _settingsFlow.value = _settingsFlow.value.copy(windUnit = unit)
    }

    fun setPressureUnit(unit: PressureUnit) {
        prefs.edit().putString(KEY_PRESSURE_UNIT, unit.name).apply()
        _settingsFlow.value = _settingsFlow.value.copy(pressureUnit = unit)
    }

    fun setThemeMode(mode: ThemeMode) {
        prefs.edit().putString(KEY_THEME_MODE, mode.name).apply()
        _settingsFlow.value = _settingsFlow.value.copy(themeMode = mode)
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_NOTIFICATIONS, enabled).apply()
        _settingsFlow.value = _settingsFlow.value.copy(notificationsEnabled = enabled)
    }

    fun setAnimationsEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_ANIMATIONS, enabled).apply()
        _settingsFlow.value = _settingsFlow.value.copy(animationsEnabled = enabled)
    }

    fun setAutoDetectLocation(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_AUTO_LOCATION, enabled).apply()
        _settingsFlow.value = _settingsFlow.value.copy(autoDetectLocation = enabled)
    }

    fun setCustomApiKey(key: String) {
        prefs.edit().putString(KEY_CUSTOM_API_KEY, key.trim()).apply()
        _settingsFlow.value = _settingsFlow.value.copy(customApiKey = key.trim())
    }

    fun getEffectiveApiKey(): String {
        val custom = _settingsFlow.value.customApiKey
        if (custom.isNotBlank() && custom != "YOUR_OPENWEATHERMAP_API_KEY") {
            return custom
        }
        // Check BuildConfig.OPENWEATHER_API_KEY if configured in .env
        return try {
            val field = BuildConfig::class.java.getField("OPENWEATHER_API_KEY")
            val key = field.get(null) as? String ?: ""
            if (key.isNotBlank() && key != "YOUR_OPENWEATHERMAP_API_KEY") key else ""
        } catch (e: Exception) {
            ""
        }
    }

    companion object {
        private const val KEY_TEMP_UNIT = "key_temp_unit"
        private const val KEY_WIND_UNIT = "key_wind_unit"
        private const val KEY_PRESSURE_UNIT = "key_pressure_unit"
        private const val KEY_THEME_MODE = "key_theme_mode"
        private const val KEY_NOTIFICATIONS = "key_notifications"
        private const val KEY_ANIMATIONS = "key_animations"
        private const val KEY_AUTO_LOCATION = "key_auto_location"
        private const val KEY_CUSTOM_API_KEY = "key_custom_api_key"
    }
}
