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
    }
}
//class PreferencesDataStore(context: Context) {
//
//    private val Context.dataStore by preferencesDataStore(name = "user_preferences")
//
//    private val dataStore = context.dataStore
//
//    val preferences: Flow
//
//    <List<String>> = dataStore.data.map { preferences ->
//        listOf(
//            preferences[CUISINE_KEY] ?: "",
//            preferences[ATMOSPHERE_KEY] ?: "",
//            preferences[FAVORITE_DISH_KEY] ?: "",
//        )
//    }
//
//    suspend fun savePreferences(cuisine: String, atmosphere: String, favoriteDish: String) {
//        dataStore.edit { preferences ->
//            preferences[CUISINE_KEY] = cuisine
//            preferences[ATMOSPHERE_KEY] = atmosphere
//            preferences[FAVORITE_DISH_KEY] = favoriteDish
//        }
//    }
//
//    companion object {
//        private val CUISINE_KEY = stringPreferencesKey("cuisine")
//        private val ATMOSPHERE_KEY = stringPreferencesKey("atmosphere")
//        private val FAVORITE_DISH_KEY = stringPreferencesKey("favorite_dish")
//    }
//}