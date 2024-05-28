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
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest
import com.google.android.libraries.places.api.net.PlacesClient
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

class MainActivity : ComponentActivity() {
    private val preferencesDataStore by lazy { PreferencesDataStore.getInstance(context = this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            NyoomApp(preferencesDataStore = preferencesDataStore)
        }

        val apiKey = BuildConfig.PLACES_API_KEY
        if (apiKey.isEmpty() || apiKey == "DEFAULT_API_KEY") {
            Log.e("Places test", "No API key")
            finish()
            return
        }
        Places.initialize(applicationContext, apiKey)
    }
}

@Composable
fun NyoomApp(preferencesDataStore: PreferencesDataStore) {
    val navController = rememberNavController()
    var location by remember { mutableStateOf<Location?>(null) }
    val context = LocalContext.current

    NavHost(navController, startDestination = "intro") {
        composable("intro") { IntroScreen(navController) }
        composable("cuisine") {
            CuisineScreen(navController, preferencesDataStore, onNavigateToLocationChoice = {
                navController.navigate("locationChoice")
            })
        }
        composable("locationChoice") {
            LocationChoiceScreen(navController, context, onLocationSet = { loc ->
                location = loc
                navController.navigate("suggestions")
            })
        }
        composable("suggestions") {
            SuggestionsScreen(navController, location)
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
fun CuisineScreen(navController: NavController, preferencesDataStore: PreferencesDataStore, onNavigateToLocationChoice: () -> Unit) {
    val cuisineOptions = listOf("Italian", "Mexican", "Chinese", "Indian", "Japanese")
    val cuisineMappings = mapOf(
        "Italian" to "italian_restaurant",
        "Mexican" to "mexican_restaurant",
        "Chinese" to "chinese_restaurant",
        "Indian" to "indian_restaurant",
        "Japanese" to "japanese_restaurant"
    )
    val savedCuisines = preferencesDataStore.cuisinesFlow.collectAsState(initial = emptySet())
    val selectedCuisines = remember { mutableStateListOf<String>() }

    LaunchedEffect(savedCuisines) {
        selectedCuisines.clear()
        selectedCuisines.addAll(savedCuisines.value)
    }

    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        bottomBar = {
            Button(
                onClick = {
                    coroutineScope.launch {
                        preferencesDataStore.saveCuisines(selectedCuisines.toSet())
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
                validateAndSetManualLocation(locationText, onLocationSet)
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

fun validateAndSetManualLocation(locationText: String, onLocationSet: (Location) -> Unit) {
    val location = Location("manual").apply {
        latitude = 0.0 // Replace with actual latitude from geocoding result
        longitude = 0.0 // Replace with actual longitude from geocoding result
    }
    onLocationSet(location)
}

fun fetchLocation(context: Context, onLocationSet: (Location) -> Unit, onError: (String) -> Unit) {
    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            if (location != null) {
                onLocationSet(location)
            } else {
                Log.e("LocationError", "Location is null")
                onError("Failed to fetch location, try again or input location")
            }
        }.addOnFailureListener {
            Log.e("LocationError", "Failed to get location", it)
            onError("Failed to fetch location, try again or input location")
        }
    } else {
        Log.e("PermissionError", "Location permission denied")
        onError("Location permission denied. Please try again or enter location manually.")
    }
}

@Composable
fun SuggestionsScreen(navController: NavController, location: Location?) {
    val context = LocalContext.current
    val placesClient = Places.createClient(context)
    var restaurantSuggestions by remember { mutableStateOf<List<Place>?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(location) {
        if (location != null) {
            val preferencesDataStore = PreferencesDataStore.getInstance(context)

            coroutineScope.launch {
                preferencesDataStore.cuisinesFlow.collect { cuisinePreferences: Set<String> ->
                    fetchRestaurantSuggestions(
                        context,
                        placesClient,
                        location,
                        cuisinePreferences,
                        onSuccess = { places ->
                            restaurantSuggestions = places
                        },
                        onFailure = { error ->
                            errorMessage = error
                        }
                    )
                }
            }
        }
    }

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

            if (restaurantSuggestions != null) {
                LazyColumn {
                    items(restaurantSuggestions!!) { place ->
                        RestaurantItem(navController, place)
                    }
                }
            } else {
                CircularProgressIndicator()
            }
            errorMessage?.let {
                Text(it, color = Color.Red)
            }
        } else {
            Text("No location available.")
        }
    }
}

@Composable
fun RestaurantItem(navController: NavController, place: Place) {
    Column(modifier = Modifier.padding(8.dp)) {
        Text(place.name ?: "Unknown Restaurant", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Text(place.address ?: "Unknown Address")
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("geo:${place.latLng?.latitude},${place.latLng?.longitude}?q=${place.name}"))
            intent.setPackage("com.google.android.apps.maps")
            if (intent.resolveActivity(navController.context.packageManager) != null) {
                navController.context.startActivity(intent)
            }
        }) {
            Text("Take Me!!!!")
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

fun fetchRestaurantSuggestions(
    context: Context,
    placesClient: PlacesClient,
    location: Location,
    cuisinePreferences: Set<String>,
    onSuccess: (List<Place>) -> Unit,
    onFailure: (String) -> Unit
) {
    val permission = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)

    if (permission == PackageManager.PERMISSION_GRANTED) {
        // Create a list of place types based on cuisine preferences
        val placeTypes = cuisinePreferences.map { Place.Type.RESTAURANT }

        // Define the fields to return
        val placeFields = listOf(Place.Field.NAME, Place.Field.ADDRESS, Place.Field.LAT_LNG, Place.Field.TYPES)

        // Create a FindCurrentPlaceRequest
        val request = FindCurrentPlaceRequest.newInstance(placeFields)

        // Fetch current place response
        val placeResponse = placesClient.findCurrentPlace(request)

        placeResponse.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val response = task.result
                val filteredPlaces = response.placeLikelihoods.filter { likelihood ->
                    likelihood.place.types?.any { it in placeTypes } == true
                }.map { it.place }
                onSuccess(filteredPlaces)
            } else {
                Log.e("FetchError", "Failed to fetch restaurant suggestions", task.exception)
                onFailure(task.exception?.message ?: "Failed to fetch restaurant suggestions")
            }
        }
    } else {
        Log.e("PermissionError", "Location permission not granted")
        onFailure("Location permission not granted")
    }
}