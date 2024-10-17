package com.example.weatherapp.view

import com.example.weatherapp.model.data.WeatherResponse

sealed class WeatherState {
    data object Loading : WeatherState()
    data class Success(val data: WeatherResponse) : WeatherState()
    data class Failure(val error: Throwable) : WeatherState()
}