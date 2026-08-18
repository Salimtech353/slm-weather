package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.data.local.SettingsManager
import com.example.data.local.WeatherDatabase
import com.example.data.model.ThemeMode
import com.example.data.model.WeatherCondition
import com.example.data.repository.WeatherRepository
import com.example.ui.components.WeatherBackgroundAnimation
import com.example.ui.navigation.GlassmorphicBottomNav
import com.example.ui.navigation.Screen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.SavedLocationsScreen
import com.example.ui.screens.SearchScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.BackgroundLight
import com.example.ui.theme.SLMWeatherTheme
import com.example.ui.viewmodel.WeatherViewModel
import com.example.ui.viewmodel.WeatherViewModelFactory

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = WeatherDatabase.getInstance(applicationContext)
        val settingsManager = SettingsManager(applicationContext)
        val repository = WeatherRepository(database.weatherDao(), settingsManager)
        val viewModelFactory = WeatherViewModelFactory(repository, settingsManager)

        setContent {
            val viewModel: WeatherViewModel = viewModel(factory = viewModelFactory)
            val settings by viewModel.settingsState.collectAsState()

            val isDarkTheme = when (settings.themeMode) {
                ThemeMode.DARK -> true
                ThemeMode.LIGHT -> false
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
            }

            SLMWeatherTheme(darkTheme = isDarkTheme) {
                SLMWeatherApp(viewModel = viewModel, isDarkTheme = isDarkTheme)
            }
        }
    }
}

@Composable
fun SLMWeatherApp(
    viewModel: WeatherViewModel,
    isDarkTheme: Boolean,
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()
    var currentRoute by remember { mutableStateOf(Screen.Home.route) }
    val uiState by viewModel.uiState.collectAsState()
    val settings by viewModel.settingsState.collectAsState()

    val currentCondition = uiState.weatherData?.current?.condition ?: WeatherCondition.CLEAR_DAY
    val currentWindSpeed = uiState.weatherData?.current?.windSpeedKmh ?: 12.0

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        // Dynamic Atmospheric Canvas Animation Background
        WeatherBackgroundAnimation(
            condition = currentCondition,
            isDarkTheme = isDarkTheme,
            windSpeedKmh = currentWindSpeed,
            animationsEnabled = settings.animationsEnabled,
            modifier = Modifier.fillMaxSize()
        )

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets.statusBars,
            bottomBar = {
                GlassmorphicBottomNav(
                    currentRoute = currentRoute,
                    onNavigate = { screen ->
                        currentRoute = screen.route
                        navController.navigate(screen.route) {
                            popUpTo(Screen.Home.route) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = Screen.Home.route,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                composable(Screen.Home.route) {
                    currentRoute = Screen.Home.route
                    HomeScreen(
                        viewModel = viewModel,
                        onNavigateSearch = {
                            currentRoute = Screen.Search.route
                            navController.navigate(Screen.Search.route)
                        },
                        onNavigateSettings = {
                            currentRoute = Screen.Settings.route
                            navController.navigate(Screen.Settings.route)
                        }
                    )
                }

                composable(Screen.Search.route) {
                    currentRoute = Screen.Search.route
                    SearchScreen(
                        viewModel = viewModel,
                        onCitySelected = {
                            currentRoute = Screen.Home.route
                            navController.navigate(Screen.Home.route) {
                                popUpTo(Screen.Home.route) { inclusive = true }
                            }
                        }
                    )
                }

                composable(Screen.Locations.route) {
                    currentRoute = Screen.Locations.route
                    SavedLocationsScreen(
                        viewModel = viewModel,
                        onCitySelected = {
                            currentRoute = Screen.Home.route
                            navController.navigate(Screen.Home.route) {
                                popUpTo(Screen.Home.route) { inclusive = true }
                            }
                        },
                        onNavigateSearch = {
                            currentRoute = Screen.Search.route
                            navController.navigate(Screen.Search.route)
                        }
                    )
                }

                composable(Screen.Settings.route) {
                    currentRoute = Screen.Settings.route
                    SettingsScreen(viewModel = viewModel)
                }
            }
        }
    }
}
