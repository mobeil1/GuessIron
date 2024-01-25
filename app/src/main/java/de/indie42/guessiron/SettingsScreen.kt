package de.indie42.guessiron

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Dock
import androidx.compose.material.icons.filled.ZoomOutMap
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(    onClickCalibration: () -> Unit,
                       onClickSetDisplayBorder: () -> Unit,
                       onBack: () -> Unit) {

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())

    val scrollState = rememberScrollState()


    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(),
                title = {
                    Text(stringResource(id = R.string.settings))
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(id = R.string.Back)
                        )
                    }
                },
                actions = {
                },
                scrollBehavior = scrollBehavior,
            )
        },

        modifier = Modifier.fillMaxSize(),
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding).verticalScroll(scrollState)) {
            ListItem( modifier = Modifier.clickable { onClickCalibration() },
                headlineContent = { Text(stringResource(id = R.string.calibration)) },
                leadingContent = {
                    Icon(
                        Icons.Filled.ZoomOutMap,
                        contentDescription = stringResource(id = R.string.calibration),
                    )
                }
            )
            ListItem(
                modifier = Modifier.clickable { onClickSetDisplayBorder() },
                headlineContent = { Text( stringResource(id = R.string.ScreenMargin)) },
                leadingContent = {
                    Icon(
                        Icons.Filled.Dock, modifier = Modifier.rotate(180F),
                        contentDescription = stringResource(id = R.string.ScreenMargin),
                    )
                }
            )

            HorizontalDivider()

        }
    }
}