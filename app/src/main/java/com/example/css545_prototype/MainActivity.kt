package com.example.css545_prototype

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role.Companion.Checkbox
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.android.gms.location.LocationServices
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
fun LocationAwareContent(onLocation: @Composable (Location?) -> Unit) {
    val context = LocalContext.current
    var location by remember { mutableStateOf<Location?>(null) }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            fetchLocation(context) { loc -> location = loc }
        } else {
            location = null // Handle permission denial by setting location to null
        }
    }

    LaunchedEffect(key1 = true) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        } else {
            fetchLocation(context) { loc -> location = loc }
        }
    }

    // Observe location changes and pass it to onLocation lambda
    location?.let {
        onLocation(it)
    } ?: onLocation(null)
}

fun fetchLocation(context: Context, onLocationFetched: (Location?) -> Unit) {
    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            onLocationFetched(location)
        }.addOnFailureListener {
            Log.e("LocationError", "Failed to get location", it)
            onLocationFetched(null)
        }
    }
}






@Composable
fun IntroScreen(navController: NavController) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Nyoom", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { navController.navigate("cuisine") }) {
            Text("Let's Go!")
        }
    }
}

@Composable
fun CuisineScreen(navController: NavController, preferencesDataStore: PreferencesDataStore) {
    val cuisineOptions = listOf("Italian", "Mexican", "Chinese", "Indian", "Japanese")
    val atmosphereOptions = listOf("Casual", "Formal", "Outdoor", "Indoor")
    val favoriteDishOptions = listOf("Pizza", "Sushi", "Burger")

    val cuisineMappings = mapOf(
        "Italian" to "italian_restaurant",
        "Mexican" to "mexican_restaurant",
        "Chinese" to "chinese_restaurant",
        "Indian" to "indian_restaurant",
        "Japanese" to "japanese_restaurant"
    )

    val atmosphereMappings = mapOf(
        "Casual" to "casual",
        "Formal" to "formal",
        "Outdoor" to "outdoor",
        "Indoor" to "indoor"
    )

    val dishMappings = mapOf(
        "Pizza" to "pizza_restaurant",
        "Sushi" to "sushi_restaurant",
        "Burger" to "hamburger_restaurant"
    )

    val savedCuisines = preferencesDataStore.cuisinesFlow.collectAsState(initial = emptySet())
    val savedAtmospheres = preferencesDataStore.atmospheresFlow.collectAsState(initial = emptySet())
    val savedDishes = preferencesDataStore.favoriteDishesFlow.collectAsState(initial = emptySet())

    val selectedCuisines = remember { mutableStateListOf<String>() }
    val selectedAtmospheres = remember { mutableStateListOf<String>() }
    val selectedDishes = remember { mutableStateListOf<String>() }

    LaunchedEffect(savedCuisines, savedAtmospheres, savedDishes) {
        selectedCuisines.clear()
        selectedCuisines.addAll(savedCuisines.value)
        selectedAtmospheres.clear()
        selectedAtmospheres.addAll(savedAtmospheres.value)
        selectedDishes.clear()
        selectedDishes.addAll(savedDishes.value)
    }

    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        bottomBar = {
            Button(
                onClick = {
                    coroutineScope.launch {
                        preferencesDataStore.saveCuisines(selectedCuisines.toSet())
                        preferencesDataStore.saveAtmospheres(selectedAtmospheres.toSet())
                        preferencesDataStore.saveFavoriteDishes(selectedDishes.toSet())
                        navController.navigate("suggestions")
                    }
                },
                modifier = Modifier.fillMaxWidth().padding(8.dp),
                content = { Text("Save Preferences") }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp)
        ) {
            item { Text("Select your favorite cuisines:") }
            items(cuisineOptions) { cuisine ->
                CheckboxItem(
                    item = cuisine,
                    isSelected = selectedCuisines.contains(cuisineMappings[cuisine]),
                    onItemClicked = { item ->
                        val internalName = cuisineMappings[item] ?: item
                        if (selectedCuisines.contains(internalName)) {
                            selectedCuisines.remove(internalName)
                        } else {
                            selectedCuisines.add(internalName)
                        }
                    }
                )
            }
            item { Spacer(modifier = Modifier.height(16.dp)) }

            item { Text("Select your preferred atmospheres:") }
            items(atmosphereOptions) { atmosphere ->
                CheckboxItem(
                    item = atmosphere,
                    isSelected = selectedAtmospheres.contains(atmosphereMappings[atmosphere]),
                    onItemClicked = { item ->
                        val internalName = atmosphereMappings[item] ?: item
                        if (selectedAtmospheres.contains(internalName)) {
                            selectedAtmospheres.remove(internalName)
                        } else {
                            selectedAtmospheres.add(internalName)
                        }
                    }
                )
            }
            item { Spacer(modifier = Modifier.height(16.dp)) }

            item { Text("Select your favorite dishes:") }
            items(favoriteDishOptions) { dish ->
                CheckboxItem(
                    item = dish,
                    isSelected = selectedDishes.contains(dishMappings[dish]),
                    onItemClicked = { item ->
                        val internalName = dishMappings[item] ?: item
                        if (selectedDishes.contains(internalName)) {
                            selectedDishes.remove(internalName)
                        } else {
                            selectedDishes.add(internalName)
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun CheckboxItem(item: String, isSelected: Boolean, onItemClicked: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { onItemClicked(item) },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = isSelected,
            onCheckedChange = { _ -> onItemClicked(item) }
        )
        Text(
            text = item,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}



//@Composable
//fun SuggestionsScreen(navController: NavController, preferencesDataStore: PreferencesDataStore) {
//
//    val selectedCuisines by preferencesDataStore.cuisinesFlow.collectAsState(initial = emptySet())
//    val selectedAtmospheres by preferencesDataStore.atmospheresFlow.collectAsState(initial = emptySet())
//    val selectedDishes by preferencesDataStore.favoriteDishesFlow.collectAsState(initial = emptySet())
//
//    Column(
//        modifier = Modifier
//            .fillMaxSize()
//            .padding(16.dp),
//        horizontalAlignment = Alignment.CenterHorizontally,
//        verticalArrangement = Arrangement.spacedBy(8.dp)
//    ) {
//        Text("Your Preferences", style = MaterialTheme.typography.headlineSmall)
//        Spacer(modifier = Modifier.height(8.dp))
//
//        // Display selected preferences
//        if (selectedCuisines.isNotEmpty()) {
//            Text("Favorite Cuisines: ${selectedCuisines.joinToString(", ")}")
//        }
//        if (selectedAtmospheres.isNotEmpty()) {
//            Text("Preferred Atmospheres: ${selectedAtmospheres.joinToString(", ")}")
//        }
//        if (selectedDishes.isNotEmpty()) {
//            Text("Favorite Dishes: ${selectedDishes.joinToString(", ")}")
//        }
//        Spacer(modifier = Modifier.height(16.dp))
//        Button(onClick = {
//            // implement the logic to save these preferences later, for now println
//            navController.navigate("suggestions")
//        }) {
//            Text("Save Preferences")
//        }
//    }
//}
@Composable
fun SuggestionsScreen(navController: NavController, preferencesDataStore: PreferencesDataStore) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        LocationAwareContent { location ->
            if (location != null) {
                Text("Current Location: Lat ${location.latitude}, Long ${location.longitude}")
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { navController.navigate("food") }) {
                    Text("Suggest Me Food")
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { navController.navigate("drinks") }) {
                    Text("Suggest Me Drinks")
                }
            } else {
                Text("Location permission needed or location is unavailable.")
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { /* trigger location fetching again or open settings */ }) {
                    Text("Retry Fetching Location")
                }
            }
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

