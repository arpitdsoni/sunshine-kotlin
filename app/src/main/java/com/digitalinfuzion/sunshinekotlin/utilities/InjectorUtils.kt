/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.digitalinfuzion.sunshinekotlin.utilities

import android.content.Context

import com.digitalinfuzion.sunshinekotlin.AppExecutors
import com.digitalinfuzion.sunshinekotlin.data.SunshineRepository
import com.digitalinfuzion.sunshinekotlin.data.database.SunshineDatabase
import com.digitalinfuzion.sunshinekotlin.data.network.WeatherNetworkDataSource
import com.digitalinfuzion.sunshinekotlin.ui.detail.DetailViewModelFactory
import com.digitalinfuzion.sunshinekotlin.ui.list.MainViewModelFactory

import java.util.Date

/**
 * Provides static methods to inject the various classes needed for Sunshine
 */
object InjectorUtils {

    fun provideRepository(context: Context): SunshineRepository {
        val database = SunshineDatabase.getInstance(context.applicationContext)
        val executors = AppExecutors.instance
        val networkDataSource = WeatherNetworkDataSource.getInstance(context.applicationContext, executors)
        return SunshineRepository.getInstance(database!!.weatherDao(), networkDataSource, executors)
    }

    fun provideNetworkDataSource(context: Context): WeatherNetworkDataSource {
        // This call to provide repository is necessary if the app starts from a service - in this
        // case the repository will not exist unless it is specifically created.
        provideRepository(context.applicationContext)
        val executors = AppExecutors.instance
        return WeatherNetworkDataSource.getInstance(context.applicationContext, executors)
    }

    fun provideDetailViewModelFactory(context: Context, date: Date): DetailViewModelFactory {
        val repository = provideRepository(context.applicationContext)
        return DetailViewModelFactory(repository, date)
    }

    fun provideMainActivityViewModelFactory(context: Context): MainViewModelFactory {
        val repository = provideRepository(context.applicationContext)
        return MainViewModelFactory(repository)
    }

}