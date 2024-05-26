package com.example.css545_prototype

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.android.gms.location.LocationServices
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role.Companion.Checkbox
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

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
    var location by remember { mutableStateOf<Location?>(null) }
    val context = LocalContext.current

    NavHost(navController, startDestination = "intro") {
        composable("intro") { IntroScreen(navController) }
        composable("cuisine") { CuisineScreen(navController, preferencesDataStore, onNavigateToLocationChoice = {
            navController.navigate("locationChoice")
        }) }
        composable("locationChoice") { LocationChoiceScreen(navController, context, onLocationSet = { loc ->
            location = loc
            navController.navigate("suggestions")
        }) }
        composable("suggestions") {
            SuggestionsScreen(navController, location)
        }
        composable("food") { ResultsScreenFood(navController) }
        composable("drinks") { ResultsScreenDrinks(navController) }
        composable("rerollfood") { ReRollFood(navController) }
        composable("rerolldrinks") { ReRollDrinks(navController) }
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
fun CuisineScreen(navController: NavController, preferencesDataStore: PreferencesDataStore, onNavigateToLocationChoice: () -> Unit) {
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
                        onNavigateToLocationChoice()
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationChoiceScreen(navController: NavController, context: Context, onLocationSet: (Location) -> Unit) {
    var locationText by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isFetching by remember { mutableStateOf(false) }
    var locationPermissionGranted by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
        locationPermissionGranted = isGranted
        if (!isGranted) {
            errorMessage = "Location permission denied. Please try again or enter location manually."
        }
    }

    if (locationPermissionGranted && isFetching) {
        LaunchedEffect(Unit) {
            val success = withTimeoutOrNull(5000) {
                fetchLocation(context, onLocationSet, onError = { error ->
                    isFetching = false
                    errorMessage = error
                })
            }

            if (success == null) {
                isFetching = false
                errorMessage = "Failed to fetch location, try again or input location"
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Choose Location") })
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Enter your location manually or use your current location:", style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.height(16.dp))
            TextField(
                value = locationText,
                onValueChange = { locationText = it },
                label = { Text("City or Address") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = {
                val location = Location("manual").apply {
                    latitude = 0.0
                    longitude = 0.0
                }
                onLocationSet(location)
                navController.navigate("suggestions")
            }) {
                Text("Use Manual Location")
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = {
                isFetching = true
                permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }) {
                Text("Use Current Location")
            }
            errorMessage?.let {
                Spacer(modifier = Modifier.height(16.dp))
                Text(it, color = Color.Red)
            }
            if (isFetching) {
                Spacer(modifier = Modifier.height(16.dp))
                CircularProgressIndicator()
            }
        }
    }
}

fun fetchLocation(context: Context, onLocationSet: (Location) -> Unit, onError: (String) -> Unit) {
    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            if (location != null) {
                onLocationSet(location)
            } else {
                onError("Failed to fetch location, try again or input location")
            }
        }.addOnFailureListener {
            Log.e("LocationError", "Failed to get location", it)
            onError("Failed to fetch location, try again or input location")
        }
    } else {
        onError("Location permission denied. Please try again or enter location manually.")
    }
}

@Composable
fun SuggestionsScreen(navController: NavController, location: Location?) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
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
            Text("No location available.")
        }
    }
}

@Composable
fun TakeMeButton(url: String) {
    val context = LocalContext.current
    Button(
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
        Text("1506 E Olive Way,")
        Text("Seattle, WA 98122")

        Spacer(modifier = Modifier.height(16.dp))
        Row {
            TakeMeButton("https://www.google.com/maps/place/Montana/@47.6177032,-122.3274541,17z/data=!4m10!1m2!2m1!1sMontana!3m6!1s0x54906acafa5a22eb:0xfff92a473406768b!8m2!3d47.6177032!4d-122.3252654!15sCgdNb250YW5hkgEQYmFydF9zdXBwbHlfcmVzdGF1cmFudPoBJENoZERTVWhOTUc5blMwVkpRMEZuU1VSYWNrbFplbU4wU1VOQlJSQUI?hl=en&entry=ttu")
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
        Text("Zig Zag Café", fontSize = 50.sp, fontWeight = FontWeight.Bold)
        Text("1501 Western Ave Ste 202,")
        Text("Seattle, WA 98101")
    }
}
