package com.maternitytracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.maternitytracker.data.database.AppDatabase
import com.maternitytracker.data.repository.ShoppingRepository
import com.maternitytracker.ui.screens.HomeScreen
import com.maternitytracker.ui.screens.LabelManagementScreen
import com.maternitytracker.ui.theme.MaternityBabyTrackerTheme
import com.maternitytracker.viewmodel.ShoppingViewModel
import com.maternitytracker.viewmodel.ShoppingViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        val database = AppDatabase.getDatabase(this)
        val repository = ShoppingRepository(database.shoppingItemDao(), database.labelDao())
        
        setContent {
            MaternityBabyTrackerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MaternityTrackerApp(repository = repository)
                }
            }
        }
    }
}

@Composable
fun MaternityTrackerApp(
    repository: ShoppingRepository,
    navController: NavHostController = rememberNavController()
) {
    val viewModel: ShoppingViewModel = viewModel(
        factory = ShoppingViewModelFactory(repository)
    )

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("home") {
                HomeScreen(
                    viewModel = viewModel,
                    onNavigateToLabelManagement = {
                        navController.navigate("label_management")
                    }
                )
            }
            composable("label_management") {
                LabelManagementScreen(
                    viewModel = viewModel,
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}