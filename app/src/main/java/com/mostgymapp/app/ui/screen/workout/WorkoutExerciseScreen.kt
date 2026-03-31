package com.mostgymapp.app.ui.screen.workout

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Timer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mostgymapp.app.domain.model.SetEntry
import com.mostgymapp.app.ui.theme.SwipeDelete
import com.mostgymapp.app.ui.theme.SwipeDuplicate
import com.mostgymapp.app.ui.viewmodel.ActiveWorkoutViewModel
import com.mostgymapp.app.utils.formatTimer
import com.mostgymapp.app.utils.formatWeight

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutExerciseScreen(
    paddingValues: PaddingValues,
    workoutExerciseId: Long,
    onBack: () -> Unit,
    viewModel: ActiveWorkoutViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val suggestions by viewModel.inputSuggestions.collectAsStateWithLifecycle()
    val insights by viewModel.insights.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.events.collect { snackbarHostState.showSnackbar(it) }
    }

    LaunchedEffect(workoutExerciseId) {
        viewModel.refreshSuggestion(workoutExerciseId)
        viewModel.refreshInsights(workoutExerciseId)
    }

    val exercise = uiState.activeWorkout
        ?.exercises
        ?.firstOrNull { it.id == workoutExerciseId }

    if (exercise == null) {
        Scaffold(modifier = Modifier.padding(paddingValues)) { inner ->
            Column(modifier = Modifier.fillMaxSize().padding(inner), verticalArrangement = Arrangement.Center) {
                Text(
                    "Exercise not found",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyLarge
                )
                Button(onClick = onBack, modifier = Modifier.padding(16.dp)) {
                    Text("Back")
                }
            }
        }
        return
    }

    val suggestion = suggestions[workoutExerciseId]
    val insight = insights[workoutExerciseId]

    var weight by rememberSaveable(workoutExerciseId) { mutableStateOf("") }
    var reps by rememberSaveable(workoutExerciseId) { mutableStateOf("") }
    var rpe by rememberSaveable(workoutExerciseId) { mutableStateOf("") }
    var note by rememberSaveable(workoutExerciseId) { mutableStateOf("") }
    var restSeconds by rememberSaveable { mutableIntStateOf(90) }
    var showCustomRestDialog by remember { mutableStateOf(false) }
    var setToDelete by remember { mutableStateOf<Long?>(null) }

    LaunchedEffect(suggestion) {
        if (suggestion != null && weight.isBlank() && reps.isBlank()) {
            weight = formatWeight(suggestion.weight)
            reps = suggestion.reps.toString()
            rpe = suggestion.rpe?.toString().orEmpty()
        }
    }

    Scaffold(
        modifier = Modifier.padding(paddingValues),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        exercise.exerciseName,
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        "Last: ${suggestion?.let { "${formatWeight(it.weight)} x ${it.reps}" } ?: "-"}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        "Best (weight*reps): ${insight?.bestScore?.let { formatWeight(it) } ?: "-"}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (uiState.timerState.isRunning) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Timer,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Timer: ${formatTimer(uiState.timerState.remainingMs)}",
                                color = MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            val textFieldColors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                cursorColor = MaterialTheme.colorScheme.primary,
                focusedLabelColor = MaterialTheme.colorScheme.primary
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    modifier = Modifier.weight(1f),
                    value = weight,
                    onValueChange = { weight = it },
                    label = { Text("Weight") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    colors = textFieldColors
                )
                OutlinedTextField(
                    modifier = Modifier.weight(1f),
                    value = reps,
                    onValueChange = { reps = it },
                    label = { Text("Reps") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    colors = textFieldColors
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    modifier = Modifier.weight(1f),
                    value = rpe,
                    onValueChange = { rpe = it },
                    label = { Text("RPE (opt)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    colors = textFieldColors
                )
                OutlinedTextField(
                    modifier = Modifier.weight(1f),
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Note") },
                    singleLine = true,
                    colors = textFieldColors
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val chipColors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.primary
                )
                FilterChip(selected = restSeconds == 60, onClick = { restSeconds = 60 }, label = { Text("60s") }, colors = chipColors)
                FilterChip(selected = restSeconds == 90, onClick = { restSeconds = 90 }, label = { Text("90s") }, colors = chipColors)
                FilterChip(selected = restSeconds == 120, onClick = { restSeconds = 120 }, label = { Text("120s") }, colors = chipColors)
                FilterChip(selected = restSeconds !in listOf(60, 90, 120), onClick = { showCustomRestDialog = true }, label = { Text("Custom") }, colors = chipColors)
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    modifier = Modifier.weight(1f),
                    onClick = {
                        suggestion?.let {
                            weight = formatWeight(it.weight)
                            reps = it.reps.toString()
                            rpe = it.rpe?.toString().orEmpty()
                        }
                    },
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Copy Last")
                }
                Button(
                    modifier = Modifier.weight(1f),
                    enabled = weight.isNotBlank() && reps.isNotBlank(),
                    onClick = {
                        viewModel.addSet(
                            workoutExerciseId = workoutExerciseId,
                            weightText = weight,
                            repsText = reps,
                            rpeText = rpe,
                            note = note,
                            restSeconds = restSeconds
                        )
                        note = ""
                    }
                ) {
                    Text("+ Set")
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(top = 16.dp, bottom = 8.dp),
                color = MaterialTheme.colorScheme.outlineVariant
            )

            if (exercise.sets.isEmpty()) {
                Text(
                    "No sets yet",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(exercise.sets, key = { it.id }) { set ->
                        SetRow(
                            set = set,
                            onToggleCompleted = { checked ->
                                viewModel.toggleSetCompleted(set.id, checked)
                            },
                            onDelete = {
                                setToDelete = set.id
                            },
                            onDuplicate = {
                                viewModel.duplicateSet(set.id, workoutExerciseId)
                            }
                        )
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    }
                }
            }
        }
    }

    setToDelete?.let { setId ->
        AlertDialog(
            onDismissRequest = { setToDelete = null },
            title = { Text("Delete Set?", style = MaterialTheme.typography.titleLarge) },
            text = {
                Text(
                    "This set will be permanently deleted.",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteSet(setId, workoutExerciseId)
                        setToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { setToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showCustomRestDialog) {
        CustomRestDialog(
            current = restSeconds,
            onDismiss = { showCustomRestDialog = false },
            onConfirm = {
                restSeconds = it
                showCustomRestDialog = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
private fun SetRow(
    set: SetEntry,
    onToggleCompleted: (Boolean) -> Unit,
    onDelete: () -> Unit,
    onDuplicate: () -> Unit
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            when (value) {
                SwipeToDismissBoxValue.StartToEnd -> {
                    onDuplicate()
                    false
                }

                SwipeToDismissBoxValue.EndToStart -> {
                    onDelete()
                    false
                }

                SwipeToDismissBoxValue.Settled -> false
            }
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .background(
                        when (dismissState.targetValue) {
                            SwipeToDismissBoxValue.StartToEnd -> SwipeDuplicate
                            SwipeToDismissBoxValue.EndToStart -> SwipeDelete
                            SwipeToDismissBoxValue.Settled -> MaterialTheme.colorScheme.surface
                        }
                    )
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Icon(Icons.Default.ContentCopy, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error)
            }
        },
        content = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .combinedClickable(
                        onClick = {},
                        onLongClick = onDuplicate
                    )
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = set.isCompleted,
                    onCheckedChange = onToggleCompleted,
                    colors = CheckboxDefaults.colors(
                        checkedColor = MaterialTheme.colorScheme.primary,
                        checkmarkColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
                Text(
                    text = "#${set.orderIndex + 1}",
                    modifier = Modifier.padding(start = 4.dp),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${formatWeight(set.weight)} x ${set.reps}",
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 12.dp),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                set.rpe?.let {
                    Text(
                        "RPE ${formatWeight(it)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    )
}

@Composable
private fun CustomRestDialog(
    current: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    var value by remember { mutableStateOf(current.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Custom Rest", style = MaterialTheme.typography.titleLarge) },
        text = {
            OutlinedTextField(
                value = value,
                onValueChange = { value = it },
                label = { Text("Seconds") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    cursorColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    value.toIntOrNull()?.takeIf { it > 0 }?.let(onConfirm)
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
