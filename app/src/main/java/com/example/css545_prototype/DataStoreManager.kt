package com.example.css545_prototype

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class PreferencesDataStore(private val context: Context) {

    private val Context.dataStore by preferencesDataStore(name = "user_preferences")

    val cuisinesFlow: Flow<Set<String>> = context.dataStore.data.map { preferences ->
        preferences[KEY_CUISINES] ?: emptySet()
    }

    val atmospheresFlow: Flow<Set<String>> = context.dataStore.data.map { preferences ->
        preferences[KEY_ATMOSPHERES] ?: emptySet()
    }

    val favoriteDishesFlow: Flow<Set<String>> = context.dataStore.data.map { preferences ->
        preferences[KEY_FAVORITE_DISHES] ?: emptySet()
    }

    suspend fun saveCuisines(cuisines: Set<String>) {
        context.dataStore.edit { preferences ->
            preferences[KEY_CUISINES] = cuisines
        }
    }

    suspend fun saveAtmospheres(atmospheres: Set<String>) {
        context.dataStore.edit { preferences ->
            preferences[KEY_ATMOSPHERES] = atmospheres
        }
    }

    suspend fun saveFavoriteDishes(dishes: Set<String>) {
        context.dataStore.edit { preferences ->
            preferences[KEY_FAVORITE_DISHES] = dishes
        }
    }

    companion object {
        private val KEY_CUISINES = stringSetPreferencesKey("key_cuisines")
        private val KEY_ATMOSPHERES = stringSetPreferencesKey("key_atmospheres")
        private val KEY_FAVORITE_DISHES = stringSetPreferencesKey("key_favorite_dishes")

        @Volatile
        private var INSTANCE: PreferencesDataStore? = null

        fun getInstance(context: Context): PreferencesDataStore {
            return INSTANCE ?: synchronized(this) {
                val instance = PreferencesDataStore(context)
                INSTANCE = instance
                instance
            }
        }
    }
}
