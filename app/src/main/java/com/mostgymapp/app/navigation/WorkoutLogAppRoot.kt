package com.mostgymapp.app.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.mostgymapp.app.ui.screen.history.HistoryDetailScreen
import com.mostgymapp.app.ui.screen.history.HistoryScreen
import com.mostgymapp.app.ui.screen.stats.StatsScreen
import com.mostgymapp.app.ui.screen.templates.TemplateDetailScreen
import com.mostgymapp.app.ui.screen.templates.TemplatesScreen
import com.mostgymapp.app.ui.screen.scanner.ScannerScreen
import com.mostgymapp.app.ui.screen.workout.WorkoutExerciseScreen
import com.mostgymapp.app.ui.screen.settings.SettingsScreen
import com.mostgymapp.app.ui.screen.workout.WorkoutScreen

@Composable
fun WorkoutLogAppRoot() {
    val navController = rememberNavController()

    val bottomItems = listOf(
        BottomItem(Route.Workout.route, "Workout", Icons.Default.FitnessCenter),
        BottomItem(Route.History.route, "History", Icons.AutoMirrored.Filled.List),
        BottomItem(Route.Templates.route, "Templates", Icons.Default.ViewAgenda),
        BottomItem(Route.Scanner.route, "Scanner", Icons.Default.QrCodeScanner),
        BottomItem(Route.Stats.route, "Stats", Icons.Default.BarChart),
        BottomItem(Route.Settings.route, "Settings", Icons.Default.Settings)
    )

    Scaffold(
        bottomBar = {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 0.dp
            ) {
                bottomItems.forEach { item ->
                    NavigationBarItem(
                        selected = currentRoute == item.route,
                        onClick = {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label, style = MaterialTheme.typography.labelSmall) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Route.Workout.route,
            modifier = Modifier
        ) {
            composable(Route.Workout.route) {
                WorkoutScreen(
                    paddingValues = paddingValues,
                    onOpenExercise = { workoutExerciseId ->
                        navController.navigate(Route.WorkoutExercise.create(workoutExerciseId))
                    }
                )
            }
            composable(
                route = Route.WorkoutExercise.route,
                arguments = listOf(navArgument("workoutExerciseId") { type = NavType.LongType })
            ) { backStackEntry ->
                val workoutExerciseId = backStackEntry.arguments?.getLong("workoutExerciseId") ?: return@composable
                WorkoutExerciseScreen(
                    paddingValues = paddingValues,
                    workoutExerciseId = workoutExerciseId,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Route.History.route) {
                HistoryScreen(
                    paddingValues = paddingValues,
                    onOpenDetails = { workoutId -> navController.navigate(Route.HistoryDetail.create(workoutId)) }
                )
            }
            composable(
                route = Route.HistoryDetail.route,
                arguments = listOf(navArgument("workoutId") { type = NavType.LongType })
            ) { backStackEntry ->
                val workoutId = backStackEntry.arguments?.getLong("workoutId") ?: return@composable
                HistoryDetailScreen(
                    workoutId = workoutId,
                    paddingValues = paddingValues,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Route.Templates.route) {
                TemplatesScreen(
                    paddingValues = paddingValues,
                    onOpenTemplate = { templateId -> navController.navigate(Route.TemplateDetail.create(templateId)) },
                    onNavigateWorkoutTab = {
                        navController.navigate(Route.Workout.route) {
                            launchSingleTop = true
                        }
                    }
                )
            }
            composable(
                route = Route.TemplateDetail.route,
                arguments = listOf(navArgument("templateId") { type = NavType.LongType })
            ) { backStackEntry ->
                val templateId = backStackEntry.arguments?.getLong("templateId") ?: return@composable
                TemplateDetailScreen(
                    templateId = templateId,
                    paddingValues = paddingValues,
                    onBack = { navController.popBackStack() },
                    onNavigateWorkoutTab = {
                        navController.navigate(Route.Workout.route) {
                            launchSingleTop = true
                        }
                    }
                )
            }

            composable(Route.Scanner.route) {
                ScannerScreen(paddingValues = paddingValues)
            }

            composable(Route.Stats.route) {
                StatsScreen(paddingValues = paddingValues)
            }

            composable(Route.Settings.route) {
                SettingsScreen(paddingValues = paddingValues)
            }
        }
    }
}

private data class BottomItem(
    val route: String,
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)
