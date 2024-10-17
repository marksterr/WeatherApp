package com.example.weatherapp.di

import com.example.weatherapp.model.repo.WeatherRepository
import com.example.weatherapp.model.repo.WeatherRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent

@Module
@InstallIn(ViewModelComponent::class)
abstract class RepositoryModule {

    @Binds
    abstract fun bindWeatherRepositoryImpl(
        impl: WeatherRepositoryImpl
    ): WeatherRepository
}