package com.example.css545_prototype

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NyoomApp()
        }
    }
}

@Composable
fun NyoomApp() {
    val navController = rememberNavController()
    NavHost(navController, startDestination = "intro") {
        composable("intro") { IntroScreen(navController) }
        composable("cuisine") { CuisineScreen(navController) }
        composable("suggestions") { SuggestionsScreen(navController) } // Add this line
    }
}

@Composable
fun IntroScreen(navController: NavController) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("nyoom", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { navController.navigate("cuisine") }) {
            Text("Let's Go!")
        }
    }
}

@Composable
fun CuisineScreen(navController: NavController) {
    var cuisine by remember { mutableStateOf("") }
    var atmosphere by remember { mutableStateOf("") }
    var favoriteDish by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        TextField(
            value = cuisine,
            onValueChange = { cuisine = it },
            label = { Text("Favorite Cuisine") }
        )
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = atmosphere,
            onValueChange = { atmosphere = it },
            label = { Text("Preferred Atmosphere") }
        )
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = favoriteDish,
            onValueChange = { favoriteDish = it },
            label = { Text("Favorite Dish") }
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = {
            // implement the logic to save these preferences later, for now println
            println("Preferences saved: Cuisine=$cuisine, Atmosphere=$atmosphere, Dish=$favoriteDish")
            navController.navigate("suggestions")
        }) {
            Text("Save Preferences")
        }
    }
}

@Composable
fun SuggestionsScreen(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Button(onClick = { /* Implement food suggestion logic */ }) {
            Text("Suggest Me Food")
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { /* Implement drink suggestion logic */ }) {
            Text("Suggest Me Drinks")
        }
    }
}
