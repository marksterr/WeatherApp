package com.example.weatherapp.model.repo

import com.example.weatherapp.view.WeatherState
import kotlinx.coroutines.flow.Flow

interface WeatherRepository {
    /** Fetch weather information by city name */
    suspend fun fetchWeatherByCityName(cityName: String): Flow<WeatherState>

    /** Fetch weather information by latitude and longitude */
    suspend fun fetchWeatherByCoordinates(lat: Double, lon: Double): Flow<WeatherState>
}