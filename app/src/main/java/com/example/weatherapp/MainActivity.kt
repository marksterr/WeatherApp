package com.example.weatherapp

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActionScope
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.weatherapp.model.PreferencesManager
import com.example.weatherapp.model.data.WeatherResponse
import com.example.weatherapp.ui.theme.WeatherAppTheme
import com.example.weatherapp.view.WeatherState
import com.example.weatherapp.view.WeatherViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.gms.location.LocationServices
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.tasks.await

@AndroidEntryPoint
class HomeActivity : ComponentActivity() {
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) {
                    WeatherMainScreen()
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun WeatherPreview() {
    MaterialTheme {
        WeatherMainScreen()
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun WeatherMainScreen() {

    val context = LocalContext.current
    val viewModel: WeatherViewModel = hiltViewModel()
    var query by remember { mutableStateOf("") }

    val permissionState = rememberPermissionState(
        permission = Manifest.permission.ACCESS_COARSE_LOCATION
    )

    RequestLocationPermission(permissionState)
    FetchLocationWeather(context, permissionState, viewModel)

    // Load the previous search query from the data store
    LaunchedEffect(Unit) {
        query = PreferencesManager.retrieveLastCity(context).orEmpty()
        if (query.isNotBlank()) {
            viewModel.fetchWeatherByCity(query, context)
        }
    }

    // Compose the default view: title, description, and search field
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .offset(0.dp, 50.dp),
        contentAlignment = Alignment.TopCenter
    ) {

        Column {
            TitleText(text = stringResource(R.string.welcome_to_weather_app))
            Spacer(modifier = Modifier.padding(8.dp))

            Text(
                text = stringResource(R.string.instructions),
                modifier = Modifier.padding(10.dp)
            )
            SearchField(
                searchQuery = query,
                label = stringResource(R.string.search_city),
                onValueChange = { newQuery ->
                    query = newQuery
                },
                onSearch = {
                    if (query.isNotBlank()) {
                        viewModel.fetchWeatherByCity(query, context)
                    }
                }
            )
            Spacer(modifier = Modifier.height(16.dp))

            when (val state = viewModel.cityWeatherState.collectAsStateWithLifecycle().value) {
                is WeatherState.Loading -> LoadingIndicator(isLoading = true)
                is WeatherState.Success -> DisplayWeather(state.data)
                is WeatherState.Failure -> {
                    Toast.makeText(
                        context,
                        stringResource(R.string.searched_city_not_found), Toast.LENGTH_LONG
                    ).show()
                    LoadingIndicator(isLoading = false)
                }
            }
        }
    }
}

/** Show or hide an indeterminate loading icon. **/
@Composable
fun LoadingIndicator(isLoading: Boolean = false) {
    if (!isLoading) return
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.width(64.dp),
            color = MaterialTheme.colorScheme.secondary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}

/** Displays the weather information using a simple Card to show fetched data. **/
@Composable
fun DisplayWeather(response: WeatherResponse) {
    LoadingIndicator(isLoading = false)
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = stringResource(R.string.city_name_arg, response.name),
                )
                Column {
                    Text(text = stringResource(R.string.latitude_arg, response.coord.lat))
                    Text(text = stringResource(R.string.longitude_arg, response.coord.lon))
                }
            }
            Spacer(modifier = Modifier.padding(16.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = stringResource(R.string.temperature_arg, response.main.temp),
                )
                Column {
                    Text(text = stringResource(R.string.min_arg, response.main.tempMin))
                    Text(text = stringResource(R.string.max_arg, response.main.tempMax))
                }
            }
            Spacer(modifier = Modifier.padding(32.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = stringResource(
                        R.string.current_weather_arg,
                        response.weather[0].description
                    )
                )
                AsyncImage(
                    model = getIconUrl(response.weather[0].icon),
                    contentDescription = null,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}

/** A single-line text field for search with actions on value change and search. **/
@Composable
fun SearchField(
    searchQuery: String,
    label: String,
    onValueChange: (String) -> Unit,
    onSearch: KeyboardActionScope.() -> Unit
) {
    TextField(
        value = searchQuery,
        onValueChange = onValueChange,
        label = { Text(text = label) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            imeAction = ImeAction.Search
        ),
        keyboardActions = KeyboardActions(
            onSearch = onSearch
        ),
        modifier = Modifier.fillMaxWidth()
    )
}

/** Headline text with prominent properties like large font, bold style, and centered alignment. **/
@Composable
fun TitleText(text: String) {
    Text(
        text = text,
        fontWeight = FontWeight.Bold,
        fontSize = MaterialTheme.typography.headlineLarge.fontSize,
        color = MaterialTheme.colorScheme.primary,
        textAlign = TextAlign.Center,
        lineHeight = 30.sp
    )
}

@Composable
@OptIn(ExperimentalPermissionsApi::class)
fun RequestLocationPermission(permissionState: PermissionState) {
    LaunchedEffect(key1 = permissionState) {
        if (!permissionState.status.isGranted) {
            permissionState.launchPermissionRequest()
        }
    }
}

/** Fetch weather for device's location if permitted. **/
@Composable
@OptIn(ExperimentalPermissionsApi::class)
fun FetchLocationWeather(
    context: Context,
    permissionState: PermissionState,
    viewModel: WeatherViewModel
) {
    val locationClient = LocationServices.getFusedLocationProviderClient(context)
    if (permissionState.status.isGranted) {
        LaunchedEffect(key1 = Unit) {
            try {
                val location = locationClient.lastLocation.await()
                location?.let {
                    viewModel.fetchWeatherByCoordinates(it.latitude, it.longitude)
                }
            } catch (e: SecurityException) {
                // User denied permission
            } catch (e: Exception) {
                Toast.makeText(
                    context,
                    context.getString(R.string.current_location_not_found),
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
}

private fun getIconUrl(iconCode: String): String {
    return "https://openweathermap.org/img/wn/$iconCode@2x.png"
}
