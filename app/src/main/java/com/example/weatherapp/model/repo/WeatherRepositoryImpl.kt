package com.example.weatherapp.model.repo

import com.example.weatherapp.model.WeatherApiService
import com.example.weatherapp.model.data.WeatherResponse
import com.example.weatherapp.view.WeatherState
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import retrofit2.Response
import javax.inject.Inject

class WeatherRepositoryImpl @Inject constructor(
    private val apiService: WeatherApiService,
    private val dispatcher: CoroutineDispatcher
) : WeatherRepository {

    override suspend fun fetchWeatherByCityName(cityName: String): Flow<WeatherState> = flow {
        emit(WeatherState.Loading)
        emitResultState(apiService.fetchWeatherByCity(cityName))
    }.flowOn(dispatcher)

    override suspend fun fetchWeatherByCoordinates(lat: Double, lon: Double): Flow<WeatherState> = flow {
        emit(WeatherState.Loading)
        emitResultState(apiService.fetchWeatherByCoordinates(lat, lon))
    }.flowOn(dispatcher)

    /** Emits a [WeatherState] based on the success of [response] or if the body is null. */
    private suspend fun FlowCollector<WeatherState>.emitResultState(response: Response<WeatherResponse>) {
        try {
            if (response.isSuccessful) {
                response.body()?.let {
                    emit(WeatherState.Success(it))
                } ?: throw IllegalStateException("Empty response from server")
            } else {
                throw Exception(response.errorBody()?.string())
            }
        } catch (e: Exception) {
            emit(WeatherState.Failure(e))
        }
    }
}