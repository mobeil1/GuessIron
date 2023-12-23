package de.indie42.guessiron

import android.os.Build
import android.text.format.DateUtils
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.SortByAlpha
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MeasuredValuesScreen(
    modifier: Modifier = Modifier,
    viewModel: GuessIronViewModel = viewModel(),
    onBack: () -> Unit,
) {

    val measuredValues by viewModel.dataState.collectAsState()

    var selectedMeasuredValue by remember { mutableIntStateOf(-1) }
    var showEditValueDialog by remember { mutableStateOf(false) }
    var showMenuDialog by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    val context = LocalContext.current

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(),
                title = {
                    Text(stringResource(id = R.string.measured))
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
                    val activeColor = IconButtonDefaults.iconButtonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    val defaultColor = IconButtonDefaults.iconButtonColors()

                    IconButton(
                        onClick = {
                            scope.launch {
                                viewModel.changeSortBy(GuessIronData.SortOrder.BY_Name)
                            }
                        },
                        colors = if (measuredValues.orderByDate == GuessIronData.SortOrder.BY_Name) activeColor else defaultColor
                    ) {
                        Icon(
                            imageVector = Icons.Filled.SortByAlpha,
                            contentDescription = stringResource(id = R.string.SortAlphabetic)
                        )
                    }
                    IconButton(
                        onClick = {
                            scope.launch {
                                viewModel.changeSortBy(GuessIronData.SortOrder.BY_Timestamp)
                            }
                        },
                        colors = if (measuredValues.orderByDate == GuessIronData.SortOrder.BY_Timestamp) activeColor else defaultColor
                    ) {
                        Icon(
                            imageVector = Icons.Filled.AccessTime,
                            contentDescription = stringResource(id = R.string.SortTimestamp)
                        )
                    }
                },
                scrollBehavior = scrollBehavior,
            )
        },

        modifier = Modifier.fillMaxSize(),
    ) { innerPadding ->

        if (measuredValues.measuredValues.isEmpty()){
            Column ( modifier = Modifier.padding(innerPadding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center) {
                Text(
                    color = MaterialTheme.colorScheme.outline,
                    text = stringResource(id = R.string.NoMeasuredValues))
            }

        }
        else {
        LazyVerticalStaggeredGrid(
            columns = StaggeredGridCells.Adaptive(150.dp),
            verticalItemSpacing = 4.dp,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            content = {
                itemsIndexed(measuredValues.measuredValues) { index, measuredValue ->
                    ElevatedCard(

                        elevation = CardDefaults.cardElevation(
                            defaultElevation = 6.dp
                        ),
                        modifier = Modifier.padding(8.dp),
                        onClick = {
                            selectedMeasuredValue = index
                            showMenuDialog = true
                        }
                    ) {


                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 8.dp, end = 8.dp)
                        ) {

                            val pattern = DateTimeFormatter.ofPattern(
                                "yyyy-MM-dd HH:mm:ss",
                                Locale.getDefault()
                            )
                            val localDateTime =
                                LocalDateTime.parse(measuredValue.timestamp, pattern)
                            val moment = DateUtils.getRelativeDateTimeString(
                                context,
                                localDateTime.atZone(ZoneId.systemDefault()).toEpochSecond() * 1000,
                                DateUtils.SECOND_IN_MILLIS,
                                DateUtils.DAY_IN_MILLIS,
                                DateUtils.FORMAT_SHOW_YEAR
                            )
                            if (measuredValue.name.isNotBlank()){
                                Text(
                                    text = measuredValue.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(bottom = 8.dp, top = 8.dp),
                                )
                            }
                            else{
                                Text(
                                    text = "<"+ stringResource(id = R.string.Empty_Placeholder) +">",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.outline,
                                    modifier = Modifier.padding(bottom = 8.dp, top = 8.dp),
                                )
                            }
                            Row(modifier = Modifier.fillMaxWidth()) {
                                Column(
                                    modifier = Modifier
                                        .weight(1f),

                                    verticalArrangement = Arrangement.Center,
                                ) {
                                    Text(
                                        modifier = Modifier.padding(bottom = 4.dp, top = 8.dp),
                                        text = measuredValue.measured.toString() + " mm",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                    Text(
                                        modifier = Modifier.padding(bottom = 4.dp),
                                        text = moment.toString(),
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                }

                            }
                        }
                    }
                }
            },
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        )

        if (showEditValueDialog) {
            val measuredValue = measuredValues.measuredValues[selectedMeasuredValue]

            SaveDialog(
                measuredValue = measuredValue.measured,
                measuredUnit = "mm",
                defaultName = measuredValue.name,
                onDismissRequest = { showEditValueDialog = false },
                onConfirmation = {newName ->
                    scope.launch {
                        viewModel.updateMeasuredValue(measuredValue, newName)
                    }
                    showEditValueDialog = false
                }
            )

        }

        if (showMenuDialog) {
            val measuredValue = measuredValues.measuredValues[selectedMeasuredValue]

            Dialog(onDismissRequest = { showMenuDialog = false }) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                ) {

                    ElevatedCard(
                        modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        elevation = CardDefaults.cardElevation(
                            defaultElevation = 6.dp
                        ),

                        ) {

                        val pattern = DateTimeFormatter.ofPattern(
                            "yyyy-MM-dd HH:mm:ss",
                            Locale.getDefault()
                        )
                        val localDateTime =
                            LocalDateTime.parse(measuredValue.timestamp, pattern)
                        val moment = DateUtils.getRelativeDateTimeString(
                            context,
                            localDateTime.atZone(ZoneId.systemDefault()).toEpochSecond() * 1000,
                            DateUtils.SECOND_IN_MILLIS,
                            DateUtils.DAY_IN_MILLIS,
                            DateUtils.FORMAT_SHOW_YEAR
                        )
                        if (measuredValue.name.isNotBlank()) {
                            Text(
                                text = measuredValue.name,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(8.dp),
                            )
                        } else {
                            Text(
                                text = "<" + stringResource(id = R.string.Empty_Placeholder) + ">",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.outline,
                                modifier = Modifier.padding(8.dp),
                            )
                        }

                        Text(
                            modifier = Modifier.padding(
                                bottom = 4.dp,
                                top = 8.dp,
                                start = 8.dp
                            ),
                            text = measuredValue.measured.toString() + " mm",
                            style = MaterialTheme.typography.headlineMedium
                        )
                        Text(
                            modifier = Modifier.padding(bottom = 8.dp, start = 8.dp),
                            text = moment.toString(),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.outline
                        )
                        Row(
                            modifier = Modifier
                                .padding(8.dp)
                                .fillMaxWidth(),
                            Arrangement.SpaceBetween
                        ) {

                            Button(
                                onClick = {
                                    scope.launch {
                                        viewModel.setMeasuredValue(measuredValue.measured)
                                        showMenuDialog = false
                                        onBack()
                                    }
                                },
                            ) {
                                Text(text = stringResource(id = R.string.Show))
                            }
                            Row(horizontalArrangement = Arrangement.End) {
                                IconButton(
                                    onClick = {
                                        showMenuDialog = false
                                        showEditValueDialog = true
                                    },
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Edit,
                                        contentDescription = stringResource(
                                            id = R.string.Edit
                                        )
                                    )
                                }
                                IconButton(
                                    onClick = {
                                        scope.launch {
                                            viewModel.removeMeasuredValue(measuredValue)
                                            showMenuDialog = false
                                        }
                                    },
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Delete,
                                        contentDescription = stringResource(id = R.string.Delete)
                                    )
                                }
                            }

                        }
                    }
                }
                }
            }
        }
    }
}
