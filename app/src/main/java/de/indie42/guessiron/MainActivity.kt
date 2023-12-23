package de.indie42.guessiron

import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.ui.Modifier
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import de.indie42.guessiron.data.GuessIronDataRepository
import de.indie42.guessiron.data.GuessIronDataSerializer
import de.indie42.guessiron.ui.theme.GuessIronTheme

val Context.guessIronDataStore : DataStore<GuessIronData> by dataStore(fileName = "guessirondata.pb", serializer = GuessIronDataSerializer)
private lateinit var viewModel: GuessIronViewModel

enum class GuessIronRoutes ( val path: String) {
    Main("main"), Measured("measured"), Calibration("calibration"), Disclaimer("disclaimer"),
}

@RequiresApi(Build.VERSION_CODES.O)

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProvider(
            this,
            TasksViewModelFactory(
                GuessIronDataRepository(guessIronDataStore)
            )
        )[GuessIronViewModel::class.java]

        setContent {
            GuessIronTheme {

                val navController = rememberNavController()

                NavHost(
                    navController = navController,
                    startDestination = GuessIronRoutes.Main.path,
                    modifier = Modifier
                ) {
                    composable(route = GuessIronRoutes.Main.path) {
                        GuessIronScreen(
                            onClickShowMeasured = {
                            navController.navigateSingleTopTo(GuessIronRoutes.Measured.path)
                            },
                            onClickEditScala = {
                                navController.navigateSingleTopTo(GuessIronRoutes.Calibration.path)
                            },
                            onShowDisclaimer = {
                                navController.navigateSingleTopTo(GuessIronRoutes.Disclaimer.path)
                            },
                            viewModel = viewModel)
                    }
                    composable(route = GuessIronRoutes.Measured.path) {
                        MeasuredValuesScreen(
                           viewModel = viewModel,
                            onBack = { navController.popBackStack() }
                        )
                    }
                    composable(route = GuessIronRoutes.Calibration.path) {
                        ScalaCalibrationScreen(
                            viewModel = viewModel,
                            onBack = { navController.popBackStack() }
                        )
                    }
                    composable(route = GuessIronRoutes.Disclaimer.path) {
                        DisclaimerScreen(
                            viewModel = viewModel,
                            onStartCalibration = { navController.navigateSingleTopTo(GuessIronRoutes.Calibration.path) },
                            onBack = { navController.popBackStack() }
                        )
                    }
                }

            }
        }
    }
}

fun NavHostController.navigateSingleTopTo(route: String) =
    this.navigate(route) {
        popUpTo(
            this@navigateSingleTopTo.graph.findStartDestination().id
        ) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }

