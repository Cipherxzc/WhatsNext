package com.cipherxzc.whatsnext.ui.todolist.utils

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun DatePicker(
    date: Date? = null,
    onDateSelected: (Date) -> Unit
) {
    var showCalendar by remember { mutableStateOf(false) }

    Row (
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = date?.let { SimpleDateFormat("yyyy-MM-dd", Locale.CHINA).format(it) } ?: "设置截止日期",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.clickable {
                showCalendar = true
            }
        )

        if (date != null) {
            TimePicker(
                initialDate = date,
                onHourChange = { hour ->
                    val newDate = date
                    val calendar = Calendar.getInstance().apply {
                        time = newDate
                        set(Calendar.HOUR_OF_DAY, hour)
                    }
                    onDateSelected(calendar.time)
                },
                onMinuteChange = { minute ->
                    val newDate = date
                    val calendar = Calendar.getInstance().apply {
                        time = newDate
                        set(Calendar.MINUTE, minute)
                    }
                    onDateSelected(calendar.time)
                }
            )
        }
    }

    if (showCalendar) {
        DatePickerDialog(
            onDateSelected = onDateSelected,
            onDismissRequest = { showCalendar = false },
            initialDate = date
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerDialog(
    onDateSelected: (Date) -> Unit,
    onDismissRequest: () -> Unit,
    initialDate: Date? = null
) {
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialDate?.time ?: System.currentTimeMillis()
    )

    DatePickerDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            TextButton(
                onClick = {
                    val selectedDateMillis = datePickerState.selectedDateMillis
                    if (selectedDateMillis != null) {
                        onDateSelected(Date(selectedDateMillis))
                    }
                    onDismissRequest()
                }
            ) {
                Text("确认")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text("取消")
            }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}