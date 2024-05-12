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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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

//All Below Functions are HardCoded!!!!! for prototype
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
        /*Button(onClick = { navController.navigate("food") }) {
            Text("Take me!")
        }*/
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
        Text("1506 E Olive Wy,")
        Text("Seattle, WA 98122")
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
        Text("The Sloop Tavern", fontSize = 50.sp, fontWeight = FontWeight.Bold)
        Text("2830 NW Market St,")
        Text("Seattle, WA 98107")
    }
}

