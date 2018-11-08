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

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.digitalinfuzion.sunshinekotlin.data.database.WeatherEntry

import com.digitalinfuzion.sunshinekotlin.data.SunshineRepository

import java.util.Date

/**
 * Factory method that allows us to create a ViewModel with a constructor that takes a
 * [SunshineRepository] and an ID for the current [WeatherEntry]
 */
class DetailViewModelFactory(private val mRepository: SunshineRepository, private val mDate: Date) : ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {

        return DetailActivityViewModel(mRepository, mDate) as T
    }
}