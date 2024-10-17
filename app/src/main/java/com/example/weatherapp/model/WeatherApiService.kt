package com.example.weatherapp.model

import com.example.weatherapp.model.data.WeatherResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApiService {
    @GET("weather")
    suspend fun fetchWeatherByCity(
        @Query("q") cityName: String,
        @Query("appid") key: String = KEY,
        @Query("units") units: String = "metric"
    ): Response<WeatherResponse>

    @GET("weather")
    suspend fun fetchWeatherByCoordinates(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("appid") key: String = KEY
    ): Response<WeatherResponse>
}