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
    Main("main"), Setting("Setting"), Measured("measured"), MeasureToEdgeSetting("MeasureToEdgeCalibration"), MeasureToEdgeCalibration("MeasureToEdgeCalibration/{direction}/{startOffset}"), CalibrationOverview("calibration"), CalibrationMode("calibration/{mode}"), Disclaimer("disclaimer")/*, CameraMeasure("CameraMeasure")*/
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
            GuessIronViewModelFactory(
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
                            onShowDisclaimer = {
                                navController.navigateSingleTopTo(GuessIronRoutes.Disclaimer.path)
                            },
                            onShowSetting = {
                                navController.navigateSingleTopTo(GuessIronRoutes.Setting.path)
                            },
                            onConfigDisplayBorder = {
                                navController.navigateSingleTopTo(GuessIronRoutes.MeasureToEdgeSetting.path)
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
                    composable(route = GuessIronRoutes.CalibrationOverview.path) {
                        ScalaCalibrationOverviewScreen(
                            viewModel = viewModel,
                            onStartCalibration = { mode -> navController.navigateSingleTopTo(GuessIronRoutes.CalibrationOverview.path+"/$mode") },
                            onBack = { navController.popBackStack() }
                        )
                    }
                    composable(route = GuessIronRoutes.Setting.path) {
                        SettingsScreen(
                            onClickCalibration = {
                                navController.navigateSingleTopTo(GuessIronRoutes.CalibrationOverview.path)
                            },
                            onClickSetDisplayBorder = {
                                navController.navigateSingleTopTo(GuessIronRoutes.MeasureToEdgeSetting.path)
                            },
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
                    composable(route = GuessIronRoutes.MeasureToEdgeSetting.path) {

                        DisplayBorderScreen(
                            viewModel = viewModel,
                            onEdit = { direction, startOffset -> navController.navigate(GuessIronRoutes.MeasureToEdgeSetting.path+"/${direction.value}/$startOffset")  },
                            onBack = { navController.popBackStack() }
                        )
                    }
                    composable(
                        route = GuessIronRoutes.MeasureToEdgeCalibration.path,
                        arguments = listOf(
                            navArgument("direction") { type = NavType.IntType },
                            navArgument("startOffset") { type = NavType.IntType }) ) {
                        backStackEntry ->
                        MeasureToEdgeCalibrationScreen(
                            offsetScalaDirection = backStackEntry.arguments?.getInt("direction") ?: 0,
                            startOffset = backStackEntry.arguments?.getInt("startOffset") ?: 0,
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

