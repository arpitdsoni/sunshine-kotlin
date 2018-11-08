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

package com.digitalinfuzion.sunshinekotlin.data.database

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

/**
 * [SunshineDatabase] database for the application including a table for [WeatherEntry]
 * with the DAO [WeatherDao].
 */

// List of the entry classes and associated TypeConverters
@Database(entities = [WeatherEntry::class], version = 1)
@TypeConverters(DateConverter::class)
abstract class SunshineDatabase : RoomDatabase() {

    // The associated DAOs for the database
    abstract fun weatherDao(): WeatherDao

    companion object {

        private val LOG_TAG = SunshineDatabase::class.java.simpleName
        private val DATABASE_NAME = "weather"

        // For Singleton instantiation
        private var sInstance: SunshineDatabase? = null

        fun getInstance(context: Context): SunshineDatabase? {
            Log.d(LOG_TAG, "Getting the database")
            return sInstance ?: synchronized(this) {
                val instance = Room.databaseBuilder(context.applicationContext,
                        SunshineDatabase::class.java, SunshineDatabase.DATABASE_NAME).build()
                Log.d(LOG_TAG, "Made new database")
                sInstance = instance
                return instance
            }
        }
    }
}
