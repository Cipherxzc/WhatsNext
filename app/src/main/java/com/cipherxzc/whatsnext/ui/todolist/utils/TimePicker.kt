package com.cipherxzc.whatsnext.ui.todolist.utils

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.util.Calendar
import java.util.Date

@Composable
fun TimePicker(
    initialDate: Date,
    onHourChange: (Int) -> Unit,
    onMinuteChange: (Int) -> Unit
) {
    val calendar = remember { Calendar.getInstance() }
    val initialHour = initialDate.let {
        calendar.time = it
        calendar.get(Calendar.HOUR_OF_DAY)
    }
    val initialMinute = initialDate.let {
        calendar.time = it
        calendar.get(Calendar.MINUTE)
    }

    var selectedHour by remember { mutableIntStateOf(initialHour) }
    var selectedMinute by remember { mutableIntStateOf(initialMinute) }

    LaunchedEffect(selectedHour) {
        onHourChange(selectedHour)
    }
    LaunchedEffect(selectedMinute) {
        onMinuteChange(selectedMinute)
    }

    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        NumberPicker(
            value = selectedHour,
            range = 0..23,
            onValueChange = { selectedHour = it },
            label = "时"
        )
        NumberPicker(
            value = selectedMinute,
            range = 0..59,
            onValueChange = { selectedMinute = it },
            label = "分"
        )
    }
}

@Composable
fun NumberPicker(
    value: Int,
    range: IntRange,
    onValueChange: (Int) -> Unit,
    label: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(0.dp),
        modifier = Modifier.wrapContentSize()
    ) {
        DropdownMenuBox(
            selectedValue = value,
            range = range,
            onValueSelected = onValueChange
        )
        Text(
            label,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 2.dp)
        )
    }
}

@Composable
fun DropdownMenuBox(
    selectedValue: Int,
    range: IntRange,
    onValueSelected: (Int) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        TextButton(
            onClick = { expanded = true }
        ) {
            Text(String.format("%02d", selectedValue), style = MaterialTheme.typography.titleMedium)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            range.forEach {
                DropdownMenuItem(
                    text = { Text(String.format("%02d", it), style = MaterialTheme.typography.titleMedium) },
                    onClick = {
                        onValueSelected(it)
                        expanded = false
                    }
                )
            }
        }
    }
}