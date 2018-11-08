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
package com.digitalinfuzion.sunshinekotlin.ui.detail

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.digitalinfuzion.sunshinekotlin.R
import com.digitalinfuzion.sunshinekotlin.data.database.WeatherEntry
import com.digitalinfuzion.sunshinekotlin.databinding.ActivityDetailBinding
import com.digitalinfuzion.sunshinekotlin.utilities.InjectorUtils
import com.digitalinfuzion.sunshinekotlin.utilities.SunshineDateUtils
import com.digitalinfuzion.sunshinekotlin.utilities.SunshineWeatherUtils

import java.util.Date

/**
 * Displays single day's forecast
 */
class DetailActivity : AppCompatActivity() {

    /*
     * This field is used for data binding. Normally, we would have to call findViewById many
     * times to get references to the Views in this Activity. With data binding however, we only
     * need to call DataBindingUtil.setContentView and pass in a Context and a layout, as we do
     * in onCreate of this class. Then, we can access all of the Views in our layout
     * programmatically without cluttering up the code with findViewById.
     */
    private var mDetailBinding: ActivityDetailBinding? = null
    private lateinit var mViewModel: DetailActivityViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mDetailBinding = DataBindingUtil.setContentView(this, R.layout.activity_detail)
        val timestamp = intent.getLongExtra(WEATHER_ID_EXTRA, -1)
        val date = Date(timestamp)

        // Get the ViewModel from the factory
        val factory = InjectorUtils.provideDetailViewModelFactory(this.getApplicationContext(), date)
        mViewModel = ViewModelProviders.of(this, factory).get(DetailActivityViewModel::class.java)

        // Observers changes in the WeatherEntry with the id mId
        mViewModel.weather.observe(this, Observer { weatherEntry ->
            // If the weather forecast details change, update the UI
            if (weatherEntry != null) bindWeatherToUI(weatherEntry)
        })

    }

    private fun bindWeatherToUI(weatherEntry: WeatherEntry) {
        /****************
         * Weather Icon *
         */

        val weatherId = weatherEntry.weatherIconId
        val weatherImageId = SunshineWeatherUtils.getLargeArtResourceIdForWeatherCondition(weatherId)

        /* Set the resource ID on the icon to display the art */
        mDetailBinding!!.primaryInfo.weatherIcon.setImageResource(weatherImageId)

        /****************
         * Weather Date *
         */
        /*
         * The date that is stored is a GMT representation at midnight of the date when the weather
         * information was loaded for.
         *
         * When displaying this date, one must add the GMT offset (in milliseconds) to acquire
         * the date representation for the local date in local time.
         * SunshineDateUtils#getFriendlyDateString takes care of this for us.
         */
        val localDateMidnightGmt = weatherEntry.date.time
        val dateText = SunshineDateUtils.getFriendlyDateString(this@DetailActivity, localDateMidnightGmt, true)
        mDetailBinding!!.primaryInfo.date.setText(dateText)

        /***********************
         * Weather Description *
         */
        /* Use the weatherId to obtain the proper description */
        val description = SunshineWeatherUtils.getStringForWeatherCondition(this@DetailActivity, weatherId)

        /* Create the accessibility (a11y) String from the weather description */
        val descriptionA11y = getString(R.string.a11y_forecast, description)

        /* Set the text and content description (for accessibility purposes) */
        mDetailBinding!!.primaryInfo.weatherDescription.setText(description)
        mDetailBinding!!.primaryInfo.weatherDescription.setContentDescription(descriptionA11y)

        /* Set the content description on the weather image (for accessibility purposes) */
        mDetailBinding!!.primaryInfo.weatherIcon.setContentDescription(descriptionA11y)

        /**************************
         * High (max) temperature *
         */

        val maxInCelsius = weatherEntry.max

        /*
         * If the user's preference for weather is fahrenheit, formatTemperature will convert
         * the temperature. This method will also append either °C or °F to the temperature
         * String.
         */
        val highString = SunshineWeatherUtils.formatTemperature(this@DetailActivity, maxInCelsius)

        /* Create the accessibility (a11y) String from the weather description */
        val highA11y = getString(R.string.a11y_high_temp, highString)

        /* Set the text and content description (for accessibility purposes) */
        mDetailBinding!!.primaryInfo.highTemperature.setText(highString)
        mDetailBinding!!.primaryInfo.highTemperature.setContentDescription(highA11y)

        /*************************
         * Low (min) temperature *
         */

        val minInCelsius = weatherEntry.min
        /*
         * If the user's preference for weather is fahrenheit, formatTemperature will convert
         * the temperature. This method will also append either °C or °F to the temperature
         * String.
         */
        val lowString = SunshineWeatherUtils.formatTemperature(this@DetailActivity, minInCelsius)

        val lowA11y = getString(R.string.a11y_low_temp, lowString)

        /* Set the text and content description (for accessibility purposes) */
        mDetailBinding!!.primaryInfo.lowTemperature.setText(lowString)
        mDetailBinding!!.primaryInfo.lowTemperature.setContentDescription(lowA11y)

        /************
         * Humidity *
         */

        val humidity = weatherEntry.humidity
        val humidityString = getString(R.string.format_humidity, humidity)
        val humidityA11y = getString(R.string.a11y_humidity, humidityString)

        /* Set the text and content description (for accessibility purposes) */
        mDetailBinding!!.extraDetails.humidity.setText(humidityString)
        mDetailBinding!!.extraDetails.humidity.setContentDescription(humidityA11y)

        mDetailBinding!!.extraDetails.humidityLabel.setContentDescription(humidityA11y)

        /****************************
         * Wind speed and direction *
         */
        /* Read wind speed (in MPH) and direction (in compass degrees)*/
        val windSpeed = weatherEntry.wind
        val windDirection = weatherEntry.degrees
        val windString = SunshineWeatherUtils.getFormattedWind(this@DetailActivity, windSpeed, windDirection)
        val windA11y = getString(R.string.a11y_wind, windString)

        /* Set the text and content description (for accessibility purposes) */
        mDetailBinding!!.extraDetails.windMeasurement.setText(windString)
        mDetailBinding!!.extraDetails.windMeasurement.setContentDescription(windA11y)
        mDetailBinding!!.extraDetails.windLabel.setContentDescription(windA11y)

        /************
         * Pressure *
         */
        val pressure = weatherEntry.pressure

        /*
         * Format the pressure text using string resources. The reason we directly access
         * resources using getString rather than using a method from SunshineWeatherUtils as
         * we have for other data displayed in this Activity is because there is no
         * additional logic that needs to be considered in order to properly display the
         * pressure.
         */
        val pressureString = getString(R.string.format_pressure, pressure)

        val pressureA11y = getString(R.string.a11y_pressure, pressureString)

        /* Set the text and content description (for accessibility purposes) */
        mDetailBinding!!.extraDetails.pressure.setText(pressureString)
        mDetailBinding!!.extraDetails.pressure.setContentDescription(pressureA11y)
        mDetailBinding!!.extraDetails.pressureLabel.setContentDescription(pressureA11y)
    }

    companion object {

        val WEATHER_ID_EXTRA = "WEATHER_ID_EXTRA"
    }
}