package com.example.weatherapp.view

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weatherapp.model.PreferencesManager
import com.example.weatherapp.model.repo.WeatherRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WeatherViewModel @Inject constructor(
    private val repository: WeatherRepository
) : ViewModel() {

    private val _cityWeatherState: MutableStateFlow<WeatherState> =
        MutableStateFlow(WeatherState.Loading)
    val cityWeatherState: StateFlow<WeatherState> get() = _cityWeatherState

    /** Fetches weather using the city name and updates [cityWeatherState]. */
    fun fetchWeatherByCity(cityName: String = "", context: Context) {
        viewModelScope.launch {
            repository.fetchWeatherByCityName(cityName).collect {
                _cityWeatherState.value = it
                if (it is WeatherState.Success) {
                    PreferencesManager.storeLastCity(context, cityName)
                }
            }
        }
    }

    /** Fetches weather using latitude and longitude coordinates and updates [cityWeatherState]. */
    fun fetchWeatherByCoordinates(latitude: Double, longitude: Double) {
        viewModelScope.launch {
            repository.fetchWeatherByCoordinates(latitude, longitude).collect {
                _cityWeatherState.value = it
            }
        }
    }
}