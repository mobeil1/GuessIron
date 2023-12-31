package de.indie42.guessiron

import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import de.indie42.guessiron.data.GuessIronDataRepository
import de.indie42.guessiron.data.GuessIronDataSerializer
import de.indie42.guessiron.ui.theme.GuessIronTheme


val Context.guessIronDataStore : DataStore<GuessIronData> by dataStore(fileName = "guessirondata.pb", serializer = GuessIronDataSerializer)
private lateinit var viewModel: GuessIronViewModel

enum class GuessIronRoutes ( val path: String) {
    Main("main"), Measured("measured"), CalibrationOverview("calibration"), CalibrationMode("calibration/{mode}"), Disclaimer("disclaimer")/*, CameraMeasure("CameraMeasure")*/
}

@RequiresApi(Build.VERSION_CODES.O)

class MainActivity : ComponentActivity() {

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //enableEdgeToEdge()

        //for immersive mode
        /*WindowCompat.setDecorFitsSystemWindows(window, false)

        WindowInsetsControllerCompat(window, window.decorView).let { controller ->
            controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.hide(WindowInsetsCompat.Type.navigationBars())
        }*/

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
                ) {
                    composable(route = GuessIronRoutes.Main.path) {
                        GuessIronScreen(
                            onClickShowMeasured = {
                            navController.navigateSingleTopTo(GuessIronRoutes.Measured.path)
                            },
                            onClickEditScala = {
                                navController.navigateSingleTopTo(GuessIronRoutes.CalibrationOverview.path)
                            },
                            onShowDisclaimer = {
                                navController.navigateSingleTopTo(GuessIronRoutes.Disclaimer.path)
                            },
                            /*onClickCameraMeasure = {
                                navController.navigateSingleTopTo(GuessIronRoutes.CameraMeasure.path)
                            },*/
                            viewModel = viewModel)
                    }
                    composable(route = GuessIronRoutes.Measured.path) {
                        MeasuredValuesScreen(
                           viewModel = viewModel,
                            onBack = { navController.popBackStack() }
                        )
                    }
//                    composable(route = GuessIronRoutes.CameraMeasure.path) {
//                        CameraMeasureScreen(
//                            onBack = { navController.popBackStack() }
//                        )
//                    }
                    composable(route = GuessIronRoutes.CalibrationOverview.path) {
                        ScalaCalibrationOverviewScreen(
                            viewModel = viewModel,
                            onStartCalibration = { mode -> navController.navigateSingleTopTo(GuessIronRoutes.CalibrationOverview.path+"/$mode") },
                            onBack = { navController.popBackStack() }
                        )
                    }
                    composable(route = GuessIronRoutes.CalibrationMode.path, arguments = listOf(navArgument("mode") { type = NavType.IntType })) {backStackEntry ->
                        ScalaCalibrationScreen(
                            mode =  backStackEntry.arguments?.getInt("mode") ?: 0,
                            viewModel = viewModel,
                            onBack = { navController.popBackStack() }
                        )
                    }
                    composable(route = GuessIronRoutes.Disclaimer.path) {
                        DisclaimerScreen(
                            viewModel = viewModel,
                            onStartCalibration = { navController.navigateSingleTopTo(GuessIronRoutes.CalibrationOverview.path) },
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

