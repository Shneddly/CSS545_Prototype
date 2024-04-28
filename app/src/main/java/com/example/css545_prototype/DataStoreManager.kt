package com.example.css545_prototype

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class PreferencesDataStore(context: Context) {

    private val Context.dataStore by preferencesDataStore(name = "user_preferences")

    private val dataStore = context.dataStore

    val preferences: Flow

    <List<String>> = dataStore.data.map { preferences ->
        listOf(
            preferences[CUISINE_KEY] ?: "",
            preferences[ATMOSPHERE_KEY] ?: "",
            preferences[FAVORITE_DISH_KEY] ?: "",
        )
    }

    suspend fun savePreferences(cuisine: String, atmosphere: String, favoriteDish: String) {
        dataStore.edit { preferences ->
            preferences[CUISINE_KEY] = cuisine
            preferences[ATMOSPHERE_KEY] = atmosphere
            preferences[FAVORITE_DISH_KEY] = favoriteDish
        }
    }

    companion object {
        private val CUISINE_KEY = stringPreferencesKey("cuisine")
        private val ATMOSPHERE_KEY = stringPreferencesKey("atmosphere")
        private val FAVORITE_DISH_KEY = stringPreferencesKey("favorite_dish")
    }
}