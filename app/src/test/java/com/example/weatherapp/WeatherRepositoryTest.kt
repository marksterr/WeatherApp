package com.example.weatherapp

import com.example.weatherapp.model.WeatherApiService
import com.example.weatherapp.model.repo.WeatherRepositoryImpl
import io.mockk.coEvery
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Before
import org.junit.Test
import retrofit2.Response

@ExperimentalCoroutinesApi
class WeatherAppRepositoryImplTest {

    private lateinit var repository: WeatherRepositoryImpl
    private lateinit var weatherAPI: WeatherApiService
    private val coroutineDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        weatherAPI = mockk()
        repository = WeatherAppRepositoryImpl(weatherAPI, coroutineDispatcher)
    }

    @Test
    fun `getWeather emits Loading state first`() = runTest {
        val city = "London"
        val flow = repository.getWeatherByCity(city)
        val firstEmission = flow.first()
        assertTrue(firstEmission is WeatherUIState.StartLoading)
    }

    @Test
    fun `getWeather emits Success state when API call is successful`() = runTest {
        val city = "London"
        val weatherResponse = WeatherResponse()
//            coord = Coord(0.0, 0.0),
//            weather = listOf(Weather("", "")),
//            base = "",
//            main = Main(0.0, 0, 0, 0, 0, 0.0),
//            visibility = 0,
//            wind = Wind(0, 0.0),
//            clouds = Clouds(0),
//            dt = 0,
//            sys = Sys("", 0, 0, 0, 0),
//            timezone = 0,
//            id = 0,
//            name = "",
//            cod = 200
//        )
        coEvery { weatherAPI.fetchWeatherByCity(city) } returns Response.success(weatherResponse)
        val flow = repository.fetchWeatherByCity(city)
        val successEmission = flow.drop(1).first()
        assertTrue(successEmission is WeatherUIState.Success)
        assertEquals((successEmission as WeatherUIState.Success).weatherResponse, weatherResponse)
    }

    @Test
    fun `getWeather emits Error state when API call fails`() = runTest {
        val city = "London"
        val exception = Exception("API error")
        coEvery { weatherAPI.getWeatherByCity(city) } throws exception
        val flow = repository.getWeatherByCity(city)
        val errorEmission = flow.drop(1).first() // Skip the Loading state
        assertTrue(errorEmission is WeatherUIState.Error)
        assertEquals((errorEmission as WeatherUIState.Error).exception.message, exception.message)
    }

    @Test
    fun `getWeather emits Error state when response body is null`() = runTest {
        val city = "London"
        coEvery { weatherAPI.fetchWeatherByCity(city) } returns Response.success(null)
        val flow = repository.fetchWeatherByCity(city)
        val errorEmission = flow.drop(1).first() // Skip the Loading state
        assertTrue(errorEmission is WeatherUIState.Error)
    }

    @Test
    fun `getWeather emits Error state when response is not successful`() = runTest {
        val city = "London"
        val responseBody = "Error response"
        coEvery { weatherAPI.fetchWeatherByCity(city) } returns Response.error(400, responseBody.toResponseBody())
        val flow = repository.fetchWeatherByCity(city)
        val errorEmission = flow.drop(1).first() // Skip the Loading state
        assertTrue(errorEmission is WeatherUIState.Error)
        assertEquals((errorEmission as WeatherUIState.Error).exception.message, responseBody)
    }
}