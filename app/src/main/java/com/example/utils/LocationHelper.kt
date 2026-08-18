package com.example.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Build
import androidx.core.content.ContextCompat
import com.example.data.model.SavedCity
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.util.Locale
import kotlin.coroutines.resume

object LocationHelper {

    fun hasLocationPermission(context: Context): Boolean {
        val fine = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val coarse = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        return fine || coarse
    }

    suspend fun getCurrentLocation(context: Context): Location? {
        if (!hasLocationPermission(context)) return null

        val fusedClient: FusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(context)

        return suspendCancellableCoroutine { continuation ->
            val cts = CancellationTokenSource()
            try {
                fusedClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cts.token)
                    .addOnSuccessListener { location: Location? ->
                        if (location != null) {
                            continuation.resume(location)
                        } else {
                            // Fallback to last known location
                            fusedClient.lastLocation
                                .addOnSuccessListener { lastLoc -> continuation.resume(lastLoc) }
                                .addOnFailureListener { continuation.resume(null) }
                        }
                    }
                    .addOnFailureListener {
                        continuation.resume(null)
                    }
            } catch (e: SecurityException) {
                continuation.resume(null)
            } catch (e: Exception) {
                continuation.resume(null)
            }

            continuation.invokeOnCancellation {
                cts.cancel()
            }
        }
    }

    /**
     * Reverse geocode GPS coordinates to obtain human-readable City name and Country code
     */
    suspend fun getCityInfoFromCoordinates(
        context: Context,
        lat: Double,
        lon: Double
    ): Triple<String, String, String> = withContext(Dispatchers.IO) {
        try {
            val geocoder = Geocoder(context, Locale.getDefault())
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                suspendCancellableCoroutine { cont ->
                    geocoder.getFromLocation(lat, lon, 1, object : Geocoder.GeocodeListener {
                        override fun onGeocode(addresses: MutableList<Address>) {
                            val address = addresses.firstOrNull()
                            if (address != null) {
                                val city = address.locality
                                    ?: address.subAdminArea
                                    ?: address.adminArea
                                    ?: address.featureName
                                    ?: "Current Location"
                                val state = address.adminArea ?: ""
                                val country = address.countryCode ?: address.countryName ?: "GPS"
                                cont.resume(Triple(city, state, country))
                            } else {
                                cont.resume(Triple("Current Location", "", "GPS"))
                            }
                        }

                        override fun onError(errorMessage: String?) {
                            cont.resume(Triple("Current Location", "", "GPS"))
                        }
                    })
                }
            } else {
                @Suppress("DEPRECATION")
                val addresses = geocoder.getFromLocation(lat, lon, 1)
                val address = addresses?.firstOrNull()
                if (address != null) {
                    val city = address.locality
                        ?: address.subAdminArea
                        ?: address.adminArea
                        ?: address.featureName
                        ?: "Current Location"
                    val state = address.adminArea ?: ""
                    val country = address.countryCode ?: address.countryName ?: "GPS"
                    Triple(city, state, country)
                } else {
                    Triple("Current Location", "", "GPS")
                }
            }
        } catch (e: Exception) {
            Triple("Current Location", "", "GPS")
        }
    }

    /**
     * Search global locations directly through Android Geocoder
     */
    suspend fun searchLocationsViaGeocoder(
        context: Context,
        query: String
    ): List<SavedCity> = withContext(Dispatchers.IO) {
        val q = query.trim()
        if (q.isBlank()) return@withContext emptyList()
        try {
            val geocoder = Geocoder(context, Locale.getDefault())
            @Suppress("DEPRECATION")
            val addresses = geocoder.getFromLocationName(q, 8)
            if (!addresses.isNullOrEmpty()) {
                addresses.mapNotNull { address ->
                    val cityName = address.locality
                        ?: address.subAdminArea
                        ?: address.featureName
                        ?: address.adminArea
                        ?: return@mapNotNull null
                    val stateName = address.adminArea ?: ""
                    val countryCode = address.countryCode ?: address.countryName ?: ""
                    SavedCity(
                        name = cityName,
                        state = stateName,
                        country = countryCode,
                        lat = address.latitude,
                        lon = address.longitude
                    )
                }.distinctBy { "${it.name}_${it.country}_${it.lat.toInt()}" }
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}

