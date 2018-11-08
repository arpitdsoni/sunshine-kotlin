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

package com.digitalinfuzion.sunshinekotlin.data

import android.util.Log
import androidx.lifecycle.LiveData

import com.digitalinfuzion.sunshinekotlin.AppExecutors
import com.digitalinfuzion.sunshinekotlin.data.database.ListWeatherEntry
import com.digitalinfuzion.sunshinekotlin.data.database.WeatherDao
import com.digitalinfuzion.sunshinekotlin.data.database.WeatherEntry
import com.digitalinfuzion.sunshinekotlin.data.network.WeatherNetworkDataSource
import com.digitalinfuzion.sunshinekotlin.utilities.SunshineDateUtils

import java.util.Date

/**
 * Handles data operations in Sunshine. Acts as a mediator between [WeatherNetworkDataSource]
 * and [WeatherDao]
 */
class SunshineRepository private constructor(private val mWeatherDao: WeatherDao,
                                             private val mWeatherNetworkDataSource: WeatherNetworkDataSource,
                                             private val mExecutors: AppExecutors) {
    private var mInitialized = false

    /**
     * Database related operations
     */

    val currentWeatherForecasts: LiveData<List<ListWeatherEntry>>
        get() {
            initializeData()
            val today = SunshineDateUtils.normalizedUtcDateForToday
            return mWeatherDao.getCurrentWeatherForecasts(today)
        }

    /**
     * Checks if there are enough days of future weather for the app to display all the needed data.
     *
     * @return Whether a fetch is needed
     */
    private val isFetchNeeded: Boolean
        get() {
            val today = SunshineDateUtils.normalizedUtcDateForToday
            val count = mWeatherDao.countAllFutureWeather(today)
            return count < WeatherNetworkDataSource.NUM_DAYS
        }

    init {

        // As long as the repository exists, observe the network LiveData.
        // If that LiveData changes, update the database.
        val networkData = mWeatherNetworkDataSource.currentWeatherForecasts

        networkData.observeForever { newForecastsFromNetwork ->
            mExecutors.diskIO().execute {
                // Deletes old historical data
                deleteOldData()
                Log.d(LOG_TAG, "Old weather deleted")
                // Insert our new weather data into Sunshine's database
                mWeatherDao.bulkInsert(newForecastsFromNetwork)
                Log.d(LOG_TAG, "New values inserted")
            }
        }
    }

    /**
     * Creates periodic sync tasks and checks to see if an immediate sync is required. If an
     * immediate sync is required, this method will take care of making sure that sync occurs.
     */
    @Synchronized
    private fun initializeData() {

        // Only perform initialization once per app lifetime. If initialization has already been
        // performed, we have nothing to do in this method.
        if (mInitialized) return
        mInitialized = true

        // This method call triggers Sunshine to create its task to synchronize weather data
        // periodically.
        mWeatherNetworkDataSource.scheduleRecurringFetchWeatherSync()

        mExecutors.diskIO().execute {
            if (isFetchNeeded) {
                startFetchWeatherService()
            }
        }
    }

    fun getWeatherByDate(date: Date): LiveData<WeatherEntry> {
        initializeData()
        return mWeatherDao.getWeatherByDate(date)
    }

    /**
     * Deletes old weather data because we don't need to keep multiple days' data
     */
    private fun deleteOldData() {
        val today = SunshineDateUtils.normalizedUtcDateForToday
        mWeatherDao.deleteOldWeather(today)
    }

    /**
     * Network related operation
     */

    private fun startFetchWeatherService() {
        mWeatherNetworkDataSource.startFetchWeatherService()
    }

    companion object {
        private val LOG_TAG = SunshineRepository::class.java.simpleName

        // For Singleton instantiation
        private var sInstance: SunshineRepository? = null

        @Synchronized
        fun getInstance(
                weatherDao: WeatherDao, weatherNetworkDataSource: WeatherNetworkDataSource,
                executors: AppExecutors): SunshineRepository {
            Log.d(LOG_TAG, "Getting the repository")
            return sInstance ?: synchronized(this) {
                val instance = SunshineRepository(weatherDao, weatherNetworkDataSource,
                        executors)
                Log.d(LOG_TAG, "Made new repository")
                sInstance = instance
                return instance
            }
        }
    }

}