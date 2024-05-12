package com.example.css545_prototype

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val preferencesDataStore by lazy { PreferencesDataStore(context = this)}
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {

            NyoomApp(preferencesDataStore = preferencesDataStore)
        }
    }
}

@Composable
fun NyoomApp(preferencesDataStore: PreferencesDataStore) {
    val navController = rememberNavController()
    //val preferencesDataStore = PreferencesDataStore(context = this)
    NavHost(navController, startDestination = "intro") {
        composable("intro") { IntroScreen(navController) }
        composable("cuisine") { CuisineScreen(navController, preferencesDataStore) }
        composable("suggestions") { SuggestionsScreen(navController, preferencesDataStore) } // Add this line
        composable("food") { ResultsScreenFood(navController)}
        composable("drinks") { ResultsScreenDrinks(navController) }
        composable("rerollfood"){ ReRollFood(navController)}
        composable("rerolldrinks"){ ReRollDrinks(navController)}
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
fun CuisineScreen(navController: NavController, preferencesDataStore: PreferencesDataStore) {
    var cuisine by remember { mutableStateOf("") }
    var atmosphere by remember { mutableStateOf("") }
    var favoriteDish by remember { mutableStateOf("") }

    val coroutineScope = rememberCoroutineScope()

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
            coroutineScope.launch {
                preferencesDataStore.savePreferences(cuisine, atmosphere, favoriteDish)
                navController.navigate("suggestions")
            }
        }) {
            Text("Save Preferences")
        }
    }
}

@Composable
fun SuggestionsScreen(navController: NavController, preferencesDataStore: PreferencesDataStore) {
    val preferences by preferencesDataStore.preferences.collectAsState(initial = emptyList())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("Your Preferences", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(8.dp))

        if (preferences.isNotEmpty()) {
            PreferenceItem(label = "Cuisine:", value = preferences[0])
            PreferenceItem(label = "Atmosphere:", value = preferences[1])
            PreferenceItem(label = "Favorite Dish:", value = preferences[2])
        } else {
            Text("No preferences found")
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { navController.navigate("food") }) {
            Text("Suggest Me Food")
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { navController.navigate("drinks") }) {
            Text("Suggest Me Drinks")
        }
    }
}

@Composable
fun PreferenceItem(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Text(text = "$label ", style = MaterialTheme.typography.bodyLarge)
        Text(text = value, style = MaterialTheme.typography.bodyLarge)
    }
}

// Takes user to google maps
//
@Composable
fun TakeMeButton(url: String) {
    val context = LocalContext.current
    Button(
        //modifier = Modifier.fillMaxWidth(),
        //contentPadding = PaddingValues(16.dp),
        onClick = {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            intent.setPackage("com.google.android.apps.maps")
            if (intent.resolveActivity(context.packageManager) != null) {
                context.startActivity(intent)
            }
        }
    ) {
        Text("Take Me!!!!")
    }
}

//All Below Functions are HardCoded!! for prototype
@Composable
fun ResultsScreenFood(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("NUE", fontSize = 25.sp, fontWeight = FontWeight.Bold)
        Text("1519 14th Ave,")
        Text("Seattle, WA 98122")


        Spacer(modifier = Modifier.height(16.dp))
        Row {
            TakeMeButton("https://www.google.com/maps/place/Nue/@47.6147255,-122.317023,17z/data=!4m6!3m5!1s0x54906acdfa55ddcf:0xaa8c3f43520ea04d!8m2!3d47.6147255!4d-122.3144481!16s%2Fg%2F11b6ds3khs?entry=ttu")
            Button(onClick = { navController.navigate("rerollfood") }) {
            Text("Re-Roll")
        }
        }
    }
}

@Composable
fun ReRollFood(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Dick's Drive-In", fontSize = 25.sp, fontWeight = FontWeight.Bold)
        Text("115 Broadway E,")
        Text("Seattle, WA 98102")


        Spacer(modifier = Modifier.height(16.dp))
        Row {

            TakeMeButton("https://www.google.com/maps/place/Dick's+Drive-In/@47.6193299,-122.3238012,16z/data=!3m1!4b1!4m6!3m5!1s0x549015329205c2e9:0xf9777b670a3cbaf3!8m2!3d47.6193263!4d-122.3212263!16s%2Fg%2F1tz75p2y?entry=ttu")
            Button(onClick = { navController.navigate("food") }) {
                Text("Re-Roll")
            }
        }

    }
}

@Composable
fun ResultsScreenDrinks(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Montana", fontSize = 50.sp, fontWeight = FontWeight.Bold)
        Text("1506 E Olive Wy,")
        Text("Seattle, WA 98122")

        Spacer(modifier = Modifier.height(16.dp))
        Row {

            TakeMeButton("https://www.google.com/maps/place/Montana/@47.617937,-122.3289392,16z/data=!3m2!4b1!5s0x549015337cbb9cf3:0x688e338ef8064f36!4m6!3m5!1s0x549015337b1595b7:0x2c2cc746034b4db2!8m2!3d47.6179334!4d-122.3263643!16s%2Fg%2F12632gdn1?entry=ttu")
            Button(onClick = { navController.navigate("rerolldrinks") }) {
                Text("Re-Roll")
            }
        }
    }
}

@Composable
fun ReRollDrinks(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("The Sloop", fontSize = 50.sp, fontWeight = FontWeight.Bold)
        Text("2830 NW Market St,")
        Text("Seattle, WA 98107")


        Spacer(modifier = Modifier.height(16.dp))
        Row {
            TakeMeButton("https://www.google.com/maps/place/Sloop+Tavern/@47.668875,-122.3968926,16z/data=!3m1!4b1!4m6!3m5!1s0x549015c30b4b0001:0x8ac88ca10fd82d46!8m2!3d47.6688714!4d-122.3943177!16s%2Fg%2F1wk4cs0s?entry=ttu")
            Button(onClick = { navController.navigate("drinks") }) {
                Text("Re-Roll")
            }
        }
    }
}

